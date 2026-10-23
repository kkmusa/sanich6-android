// Local Android WebView reader. Page images, links and hit boxes scale together.
(function () {
  'use strict';
  const pages = [...document.querySelectorAll('.page')];
  if (!pages.length) return;
  const KEY = 'sanich6_vnr_reader_v1';
  const style = document.createElement('style');
  style.textContent =
    'html{overflow-anchor:none}body{box-sizing:border-box;width:100vw}.vnr-position,.vnr-bookmarks{box-sizing:border-box;width:calc(100vw - 16px);right:auto}.page{width:calc((100vw - 10px) * var(--vnr-zoom,1));max-width:calc(960px * var(--vnr-zoom,1))}';
  document.head.appendChild(style);
  let zoom = 1,
    restoring = true,
    timer,
    last;
  const clamp = (value, low, high) => Math.max(low, Math.min(high, value));
  function inset() {
    return (document.querySelector('.vnr-position')?.getBoundingClientRect().bottom || 0) + 8;
  }
  function position() {
    const line = scrollY + inset();
    let low = 0,
      high = pages.length - 1;
    while (low < high) {
      const mid = Math.ceil((low + high) / 2);
      if (pages[mid].offsetTop <= line) low = mid;
      else high = mid - 1;
    }
    const page = pages[low];
    return {
      page: low + 1,
      fraction: clamp((line - page.offsetTop) / page.offsetHeight, 0, 1),
      horizontal: clamp(scrollX / page.offsetWidth, 0, 1),
      zoom,
    };
  }
  function valid(value) {
    return (
      value &&
      Number.isInteger(value.page) &&
      value.page >= 1 &&
      value.page <= pages.length &&
      Number.isFinite(value.fraction) &&
      value.fraction >= 0 &&
      value.fraction <= 1 &&
      Number.isFinite(value.horizontal) &&
      value.horizontal >= 0 &&
      value.horizontal <= 1 &&
      Number.isFinite(value.zoom) &&
      value.zoom >= 0.75 &&
      value.zoom <= 3
    );
  }
  function save() {
    if (restoring) return;
    clearTimeout(timer);
    last = position();
    try {
      const value = JSON.stringify(last);
      if (window.SanichBookmarks?.writeView) window.SanichBookmarks.writeView(value);
      else localStorage.setItem(KEY, value);
    } catch {
      /* A failed preference write must not interrupt reading. */
    }
  }
  function restore(anchor) {
    const page = pages[anchor.page - 1];
    scrollTo(
      anchor.horizontal * page.offsetWidth,
      page.offsetTop + anchor.fraction * page.offsetHeight - inset(),
    );
  }
  function setZoom(value) {
    const requested = Number(value);
    if (!Number.isFinite(requested)) return zoom;
    const anchor = position();
    zoom = Math.round(clamp(requested, 0.75, 3) * 100) / 100;
    restoring = true;
    document.documentElement.style.setProperty('--vnr-zoom', zoom);
    // Layout is synchronous: retain the actual visible page fraction, not a stale page number.
    restore(anchor);
    restoring = false;
    save();
    return zoom;
  }
  let initial;
  try {
    const value = window.SanichBookmarks?.readView
      ? window.SanichBookmarks.readView()
      : localStorage.getItem(KEY);
    const parsed = JSON.parse(value || 'null');
    if (valid(parsed)) initial = parsed;
  } catch {
    /* Ignore corrupt preferences, never bookmarks. */
  }
  if (initial) {
    zoom = initial.zoom;
    document.documentElement.style.setProperty('--vnr-zoom', zoom);
    restore(initial);
  }
  restoring = false;
  last = position();
  window.jumpPage = function (number) {
    const page = pages[number - 1];
    if (page) {
      scrollTo(0, page.offsetTop - inset() + 1);
      save();
    }
  };
  window.VnrReader = { getZoom: () => zoom, getPosition: position, setZoom, save };
  window.addEventListener(
    'scroll',
    () => {
      if (restoring) return;
      last = position();
      clearTimeout(timer);
      timer = setTimeout(save, 150);
    },
    { passive: true },
  );
  window.addEventListener('resize', () => {
    restoring = true;
    restore(last);
    restoring = false;
    save();
  });
  window.addEventListener('pagehide', save);
  document.addEventListener('visibilitychange', () => {
    if (document.hidden) save();
  });
})();
