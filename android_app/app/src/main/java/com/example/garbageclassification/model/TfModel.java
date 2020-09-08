package com.example.garbageclassification.model;

import android.app.Activity;
import android.graphics.Bitmap;

import org.tensorflow.lite.DataType;
import org.tensorflow.lite.Interpreter;
import org.tensorflow.lite.support.common.FileUtil;
import org.tensorflow.lite.support.common.TensorProcessor;
import org.tensorflow.lite.support.image.ImageProcessor;
import org.tensorflow.lite.support.image.TensorImage;
import org.tensorflow.lite.support.image.ops.ResizeOp;
import org.tensorflow.lite.support.image.ops.ResizeWithCropOrPadOp;
import org.tensorflow.lite.support.label.TensorLabel;
import org.tensorflow.lite.support.tensorbuffer.TensorBuffer;

import java.io.IOException;
import java.nio.MappedByteBuffer;
import java.util.List;
import java.util.Map;

public class TfModel implements IModel {
    private Interpreter tflite;
    private MappedByteBuffer tfliteModel;
    private final Interpreter.Options tfliteOptions;

    private TensorImage imageBuffer;
    private final TensorBuffer probabilityBuffer;

    private final TensorProcessor probabilityProcessor;

    private List<String> labels;

    private int targetHeight;
    private int targetWidth;

    private final static String MODELPATH = "models/garbage_classification_model.tflite";
    private final static String LABELSPATH = "models/labels.txt";

    TfModel(Activity activity, int numThreads) throws IOException {
        this.tfliteModel = FileUtil.loadMappedFile(activity, MODELPATH);
        this.labels = FileUtil.loadLabels(activity, LABELSPATH);

        this.tfliteOptions = new Interpreter.Options();
        tfliteOptions.setNumThreads(numThreads);
        this.tflite = new Interpreter(tfliteModel, tfliteOptions);

        // Reads type and shape of input and output tensors, respectively.
        int imageTensorIndex = 0;
        int[] imageShape = tflite.getInputTensor(imageTensorIndex).shape(); // {1, height, width, 3}
        this.targetHeight = imageShape[1];
        this.targetWidth = imageShape[2];
        DataType imageDataType = this.tflite.getInputTensor(imageTensorIndex).dataType();
        int probabilityTensorIndex = 0;
        int[] probabilityShape =
                this.tflite.getOutputTensor(probabilityTensorIndex).shape(); // {1, NUM_CLASSES}
        DataType probabilityDataType = this.tflite.getOutputTensor(probabilityTensorIndex).dataType();

        // Creates the input tensor.
        this.imageBuffer = new TensorImage(imageDataType);

        // Creates the output tensor and its processor.
        this.probabilityBuffer = TensorBuffer.createFixedSize(probabilityShape, probabilityDataType);

        this.probabilityProcessor = new TensorProcessor.Builder().build();
    }

    public String predict(final Bitmap bitmap) {
        this.imageBuffer = loadImage(bitmap);
        this.tflite.run(imageBuffer.getBuffer(), probabilityBuffer.getBuffer().rewind());

        Map<String, Float> labeledProbability =
                new TensorLabel(labels, probabilityProcessor.process(probabilityBuffer))
                .getMapWithFloatValue();

        return labeledProbability.toString();

//        Float maxProbability = -Float.MAX_VALUE;
//        String label = "";

//        for (Map.Entry<String, Float> entry : labeledProbability.entrySet()) {
//            if (entry.getValue() > maxProbability) {
//                label = entry.getKey();
//            }
//        }
//
//        return label;
    }

    public void close() {
        this.tflite.close();
        this.tflite = null;
    }

    private TensorImage loadImage(final Bitmap bitmap) {
        this.imageBuffer.load(bitmap);

        // Creates processor for the TensorImage.
        int cropSize = Math.min(bitmap.getWidth(), bitmap.getHeight());

        ImageProcessor imageProcessor =
                new ImageProcessor.Builder()
                        .add(new ResizeWithCropOrPadOp(cropSize, cropSize))
                        .add(new ResizeOp(this.targetHeight, this.targetWidth, ResizeOp.ResizeMethod.NEAREST_NEIGHBOR))
                        .build();
        return imageProcessor.process(imageBuffer);
    }
}
