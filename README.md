# CineRecreate AI

CineRecreate AI is currently a responsive, installable Progressive Web App shell for Android browsers and GitHub Pages.

## GitHub Pages deployment

1. Open the repository's **Settings → Pages**.
2. Select **Deploy from a branch**, choose `main` and `/ (root)`, then save (or use the repository's Pages workflow).
3. Wait until the published Pages URL is available over HTTPS. Service workers and PWA installation require a secure context; GitHub Pages serves sites over HTTPS.
4. Open the published URL in Chrome or Samsung Internet on Android. Install from the in-page prompt when available, or use the browser menu and tap **Install app** / **Add to Home screen**.

All app-shell URLs are relative to the service worker scope so the repository works from a GitHub Pages project subpath.

## Offline behavior

The service worker precaches the application shell and local icons. It serves the cached interface when a page navigation cannot reach the network. It does not cache API responses. AI generation and any API-backed features require an active internet connection; the existing interface reports offline status and explains this limitation.

## Android installation (Samsung phone)

1. Ensure the site has been published and open its HTTPS URL in **Chrome** or **Samsung Internet**.
2. In Chrome, tap **⋮ → Install app** (or **Add to Home screen** if that is the option shown). In Samsung Internet, tap **☰ → Add page to → Home screen** or **Install app**, depending on the browser version.
3. Confirm **Install** / **Add**. The CineRecreate AI icon will appear in the app drawer or on the home screen. Tap it to open the site in standalone app mode without the usual browser address bar.

The browser controls the exact wording and availability of its install action, so it may differ by browser and version. The in-page install button is shown when the browser provides its install prompt.

## Current functionality and limitations

This repository currently contains the PWA landing/interface shell, install prompt, connectivity status, manifest, and offline app-shell support. It does not contain an AI movie-generation backend or other AI tools; those features cannot work offline and will need to be added separately. The PWA setup preserves the existing page and is not a native Android APK.
