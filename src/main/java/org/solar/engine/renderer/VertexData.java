package org.solar.engine.renderer;

import org.solar.engine.Utils;

public class VertexData {
    public FloatArray[] arrays;
    public float[] rawData;

    public VertexData(FloatArray ...data) {
        arrays = data;
        rawData = new float[getSumSize()];
        //
//        int j = 0;
//        for(FloatArray array: arrays){
//            System.out.println(j++ + ": " + array.name);
//            int k = 0;
//            while(k < array.data.length) {
//                for (int i = 0; i < array.step; i++)
//                    System.out.print(array.data[i + k] + ";");
//                System.out.print("\n");
//                k += array.step;
//            }
//            System.out.println("=======");
//        }
        //
        int index = 0;
        while(getSumVarStep() < getSumSize()) {
            for(FloatArray arr: arrays) {
                float[] line = arr.getNextLine();
                for (float v : line) {
                    rawData[index++] = v;
                    System.out.printf("%f,",v);
                    //index++;
                }
            }
            System.out.printf("\n");
        }
        System.out.println("==========");
    }

    private int getSumVarStep() {
        int total = 0;
        for(FloatArray arr: arrays) {
            total += arr.varstep;
        }
        return total;
    }

    public int getSumStep() {
        int total = 0;
        for(FloatArray arr: arrays) {
            total += arr.step;
        }
        return total;
    }

    private int getSumSize() {
        int total = 0;
        for(FloatArray arr: arrays) {
            total += arr.data.length;
        }
        return total;
    }
}
