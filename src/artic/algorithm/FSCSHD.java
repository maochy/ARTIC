package artic.algorithm;

import java.util.*;

import artic.combinatorial.*;
import artic.common.Case;
import artic.common.Generation;
import artic.common.RandomSample;

/**
 * @author: Linlin Wen
 *
 * FSCS-HD algorithm.
 *
 * It selects the candidate farthest from the current test suite by Hamming distance.
 */
public class FSCSHD implements Generation {
    private CTModel model;
    private final Random random;
    private int CANDIDATE;
    
    public FSCSHD() {
        this.CANDIDATE = 10;
        this.random = new Random();
    }
    
    /**
     * Generates a complete covering array for CAT-measure.
     */
    @Override
    public void generation(final CTModel model, final TestSuite ts) {
        final long startTime = System.nanoTime();
        (this.model = model).initialization();
		ts.suite.clear();
        final int[] test = RandomSample.sample(model, this.random);
        ts.suite.add(new TestCase(test));
        model.updateCombination(test);
        while (model.getCombUncovered() != 0L) {
            final TestCase tc = new TestCase(this.nextTestCase(ts, ts.getTestSuiteSize(), this.CANDIDATE));
            ts.suite.add(tc);
            model.updateCombination(tc.test);
        }
        final long endTime = System.nanoTime();
        ts.time = endTime - startTime;
    }

    /**
     * Records cumulative generation time at fixed target suite sizes.
     */
    @Override
    public Map<Integer, Long> generation(final CTModel model, final TestSuite ts, int[] sizes) {
    	(this.model = model).initialization();
        final long startTime = System.nanoTime();
        ts.suite.clear();
        final int[] test = RandomSample.sample(model, this.random);
        ts.suite.add(new TestCase(test));
        Map<Integer,Long> map = new HashMap<>();
        int cnt = 0;
        int size = sizes[cnt];
        while (true) {
            final TestCase tc = new TestCase(this.nextTestCase(ts, ts.getTestSuiteSize(), this.CANDIDATE));
            ts.suite.add(tc);
            if (ts.getTestSuiteSize() == size) {
                map.put(ts.getTestSuiteSize(), System.nanoTime()-startTime);
                cnt ++;
                if (cnt == sizes.length) {
                    break;
                }
                size = sizes[cnt];
            }
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
        if (scenario.detected(testCase, model)) {
			return;
		}
        int count = 1;
        while (true) {
        	 final TestCase tc = new TestCase(this.nextTestCase(ts, count++, this.CANDIDATE));
             ts.suite.add(tc);
             if(scenario.detected(tc, model)) {
             	return;
             }
		}
    }
    
    private int[] nextTestCase(final TestSuite ts, final int index, final int k) {
        final int[][] candidate = new int[k][];
        int[] tp;
        for (int pc = 0; pc < k; candidate[pc++] = tp) {
            tp = RandomSample.sample(this.model, this.random);
        }
        final int[] fitness = new int[candidate.length];
        for (int i = 0; i < candidate.length; ++i) {
            fitness[i] = Integer.MAX_VALUE;
        }
        for (int j = 0; j < candidate.length; ++j) {
            for (int l = 0; l < index; ++l) {
                final int dist = this.distance(candidate[j], ts.suite.get(l).test);
                if (dist < fitness[j]) {
                    fitness[j] = dist;
                }
            }
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
    
    private int distance(final int[] x, final int[] y) {
        int dist = 0;
        for (int i = 0; i < x.length; ++i) {
            if (x[i] != y[i]) {
                ++dist;
            }
        }
        return dist;
    }
}
