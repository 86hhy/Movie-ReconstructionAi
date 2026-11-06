const CACHE_PREFIX = 'cinerecreate-shell-';
const CACHE_NAME = `${CACHE_PREFIX}v5`;
const SHELL_PATHS = ['./', './index.html', './studio.html', './video.html', './manifest.json', './icons/icon-192.svg', './icons/icon-512.svg'];
const scopeUrl = new URL(self.registration.scope);
const rootUrl = new URL('./', scopeUrl);
const shellUrl = new URL('./video.html', scopeUrl).href;

self.addEventListener('install', event => {
  event.waitUntil(
    caches.open(CACHE_NAME)
      .then(cache => cache.addAll(SHELL_PATHS.map(path => new URL(path, scopeUrl).href)))
      .then(() => self.skipWaiting())
  );
});

self.addEventListener('activate', event => {
  event.waitUntil(
    caches.keys()
      .then(keys => Promise.all(keys
        .filter(key => key.startsWith(CACHE_PREFIX) && key !== CACHE_NAME)
        .map(key => caches.delete(key))))
      .then(() => self.clients.claim())
  );
});

self.addEventListener('fetch', event => {
  const request = event.request;
  if (request.method !== 'GET' || request.headers.has('authorization')) return;
  const url = new URL(request.url);
  if (url.origin !== scopeUrl.origin || !url.href.startsWith(scopeUrl.href)) return;

  if (request.mode === 'navigate') {
    event.respondWith((async () => {
      if (url.pathname === rootUrl.pathname) {
        try { return await fetch(shellUrl); }
        catch { return (await caches.match(shellUrl)) || Response.error(); }
      }
      try { return await fetch(request); }
      catch {
        return (await caches.match(request, { ignoreSearch: true }))
          || (await caches.match(shellUrl))
          || Response.error();
      }
    })());
    return;
  }

  // API requests to a separately deployed host are never intercepted or cached.
  if (!['script', 'style', 'image', 'font', 'manifest'].includes(request.destination)) return;
  event.respondWith((async () => {
    const cached = await caches.match(request);
    if (cached) return cached;
    const response = await fetch(request);
    if (response.ok && response.type === 'basic') {
      event.waitUntil(caches.open(CACHE_NAME).then(cache => cache.put(request, response.clone())));
    }
    return response;
  })());
});
