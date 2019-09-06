package com.example.testloadtflitemodel.classifier;

import android.app.Activity;

import java.io.IOException;

public class ResidualClassifier extends Classifier {


    public ResidualClassifier(Activity activity, int dim_pixel) throws IOException {
        super(activity, dim_pixel);
    }

    public ResidualClassifier(String modelFilename, int dimPixel) throws IOException {
        super(modelFilename, dimPixel);
    }

    @Override
    protected String getModelPath() {
        return "resnet18_residual.tflite";
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
