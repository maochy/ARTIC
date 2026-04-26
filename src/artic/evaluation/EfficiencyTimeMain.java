package artic.evaluation;

import artic.algorithm.ARTICGEOO;
import artic.algorithm.ARTsum;
import artic.algorithm.FSCSHD;
import artic.algorithm.RT;
import artic.combinatorial.CTModel;
import artic.combinatorial.TestSuite;
import artic.common.GeneralCase;
import artic.common.Generation;

import java.util.Map;

/**
 * @author: Linlin Wen
 *
 * Efficiency-time evaluation entry.
 *
 * Records cumulative generation time at fixed target test suite sizes.
 */
public class EfficiencyTimeMain {
    public static void main(String[] args) throws Exception {
        // Output path and default experiment settings.
        String rootPath = "result/EfficiencyTime";
        int repeat = EvaluationConfig.DEFAULT_SIR_REPEAT;
        int[] parameterCounts = {5, 10};
        int[] sizes = {100, 200, 500, 1000, 2000, 5000, 10000};
        runCompareExperiment(rootPath, repeat, parameterCounts, sizes);
        runArticStrengthExperiment(rootPath, repeat, parameterCounts, sizes);
    }

    /**
     * Builds a binary input model with g parameters.
     */
    private static int[] buildBinaryValues(int para) {
        int[] values = new int[para];
        for (int i = 0; i < values.length; i++) {
            values[i] = 2;
        }
        return values;
    }

    /** Runs the efficiency-time comparison for first-category algorithms. */
    private static void runCompareExperiment(String rootPath, int repeat, int[] parameterCounts,
                                             int[] sizes) throws Exception {
        int strength = 2;
        Class<?>[] algorithms = new Class<?>[] {
                RT.class,
                FSCSHD.class,
                ARTsum.class,
                ARTICGEOO.class
        };

        for (int para : parameterCounts) {
            int[] values = buildBinaryValues(para);
            run(rootPath, repeat, strength, para, values, sizes, algorithms);
        }
    }

    /** Runs the extended-strength efficiency-time evaluation for ARTICGEOO. */
    private static void runArticStrengthExperiment(String rootPath, int repeat, int[] parameterCounts,
                                                   int[] sizes) throws Exception {
        int[] strengths = {3};

        for (int para : parameterCounts) {
            int[] values = buildBinaryValues(para);
            for (int strength : strengths) {
                run(rootPath, repeat, strength, para, values, sizes, ARTICGEOO.class);
            }
        }
    }

    /**
     * Runs the time experiment for a group of algorithms.
     */
    private static void run(String rootPath, int repeat, int strength, int para, int[] value,
                            int[] sizes, Class<?>[] algorithms) throws Exception {
        for (Class<?> algorithm : algorithms) {
            evaluate(rootPath, repeat, strength, para, value, sizes, algorithm);
        }
    }

    /**
     * Runs the time experiment for one algorithm.
     */
    private static void run(String rootPath, int repeat, int strength, int para, int[] value,
                            int[] sizes, Class<?> algorithm) throws Exception {
        evaluate(rootPath, repeat, strength, para, value, sizes, algorithm);
    }

    /**
     * Records cumulative generation time at several target suite sizes.
     */
    private static void evaluate(String rootPath, int repeat, int strength, int para, int[] value,
                                 int[] sizes, Class<?> algorithm) throws Exception {
        System.out.println(algorithm.getSimpleName() + "==g" + para + "==" + strength + "-way");
        String configRoot = rootPath + "/" + strength + "/" + para;

        for (int round = 0; round < repeat; round++) {
            GeneralCase subject = new GeneralCase(para, value);
            CTModel model = subject.getSubModel(para, 0, strength);
            model.name = "g" + para;
            Generation generation = EvaluationUtils.createGeneration(algorithm);
            TestSuite ts = new TestSuite();
            Map<Integer, Long> timeMap = generation.generation(model, ts, sizes);
            if (timeMap == null) {
                throw new IllegalStateException("Algorithm does not support efficiency time analysis: " + algorithm.getSimpleName());
            }
            for (int size : sizes) {
                Long time = timeMap.get(size);
                if (time != null) {
                    String fileName = configRoot + "/" + algorithm.getSimpleName() + "/" + size + ".txt";
                    EvaluationUtils.appendLine(fileName, time);
                }
            }
        }
    }
}
