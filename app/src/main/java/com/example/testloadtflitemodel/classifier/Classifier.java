package com.example.testloadtflitemodel.classifier;

import android.app.Activity;
import android.content.res.AssetFileDescriptor;
import android.os.SystemClock;
import android.util.Log;

import org.tensorflow.lite.Interpreter;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.MappedByteBuffer;
import java.nio.channels.FileChannel;
import java.util.ArrayList;
import java.util.List;

/**
 * only for tensorflow-lite:0.0.0-nightly
 */

public abstract class Classifier {

    /**
     * Tag for the {@link Log}.
     */
    private static final String TAG = "TimeCost";

    /**
     * Number of results to show in the UI.
     */
    private static final int RESULTS_TO_SHOW = 3;

    /**
     * Dimensions of inputs.
     */
    private static final int BYTES_PER_CHANNEL = 4;

    private static final int DIM_BATCH_SIZE = 1;

    public static final int I_DIM_PIXEL = 3;
    public static final int MV_DIM_PIXEL = 2;
    public static final int RES_DIM_PIXEL = 3;

    private int dim_pixel = 2;

    public void setDimPixel(int dim_pixel) {
        this.dim_pixel = dim_pixel;
    }

    private static final int IMAGE_SIZE = 224 * 224;
    public static final float[] I_RES_FRAME_SAMPLE = new float[IMAGE_SIZE * 3];
    public static final float[] MV_FRAME_SAMPLE = new float[IMAGE_SIZE * 2];

    /** Options for configuring the Interpreter. */
    /**
     * only for tensorflow-lite:0.0.0-nightly
     */
    private Interpreter.Options tfliteOptions = new Interpreter.Options();

    /**
     * The loaded TensorFlow Lite model.
     */
    private MappedByteBuffer tfliteModel;

    /**
     * An instance of the driver class to run model inference with Tensorflow Lite.
     */
    protected Interpreter tflite;

    /**
     * Labels corresponding to the output of the vision model.
     */
    private List<String> labelList;

    /**
     * A ByteBuffer to hold image data, to be feed into Tensorflow Lite as inputs.
     */
    protected ByteBuffer Data = null;

    /**
     * multi-stage low pass filter *
     */
    private float[][] filterLabelProbArray = null;

    private float[][] outArr;

    private static final int FILTER_STAGES = 3;
    private static final float FILTER_FACTOR = 0.4f;

    /** holds a gpu delegate */
    /**
     * only for tensorflow-lite:0.0.0-nightly
     */
//    Delegate gpuDelegate = null;

    Classifier(Activity activity, int dim_pixel) throws IOException {
        long startTime = SystemClock.uptimeMillis();
        tfliteModel = loadModelFile(activity);
        long endTime = SystemClock.uptimeMillis();
        Log.d(TAG, "Timecost to load model file: " + Long.toString(endTime - startTime));

        /** only for tensorflow-lite:0.0.0-nightly */
        tfliteOptions.setUseNNAPI(false);

        tfliteOptions.setNumThreads(4);
        tflite = new Interpreter(tfliteModel, tfliteOptions);

//        tflite = new Interpreter(tfliteModel);
//        tflite.setUseNNAPI(true);

//        Log.d("tflite", "InputTensorCount " + tflite.getInputTensorCount());
//        Log.d("tflite", "OutputTensorCount " + tflite.getOutputTensorCount());
//        Log.d("tflite", "InputTensor dataType " + tflite.getInputTensor(0).dataType());
//        Log.d("tflite", "InputTensor numDimensions " + tflite.getInputTensor(0).numDimensions());
//        Log.d("tflite", "InputTensor numBytes " + tflite.getInputTensor(0).numBytes());
//        Log.d("tflite", "InputTensor numElements " + tflite.getInputTensor(0).numElements());

        setDimPixel(dim_pixel);
        labelList = loadLabelList(activity);

        Data = ByteBuffer.allocateDirect(
                DIM_BATCH_SIZE
                        * IMAGE_SIZE
                        * this.dim_pixel
                        * BYTES_PER_CHANNEL);
        Data.order(ByteOrder.nativeOrder());
        //filterLabelProbArray = new float[FILTER_STAGES][getNumLabels()];
        Log.d(TAG, "Created a Tensorflow Lite Image Classifier.");
    }

