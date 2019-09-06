package com.example.testloadtflitemodel.classifier;

import android.app.Activity;

import java.io.IOException;

public class FloatMobileNetClassifier extends Classifier {

    /**
     * An array to hold inference results, to be feed into Tensorflow Lite as outputs. This isn't part
     * of the super class, because we need a primitive array here.
     */
    private float[][] labelProbArray = null;

    public FloatMobileNetClassifier(Activity activity, int dim_pixel) throws IOException {
        super(activity, dim_pixel);
        labelProbArray = new float[1][getNumLabels()];
    }

    @Override
    protected String getModelPath() {
        return "mobilenet_v1_1.0_224.tflite";
    }

    @Override
    protected String getLabelPath() {
        return "labels_mobilenet_quant_v1_224.txt";
    }

    @Override
    protected float[] runInference() {
        tflite.run(Data, labelProbArray);
        return  labelProbArray[0];
    }
}
