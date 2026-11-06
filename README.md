# CineRecreate AI

CineRecreate AI is a mobile-friendly PWA with a local story planner and an AI video-scene generator integration for Replicate. The static website runs on GitHub Pages; video generation requires deploying the included serverless API and configuring a Replicate account/token.

## GitHub Pages deployment

1. Open **Settings → Pages** and publish `main` from `/ (root)` (or use an existing Pages workflow).
2. Wait for the HTTPS site to publish.
3. Open `https://86hhy.github.io/Movie-ReconstructionAi/`. The installed app starts in the AI Video Studio; the story planner is linked from the top navigation.

## Enable Replicate video generation (required)

The GitHub Pages site cannot execute backend code or keep a Replicate token secret. The included `api/` functions are intended for a separate Vercel deployment from this repository.

1. Create/sign in to a Replicate account and check the selected model's access, input schema, and pricing on its Replicate model page. Replicate generation can incur charges.
2. Import this repository into Vercel and deploy it. Vercel serves the static files and the included `/api/generate` and `/api/predictions` serverless functions. You can keep GitHub Pages as the public website if preferred; the functions must be deployed to Vercel.
3. In the Vercel project's **Settings → Environment Variables**, set:
   - `REPLICATE_API_TOKEN` = your Replicate token (server secret; never add it to HTML, JavaScript, GitHub, or this chat).
   - `REPLICATE_MODEL` = the Replicate model slug `owner/model` to use. It defaults to `google/veo-3`; confirm the model is currently available to your account and its input accepts a `prompt` string. Change this value if you choose another compatible text-to-video model.
   - `CORS_ALLOWED_ORIGIN` = `https://86hhy.github.io` when using this GitHub Pages address.
4. Redeploy after adding/changing environment variables. Open the published app's **AI Video Studio**, enter the Vercel deployment origin (for example `https://your-project.vercel.app`) in **Replicate API server URL**, and save it on that device.
5. Enter a shot prompt and tap **Generate video**. The app submits a job, checks its status, and displays the returned video when ready.

The frontend never receives the Replicate token. The included API restricts browser CORS to the configured site origin, but CORS alone is **not** authentication or abuse prevention: before sharing a public generator, add real user authentication, quotas/rate limits, and cost monitoring to the backend. Do not publish a public paid endpoint without those controls.

## Create a movie plan

The **Story planner** creates a structured scene outline locally and supports project export. Projects are stored in that browser on that device. The video generator currently makes individual clips, not a fully edited feature film; editing, sound design, and assembling generated clips are future work.

## Samsung Android installation

Open the published HTTPS site in Chrome or Samsung Internet, use the browser menu's **Install app** / **Add to Home screen**, and confirm. If an older CineRecreate icon is already installed, remove it and reinstall after the Pages deployment finishes so the icon uses the new video-studio start page. Open the app online at least once to cache its interface. The UI can open offline, but Replicate generation and fetching results need internet.

## API behavior and local development notes

`POST /api/generate` submits the prompt to Replicate using the server-only token; `GET /api/predictions?id=...` retrieves prediction status/output. The API relays provider errors to the interface and never caches predictions. The model's input/output schema and availability may change; verify against the model page. Static GitHub Pages cannot host these API routes, so a deployed Vercel backend and its environment secrets are required for live generation.
