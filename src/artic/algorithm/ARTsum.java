package artic.algorithm;

import java.util.*;
import artic.combinatorial.*;
import artic.common.Case;
import artic.common.Generation;
import artic.common.RandomSample;

/**
 * @author: Linlin Wen
 *
 * ARTsum algorithm.
 *
 * It favors candidates with less frequent parameter values in the current suite.
 */
public class ARTsum implements Generation {
    private CTModel model;
    private final Random random;
    private int CANDIDATE;
    private int[][] paraVector;

    public ARTsum() {
        this.CANDIDATE = 10;
        this.random = new Random();
    }

    private void initParaVector(CTModel model) {
    	paraVector = new int[model.parameter][];
    	for (int i = 0; i < paraVector.length; i++) {
    		paraVector[i] = new int[model.value[i]];
		}
    }

    private void updateParaVector(int[] x) {
    	for (int i = 0; i < x.length; i++) {
			paraVector[i][x[i]]++;
		}
    }

    /**
     * Records cumulative generation time at fixed target suite sizes.
     */
    @Override
    public Map<Integer, Long> generation(CTModel model, TestSuite ts, int[] sizes) {
    	(this.model = model).initialization();
        final long startTime = System.nanoTime();
        initParaVector(model);
        ts.suite.clear();
        Map<Integer,Long> map = new HashMap<>();
        int cnt = 0;
        int size = sizes[cnt];
        final int[] test = RandomSample.sample(model, this.random);
        ts.suite.add(new TestCase(test));
        updateParaVector(test);
        while (true) {
            TestCase tc = new TestCase(this.nextTestCase(ts, ts.getTestSuiteSize(), this.CANDIDATE));
            ts.suite.add(tc);
            if (ts.getTestSuiteSize() == size) {
                map.put(ts.getTestSuiteSize(), System.nanoTime()-startTime);
                cnt ++;
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

    /**
     * Generates a complete covering array for CAT-measure.
     */
    @Override
    public void generation(CTModel model, TestSuite ts) {
        final long startTime = System.nanoTime();
        this.model = model;
        model.initialization();
        initParaVector(model);
        ts.suite.clear();
        final int[] test = RandomSample.sample(model, this.random);
        ts.suite.add(new TestCase(test));
        updateParaVector(test);
        model.updateCombination(test);
        while (model.getCombUncovered() != 0L) {
            TestCase tc = new TestCase(this.nextTestCase(ts, ts.getTestSuiteSize(), this.CANDIDATE));
            ts.suite.add(tc);
            updateParaVector(tc.test);
            model.updateCombination(tc.test);
        }
        final long endTime = System.nanoTime();
        ts.time = endTime - startTime;
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
        ts.suite.add(new TestCase(test));
        if(scenario.detected(testCase, model)) {
        	return;
        }
        initParaVector(model);
        updateParaVector(test);
        int count = 1;
        while (ts.getTestSuiteSize() < 50/scenario.getRate()) {
        	TestCase tc = new TestCase(this.nextTestCase(ts, count, this.CANDIDATE));
            ts.suite.add(tc);
            if(scenario.detected(tc, model)) {
            	return;
            }
            updateParaVector(tc.test);
            count++;
		}
    }

    private int[] nextTestCase(TestSuite ts,int size, final int k) {
        final int[][] candidate = new int[k][];
        int[] tp;
        for (int pc = 0; pc < k; candidate[pc++] = tp) {
            tp = RandomSample.sample(this.model, this.random);
        }
        int index = 0;
        int maxFit = Integer.MIN_VALUE;
        for (int i = 0; i < candidate.length; i ++) {
        	int distance = distance(candidate[i], size);
        	if(distance > maxFit) {
        		index = i;
        		maxFit = distance;
        	}
        }
        return candidate[index];
    }

    private int distance(final int[] x,int size) {
        int dist = 0;
        for (int i = 0; i < x.length; i++) {
			dist += (size-paraVector[i][x[i]]);
		}
        return dist;
    }
}
