package ai.cinerecreate.mobile;

import android.app.Activity;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.graphics.Typeface;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.view.Gravity;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.TextView;

import org.tensorflow.lite.DataType;
import org.tensorflow.lite.Interpreter;
import org.tensorflow.lite.Tensor;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.MappedByteBuffer;
import java.nio.channels.FileChannel;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * Native feasibility spike: downloads and runs a real image classifier locally.
 * This is intentionally not advertised as video generation.
 */
public final class MainActivity extends Activity {
    private static final int PICK_IMAGE = 42;
    private static final String MODEL_URL = "https://tfhub.dev/tensorflow/lite-model/efficientnet/lite0/int8/2?lite-format=tflite";
    private static final String LABELS_URL = "https://storage.googleapis.com/download.tensorflow.org/data/imagenet_labels.txt";
    private static final int INPUT_SIZE = 224;
    private static final long MAX_MODEL_BYTES = 150L * 1024L * 1024L;

    private final ExecutorService worker = Executors.newSingleThreadExecutor();
    private final Handler main = new Handler(Looper.getMainLooper());
    private TextView status;
    private TextView result;
    private ImageView preview;
    private Button modelButton;
    private Button chooseButton;
    private Uri selectedImage;
    private volatile boolean busy;

    @Override public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getWindow().setStatusBarColor(Color.rgb(16, 16, 24));
        getWindow().setNavigationBarColor(Color.rgb(16, 16, 24));
        buildUi();
        refreshModelState();
    }

    private void buildUi() {
        ScrollView scroll = new ScrollView(this);
        scroll.setFillViewport(true);
        LinearLayout page = new LinearLayout(this);
        page.setOrientation(LinearLayout.VERTICAL);
        page.setPadding(dp(22), dp(28), dp(22), dp(28));
        page.setBackgroundColor(Color.rgb(16, 16, 24));
        scroll.addView(page);

        TextView brand = text("CineRecreate AI  /  Android Device Lab", 14, 0xFFC6A9FF, true);
        page.addView(brand);
        TextView title = text("Run a real model on this phone", 30, Color.WHITE, true);
        LinearLayout.LayoutParams titleParams = params(-1, -2);
        titleParams.topMargin = dp(18);
        page.addView(title, titleParams);
        TextView explanation = text("This prototype downloads an image-classification model once, then runs inference locally on a selected image. It tests the Android model runtime only. It does not generate video.", 15, 0xFFBDB8C9, false);
        LinearLayout.LayoutParams explParams = params(-1, -2);
        explParams.topMargin = dp(10);
        page.addView(explanation, explParams);

        TextView warning = text("Model download requires internet. After download, classification runs on-device. The model is not bundled in the APK. Results are not uploaded.", 14, 0xFFE6D5B8, false);
        warning.setPadding(dp(14), dp(13), dp(14), dp(13));
        warning.setBackgroundColor(0xFF292219);
        LinearLayout.LayoutParams warningParams = params(-1, -2);
        warningParams.topMargin = dp(20);
        page.addView(warning, warningParams);

        status = text("Checking model…", 14, 0xFFBDB8C9, false);
        LinearLayout.LayoutParams statusParams = params(-1, -2);
        statusParams.topMargin = dp(18);
        page.addView(status, statusParams);

        modelButton = button("Download model for offline tests");
        LinearLayout.LayoutParams modelParams = params(-1, -2);
        modelParams.topMargin = dp(12);
        page.addView(modelButton, modelParams);
        modelButton.setOnClickListener(v -> downloadModel());

        chooseButton = button("Choose an image and classify on-device");
        LinearLayout.LayoutParams chooseParams = params(-1, -2);
        chooseParams.topMargin = dp(10);
        page.addView(chooseButton, chooseParams);
        chooseButton.setOnClickListener(v -> openImagePicker());

        preview = new ImageView(this);
        preview.setAdjustViewBounds(true);
        preview.setScaleType(ImageView.ScaleType.FIT_CENTER);
        LinearLayout.LayoutParams imageParams = params(-1, dp(260));
        imageParams.topMargin = dp(18);
        page.addView(preview, imageParams);

        result = text("Top predictions will appear here.", 15, Color.WHITE, false);
        result.setPadding(dp(14), dp(14), dp(14), dp(14));
        result.setBackgroundColor(0xFF1D1B25);
        LinearLayout.LayoutParams resultParams = params(-1, -2);
        resultParams.topMargin = dp(12);
        page.addView(result, resultParams);

        TextView boundary = text("Why this test matters: image classification is much smaller than video generation. A successful result proves only that this model ran on the device; it does not prove that video generation will fit, run fast enough, or produce useful clips.", 13, 0xFFAAA5B7, false);
        LinearLayout.LayoutParams boundaryParams = params(-1, -2);
        boundaryParams.topMargin = dp(20);
        page.addView(boundary, boundaryParams);
        setContentView(scroll);
    }

    private void refreshModelState() {
        File model = new File(getFilesDir(), "efficientnet_lite0_int8.tflite");
        boolean exists = model.isFile() && model.length() > 100_000;
        status.setText(exists ? "Model ready offline · " + (model.length() / (1024 * 1024)) + " MB" : "Model not downloaded yet · internet required once");
        modelButton.setText(exists ? "Download model again" : "Download model for offline tests");
        chooseButton.setEnabled(exists && !busy);
    }

    private void downloadModel() {
        if (busy) return;
        busy = true;
        modelButton.setEnabled(false);
        status.setText("Downloading model… keep the app open and use Wi-Fi if possible.");
        worker.execute(() -> {
            try {
                File model = new File(getFilesDir(), "efficientnet_lite0_int8.tflite");
                downloadAtomically(MODEL_URL, model, MAX_MODEL_BYTES);
                File labels = new File(getFilesDir(), "imagenet_labels.txt");
                downloadAtomically(LABELS_URL, labels, 2L * 1024L * 1024L);
                onMain(() -> {
                    busy = false;
                    modelButton.setEnabled(true);
                    status.setText("Download complete · model ready for offline inference");
                    refreshModelState();
                });
            } catch (Exception ex) {
                onMain(() -> {
                    busy = false;
                    modelButton.setEnabled(true);
                    status.setText("Download failed: " + safeMessage(ex));
                    refreshModelState();
                });
            }
        });
    }

    private void downloadAtomically(String source, File destination, long maxBytes) throws Exception {
        File temp = new File(destination.getParentFile(), destination.getName() + ".part");
        HttpURLConnection connection = (HttpURLConnection) new URL(source).openConnection();
        connection.setConnectTimeout(20_000);
        connection.setReadTimeout(60_000);
        connection.setInstanceFollowRedirects(true);
        connection.setRequestProperty("User-Agent", "CineRecreateAndroidPrototype/0.1");
        try {
            int code = connection.getResponseCode();
            if (code < 200 || code >= 300) throw new IllegalStateException("Download server returned HTTP " + code);
            long declared = connection.getContentLengthLong();
            if (declared > maxBytes) throw new IllegalStateException("Download exceeds the safety size limit");
            long total = 0;
            try (InputStream input = connection.getInputStream(); FileOutputStream output = new FileOutputStream(temp)) {
                byte[] buffer = new byte[32 * 1024];
                int count;
                while ((count = input.read(buffer)) != -1) {
                    total += count;
                    if (total > maxBytes) throw new IllegalStateException("Download exceeds the safety size limit");
                    output.write(buffer, 0, count);
                }
                output.getFD().sync();
            }
            if (total < 1000) throw new IllegalStateException("Downloaded file is unexpectedly small");
            if (destination.exists() && !destination.delete()) throw new IllegalStateException("Cannot replace existing model file");
            if (!temp.renameTo(destination)) throw new IllegalStateException("Cannot finalize downloaded file");
        } finally {
            connection.disconnect();
            if (temp.exists()) temp.delete();
        }
    }

    private void openImagePicker() {
        if (busy) return;
        Intent intent = new Intent(Intent.ACTION_OPEN_DOCUMENT);
        intent.addCategory(Intent.CATEGORY_OPENABLE);
        intent.setType("image/*");
        startActivityForResult(intent, PICK_IMAGE);
    }

    @Override protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode != PICK_IMAGE || resultCode != RESULT_OK || data == null || data.getData() == null) return;
        selectedImage = data.getData();
        try {
            getContentResolver().takePersistableUriPermission(selectedImage, Intent.FLAG_GRANT_READ_URI_PERMISSION);
        } catch (SecurityException ignored) { }
        try (InputStream input = getContentResolver().openInputStream(selectedImage)) {
            Bitmap bitmap = BitmapFactory.decodeStream(input);
            if (bitmap == null) throw new IllegalStateException("Could not decode the selected image");
            preview.setImageBitmap(bitmap);
            runClassification(bitmap);
        } catch (Exception ex) {
            result.setText("Could not open image: " + safeMessage(ex));
        }
    }

    private void runClassification(Bitmap source) {
        if (busy) return;
        busy = true;
        chooseButton.setEnabled(false);
        result.setText("Running local inference…");
        Bitmap inputBitmap = Bitmap.createScaledBitmap(source, INPUT_SIZE, INPUT_SIZE, true);
        worker.execute(() -> {
            long started = System.nanoTime();
            try {
                File modelFile = new File(getFilesDir(), "efficientnet_lite0_int8.tflite");
                if (!modelFile.isFile()) throw new IllegalStateException("Download the model first");
                try (FileInputStream stream = new FileInputStream(modelFile); FileChannel channel = stream.getChannel()) {
                    MappedByteBuffer mapped = channel.map(FileChannel.MapMode.READ_ONLY, 0, channel.size());
                    Interpreter.Options options = new Interpreter.Options().setNumThreads(4);
                    try (Interpreter interpreter = new Interpreter(mapped, options)) {
                        Tensor inputTensor = interpreter.getInputTensor(0);
                        int[] shape = inputTensor.shape();
                        if (shape.length != 4 || shape[0] != 1 || shape[1] != INPUT_SIZE || shape[2] != INPUT_SIZE || shape[3] != 3) {
                            throw new IllegalStateException("Unexpected model input shape: " + java.util.Arrays.toString(shape));
                        }
                        ByteBuffer input = makeInput(inputBitmap, inputTensor);
                        Tensor outputTensor = interpreter.getOutputTensor(0);
                        ByteBuffer output = ByteBuffer.allocateDirect(outputTensor.numBytes()).order(ByteOrder.nativeOrder());
                        long inferenceStart = System.nanoTime();
                        interpreter.run(input, output);
                        long inferenceMs = (System.nanoTime() - inferenceStart) / 1_000_000L;
                        List<String> labels = loadLabels();
                        List<Score> scores = topScores(output, outputTensor, labels, 5);
                        StringBuilder text = new StringBuilder("On-device top predictions\n");
                        for (int i = 0; i < scores.size(); i++) {
                            Score score = scores.get(i);
                            text.append(i + 1).append(". ").append(score.label).append(" — ")
                                .append(String.format(java.util.Locale.US, "%.1f%%", score.probability * 100)).append('\n');
                        }
                        text.append("\nModel inference: ").append(inferenceMs).append(" ms · CPU runtime · ")
                            .append(String.format(java.util.Locale.US, "%.1f", (System.nanoTime() - started) / 1_000_000_000.0)).append(" s total");
                        onMain(() -> { result.setText(text.toString()); busy = false; refreshModelState(); });
                    }
                }
            } catch (Exception ex) {
                onMain(() -> { result.setText("Local inference failed: " + safeMessage(ex)); busy = false; refreshModelState(); });
            }
        });
    }

    private ByteBuffer makeInput(Bitmap bitmap, Tensor tensor) {
        int typeSize = tensor.dataType() == DataType.FLOAT32 ? 4 : 1;
        ByteBuffer buffer = ByteBuffer.allocateDirect(INPUT_SIZE * INPUT_SIZE * 3 * typeSize).order(ByteOrder.nativeOrder());
        int[] pixels = new int[INPUT_SIZE * INPUT_SIZE];
        bitmap.getPixels(pixels, 0, INPUT_SIZE, 0, 0, INPUT_SIZE, INPUT_SIZE);
        Tensor.QuantizationParams quant = tensor.quantizationParams();
        float scale = quant.getScale();
        int zeroPoint = quant.getZeroPoint();
        for (int pixel : pixels) {
            int[] rgb = { Color.red(pixel), Color.green(pixel), Color.blue(pixel) };
            for (int channel : rgb) {
                float real = channel / 255.0f;
                if (tensor.dataType() == DataType.FLOAT32) buffer.putFloat(real);
                else if (tensor.dataType() == DataType.UINT8) {
                    int q = scale > 0 ? Math.round(real / scale + zeroPoint) : channel;
                    buffer.put((byte) Math.max(0, Math.min(255, q)));
                } else if (tensor.dataType() == DataType.INT8) {
                    int q = scale > 0 ? Math.round(real / scale + zeroPoint) : channel - 128;
                    buffer.put((byte) Math.max(-128, Math.min(127, q)));
                } else {
                    throw new IllegalStateException("Unsupported input tensor type: " + tensor.dataType());
                }
            }
        }
        buffer.rewind();
        return buffer;
    }

    private List<Score> topScores(ByteBuffer output, Tensor tensor, List<String> labels, int limit) {
        int count = tensor.numElements();
        Tensor.QuantizationParams q = tensor.quantizationParams();
        float scale = q.getScale();
        int zeroPoint = q.getZeroPoint();
        List<Score> scores = new ArrayList<>();
        output.rewind();
        for (int i = 0; i < count; i++) {
            float value;
            if (tensor.dataType() == DataType.FLOAT32) value = output.getFloat();
            else if (tensor.dataType() == DataType.UINT8) value = ((output.get() & 0xFF) - zeroPoint) * (scale > 0 ? scale : 1f);
            else if (tensor.dataType() == DataType.INT8) value = (output.get() - zeroPoint) * (scale > 0 ? scale : 1f);
            else throw new IllegalStateException("Unsupported output tensor type: " + tensor.dataType());
            String label = i < labels.size() ? labels.get(i) : "Class " + i;
            scores.add(new Score(label, value));
        }
        Collections.sort(scores, Comparator.comparingDouble((Score s) -> s.probability).reversed());
        return scores.subList(0, Math.min(limit, scores.size()));
    }

    private List<String> loadLabels() throws Exception {
        File file = new File(getFilesDir(), "imagenet_labels.txt");
        List<String> labels = new ArrayList<>();
        try (BufferedReader reader = new BufferedReader(new InputStreamReader(new FileInputStream(file), "UTF-8"))) {
            String line;
            while ((line = reader.readLine()) != null) labels.add(line.trim());
        }
        return labels;
    }

    private void onMain(Runnable runnable) { main.post(runnable); }
    private String safeMessage(Exception error) { return error.getMessage() == null ? error.getClass().getSimpleName() : error.getMessage(); }
    private int dp(int value) { return Math.round(value * getResources().getDisplayMetrics().density); }
    private LinearLayout.LayoutParams params(int width, int height) { return new LinearLayout.LayoutParams(width, height); }
    private TextView text(String value, int sizeSp, int color, boolean bold) {
        TextView view = new TextView(this);
        view.setText(value);
        view.setTextSize(sizeSp);
        view.setTextColor(color);
        if (bold) view.setTypeface(Typeface.DEFAULT, Typeface.BOLD);
        view.setGravity(Gravity.CENTER_VERTICAL);
        return view;
    }
    private Button button(String label) {
        Button button = new Button(this);
        button.setText(label);
        button.setAllCaps(false);
        button.setTextColor(Color.rgb(28, 20, 38));
        return button;
    }
    @Override protected void onDestroy() {
        worker.shutdownNow();
        super.onDestroy();
    }

    private static final class Score {
        final String label;
        final float probability;
        Score(String label, float probability) { this.label = label; this.probability = probability; }
    }
}
