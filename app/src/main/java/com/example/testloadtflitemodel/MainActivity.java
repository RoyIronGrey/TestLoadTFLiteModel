package com.example.testloadtflitemodel;

import android.Manifest;
import android.content.pm.PackageManager;
import android.os.SystemClock;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;

import com.example.testloadtflitemodel.classifier.Classifier;
import com.example.testloadtflitemodel.classifier.FloatMobileNetClassifier;
import com.example.testloadtflitemodel.classifier.IFrameClassifier;
import com.example.testloadtflitemodel.classifier.MVClassifier;
import com.example.testloadtflitemodel.util.FileUtil;

import java.io.IOException;

public class MainActivity extends AppCompatActivity {

    Button buttonTestRes152;
    Button buttonTestRes18;
    Button buttonTestMobNet;
    Classifier mobileNetClassifier;
    Classifier iFrameClassifier;
    Classifier mvClassifier;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        checkPermission();
        setContentView(R.layout.activity_main);

        buttonTestRes152 = findViewById(R.id.testRes152);
        buttonTestRes18 = findViewById(R.id.testRes18);
        buttonTestMobNet = findViewById(R.id.testMobNet);

        try {
            long start = SystemClock.uptimeMillis();
//            iFrameClassifier = new IFrameClassifier(FileUtil.getResnet152I(),Classifier.I_DIM_PIXEL);
            Log.d("TimeCost", (SystemClock.uptimeMillis() - start) + " ms");
            mobileNetClassifier = new FloatMobileNetClassifier(MainActivity.this,3);
//            mvClassifier = new MVClassifier(FileUtil.getResnet18MV(), Classifier.MV_DIM_PIXEL);
        } catch (IOException e) {
            e.printStackTrace();
        }

        buttonTestRes152.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                new Thread(new Runnable() {
                    @Override
                    public void run() {
//                        iFrameClassifier.classifyFrame(Classifier.I_RES_FRAME_SAMPLE);
//                        try {
//                            Thread.sleep(300);
//                        } catch (InterruptedException e) {
//                            e.printStackTrace();
//                        }
//                        iFrameClassifier.classifyFrame(Classifier.I_RES_FRAME_SAMPLE);
//                        try {
//                            Thread.sleep(300);
//                        } catch (InterruptedException e) {
//                            e.printStackTrace();
//                        }
//                        iFrameClassifier.classifyFrame(Classifier.I_RES_FRAME_SAMPLE);
                    }
                }).start();
            }
        });

        buttonTestRes18.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                new Thread(new Runnable() {
                    @Override
                    public void run() {
//                        mvClassifier.classifyFrame(Classifier.MV_FRAME_SAMPLE);
//                        mvClassifier.classifyFrame(Classifier.MV_FRAME_SAMPLE);
//                        mvClassifier.classifyFrame(Classifier.MV_FRAME_SAMPLE);
                    }
                }).start();
            }
        });

        buttonTestMobNet.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                new Thread(new Runnable() {
                    @Override
                    public void run() {
                        mobileNetClassifier.classifyFrame(Classifier.I_RES_FRAME_SAMPLE);
                        mobileNetClassifier.classifyFrame(Classifier.I_RES_FRAME_SAMPLE);
                        mobileNetClassifier.classifyFrame(Classifier.I_RES_FRAME_SAMPLE);
                    }
                }).start();
            }
        });
    }

    //Runtime check permissions
    public void checkPermission() {
        boolean is_granted = true;
        if (android.os.Build.VERSION.SDK_INT >= 23) {
            if (this.checkSelfPermission(Manifest.permission.WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
                //If do not write sd card permissions
                is_granted = false;
            }
            if (this.checkSelfPermission(Manifest.permission.READ_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
                is_granted = false;
            }
            Log.i("cbs", "is_granted == " + is_granted);
            if (!is_granted) {
                this.requestPermissions(
                        new String[]{
                                Manifest.permission.READ_EXTERNAL_STORAGE,
                                Manifest.permission.WRITE_EXTERNAL_STORAGE},
                        102);
            }
        }
    }


}
