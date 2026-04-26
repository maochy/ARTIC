package artic.algorithm;

import java.util.*;
import artic.combinatorial.*;
import artic.common.Case;
import artic.common.Generation;
import artic.common.RandomSample;

/**
 * @author: Linlin Wen
 *
 * Random testing baseline.
 *
 * Each step samples a valid test case at random without candidate selection.
 */
public class RT implements Generation {
    private final Random random;

    public RT() {
        this.random = new Random();
    }

    /**
     * Generates a complete covering array for CAT-measure.
     */
    @Override
    public void generation(final CTModel model, final TestSuite ts) {
        final long startTime = System.nanoTime();
        model.initialization();
		ts.suite.clear();
        while (model.getCombUncovered() != 0L) {
            int[] tc = RandomSample.sample(model, this.random);
            if (model.isValid(tc)) {
                ts.suite.add(new TestCase(tc));
                model.updateCombination(tc);
            }
        }
        final long endTime = System.nanoTime();
        ts.time = endTime - startTime;
    }

    /**
     * Records cumulative generation time at fixed target suite sizes.
     */
    @Override
    public Map<Integer, Long> generation(final CTModel model, final TestSuite ts, int[] sizes) {
        final long startTime = System.nanoTime();
        Map<Integer,Long> map = new HashMap<>();
        int cnt = 0;
        int size = sizes[cnt];
        while (true) {
            int[] tc = RandomSample.sample(model, this.random);
            ts.suite.add(new TestCase(tc));
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
        ts.suite.clear();
        while(true) {
        	final int[] tc = RandomSample.sample(model, this.random);
            if (model.isValid(tc)) {
            	TestCase testCase = new TestCase(tc);
                ts.suite.add(testCase);
                if(scenario.detected(testCase, model)) {
                	return;
                }
            }
        }
    }
}
