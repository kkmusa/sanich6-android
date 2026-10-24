const { test } = require('node:test');
const assert = require('node:assert/strict');
const fs = require('node:fs');
const path = require('node:path');
const { createHash } = require('node:crypto');
const { chromium } = require('playwright');

const root = path.resolve(__dirname, '..');
const resources = path.join(root, 'app/src/main/res');
const report = require('../docs/station-photo-compression.json');

function filesBelow(folder) {
  return fs.readdirSync(folder, { withFileTypes: true }).flatMap((entry) => {
    const file = path.join(folder, entry.name);
    return entry.isDirectory() ? filesBelow(file) : [file];
  });
}

test('all drawable references still resolve after the station photo conversion', () => {
  const declared = new Set(
    fs
      .readdirSync(resources)
      .filter((name) => name.startsWith('drawable'))
      .flatMap((name) => fs.readdirSync(path.join(resources, name)))
      .map((name) => name.split('.')[0]),
  );
  const referenced = new Set();
  for (const file of filesBelow(path.join(root, 'app/src/main/java'))) {
    if (!file.endsWith('.kt')) continue;
    for (const match of fs
      .readFileSync(file, 'utf8')
      .matchAll(/(?<![\w.])R\.drawable\.([a-z0-9_]+)/g)) {
      referenced.add(match[1]);
    }
  }
  assert.ok(referenced.size > 70);
  for (const name of referenced) assert.ok(declared.has(name), `Missing drawable ${name}`);
});

test('all 92 compressed photos decode, retain dimensions and have the expected content', async () => {
  assert.equal(report.images.length, 92);
  const browser = await chromium.launch({
    headless: true,
    args: ['--no-sandbox'],
    ...(fs.existsSync('/usr/bin/chromium') ? { executablePath: '/usr/bin/chromium' } : {}),
  });
  try {
    const page = await browser.newPage();
    let total = 0;
    for (const image of report.images) {
      const data = fs.readFileSync(path.join(resources, image.directory, image.resource + '.webp'));
      total += data.length;
      assert.equal(data.length, image.compressedBytes, image.resource);
      assert.ok(data.length < image.originalBytes, image.resource);
      assert.equal(
        createHash('sha256').update(data).digest('hex'),
        image.compressedSha256,
        image.resource,
      );
      assert.equal(data.toString('ascii', 0, 4), 'RIFF');
      assert.equal(data.toString('ascii', 8, 12), 'WEBP');
      const dimensions = await page.evaluate(async (encoded) => {
        const photo = new Image();
        photo.src = 'data:image/webp;base64,' + encoded;
        await photo.decode();
        const result = [photo.naturalWidth, photo.naturalHeight];
        photo.src = '';
        return result;
      }, data.toString('base64'));
      assert.deepEqual(dimensions, [image.width, image.height], image.resource);
    }
    assert.equal(total, report.compressedBytes);
  } finally {
    await browser.close();
  }
});
