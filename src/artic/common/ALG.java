package artic.common;

import java.util.*;

public class ALG
{
    public static int combine(final int n, final int m) {
        int ret = 1;
        for (int p = n, x = 1; x <= m; ++x, --p) {
            ret *= p;
            ret /= x;
        }
        return ret;
    }
    
    /**
     * @param position t-way parameter indexes
     * @param value number of values for each parameter
     * @return number of value combinations
     */
    public static int combineValue(final int[] position, final int[] value) {
        int comb = 1;
        for (int k = 0; k < position.length; ++k) {
            comb *= value[position[k]];
        }
        return comb;
    }
    
    /**
     * @param c t-way combination indexes
     * @param n number of parameters
     * @param m interaction strength
     * @return row index in the coverage matrix
     */
    public static int combine2num(final int[] c, final int n, final int m) {
        int ret = combine(n, m);
        for (int i = 0; i < m; ++i) {
            ret -= combine(n - c[i] - 1, m - i);
        }
        return ret - 1;
    }
    
    public static int[] num2combine(int t, final int n, final int m) {
        final int[] ret = new int[m];
        ++t;
        for (int j = 1, i = 0; i < m; ret[i++] = j++) {
            int k;
            while (t > (k = combine(n - j, m - i - 1))) {
                t -= k;
                ++j;
            }
        }
        for (int p = 0; p < m; ++p) {
            --ret[p];
        }
        return ret;
    }
    
    /**
     * 
     * @param n number of parameters
     * @param m interaction strength
     * @return
     */
    public static List<int[]> allCombination(final int n, final int m) {
        final List<int[]> data = new ArrayList<int[]>();
        dfs(data, new int[m], m, 1, n - m + 1, 0);
        return data;
    }
    
    private static void dfs(final List<int[]> data, final int[] list, final int k_left, final int from, final int to, int index) {
        if (k_left == 0) {
            data.add(list.clone());
            return;
        }
        for (int i = from; i <= to; ++i) {
            list[index++] = i - 1;
            dfs(data, list, k_left - 1, i + 1, to + 1, index);
            --index;
        }
    }
    
    /**
     * @param pos parameter combination
     * @param sch value combination
     * @param t interaction strength
     * @param value number of values for each parameter
     * @return column index in the coverage matrix
     */
    public static int val2num(final int[] pos, final int[] sch, final int t, final int[] value) {
        int com = 1;
        int ret = 0;
        for (int k = t - 1; k >= 0; --k) {
            ret += com * sch[k];
            com *= value[pos[k]];
        }
        return ret;
    }
    
    /**
     * 
     * Converts a coverage-matrix column index to a value combination.
     * @param i column index of the value combination
     * @param pos parameter combination
     * @param t interaction strength
     * @param value number of values for each parameter
     * @return decoded value combination
     */
    public static int[] num2val(int i, final int[] pos, final int t, final int[] value) {
        final int[] ret = new int[t]; // Decoded value combination.
        int div = 1; // Value-combination divisor.
        for (int k = t - 1; k > 0; --k) {
            div *= value[pos[k]];
        }
        
        for (int k = 0; k < t - 1; ++k) {
            ret[k] =  i / div;
            i -= ret[k] * div;
            div /= value[pos[k + 1]];
        }
        ret[t - 1] = i / div;
        return ret;
    }
    
    public static int[][] allV(final int[] pos, final int t, final int[] value) {
        if (t == 0) {
            return new int[1][0];
        }
        final int[] counter = new int[t];
        final int[] counter_max = new int[t];
        int comb = 1;
        for (int k = 0; k < t; ++k) {
            counter[k] = 0;
            counter_max[k] = value[pos[k]] - 1;
            comb *= value[pos[k]];
        }
        final int end = t - 1;
        final int[][] data = new int[comb][t];
        for (int i = 0; i < comb; ++i) {
            data[i] = counter.clone();
            ++counter[end];
            int[] array;
            int n;
            for (int ptr = end; ptr > 0 && counter[ptr] > counter_max[ptr]; n = --ptr, ++array[n]) {
                counter[ptr] = 0;
                array = counter;
            }
        }
        return data;
    }
    
    public static int cal_factorial(final int t) {
        int n = 1;
        for (int i = 2; i <= t; ++i) {
            n *= i;
        }
        return n;
    }
    
