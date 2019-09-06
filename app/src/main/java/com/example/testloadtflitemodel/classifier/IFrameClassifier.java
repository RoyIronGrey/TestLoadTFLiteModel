package com.example.testloadtflitemodel.classifier;

import android.app.Activity;

import java.io.IOException;

public class IFrameClassifier extends Classifier {

    public IFrameClassifier(Activity activity, int dim_pixel) throws IOException {
        super(activity, dim_pixel);
    }

    public IFrameClassifier(String modelFilename, int dimPixel) throws IOException {
        super(modelFilename, dimPixel);
    }

    @Override
    protected String getModelPath() {
        return "resnet152_iframe.tflite";
    }

    @Override
    protected String getLabelPath() {
        return "mytest.txt";
    }

    @Override
    protected float[] runInference() {
        float[][] outArr = new float[1][101];
        tflite.run(Data, outArr);
        return outArr[0];
    }
}
