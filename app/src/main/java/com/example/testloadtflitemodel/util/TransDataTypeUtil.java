package com.example.testloadtflitemodel.util;

public class TransDataTypeUtil {

    //Low Endian
    public static byte[] int2Bytes(int i) {
        byte[] bytes = new byte[4];
        bytes[0] = (byte) (i & 0xFF);
        bytes[1] = (byte) ((i >> 8) & 0xFF);
        bytes[2] = (byte) ((i >> 16) & 0xFF);
        bytes[3] = (byte) ((i >> 24) & 0xFF);

        return bytes;
    }

    //Low Endian
    public static int bytes2Int(byte[] byteArray) {
        int res = 0;
        for (int i = 0; i < byteArray.length; i++) {
            res += (byteArray[i] & 0xff) << (i * 8);
        }
        return res;
    }

    public static float[] getScoreFromBytes(byte[] bytes) {
        int len = bytes.length - 16;
        float[] result = new float[len / 4];
        for (int i = 0; i < len / 4; i++) {
            int j = i * 4 + 16;
            int intermediary;
            intermediary = bytes[j];
            intermediary &= 0xff;
            intermediary |= ((int) bytes[j + 1] << 8);
            intermediary &= 0xffff;
            intermediary |= ((int) bytes[j + 2] << 16);
            intermediary &= 0xffffff;
            intermediary |= ((int) bytes[j + 3] << 24);
            result[i] = Float.intBitsToFloat(intermediary);
        }

        return result;
    }


}
