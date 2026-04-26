package artic.algorithm;

import com.carrotsearch.hppc.IntHashSet;
import com.carrotsearch.hppc.IntSet;
import com.carrotsearch.hppc.cursors.IntCursor;
import artic.combinatorial.CTModel;
import artic.combinatorial.TestCase;
import artic.combinatorial.TestSuite;
import artic.common.Case;
import artic.common.Generation;
import artic.handler.MultiDimArray;

import java.util.*;

/**
 * @author: Linlin Wen
 *
 * ARTICGAET algorithm.
 *
 * It generates a complete test suite and then removes redundant test cases.
 */
public class ARTICGAET implements Generation {
    private static final int CANDIDATE = 10;

    private CTModel model;
    private final Random random;
    private ArrayList<int[]> pairs;
    private long unCover = 0;
    private List<MultiDimArray> uncoverArray;
    private Inx[] uncoverIndex;
    private ArrayList<Integer> PCTupleSize;
    private long totalSize;

    public ARTICGAET() {
        this.random = new Random();
    }

    private static class Inx {
        private int index;
        private int val;

        Inx(int index, int val) {
            this.index = index;
            this.val = val;
        }
    }

    private void initParaVector(CTModel model) {
        uncoverArray = new ArrayList<>();
        pairs = new ArrayList<>();
        PCTupleSize = new ArrayList<>();
        getPairs();
        uncoverIndex = new Inx[(int) totalSize];
        for (int i = 0; i < totalSize; i++) {
            uncoverIndex[i] = new Inx(i, i);
        }
    }

    private void getPairs() {
        int[] starts = new int[model.t_way];
        int depth = 0;
        int[] pair = new int[model.t_way];
        int totalPC = 0;
        ArrayList<Integer> pcs = new ArrayList<>();
        while (starts[0] <= model.parameter - model.t_way) {
            if (depth == model.t_way - 1) {
                for (; starts[depth] < model.parameter; starts[depth]++) {
                    pair[depth] = starts[depth];
                    int[] temp = new int[pair.length];
                    int count = 1;
                    int[] lens = new int[model.t_way];
                    for (int i = 0; i < temp.length; i++) {
                        temp[i] = pair[i];
                        lens[i] = model.value[pair[i]];
                        count *= lens[i];
                    }
                    uncoverArray.add(new MultiDimArray(lens));
                    pcs.add(totalPC);
                    totalPC += count;
                    pairs.add(temp);
                }
                starts[depth - 1]++;
                depth--;
                continue;
            }
            if (starts[depth] < model.parameter) {
                pair[depth] = starts[depth];
                depth++;
                starts[depth] = starts[depth - 1] + 1;
                continue;
            } else {
                starts[depth - 1]++;
                depth--;
            }
        }
        unCover = totalPC;
        totalSize = totalPC;
        PCTupleSize = pcs;
    }

    private int getVCIndex(int[] x, int[] pair) {
        int last = pair[pair.length - 1];
        int cnt = x[last];
        int base = model.value[last];
        for (int i = pair.length - 2; i >= 0; i--) {
            cnt += base * x[pair[i]];
            base *= model.value[pair[i]];
        }
        return cnt;
    }

    private void updateParaVector(int[] x) {
        for (int i = 0; i < pairs.size(); i++) {
            int[] pair = pairs.get(i);
            int val = uncoverArray.get(i).getValueIncrease(pair, x);
            if (val == 0) {
                unCover--;
                int vcIndex = getVCIndex(x, pair);
                int k = PCTupleSize.get(i) + vcIndex;
                int realIndex = uncoverIndex[k].index;
                int lastVal = uncoverIndex[(int) unCover].val;
                uncoverIndex[lastVal].index = realIndex;
                uncoverIndex[realIndex].val = lastVal;
            }
        }
    }

