package artic.matrix;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;

public class ArrayMatrix {

    protected int[][] matrix;

    HashMap<String,Integer> keys;

    int[] values;

    public ArrayMatrix(ArrayList<int[]> pairs,int[] values) {
        matrix = new int[pairs.size()][];
        keys = new HashMap<>();
        for (int i = 0; i < pairs.size(); i++) {
            int[] pair = pairs.get(i);
            String key = Arrays.toString(pair);
            keys.put(key, i);
            int size = 1;
            for (int j = 0; j < pair.length; j++) {
                size *= values[pair[j]];
            }
            matrix[i] = new int[size];
        }
        this.values = values;
    }

    public void init(){
        for (int i = 0; i < matrix.length; i++) {
            for (int j = 0; j < matrix[i].length; j++) {
                matrix[i][j] = 0;
            }
        }
    }



    public int getAndIncrease(int[] indices, int[] x) {
        long st = System.nanoTime();
        int i1 = keys.get(Arrays.toString(indices));
        int i2 = x[indices[indices.length-1]];
        for (int j = 0; j < indices.length-1; j++) {
            int temp = 1;
            for (int k = j+1; k < indices.length; k++) {
                temp *= values[indices[k]];
            }
            i2 += x[indices[j]] * temp;
        }
        matrix[i1][i2] ++;
        return matrix[i1][i2];
    }

    public int getAndIncrease(int i1, int[] indices, int[] x) {
        long st = System.nanoTime();
        int i2 = x[indices[indices.length-1]];
        for (int j = 0; j < indices.length-1; j++) {
            int temp = 1;
            for (int k = j+1; k < indices.length; k++) {
                temp *= values[indices[k]];
            }
            i2 += x[indices[j]] * temp;
        }
        matrix[i1][i2] ++;
        return matrix[i1][i2];
    }

    public int getAndDecrease(int i1, int[] indices, int[] x) {
        long st = System.nanoTime();
        int i2 = x[indices[indices.length-1]];
        for (int j = 0; j < indices.length-1; j++) {
            int temp = 1;
            for (int k = j+1; k < indices.length; k++) {
                temp *= values[indices[k]];
            }
            i2 += x[indices[j]] * temp;
        }
        matrix[i1][i2] --;
        return matrix[i1][i2];
    }

    public int getAndDecrease(int[] indices, int[] x) {
        long st = System.nanoTime();
        int i1 = keys.get(Arrays.toString(indices));
        int i2 = x[indices[indices.length-1]];
        for (int j = 0; j < indices.length-1; j++) {
            int temp = 1;
            for (int k = j+1; k < indices.length; k++) {
                temp *= values[indices[k]];
            }
            i2 += x[indices[j]] * temp;
        }
        matrix[i1][i2] --;
        return matrix[i1][i2];
    }

    public int getWithPC( int[] indices, int[] vc) {
        try {
            long st = System.nanoTime();
            int i1 = keys.get(Arrays.toString(indices));
            int i2 = vc[indices.length-1];
            for (int j = 0; j < indices.length-1; j++) {
                int temp = 1;
                for (int k = j+1; k < indices.length; k++) {
                    temp *= values[indices[k]];
                }
                i2 += vc[j] * temp;
            }
            return matrix[i1][i2];
        }
        catch (Exception e){
            e.printStackTrace();
            return 0;
        }
    }

    public int get( int[] indices, int[] x) {
        try {
            long st = System.nanoTime();
            int i1 = keys.get(Arrays.toString(indices));
            int i2 = x[indices[indices.length-1]];
            for (int j = 0; j < indices.length-1; j++) {
                int temp = 1;
                for (int k = j+1; k < indices.length; k++) {
                    temp *= values[indices[k]];
                }
                i2 += x[indices[j]] * temp;
            }
            return matrix[i1][i2];
        }
        catch (Exception e){
            e.printStackTrace();
            return 0;
        }
    }

    public static void main(String[] args) {
        int[] values = {2,3,2,2};
        ArrayList<int[]> pairs = new ArrayList<>();
        pairs.add(new int[]{0,1,2,3});
        ArrayMatrix arrayMatrix = new ArrayMatrix(pairs,values);
        System.out.println(Arrays.deepToString(arrayMatrix.matrix));
    }

}
