# CineRecreate Android inference prototype

This is the first **native Android model-runtime spike** for CineRecreate. It downloads Google's TensorFlow Lite EfficientNet-Lite0 INT8 image-classification model and ImageNet labels, then runs classification locally on a chosen image. The model is downloaded to the app's private files directory and is not included in the repository or APK.

## Important scope

- This app runs a real image classifier on-device after a one-time model download.
- It does **not** create, animate, or generate AI video. Image classification is a small runtime test only.
- The first implementation uses the TensorFlow Lite CPU interpreter. It does not claim to use the Exynos NPU or Mali GPU.
- The code has not been built or tested on a Galaxy A26 in this environment. Build/install it before treating inference performance as verified.
- The model and labels download from their upstream HTTPS URLs. This is separate from the GitHub Pages PWA and requires internet on first download.

## Build with Android Studio

1. Install Android Studio with Android SDK Platform 35 and a compatible JDK (Android Studio's bundled JDK is suitable).
2. Open the `android-prototype/` directory as an existing Gradle project.
3. Let Gradle sync and download the TensorFlow Lite dependency.
4. Connect the Galaxy A26 with USB debugging enabled, or use Android Studio's device pairing, then press **Run**.
5. In the app, tap **Download model for offline tests**. Use Wi-Fi; the model is downloaded to private app storage.
6. Tap **Choose an image and classify on-device**, choose a photo, and record the displayed inference time and any error. The first download needs internet; subsequent inference uses the local model.

To build from a terminal with Gradle installed, run `gradle :app:assembleDebug` from this directory. The APK output will be under `app/build/outputs/apk/debug/`.

## Model source

- TensorFlow Hub EfficientNet-Lite0 INT8: https://tfhub.dev/tensorflow/lite-model/efficientnet/lite0/int8/2?lite-format=tflite
- ImageNet labels: https://storage.googleapis.com/download.tensorflow.org/data/imagenet_labels.txt
- Expected image input: 224 × 224 RGB. The app reads model tensor shape/type/quantization at runtime and preprocesses to match common INT8/UINT8/FLOAT32 tensors.
- Runtime dependency: `org.tensorflow:tensorflow-lite:2.17.0`

Verify the upstream model terms and input/output metadata before redistributing the model or using it commercially. The app does not bundle model weights.

## Next research milestone

Run this prototype on the actual A26 and collect model download success, input/output tensor metadata, inference latency, peak memory, heat, battery change, and crash behavior. This classifier result cannot be extrapolated to text-to-video. Only after a real mobile-sized video model and compatible runtime are identified should we implement a local clip-generation experiment.
