package artic.matrix;

import java.util.HashMap;
import java.util.Map;

public class HashMatrix {

    protected Map<String,Integer> matrix;


    public HashMatrix(int size) {
        matrix = new HashMap<>(size);
    }

    public void setValue(int[] indices, int[] x, int value) {
        long st = System.nanoTime();
        StringBuilder keyBuilder = new StringBuilder();
        for (int index : indices) {
            keyBuilder.append(index);
        }
        for (int index : x) {
            keyBuilder.append(index);
        }
        matrix.put(keyBuilder.toString(), value);
    }

    public int getValue(int[] indices, int[] x) {
        long st = System.nanoTime();
        StringBuilder keyBuilder = new StringBuilder();
        for (int index : indices) {
            keyBuilder.append(index);
        }
        for (int index : x) {
            keyBuilder.append(index);
        }
        return matrix.getOrDefault(keyBuilder.toString(), 0);
    }

}
