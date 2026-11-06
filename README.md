# CineRecreate AI

CineRecreate AI is a mobile-friendly Progressive Web App with an offline-ready movie story-planning studio.

## Publish on GitHub Pages

1. Open **Settings → Pages**.
2. Select **Deploy from a branch**, choose `main` and `/ (root)`, then save (or use a Pages workflow).
3. Wait until the HTTPS Pages site is published.
4. Open `https://86hhy.github.io/Movie-ReconstructionAi/` in Chrome or Samsung Internet. The project root remains available as the PWA introduction; the installed app launches the Movie Studio page.

All application paths are relative to the service worker scope, which supports the repository's GitHub Pages project subpath.

## Make a first movie plan

Open the published site and choose the **Movie Studio** start page (or go to `https://86hhy.github.io/Movie-ReconstructionAi/studio.html`). Enter a title, genre, length, and premise, then tap **Build my story outline**. CineRecreate creates a scene-beat outline with suggested shots and saves it in this browser's local storage. Select a saved project to review it, export a JSON backup, or download a plain-text outline.

This is a working local story-planning MVP, not an AI model: it does not yet write original AI scripts, create images/video, or sync projects to the cloud. Those capabilities need a backend and a configured AI provider. Do not put secret provider API keys in client-side HTML or JavaScript.

## Install on a Samsung Android phone

1. Open the published HTTPS address in Chrome or Samsung Internet.
2. In Chrome, tap **⋮ → Install app** (or **Add to Home screen**). In Samsung Internet use **☰ → Add page to → Home screen** or **Install app**, depending on version.
3. Confirm installation. The app icon opens the standalone Movie Studio page. If you already installed it before this update, uninstall the old shortcut/app and reinstall after GitHub Pages finishes publishing the new version.

The browser controls the exact install option labels. GitHub Pages serves over HTTPS; service-worker install/offline behavior requires HTTPS and the app to be opened online once so its app shell can be cached.

## Offline behavior

The service worker caches the local app shell, including `studio.html`, and falls back to the cached studio page when a navigation fails offline. Projects are stored locally on the device. AI provider features, once added, will require internet unless separately designed to run on-device.
