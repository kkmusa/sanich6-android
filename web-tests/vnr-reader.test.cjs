const { test, before, after } = require('node:test');
const assert = require('node:assert/strict');
const { spawn } = require('node:child_process');
const { once } = require('node:events');
const fs = require('node:fs');
const path = require('node:path');
const { chromium } = require('playwright');
let server, browser, base;
before(async () => {
  server = spawn('python3', ['-u', '-m', 'http.server', '0', '--bind', '127.0.0.1'], {
    cwd: path.resolve(__dirname, '../app/src/main/assets'),
    stdio: ['ignore', 'pipe', 'pipe'],
  });
  base = await new Promise((resolve, reject) => {
    server.on('error', reject);
    server.stdout.on('data', (chunk) => {
      const match = chunk.toString().match(/port (\d+)/);
      if (match) resolve(`http://127.0.0.1:${match[1]}/vnr/reglament.html`);
    });
    server.stderr.resume();
  });
  browser = await chromium.launch({
    headless: true,
    args: ['--no-sandbox'],
    ...(fs.existsSync('/usr/bin/chromium') ? { executablePath: '/usr/bin/chromium' } : {}),
  });
});
after(async () => {
  await browser?.close();
  if (server && !server.killed) {
    server.kill();
    await once(server, 'exit');
  }
});
async function withReader(run) {
  const context = await browser.newContext({
    viewport: { width: 390, height: 650 },
    isMobile: true,
    hasTouch: true,
  });
  try {
    // Same synchronous contract as Android JavascriptInterface; native preferences require a device.
    await context.addInitScript(() => {
      window.SanichBookmarks = {
        read: () => localStorage.getItem('native-bookmarks') || '[]',
        write: (data) => {
          localStorage.setItem('native-bookmarks', data);
          return true;
        },
        readView: () => localStorage.getItem('native-reader') || 'null',
        writeView: (data) => {
          localStorage.setItem('native-reader', data);
          return true;
        },
      };
    });
    const page = await context.newPage();
    const errors = [];
    page.on('pageerror', (error) => errors.push(error.message));
    await page.goto(base);
    await page.waitForFunction(() => !!window.VnrReader);
    await run(page);
    assert.deepEqual(errors, []);
  } finally {
    await context.close();
  }
}
test('207 bundled pages; zoom retains the actual reading paragraph and persists after reopening', () =>
  withReader(async (page) => {
    assert.equal(await page.locator('.page').count(), 207);
    for (let pageNumber = 1; pageNumber <= 207; pageNumber++) {
      const data = fs.readFileSync(
        path.resolve(
          __dirname,
          '../app/src/main/assets/vnr/pages',
          String(pageNumber).padStart(3, '0') + '.jpg',
        ),
      );
      assert.ok(
        data.length > 1024 && data[0] === 0xff && data[1] === 0xd8,
        'bundled JPEG ' + pageNumber,
      );
    }
    await page.evaluate(() => {
      const p = document.getElementById('page-87');
      scrollTo(0, p.offsetTop + p.offsetHeight * 0.37 - 62);
    });
    const initial = await page.evaluate(() => VnrReader.getPosition());
    assert.equal(initial.page, 87);
    for (const zoom of [1.25, 1.5, 2, 1, 0.75, 3, 1.5]) {
      await page.evaluate((zoom) => VnrReader.setZoom(zoom), zoom);
      const current = await page.evaluate(() => VnrReader.getPosition());
      assert.equal(current.page, initial.page);
      assert.ok(Math.abs(current.fraction - initial.fraction) < 0.01);
      assert.equal(current.zoom, zoom);
    }
    await page.evaluate(() => VnrReader.save());
    await page.reload();
    const reopened = await page.evaluate(() => VnrReader.getPosition());
    assert.equal(reopened.page, 87);
    assert.ok(Math.abs(reopened.fraction - initial.fraction) < 0.01);
    assert.equal(reopened.zoom, 1.5);
    await page.evaluate(() => VnrReader.setZoom(99));
    assert.equal(await page.evaluate(() => VnrReader.getZoom()), 3);
    await page.evaluate(() => VnrReader.setZoom(-99));
    assert.equal(await page.evaluate(() => VnrReader.getZoom()), 0.75);
  }));
test('bookmarks add, prevent duplicates, rename, reopen after reload and delete', () =>
  withReader(async (page) => {
    await page.evaluate(() => jumpPage(120));
    await page.locator('#vnr-bookmarks-toggle').click();
    await page.locator('#vnr-bookmark-add').click();
    await page.locator('#vnr-bookmark-add').click();
    assert.equal(await page.locator('.vnr-bookmark-row').count(), 1);
    page.once('dialog', (dialog) => dialog.accept('Моя инструкция'));
    await page.getByRole('button', { name: 'Переименовать закладку 120' }).click();
    await page.reload();
    await page.evaluate(() => jumpPage(2));
    await page.locator('#vnr-bookmarks-toggle').click();
    assert.match(await page.locator('.open-bookmark').textContent(), /120.*Моя инструкция/);
    await page.locator('.open-bookmark').click();
    assert.equal(await page.evaluate(() => VnrReader.getPosition().page), 120);
    await page.locator('#vnr-bookmarks-toggle').click();
    await page.getByRole('button', { name: 'Удалить закладку 120' }).click();
    assert.equal(await page.locator('.vnr-bookmark-row').count(), 0);
  }));
test('search highlights and contents links still work at enlarged scale', () =>
  withReader(async (page) => {
    const found = await page.evaluate(() => {
      VnrReader.setZoom(1.75);
      return runSearch('скважин');
    });
    assert.ok(found.total > 100);
    const before = await page.evaluate(() => VnrReader.getPosition());
    await page.evaluate(() => VnrReader.setZoom(2));
    const afterZoom = await page.evaluate(() => VnrReader.getPosition());
    assert.equal(afterZoom.page, before.page);
    assert.ok(Math.abs(afterZoom.fraction - before.fraction) < 0.01);
    assert.ok(await page.locator('.hit.current').count());
    const next = await page.evaluate(() => nextHit(true));
    assert.equal(next.total, found.total);
    assert.equal(next.active, 2);
    await page.evaluate(() => {
      runSearch('');
      jumpPage(2);
    });
    assert.equal(await page.locator('.hit').count(), 0);
    await page.locator('#page-2 .link-hotspot').first().click();
    await page.waitForFunction(() => VnrReader.getPosition().page === 7);
    assert.ok(
      await page
        .locator('.vnr-position')
        .evaluate((el) => el.getBoundingClientRect().width <= visualViewport.width),
    );
  }));
test('corrupt saved reader state does not break the document or delete bookmarks', () =>
  withReader(async (page) => {
    await page.evaluate(() => {
      SanichBookmarks.write('[{"page":15,"name":"Сохранённая"}]');
      localStorage.setItem('native-reader', '{"page":999,"zoom":-4}');
    });
    await page.reload();
    assert.equal(await page.evaluate(() => VnrReader.getZoom()), 1);
    await page.locator('#vnr-bookmarks-toggle').click();
    assert.match(await page.locator('.open-bookmark').textContent(), /Сохранённая/);
  }));
