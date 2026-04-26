package artic.evaluation;

import artic.algorithm.AETG;
import artic.algorithm.ARTICGAET;
import artic.algorithm.FSCSSD;
import artic.algorithm.IPOG;
import artic.combinatorial.TestCase;
import artic.common.Case;
import artic.common.CaseLARGE;
import artic.common.CaseSIR;

import java.util.ArrayList;
import java.util.List;

/**
 * @author: Linlin Wen
 *
 * P-measure evaluation entry.
 *
 * Estimates the probability of detecting at least one failure.
 */
public class PMeasureMain {
    public static void main(String[] args) throws Exception {
        // Output path and TS settings.
        String rootPath = "result/PMeasure";
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
                ARTICGAET.class
        };
        runPMeasure(rootPath, strength, EvaluationConfig.SIR_NAMES, EvaluationConfig.SIR_PARAS,
                alg, CaseSIR.class, sirRepeat, tsRoot, regenerateTS);
        runPMeasure(rootPath, strength, EvaluationConfig.LARGE_NAMES, EvaluationConfig.LARGE_PARAS,
                alg, CaseLARGE.class, largeRepeat, tsRoot, regenerateTS);
    }

    /**
     * Runs the P-measure experiment for one subject group.
     */
    private static void runPMeasure(String rootPath, int[] strength, String[] names, int[] paras,
                                    Class<?>[] algs, Class<?> caseClass, int repeat,
                                    String tsRoot, boolean regenerateTS) throws Exception {
        evaluationPMeasure(rootPath, strength, names, paras, caseClass, algs, repeat, tsRoot, regenerateTS);
    }

    /** Evaluates P-measure for the current subject group. */
    private static void evaluationPMeasure(String rootPath, int[] strength, String[] names, int[] paras,
                                           Class<?> caseClass, Class<?>[] algorithms, int repeat,
                                           String tsRoot, boolean regenerateTS) throws Exception {
        for (int strengthValue : strength) {
            for (int j = 0; j < names.length; j++) {
                System.out.println(names[j] + "==" + strengthValue + "-way");

                List<ScenarioTools.ScenarioFault> faults = ScenarioTools.loadScenarioFaults(names[j]);
                List<List<Integer>>[] scenarioValues = new ArrayList[algorithms.length];
                Case[] subjects = new Case[algorithms.length];
                for (int algIndex = 0; algIndex < algorithms.length; algIndex++) {
                    subjects[algIndex] = EvaluationUtils.createCase(caseClass, names[j]);
                    scenarioValues[algIndex] = new ArrayList<>();
                    for (int k = 0; k < faults.size(); k++) {
                        scenarioValues[algIndex].add(new ArrayList<Integer>());
                    }
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
                            Case subject = subjects[algIndex];
                            Class<?> algorithm = algorithms[algIndex];
                            for (int sf = 0; sf < faults.size(); sf++) {
                                ScenarioTools.ScenarioFault scenarioFault = faults.get(sf);
                                subject.setFaultsList(scenarioFault.faults, scenarioFault.rate);
                                int emValue = subject.computeEM(trimmed);
                                int pValue = emValue > 0 ? 1 : 0;
                                scenarioValues[algIndex].get(sf).add(pValue);
                                EvaluationUtils.appendMetric(rootPath, strengthValue, algorithm,
                                        names[j], paras[j], sf, pValue);
                            }
                        }
                    }
                }

                for (int algIndex = 0; algIndex < algorithms.length; algIndex++) {
                    System.out.println(algorithms[algIndex].getSimpleName());
                    for (int sf = 0; sf < faults.size(); sf++) {
                        System.out.printf("scenario %d avg P = %.6f%n",
                                sf, EvaluationUtils.average(scenarioValues[algIndex].get(sf)));
                    }
                }
            }
        }
        System.out.println();
    }
}