    Classifier(String filename, int dimPixel) throws IOException {
        long startTime = SystemClock.uptimeMillis();
        tfliteModel = loadModelFile(filename);
        //Only when run method load() of MappedByteBuffer Object, this buffer's content is being loaded into physical memory
//        tfliteModel.load();
        long endTime = SystemClock.uptimeMillis();
        Log.d(TAG, "Timecost to load model file: " + Long.toString(endTime - startTime));

        tfliteOptions.setUseNNAPI(false);
        tfliteOptions.setNumThreads(4);
        tflite = new Interpreter(tfliteModel, tfliteOptions);

        setDimPixel(dimPixel);

        Data = ByteBuffer.allocateDirect(
                DIM_BATCH_SIZE
                        * IMAGE_SIZE
                        * this.dim_pixel
                        * BYTES_PER_CHANNEL);
        Data.order(ByteOrder.nativeOrder());

        Log.d(TAG, "Time to Creat a Tensorflow Lite Image Classifier: "+ (SystemClock.uptimeMillis()-startTime));
        Log.d(TAG, "Created a Tensorflow Lite Image Classifier.");
    }

    public float[] classifyFrame(float input[]) {
        if (tflite == null) {
            Log.e(TAG, "Image classifier has not been initialized; Skipped.");
        }
        convertArrayToByteBuffer(input);
        // Here's where the magic happens!!!
        long startTime = SystemClock.uptimeMillis();
        float[] result = runInference();
        long endTime = SystemClock.uptimeMillis();
        Log.d(TAG, "Timecost to run model inference: " + Long.toString(endTime - startTime));
        return result;
    }


    /**
     * Memory-map the model file in Assets or External Storage.
     * How MappedByteBuffer work: https://docs.oracle.com/en/java/javase/12/docs/api/java.base/java/nio/MappedByteBuffer.html
     */
    private MappedByteBuffer loadModelFile(Activity activity) throws IOException {
        AssetFileDescriptor fileDescriptor = activity.getAssets().openFd(getModelPath());
        //AssetFileDescriptor fileDescriptor = activity.getAssets().openFd(getLabelPath());
        FileInputStream inputStream = new FileInputStream(fileDescriptor.getFileDescriptor());
        FileChannel fileChannel = inputStream.getChannel();
        long startOffset = fileDescriptor.getStartOffset();
        long declaredLength = fileDescriptor.getDeclaredLength();
        return fileChannel.map(FileChannel.MapMode.READ_ONLY, startOffset, declaredLength);
    }

    private MappedByteBuffer loadModelFile(String filename) throws IOException {
        File file = new File(filename);
        FileInputStream inputStream = new FileInputStream(file);
        FileChannel fileChannel = inputStream.getChannel();
        return fileChannel.map(FileChannel.MapMode.READ_ONLY, 0, file.length());
    }

    /**
     * Reads label list from Assets.
     */
    private List<String> loadLabelList(Activity activity) throws IOException {
        List<String> labelList = new ArrayList<String>();
        BufferedReader reader =
                new BufferedReader(new InputStreamReader(activity.getAssets().open(getLabelPath())));
        String line;
        while ((line = reader.readLine()) != null) {
            labelList.add(line);
        }
        reader.close();
        return labelList;
    }

    private void convertArrayToByteBuffer(float[] floats) {
        if (Data == null) {
            return;
        }
        Data.rewind();

        int pixel = 0;
        long startTime = SystemClock.uptimeMillis();
        for (int i = 0; i < floats.length; ++i) {
            Data.putFloat(floats[i]);
            //Data.putFloat(mv[i]);
        }
        long endTime = SystemClock.uptimeMillis();
        Log.d(TAG, "Timecost to put values into ByteBuffer: " + Long.toString(endTime - startTime));
    }


    /**
     * only for tensorflow-lite:0.0.0-nightly
     */
//    public void useCPU() {
//        tfliteOptions.setUseNNAPI(false);
//        recreateInterpreter();
//    }
//
//    public void useNNAPI() {
//        tfliteOptions.setUseNNAPI(true);
//        recreateInterpreter();
//    }
//
//    public void setNumThreads(int numThreads) {
//        tfliteOptions.setNumThreads(numThreads);
//        recreateInterpreter();
//    }

//    private void recreateInterpreter() {
//        if (tflite != null) {
//            tflite.close();
//            // TODO(b/120679982)
//            tflite = new Interpreter(tfliteModel, tfliteOptions);
//        }
//    }
    protected abstract String getModelPath();

    protected abstract String getLabelPath();

    protected int getNumLabels() {
        return labelList.size();
    }

    protected abstract float[] runInference();
}
