package artic.algorithm;

import java.util.*;

import artic.combinatorial.*;
import artic.common.Case;
import artic.common.Generation;
import artic.common.RandomSample;

/**
 * @author: Linlin Wen
 *
 * FSCS-SD algorithm.
 *
 * It selects the candidate that covers more uncovered t-way tuples.
 */
public class FSCSSD implements Generation {
    private CTModel model;
    private final Random random;
    private int CANDIDATE;
    private Set<Integer> uncoverTuple;
    private ArrayList<Integer> PCTupleSize;
    private ArrayList<int[]> pairs;
    private int unCover;
    
    public FSCSSD() {
        this.CANDIDATE = 10;
        this.random = new Random();
    }
    
    private void initUncover() {
        pairs = new ArrayList<>();
        uncoverTuple = new HashSet<>();
        getPairs();
        for (int i = 0; i < unCover; i++) {
            uncoverTuple.add(i);
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
                    for (int i = 0; i < temp.length; i++) {
                        temp[i] = pair[i];
                        count*= model.value[pair[i]];
                    }
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
        PCTupleSize = pcs;
        unCover = totalPC;
    }

    private void updateCombination(int[] x) {
        for (int i = 0; i < pairs.size(); i++) {
            int[] pair = pairs.get(i);
            int vcIndex = getVCIndex(x, pair);
            int k = PCTupleSize.get(i)+vcIndex;
            if(uncoverTuple.contains(k)){
                unCover --;
                uncoverTuple.remove(k);
            }
        }
    }

    private int getVCIndex(int[]x, int[] pair){
        int last = pair[pair.length - 1];
        int cnt = x[last];
        int base = model.value[last];
        for (int i = pair.length-2; i >= 0; i--) {
            cnt += base*x[pair[i]];
            base *= model.value[pair[i]];
        }
        return cnt;
    }

    /**
     * Generates a complete covering array for CAT-measure.
     */
    @Override
    public void generation(final CTModel model, final TestSuite ts) {
        this.model = model;
        final long startTime = System.nanoTime();
        initUncover();
		ts.suite.clear();
        final int[] test = RandomSample.sample(model, this.random);
        ts.suite.add(new TestCase(test));
        updateCombination(test);
        while (!uncoverTuple.isEmpty()) {
            final TestCase tc = new TestCase(this.nextTestCase(CANDIDATE));
            ts.suite.add(tc);
            updateCombination(tc.test);
        }
        final long endTime = System.nanoTime();
        ts.time = endTime - startTime;
    }

    /**
     * Records cumulative generation time at fixed target suite sizes.
     */
    @Override
    public Map<Integer, Long> generation(final CTModel model, final TestSuite ts, int[] sizes) {
    	this.model = model;
        final long startTime = System.nanoTime();
        initUncover();
        ts.suite.clear();
        Map<Integer,Long> map = new HashMap<>();
        int cnt = 0;
        int size = sizes[cnt];
        final int[] test = RandomSample.sample(model, this.random);
        ts.suite.add(new TestCase(test));
        updateCombination(test);
        while (true) {
            final TestCase tc = new TestCase(this.nextTestCase(CANDIDATE));
            ts.suite.add(tc);
            if (ts.getTestSuiteSize() == size) {
                map.put(ts.getTestSuiteSize(), System.nanoTime()-startTime);
                cnt ++;
                if (cnt == sizes.length) {
                    break;
                }
                size = sizes[cnt];
            }
            updateCombination(tc.test);
        }
        final long endTime = System.nanoTime();
        ts.time = endTime - startTime;
        return map;
    }

    /**
     * Stops when the current scenario detects a failure for F-measure.
     */
    @Override
    public void generation(CTModel model, TestSuite ts,Case scenario) {
        this.model = model;
        ts.suite.clear();
        final int[] test = RandomSample.sample(model, this.random);
        TestCase testCase = new TestCase(test); 
        ts.suite.add(testCase);
        updateCombination(testCase.test);
        if (scenario.detected(testCase, model)) {
			return;
		}
        int count = 1;
        while (true) {
        	 final TestCase tc = new TestCase(this.nextTestCase(CANDIDATE));
             ts.suite.add(tc);
             if(scenario.detected(tc, model)) {
             	return;
             }
             updateCombination(tc.test);
		}
    }
    
    private int[] nextTestCase(final int k) {
        final int[][] candidate = new int[k][];
        int[] tp;
        long[] fitness = new long[candidate.length];
        for (int pc = 0; pc < k; candidate[pc++] = tp) {
            tp = RandomSample.sample(this.model, this.random);
            fitness[pc] = fitnessValue(tp);
        }
        int maxIndex = 0;
        double maxFit = fitness[0];
        for (int m = 1; m < fitness.length; ++m) {
            if (fitness[m] > maxFit) {
                maxIndex = m;
                maxFit = fitness[m];
            }
        }
        return candidate[maxIndex];
    }

    private int fitnessValue(int[] x){
        int sum = 0;
        for (int i = 0; i < pairs.size(); i++) {
            int[] pair = pairs.get(i);
            int vcIndex = getVCIndex(x, pair);
            int k = PCTupleSize.get(i)+vcIndex;
            if(uncoverTuple.contains(k)){
                sum ++;
            }
        }
        return sum;
    }
    
}
