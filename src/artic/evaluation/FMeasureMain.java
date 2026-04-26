package artic.evaluation;

import artic.algorithm.ARTICGEOO;
import artic.algorithm.ARTsum;
import artic.algorithm.FSCSHD;
import artic.algorithm.RT;
import artic.combinatorial.CTModel;
import artic.common.Case;
import artic.common.CaseLARGE;
import artic.common.CaseSIR;
import java.util.ArrayList;
import java.util.List;

/**
 * @author: Linlin Wen
 *
 * F-measure evaluation entry.
 *
 * Records the test suite size when the first failure is detected.
 */
public class FMeasureMain {
    public static void main(String[] args) throws Exception {
        // Output path and default repeat counts.
        String rootPath = "result/FMeasure";
        int sirRepeat = EvaluationConfig.DEFAULT_SIR_REPEAT;
        int largeRepeat = EvaluationConfig.DEFAULT_LARGE_REPEAT;
        int[] strength = EvaluationConfig.DEFAULT_STRENGTH;

        // F-measure compares the first-category algorithms.
        Class<?>[] alg = new Class<?>[] {
                RT.class,
                FSCSHD.class,
                ARTsum.class,
                ARTICGEOO.class
        };
        runFMeasure(rootPath, strength, EvaluationConfig.SIR_NAMES, EvaluationConfig.SIR_PARAS,
                alg, CaseSIR.class, sirRepeat);
        runFMeasure(rootPath, strength, EvaluationConfig.LARGE_NAMES, EvaluationConfig.LARGE_PARAS,
                alg, CaseLARGE.class, largeRepeat);
    }

    /**
     * Runs the F-measure experiment for each algorithm.
     */
    private static void runFMeasure(String rootPath, int[] strength, String[] names, int[] paras,
                                    Class<?>[] algs, Class<?> caseClass, int repeat) throws Exception {
        for (Class<?> algorithm : algs) {
            evaluationFMeasure(rootPath, strength, names, paras, caseClass, algorithm, repeat);
        }
    }

    /** Evaluates F-measure for one algorithm. */
    private static void evaluationFMeasure(String rootPath, int[] strength, String[] names, int[] paras,
                                           Class<?> caseClass, Class<?> algorithm, int repeat) throws Exception {
        for (int strengthValue : strength) {
            for (int j = 0; j < names.length; j++) {
                System.out.println(algorithm.getSimpleName() + "==" + names[j] + "==" + strengthValue + "-way");

                Case subject = EvaluationUtils.createCase(caseClass, names[j]);
                List<ScenarioTools.ScenarioFault> faults = ScenarioTools.loadScenarioFaults(names[j]);
                List<List<Integer>> scenarioValues = new ArrayList<>();
                for (int k = 0; k < faults.size(); k++) {
                    scenarioValues.add(new ArrayList<Integer>());
                }

                for (int sf = 0; sf < faults.size(); sf++) {
                    ScenarioTools.ScenarioFault scenarioFault = faults.get(sf);
                    subject.setFaultsList(scenarioFault.faults, scenarioFault.rate);

                    for (int round = 0; round < repeat; round++) {
                        CTModel sub = EvaluationUtils.buildModel(subject, paras[j], strengthValue, names[j]);
                        int size = EvaluationUtils.generateSuite(algorithm, sub, subject).getTestSuiteSize();
                        scenarioValues.get(sf).add(size);
                        EvaluationUtils.appendMetric(rootPath, strengthValue, algorithm, names[j], paras[j], sf,
                                size);
                    }
                }

                for (int sf = 0; sf < faults.size(); sf++) {
                    System.out.printf("scenario %d avg F = %.4f%n",
                            sf, EvaluationUtils.average(scenarioValues.get(sf)));
                }
            }
        }
        System.out.println();
    }
}
