package artic.evaluation;

import artic.algorithm.AETG;
import artic.algorithm.ARTICGAET;
import artic.algorithm.DPSO;
import artic.algorithm.FSCSSD;
import artic.algorithm.GSA;
import artic.algorithm.IPOG;
import artic.combinatorial.TestCase;
import artic.common.Case;
import artic.common.CaseLARGE;
import artic.common.CaseSIR;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * @author: Linlin Wen
 *
 * FDR evaluation entry.
 *
 * Records fault detection results for all-bugs scenarios.
 */
public class FDRMain {
    public static void main(String[] args) throws Exception {
        // Output path and TS settings.
        String rootPath = "result/FDR";
        String tsRoot = EvaluationConfig.DEFAULT_TS_ROOT;
        boolean regenerateTS = EvaluationConfig.DEFAULT_REGENERATE_TS;
        int sirRepeat = EvaluationConfig.DEFAULT_SIR_REPEAT;
        int largeRepeat = EvaluationConfig.DEFAULT_LARGE_REPEAT;
        int[] strength = EvaluationConfig.DEFAULT_STRENGTH;

        // Default comparison algorithms.
        Class<?>[] alg = new Class<?>[] {
                FSCSSD.class,
                AETG.class,
                IPOG.class,
                DPSO.class,
                GSA.class,
                ARTICGAET.class
        };
        runFDR(rootPath, strength, EvaluationConfig.SIR_NAMES, EvaluationConfig.SIR_PARAS,
                alg, CaseSIR.class, sirRepeat, tsRoot, regenerateTS);
        runFDR(rootPath, strength, EvaluationConfig.LARGE_NAMES, EvaluationConfig.LARGE_PARAS,
                alg, CaseLARGE.class, largeRepeat, tsRoot, regenerateTS);
    }

    /**
     * Runs the FDR experiment for one subject group.
     */
    private static void runFDR(String rootPath, int[] strength, String[] names, int[] paras,
                               Class<?>[] algs, Class<?> caseClass, int repeat,
                               String tsRoot, boolean regenerateTS) throws Exception {
        evaluationFDR(rootPath, strength, names, paras, caseClass, algs, repeat, tsRoot, regenerateTS);
    }

    /** Evaluates FDR for the current subject group. */
    private static void evaluationFDR(String rootPath, int[] strength, String[] names, int[] paras,
                                      Class<?> caseClass, Class<?>[] algorithms, int repeat,
                                      String tsRoot, boolean regenerateTS) throws Exception {
        for (int strengthValue : strength) {
            for (int j = 0; j < names.length; j++) {
                System.out.println(names[j] + "==" + strengthValue + "-way");

                List<Double>[] values = new ArrayList[algorithms.length];
                Case[] subjects = new Case[algorithms.length];
                for (int algIndex = 0; algIndex < algorithms.length; algIndex++) {
                    subjects[algIndex] = EvaluationUtils.createCase(caseClass, names[j]);
                    values[algIndex] = new ArrayList<>();
                }

                SuiteRepository.ensureSuites(tsRoot, regenerateTS, caseClass, algorithms,
                        names[j], paras[j], strengthValue, repeat);
                int globalMinSize = SuiteRepository.computeGlobalMinSize(
                        tsRoot, algorithms, names[j], paras[j], strengthValue, repeat);
                try (SuiteRepository.SuiteRoundReader reader =
                             SuiteRepository.openRoundReader(tsRoot, algorithms, names[j], paras[j], strengthValue)) {
                    for (int round = 0; round < repeat; round++) {
                        SuiteRepository.SuiteRound aligned = reader.nextRound(paras[j]);
                        for (int algIndex = 0; algIndex < algorithms.length; algIndex++) {
                            ArrayList<TestCase> trimmed = SuiteTools.truncate(aligned.suites[algIndex], globalMinSize);
                            int[] matrix = subjects[algIndex].computeFaultMatrix(trimmed);
                            values[algIndex].add(detectionRatio(matrix));
                            EvaluationUtils.appendMetric(rootPath, strengthValue, algorithms[algIndex], names[j], paras[j], -1,
                                    formatMatrix(matrix));
                        }
                    }
                }

                for (int algIndex = 0; algIndex < algorithms.length; algIndex++) {
                    System.out.printf("%s avg FDR = %.6f (%d runs)%n",
                            algorithms[algIndex].getSimpleName(), EvaluationUtils.average(values[algIndex]), repeat);
                }
            }
        }
        System.out.println();
    }

    /** Formats the FDR output row. */
    private static String formatMatrix(int[] matrix) {
        return Arrays.toString(matrix).replace("[", "").replace("]", "").replace(",", "\t");
    }

    /** Computes the FDR value. */
    private static double detectionRatio(int[] matrix) {
        if (matrix.length == 0) {
            return 0.0;
        }
        int detected = 0;
        for (int value : matrix) {
            detected += value;
        }
        return detected / (double) matrix.length;
    }
}
