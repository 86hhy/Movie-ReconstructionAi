const CACHE_PREFIX = 'cinerecreate-shell-';
const CACHE_NAME = `${CACHE_PREFIX}v2`;
const APP_SHELL = [
  './',
  './index.html',
  './manifest.json',
  './icons/icon-192.svg',
  './icons/icon-512.svg'
];
const scopeUrl = new URL(self.registration.scope);
const shellUrl = new URL('./index.html', scopeUrl).href;

self.addEventListener('install', event => {
  event.waitUntil(
    caches.open(CACHE_NAME)
      .then(cache => cache.addAll(APP_SHELL.map(path => new URL(path, scopeUrl).href)))
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
      try {
        return await fetch(request);
      } catch {
        return (await caches.match(shellUrl)) || Response.error();
      }
    })());
    return;
  }

  // Cache the local app shell and static resources only; never cache API responses.
  if (!['script', 'style', 'image', 'font', 'manifest'].includes(request.destination)) return;
  event.respondWith((async () => {
    const cached = await caches.match(request);
    if (cached) return cached;

    const response = await fetch(request);
    if (response.ok && response.type === 'basic') {
      const copy = response.clone();
      event.waitUntil(caches.open(CACHE_NAME).then(cache => cache.put(request, copy)));
    }
    return response;
  })());
});