    public Map<Integer, Long> generation(final CTModel model, final TestSuite ts, int[] sizes) {
        final long startTime = System.nanoTime();
        this.model = model;
        initParaVector(model);
        ts.suite.clear();
        Map<Integer, Long> map = new HashMap<>();
        int cnt = 0;
        int size = sizes[cnt];
        final int[] test = new int[model.parameter];
        ts.suite.add(new TestCase(test));
        if (ts.getTestSuiteSize() == size) {
            map.put(ts.getTestSuiteSize(), System.nanoTime() - startTime);
            cnt++;
            if (cnt == sizes.length) {
                ts.time = System.nanoTime() - startTime;
                return map;
            }
            size = sizes[cnt];
        }
        updateParaVector(test);
        while (true) {
            final TestCase tc = new TestCase(this.nextTestCase(ts, ts.getTestSuiteSize(), this.CANDIDATE));
            ts.suite.add(tc);
            if (ts.getTestSuiteSize() == size) {
                map.put(ts.getTestSuiteSize(), System.nanoTime() - startTime);
                cnt++;
                if (cnt == sizes.length) {
                    break;
                }
                size = sizes[cnt];
            }
            updateParaVector(tc.test);
        }
        final long endTime = System.nanoTime();
        ts.time = endTime - startTime;
        return map;
    }

    public void generation(CTModel model, TestSuite ts) {
        final long startTime = System.nanoTime();
        this.model = model;
        initParaVector(model);
        ts.suite.clear();
        final int[] test = new int[model.parameter];
        ts.suite.add(new TestCase(test));
        updateParaVector(test);
        while (unCover != 0L) {
            final TestCase tc = new TestCase(this.nextTestCase(ts, ts.getTestSuiteSize(), this.CANDIDATE));
            ts.suite.add(tc);
            updateParaVector(tc.test);
        }
        removeRepeat(ts, uncoverArray, pairs);
        final long endTime = System.nanoTime();
        ts.time = endTime - startTime;
    }

    public void generation(CTModel model, TestSuite ts, Case scenario) {
        this.model = model;
        ts.suite.clear();
        final int[] test = new int[model.parameter];
        TestCase testCase = new TestCase(test);
        ts.suite.add(testCase);
        if (scenario.detected(testCase, model)) {
            return;
        }
        initParaVector(model);
        updateParaVector(test);
        int count = 1;
        while (true) {
            TestCase tc = new TestCase(this.nextTestCase(ts, count, this.CANDIDATE));
            ts.suite.add(tc);
            if (scenario.detected(tc, model)) {
                return;
            }
            updateParaVector(tc.test);
            count++;
        }
    }

    private void reset(int[] data, int pos) {
        for (int i = 0; i < data.length; i++) {
            data[i]--;
            if (data[i] == 0) {
                int index = pos + i;
                int last = (int) (unCover);
                uncoverIndex[last].val = index;
                uncoverIndex[index].index = last;
                unCover++;
            }
        }
    }

    private int[] nextTestCase(TestSuite ts, int size, final int k) {
        if (unCover == 0) {
            for (int i = 0; i < pairs.size(); i++) {
                MultiDimArray multipleArray = uncoverArray.get(i);
                reset(multipleArray.getData(), PCTupleSize.get(i));
            }
        }
        final int[][] candidate = new int[k][];
        long num = (long) Math
                .ceil(0.5 * (1.0 + unCover / (double) totalSize) * (model.parameter / (double) model.t_way));
        num = Math.min(num, unCover);
        double[] dist = new double[k];
        for (int pc = 0; pc < k; pc++) {
            int[] test = new int[model.parameter];
            for (int j = 0; j < model.parameter; ++j) {
                test[j] = -1;
            }
            setUncoveredTuples(test, num);
            sampleByGreedy(ts.getTestSuiteSize(), test);
            candidate[pc] = test;
            dist[pc] = distance(test, size);
        }
        int index = 0;
        double maxFit = -1;
        for (int i = 0; i < candidate.length; i++) {
            double distance = dist[i];
            if (distance > maxFit) {
                index = i;
                maxFit = distance;
            }
        }
        return candidate[index];
    }

