# CineRecreate AI

Progressive Web App shell for CineRecreate AI, designed for GitHub Pages and Android browsers.

## GitHub Pages deployment

1. In the repository, open **Settings → Pages**.
2. Select **Deploy from a branch**, choose `main` and `/ (root)`, then save. (Alternatively, use an existing Pages workflow.)
3. Wait for the HTTPS Pages URL to become available. Service workers and PWA installation require a secure context; GitHub Pages provides HTTPS.
4. Open the published site in Chrome or Samsung Internet on Android. Use the in-page install prompt when offered, or open the browser menu and tap **Install app** / **Add to Home screen**.

All app-shell asset paths are relative so the PWA also works when Pages publishes the repository under a project subpath.

## Offline behavior

The service worker caches the app shell and serves the interface for offline launches. AI generation and any API-backed features require an internet connection and are not provided offline.

## Current project contents

The repository initially contained only this README, with no existing website or AI implementation to preserve or integrate with. This commit adds the installable PWA shell; existing AI functionality should be integrated into `index.html` when its source becomes available. The included artwork is SVG at 192×192 and 512×512 and is declared in the web manifest.
