# CineRecreate AI

## Current state

CineRecreate is a GitHub Pages PWA with a local story planner (`studio.html`), a Replicate video interface (`video.html`, requiring a separately configured server), and an on-device feasibility benchmark (`device-lab.html`). **There is not yet a locally running AI video model or AI video generator.** The Device Lab is a real WebGL workload benchmark, not an AI model and not a prediction of model inference speed.

## Start the on-device feasibility test

Open `https://86hhy.github.io/Movie-ReconstructionAi/device-lab.html` in Chrome on the target Samsung phone. Note the reported browser capabilities, run the short graphics workload once while the phone is cool, and record the result. Nothing from this test is uploaded. Device/browser privacy settings may hide the exact chipset or RAM.

This test is only the first measurement. It does not download weights, invoke an NPU, or produce a movie. A real offline model requires selecting an appropriately licensed model, building/quantizing a compatible Android inference runtime, integrating it with a native Android app, and testing memory, speed, heat, battery use, and output quality on the actual handset. A PWA benchmark cannot establish that those steps will succeed.

## Project roadmap for phone-local video research

1. Record target phone's exact regional chipset/RAM and benchmark results; don't infer AI capability from WebGL scores.
2. Evaluate published, properly licensed mobile/edge video models and available Android inference runtimes. Verify license and model-card requirements before redistribution.
3. Create a minimal native Android inference spike with a tiny, short, low-resolution model; keep model weights as separately downloaded assets, not silently bundled in the website.
4. Measure peak memory, runtime, thermal throttling, battery impact, crash rate, and output. Only then decide whether on-device generation is viable for the target device.
5. Integrate successful inference into CineRecreate and build movie reconstruction features such as shot planning, reference-frame conditioning, and clip sequencing.

No timeline or quality target is promised before real inference runs on the target phone. If the device cannot run a useful model, the app must say so rather than silently fall back to paid cloud generation.

## Existing web application

- `studio.html`: local story outline planner; projects are stored in that browser.
- `video.html`: Replicate client interface; works only with a separately deployed API and a server-side Replicate token.
- `device-lab.html`: offline browser graphics benchmark; no network upload and no AI inference.
- `api/`: server-side Replicate endpoints; never put the provider token in client code.

GitHub Pages publishes the static app over HTTPS. To install on Android, open the Pages URL in Chrome/Samsung Internet and choose **Install app** / **Add to Home screen**. Existing installed apps may need reinstalling to pick up manifest start URL changes.
