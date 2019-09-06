package com.example.testloadtflitemodel.util;

import android.os.Environment;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.text.DecimalFormat;

public class FileUtil {

    private static final String UCF101 = "UCF101-mobile";
    private static final String UCF101_MINLIST_TEST = "ucf101_minlist_test.txt";
    public static final String UCF101_PIC = "UCF101_PIC";
    public static final String UCF101_YUV_FOLDER = "v_YoYo_g01_c01_yuv";
    public static final String UCF101_YUV_Y_FOLDER = "v_YoYo_g01_c01_yuv_y";
    public static final String UCF101_YUV_U_FOLDER = "v_YoYo_g01_c01_yuv_u";
    public static final String UCF101_YUV_V_FOLDER = "v_YoYo_g01_c01_yuv_v";

    private static final String MODEL_DIR = "coviar_opencl/tflite_model/";
    private static final String RESNET_152_I = "resnet152_iframe.tflite";
    private static final String RESNET_18_MV = "resnet18_mv.tflite";
    private static final String RESNET_18_RESIDUAL = "resnet18_residual.tflite";

    public static String getUCF101Folder() {
        return Environment.getExternalStorageDirectory().toString() + File.separator + UCF101;
    }


    public static String getUCF101MinListTest() {
        return getUCF101Folder() + File.separator + UCF101_MINLIST_TEST;
    }

    public static String getTestVideo(){
        return Environment.getExternalStorageDirectory().toString() + File.separator + "coviar_opencl/UCF-101/YoYo/v_YoYo_g01_c01.yuv";
    }

    public static String getResnet152I() {
        return Environment.getExternalStorageDirectory().toString() + File.separator + MODEL_DIR + RESNET_152_I;
    }

    public static String getResnet18MV() {
        return Environment.getExternalStorageDirectory().toString() + File.separator + MODEL_DIR + RESNET_18_MV;
    }

    public static String getResnet18Residual() {
        return Environment.getExternalStorageDirectory().toString() + File.separator + MODEL_DIR + RESNET_18_RESIDUAL;
    }

//    public static String DATA_LIST =
//            Environment.getExternalStorageDirectory().toString()+ File.separator+
//                    "Coviar/datalist";
//
//    public static String DATA_PATH =
//            Environment.getExternalStorageDirectory().toString()+ File.separator+
//                    "Coviar/datapath";


    public static boolean fileExists(String filename) {
        return new File(filename).exists();
    }

    public static void deleteFile(String filename) {
        File file = new File(filename);
        if (file.exists())
            file.delete();
    }

    public static String pathCombine(String path1, String path2) {
        StringBuilder sb = new StringBuilder(path1);
        if (!path1.endsWith(File.separator))
            sb.append(File.separator);
        sb.append(path2);

        return sb.toString();
    }

    private static String modifyVideoPath(String videoPath, String ext) {
        int ei = videoPath.lastIndexOf(".");
        return videoPath.substring(0, ei) + ext;
    }

