package com.example.testloadtflitemodel.classifier;

import android.app.Activity;

import java.io.IOException;

/**
 * only for tensorflow-lite:0.0.0-nightly
 */
//import org.tensorflow.lite.Delegate;

public class MVClassifier extends Classifier{


    public MVClassifier(Activity activity, int dim_pixel) throws IOException {
        super(activity, dim_pixel);
    }

    public MVClassifier(String modelFilename, int dimPixel) throws IOException {
        super(modelFilename, dimPixel);
    }

    protected String getModelPath() {
        return "resnet18_mv.tflite";
    }

    protected String getLabelPath() {
        return "mytest.txt";
    }

    protected float[] runInference() {
        float[][] outArr = new float[1][101];
        tflite.run(Data, outArr);
        return outArr[0];
    }
}
