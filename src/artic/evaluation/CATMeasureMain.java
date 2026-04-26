package artic.evaluation;

import artic.algorithm.AETG;
import artic.algorithm.ARTICGAET;
import artic.algorithm.DPSO;
import artic.algorithm.FSCSSD;
import artic.algorithm.GSA;
import artic.algorithm.IPOG;
import artic.combinatorial.CTModel;
import artic.common.Case;
import artic.common.CaseLARGE;
import artic.common.CaseSIR;

import java.util.ArrayList;
import java.util.List;

/**
 * @author: Linlin Wen
 *
 * CAT-measure evaluation entry.
 *
 * Records test suite size for GAET-group algorithms and writes generation time separately.
 */
public class CATMeasureMain {
    public static void main(String[] args) throws Exception {
        // Output directories and repeat counts.
        String measureRootPath = "result/CATMeasure";
        String timeRootPath = "result/CATTime";
        int sirRepeat = EvaluationConfig.DEFAULT_SIR_REPEAT;
        int largeRepeat = EvaluationConfig.DEFAULT_LARGE_REPEAT;
        int[] strength = EvaluationConfig.DEFAULT_STRENGTH;

        // GAET-group algorithms.
        Class<?>[] algorithms = new Class<?>[] {
                FSCSSD.class,
                AETG.class,
                IPOG.class,
                DPSO.class,
                GSA.class,
                ARTICGAET.class
        };
        runCATMeasure(measureRootPath, timeRootPath, strength, EvaluationConfig.SIR_NAMES, EvaluationConfig.SIR_PARAS,
                algorithms, CaseSIR.class, sirRepeat);
        runCATMeasure(measureRootPath, timeRootPath, strength, EvaluationConfig.LARGE_NAMES, EvaluationConfig.LARGE_PARAS,
                algorithms, CaseLARGE.class, largeRepeat);
    }

    /**
     * Runs the CAT-measure experiment for each algorithm.
     */
    private static void runCATMeasure(String measureRootPath, String timeRootPath, int[] strength, String[] names, int[] paras,
                                      Class<?>[] algs, Class<?> caseClass, int repeat) throws Exception {
        for (Class<?> algorithm : algs) {
            evaluationCATMeasure(measureRootPath, timeRootPath, strength, names, paras, caseClass, algorithm, repeat);
        }
    }

    /** Runs CAT-measure evaluation for one algorithm. */
    private static void evaluationCATMeasure(String measureRootPath, String timeRootPath, int[] strength, String[] names, int[] paras,
                                             Class<?> caseClass, Class<?> algorithm, int repeat) throws Exception {
        for (int strengthValue : strength) {
            for (int j = 0; j < names.length; j++) {
                System.out.println(algorithm.getSimpleName() + "==" + names[j] + "==" + strengthValue + "-way");

                Case subject = EvaluationUtils.createCase(caseClass, names[j]);
                List<Integer> sizeValues = new ArrayList<>();
                List<Long> timeValues = new ArrayList<>();
                for (int round = 0; round < repeat; round++) {
                    CTModel sub = EvaluationUtils.buildModel(subject, paras[j], strengthValue, names[j]);
                    EvaluationUtils.TimedSuite timedSuite = EvaluationUtils.generateSuiteWithTime(algorithm, sub);
                    sizeValues.add(timedSuite.suite.getTestSuiteSize());
                    timeValues.add(timedSuite.time);
                    EvaluationUtils.appendMetric(measureRootPath, strengthValue, algorithm, names[j], paras[j], 0,
                            timedSuite.suite.getTestSuiteSize());
                    EvaluationUtils.appendMetric(timeRootPath, strengthValue, algorithm, names[j], paras[j], 0,
                            timedSuite.time);
                }

                System.out.printf("avg CAT = %.4f, avg time = %.4f ns (%d runs)%n",
                        EvaluationUtils.average(sizeValues), EvaluationUtils.average(timeValues), repeat);
            }
        }
        System.out.println();
    }
}
