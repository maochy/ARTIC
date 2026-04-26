package artic.handler;

/**
 * Compact 2D-4D integer array backed by one linear array.
 * Precomputed strides provide fast access, and callers must pass valid indexes.
 */
public class MultiDimArray {
    private final int[] data;
    private final int[] dimensions;
    private final int[] strides;
    private final int dimCount;

    public MultiDimArray(int... dimSizes) {
        if (dimSizes == null || dimSizes.length < 2 || dimSizes.length > 4) {
            throw new IllegalArgumentException("Dimension sizes must be between 2 and 4");
        }

        this.dimCount = dimSizes.length;
        this.dimensions = dimSizes.clone();
        this.strides = new int[dimCount];

        int totalSize = 1;
        for (int i = dimCount - 1; i >= 0; i--) {
            strides[i] = (i == dimCount - 1) ? 1 : strides[i + 1] * dimensions[i + 1];
            totalSize *= dimensions[i];
        }

        this.data = new int[totalSize];
    }

    public int get(int... indices) {
        return data[calculateIndex(indices)];
    }

    public int getValueIncrease(int[] pair,int[] x) {
        return data[calculateIndex(x,pair)]++;
    }
    public int getValueDescend(int[] pair, int[] x) {
        return data[calculateIndex(x,pair)]--;
    }

    public int getValue(int[] pair,int[] x) {
        return data[calculateIndex(x,pair)];
    }

    public void set(int value, int... indices) {
        data[calculateIndex(indices)] = value;
    }

    private int calculateIndex(int[] indices) {
        int index = 0;
        for (int i = 0; i < dimCount; i++) {
            index += indices[i] * strides[i];
        }
        return index;
    }

    private int calculateIndex(int[] x,int[] indices) {
        switch (indices.length) {
            case 2:
                return x[indices[0]]*strides[0] + x[indices[1]]*strides[1];
            case 3:
                return x[indices[0]]*strides[0]+x[indices[1]]*strides[1]+x[indices[2]]*strides[2];
            case 4:
                return x[indices[0]]*strides[0]+x[indices[1]]*strides[1]+x[indices[2]]*strides[2]+x[indices[3]]*strides[3];
            default:
                throw new IllegalArgumentException("Dimension sizes must be between 2 and 4");
        }
//        int index = 0;
//        for (int i = 0; i < dimCount; i++) {
//            index += x[indices[i]] * strides[i];
//        }
//        return index;
    }

    public int size() {
        return data.length;
    }

    public int[] getData() {
        return data;
    }

}