    public static void saveIFrame(byte[] bytes) {
        try {
            FileWriter fw = new FileWriter(getUCF101Folder() + File.separator + "IFrame.txt", true);
            for (int i = 0; i < bytes.length; i++) {
                fw.write((bytes[i] & 0xff) + "\t");
                if ((i + 1) % 340 == 0) {
                    fw.write("\n");
                }
            }
            fw.flush();
            fw.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static void saveTransformedI(float[] floats) {
        try {
            FileWriter fw = new FileWriter(getUCF101Folder() + File.separator + "transformedI.txt", true);
            DecimalFormat decimalFormat = new DecimalFormat(".00");
            for (int i = 0; i < 224 * 224; i++) {
                fw.write("(" + decimalFormat.format(floats[i * 3]) + "," + decimalFormat.format(floats[i * 3 + 1]) + "," + decimalFormat.format(floats[i * 3 + 2]) + ")" + '\t');
                if ((i + 1) % 224 == 0) {
                    fw.write("\n");
                }
            }
            fw.flush();
            fw.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static void saveScore(float[] score, String fileName) {
        try {
            FileWriter fw = new FileWriter(getUCF101Folder() + File.separator + fileName, true);
            for (int i = 0; i < score.length; i++) {
                fw.write(score[i] + "\n");
            }
            fw.write("=====================================================\n");
            fw.flush();
            fw.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static void saveMVorResidual(int[] mv, String fileName) {
        try {
            FileWriter fw = new FileWriter(getUCF101Folder() + File.separator + fileName, true);
            for (int i = 0; i < mv.length; i++) {
                fw.write(mv[i] + "\t");
                if ((i + 1) % 340 == 0) {
                    fw.write("\n");
                }
            }
            fw.flush();
            fw.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static void saveTransformedMV(float[] floats) {
        try {
            FileWriter fw = new FileWriter(getUCF101Folder() + File.separator + "transformedMV.txt", true);
            DecimalFormat decimalFormat = new DecimalFormat("0.00");
            for (int i = 0; i < 224 * 224; i++) {
                fw.write("(" + decimalFormat.format(floats[i * 2]) + "," + decimalFormat.format(floats[i * 2 + 1]) + ")" + '\t');
                if ((i + 1) % 224 == 0) {
                    fw.write("\n");
                }
            }
            fw.flush();
            fw.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static void saveTransformedRes(float[] floats) {
        try {
            FileWriter fw = new FileWriter(getUCF101Folder() + File.separator + "transformedRes.txt", true);
            DecimalFormat decimalFormat = new DecimalFormat("0.00");
            for (int i = 0; i < 224 * 224; i++) {
                fw.write("(" + decimalFormat.format(floats[i * 3]) + "," + decimalFormat.format(floats[i * 3 + 1]) + "," + decimalFormat.format(floats[i * 3 + 2]) + ")" + '\t');
                if ((i + 1) % 224 == 0) {
                    fw.write("\n");
                }
            }
            fw.flush();
            fw.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static void saveEncodeTime(long[] EncodeTime, int np) {
        try {
            FileWriter fw = new FileWriter(getUCF101Folder() + File.separator + "EncodeTimeWithRS(np=" + np + ").csv", true);
//            FileWriter fw = new FileWriter(getUCF101Folder() + File.separator + "EncodeTime(np=" + np + ").csv", true);

//            fw.write("This is EncodeTime: \n");

//            fw.write("Search and Accumulate,EC(MV),Get Residual,DCT(Residual),EC(Residual)");

            for (int i = 0; i < EncodeTime.length; i++) {
                fw.write(EncodeTime[i] + ",");
            }
            fw.write("\n");

            fw.flush();
            fw.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static void saveParallelPFrameAndInferITime(long Time, int np){
        try {
            FileWriter fw = new FileWriter(getUCF101Folder() + File.separator + "InferIFrameTimeWithHighCPUPreemption(np=" + np + ").csv", true);
//            FileWriter fw = new FileWriter(getUCF101Folder() + File.separator + "InferIFrameTimeWithCPUPreemption(np=" + np + ").csv", true);
//            FileWriter fw = new FileWriter(getUCF101Folder() + File.separator + "InferIFrameTime(np=" + np + ").csv", true);
            fw.write(Time+"\n");
            fw.flush();
            fw.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static void saveLocalProcessDelay(int delay){
        try {
            FileWriter fw = new FileWriter(getUCF101Folder() + File.separator + "localProcessDelay.csv", true);
            fw.write(delay + "\n");
            fw.flush();
            fw.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static void saveRemoteProcessDelay(int delay, int bandwidth, double acc){
            //AAAAAAARemote
        try {
            FileWriter fw = new FileWriter(getUCF101Folder() + File.separator + "AAAAAAARemote"+File.separator+"remoteProcessDelay_"+bandwidth+"_"+acc+".csv", true);
            fw.write(delay+"\n");
            fw.flush();
            fw.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static void saveScheduleProcessDelay(int delay, int bandwidth, double acc){
        //AAAAAAASchedule
        try {
            FileWriter fw = new FileWriter(getUCF101Folder() + File.separator + "AAAAAAASchedule"+File.separator+"scheduleProcessDelay_"+bandwidth+"_"+acc+".csv", true);
            fw.write(delay+"\n");
            fw.flush();
            fw.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static void saveGoFCount(int GoFCount){
        try {
            FileWriter fw = new FileWriter(getUCF101Folder() + File.separator + "GoFCount.csv", true);
            fw.write(GoFCount+"\n");
            fw.flush();
            fw.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

}