    public static HashMap<ArrayList<Integer>, Integer> cal_permutation(final int t) {
        final HashMap<ArrayList<Integer>, Integer> permutation = new HashMap<ArrayList<Integer>, Integer>();
        int count = 0;
        final Integer[] v = new Integer[t];
        final int[] p = new int[t + 1];
        for (int i = 1; i < t + 1; ++i) {
            v[i - 1] = i - 1;
            p[i] = i;
        }
        permutation.put(new ArrayList<Integer>(Arrays.asList(v)), count);
        ++count;
        int i = 1;
        while (i < t) {
            --p[i];
            final int j = i % 2 * p[i];
            final Integer temp = v[i];
            v[i] = v[j];
            v[j] = temp;
            permutation.put(new ArrayList<Integer>(Arrays.asList(v)), count);
            ++count;
            for (i = 1; p[i] == 0; ++i) {
                p[i] = i;
            }
        }
        return permutation;
    }
    
    public static void sortArray(final int[] a) {
        sortArray(a, 0, a.length - 1);
    }
    
    public static void sortArray(final int[] a, final int left, final int right) {
        if (left < right) {
            int i = left;
            int j = right;
            final int temp = a[i];
            while (i != j) {
                while (a[j] >= temp && i < j) {
                    --j;
                }
                if (i < j) {
                    a[i] = a[j];
                    ++i;
                }
                while (a[i] <= temp && i < j) {
                    ++i;
                }
                if (i < j) {
                    a[j] = a[i];
                    --j;
                }
            }
            a[i] = temp;
            sortArray(a, left, i - 1);
            sortArray(a, i + 1, right);
        }
    }
    
    public static void sortArray(final int[] a, final int[] b, final int version) {
        sortArray(a, b, 0, a.length - 1, version);
    }
    
    public static void sortArray(final int[] a, final int[] b, final int left, final int right, final int version) {
        if (version == 0 && left < right) {
            int i = left;
            int j = right;
            final int temp = a[i];
            final int temp_1 = b[i];
            while (i != j) {
                while (a[j] >= temp && i < j) {
                    --j;
                }
                if (i < j) {
                    a[i] = a[j];
                    b[i] = b[j];
                    ++i;
                }
                while (a[i] <= temp && i < j) {
                    ++i;
                }
                if (i < j) {
                    a[j] = a[i];
                    b[j] = b[i];
                    --j;
                }
            }
            a[i] = temp;
            b[i] = temp_1;
            sortArray(a, b, left, i - 1, version);
            sortArray(a, b, i + 1, right, version);
        }
        if (version == 1 && left < right) {
            int i = left;
            int j = right;
            final int temp = a[i];
            final int temp_1 = b[i];
            while (i != j) {
                while (a[j] <= temp && i < j) {
                    --j;
                }
                if (i < j) {
                    a[i] = a[j];
                    b[i] = b[j];
                    ++i;
                }
                while (a[i] >= temp && i < j) {
                    ++i;
                }
                if (i < j) {
                    a[j] = a[i];
                    b[j] = b[i];
                    --j;
                }
            }
            a[i] = temp;
            b[i] = temp_1;
            sortArray(a, b, left, i - 1, version);
            sortArray(a, b, i + 1, right, version);
        }
    }
    
    public static void sortArray(final int[] a, final int[][] b) {
        sortArray(a, b, 0, a.length - 1);
    }
    
    public static void sortArray(final int[] a, final int[][] b, final int left, final int right) {
        final int len = b[0].length;
        if (left < right) {
            int i = left;
            int j = right;
            final int temp = a[i];
            final int[] tpc = new int[len];
            for (int k = 0; k < len; ++k) {
                tpc[k] = b[i][k];
            }
            while (i != j) {
                while (a[j] > temp && i < j) {
                    --j;
                }
                if (i < j) {
                    a[i] = a[j];
                    for (int k = 0; k < len; ++k) {
                        b[i][k] = b[j][k];
                    }
                    ++i;
                }
                while (a[i] < temp && i < j) {
                    ++i;
                }
                if (i < j) {
                    a[j] = a[i];
                    for (int k = 0; k < len; ++k) {
                        b[j][k] = b[i][k];
                    }
                    --j;
                }
            }
            a[i] = temp;
            for (int k = 0; k < len; ++k) {
                b[i][k] = tpc[k];
            }
            sortArray(a, b, left, i - 1);
            sortArray(a, b, i + 1, right);
        }
    }
    
    public static boolean inList(final List<int[]> list, final int[] t) {
        for (final int[] item : list) {
            if (Arrays.equals(item, t)) {
                return true;
            }
        }
        return false;
    }
    
    public static boolean inArray(final int[][] array, final int bound, final int[] t) {
        for (int i = 0; i < bound; ++i) {
            if (Arrays.equals(array[i], t)) {
                return true;
            }
        }
        return false;
    }
}