    private void sampleByGreedy(int size, int[] test) {
        List<Integer> nonAss = new ArrayList<Integer>();
        List<Integer> ass = new ArrayList<Integer>();
        for (int k = 0; k < this.model.parameter; ++k) {
            if (test[k] == -1) {
                nonAss.add(k);
            } else {
                ass.add(k);
            }
        }
        Collections.shuffle(nonAss);
        for (int pos : nonAss) {
            test[pos] = selectBestValue(test, ass, pos, size);
            ass.add(pos);
        }
    }

    private int selectBestValue(int[] test, List<Integer> ass, int lv, int size) {
        IntSet subTuplesIdx = new IntHashSet();
        int listSize = (int) (model.t_way * ass.size() * 2);
        int max = comb(ass.size(), model.t_way - 1);

        listSize = Math.min(max, listSize);
        getRandomK(listSize, max, subTuplesIdx, ass, lv);
        double MAX = -1;
        int level = -1;
        for (int i = 0; i < model.value[lv]; i++) {
            double val = 0;
            test[lv] = i;
            Iterator<IntCursor> iterator = subTuplesIdx.iterator();
            while (iterator.hasNext()) {
                int key = iterator.next().value;
                int[] pos = pairs.get(key);
                try {
                    int temp = uncoverArray.get(key).getValue(pos, test);
                    val += 1.0 / Math.pow(1 + temp, model.t_way) * (size - temp);
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
            if (val >= MAX) {
                MAX = val;
                level = i;
            }
        }
        return level;
    }

    private void getRandomK(int listSize, int N, IntSet subTuplesIdx, List<Integer> ass, int lv) {
        if (listSize == N) {
            int t = model.t_way - 1;
            int[] pair = new int[model.t_way];
            int[] starts = new int[t];
            int depth = 0;
            while (starts[0] <= ass.size() - t) {
                if (depth == t - 1) {
                    for (; starts[depth] < ass.size(); starts[depth]++) {
                        pair[depth] = ass.get(starts[depth]);
                        int[] temp = pair.clone();
                        temp[pair.length - 1] = lv;
                        Arrays.sort(temp);
                        int pcIndex = calculatePCIndex(temp);
                        subTuplesIdx.add(pcIndex);
                    }
                    if (depth == 0) {
                        break;
                    }
                    starts[depth - 1]++;
                    depth--;
                    continue;
                }
                if (starts[depth] < ass.size()) {
                    pair[depth] = ass.get(starts[depth]);
                    depth++;
                    starts[depth] = starts[depth - 1] + 1;
                    continue;
                } else {
                    starts[depth - 1]++;
                    depth--;
                }
            }
        } else if (listSize >= N / 2) {
            ArrayList<int[]> tempTuples = new ArrayList<>();
            int t = model.t_way - 1;
            int[] pair = new int[model.t_way];
            int[] starts = new int[t];
            int depth = 0;
            while (starts[0] <= ass.size() - t) {
                if (depth == t - 1) {
                    for (; starts[depth] < ass.size(); starts[depth]++) {
                        pair[depth] = ass.get(starts[depth]);
                        int[] temp = pair.clone();
                        temp[pair.length - 1] = lv;
                        tempTuples.add(temp);
                    }
                    if (depth == 0) {
                        break;
                    }
                    starts[depth - 1]++;
                    depth--;
                    continue;
                }
                if (starts[depth] < ass.size()) {
                    pair[depth] = ass.get(starts[depth]);
                    depth++;
                    starts[depth] = starts[depth - 1] + 1;
                    continue;
                } else {
                    starts[depth - 1]++;
                    depth--;
                }
            }
            for (int i = 0; i < listSize; i++) {
                int j = i + random.nextInt(tempTuples.size() - i);
                Collections.swap(tempTuples, i, j);
            }
            for (int i = 0; i < listSize; i++) {
                int[] temp = tempTuples.get(i);
                Arrays.sort(temp);
                int pcIndex = calculatePCIndex(temp);
                subTuplesIdx.add(pcIndex);
            }
        } else {
            while (subTuplesIdx.size() < listSize) {
                int len = ass.size();
                int[] pos = new int[model.t_way];
                for (int i = 0; i < model.t_way; i++) {
                    int ind = random.nextInt(len);
                    len--;
                    pos[i] = ass.get(ind);
                    ass.set(ind, ass.get(len));
                    ass.set(len, pos[i]);
                }
                pos[pos.length - 1] = lv;
                Arrays.sort(pos);
                int index = calculatePCIndex(pos);
                subTuplesIdx.add(index);
            }
        }
    }

    private void setUncoveredTuples(int[] tc, long num) {
        Inx[] intArray = uncoverIndex;
        int size = (int) unCover;
        List<Integer> res = new ArrayList<>();
        for (int i = 0; i < intArray.length; i++) {
            int index = random.nextInt(size);
            int val = intArray[index].val;
            res.add(val);
            size--;

            intArray[index].val = intArray[size].val;
            intArray[intArray[index].val].index = index;
            intArray[size].val = val;
            intArray[val].index = size;

            if (res.size() == num) {
                break;
            }
        }
        for (int key : res) {
            int pos = Collections.binarySearch(PCTupleSize, key);
            pos = pos >= 0 ? pos : -(pos + 2);
            int[] pair = pairs.get(pos);
            int[] vcTuple = getVC(key - PCTupleSize.get(pos), pair);

            boolean imp = true;
            for (int j = 0; j < model.t_way; j++) {
                if (tc[pair[j]] != -1 && tc[pair[j]] != vcTuple[j]) {
                    imp = false;
                    break;
                }
            }
            if (imp) {
                for (int j = 0; j < model.t_way; j++) {
                    tc[pair[j]] = vcTuple[j];
                }
            }
        }

    }

    private int[] getVC(int tupleIndex, int[] pair) {
        int[] vc = new int[pair.length];
        int base = tupleIndex;
        for (int i = pair.length - 1; i >= 0; i--) {
            vc[i] = base % model.value[pair[i]];
            base /= model.value[pair[i]];
        }
        return vc;
    }

    private double distance(final int[] x, int size) {
        double dist = 0;
        int[] pair = null;
        for (int i = 0; i < pairs.size(); i++) {
            pair = pairs.get(i);
            double temp = uncoverArray.get(i).getValue(pair, x);
            dist += 1.0 / Math.pow(1.0 + temp, model.t_way) * (size - temp);
        }
        return dist;
    }

    private int calculatePCIndex(int[] tuple) {
        int index = 0;
        int prev = -1;
        for (int i = 0; i < model.t_way; i++) {
            int current = tuple[i];
            for (int k = prev + 1; k < current; k++) {
                int a = model.parameter - k - 1;
                int remaining = model.t_way - i - 1;
                index += comb(a, remaining);
            }
            prev = current;
        }
        return index;
    }

    private int comb(int a, int b) {
        if (b == 0)
            return 1;
        if (a < b)
            return 0;
        switch (b) {
            case 1:
                return a;
            case 2:
                return a * (a - 1) / 2;
            case 3:
                return a * (a - 1) * (a - 2) / 6;
            default:
                throw new IllegalArgumentException("b must be between 0 and 3");
        }
    }

    private void removeRepeat(TestSuite ts, List<MultiDimArray> uncoverArray, ArrayList<int[]> pairs) {
        ArrayList<Integer> rem = new ArrayList<>();
        for (int i = 0; i < ts.getTestSuiteSize(); i++) {
            boolean flag = false;
            int[] tc = ts.suite.get(i).test;
            int cnt = 0;
            for (int[] pair : pairs) {
                if (uncoverArray.get(cnt).getValue(pair, tc) == 1) {
                    flag = true;
                    break;
                }
                cnt++;
            }
            if (!flag) {
                rem.add(i);
                cnt = 0;
                for (int[] pair : pairs) {
                    try {
                        uncoverArray.get(cnt).getValueDescend(pair, tc);
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                    cnt++;
                }
            }

        }
        for (int i = rem.size() - 1; i >= 0; i--) {
            ts.suite.remove((int) rem.get(i));
        }
    }
}
