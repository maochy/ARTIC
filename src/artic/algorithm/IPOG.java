package artic.algorithm;

import artic.combinatorial.CTModel;
import artic.combinatorial.TestCase;
import artic.combinatorial.TestSuite;
import artic.common.Case;
import artic.common.Generation;
import artic.common.ProjectResource;
import edu.uta.cse.fireeye.common.Parameter;
import edu.uta.cse.fireeye.common.SUT;
import edu.uta.cse.fireeye.common.TestGenProfile;
import edu.uta.cse.fireeye.common.TestSet;
import edu.uta.cse.fireeye.service.engine.FireEye;
import edu.uta.cse.fireeye.service.engine.SUTInfoReader;
import edu.uta.cse.fireeye.service.exception.OperationServiceException;
import edu.uta.cse.fireeye.util.Util;
import artic.common.RandomSample;

import java.io.IOException;
import java.util.*;

/**
 * @author: Linlin Wen
 *
 * IPOG algorithm.
 *
 * It uses FireEye/ACTS to generate covering arrays and falls back to random tests for F-measure.
 */
public class IPOG implements Generation{

    private static List<int[]> generation(String name, int t){
        TestGenProfile profile = TestGenProfile.instance();
        setCommandLineProperties(profile);
        profile.setDOI(t);
        SUTInfoReader reader = null;
        String inputFileName;
        try {
            inputFileName = ProjectResource.resolveFile("IPM/" + name + ".txt").getPath();
            reader = new SUTInfoReader(inputFileName);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
        SUT sut = null;
        try {
            sut = reader.getSUT();
        } catch (IOException e) {
            Util.abort(e.getMessage());
            e.printStackTrace();
        }
        TestSet testSet = new TestSet();
        try {
            testSet = FireEye.generateTestSet(testSet, sut);
        } catch (OperationServiceException e) {
            throw new RuntimeException(e);
        }
        return translate(testSet);
    }

    private static void setCommandLineProperties(TestGenProfile profile) {
        profile.setOutputFormat("nist");
        String out = System.getProperty("output");
        if (out != null && out.length() > 0) {
            profile.setOutputFormat(out);
        }

        out = System.getProperty("algo");
        if (out != null && out.length() > 0) {
            profile.setAlgorithm(out);
        }

        out = System.getProperty("doi");
        if (out != null && out.length() > 0) {
            profile.setDOI(Integer.parseInt(out));
        }

        out = System.getProperty("opt");
        if (out != null && out.length() > 0) {
            profile.setOptLevel(Integer.parseInt(out));
        }

        String rand = System.getProperty("randstar");
        if (rand != null && rand.length() > 0) {
            profile.setRandstar(rand);
        }

        String mode = System.getProperty("mode");
        if (mode != null && mode.length() > 0) {
            profile.setMode(mode);
        }

        String check = System.getProperty("check");
        if (check != null && check.length() > 0) {
            profile.setCheckCoverage(check);
        }

        String progress = System.getProperty("progress");
        if (progress != null && progress.length() > 0) {
            profile.setProgress(progress);
        }

        String combine = System.getProperty("combine");
        if (combine != null && !combine.isEmpty()) {
            profile.setCombine(combine);
        }

        String debug = System.getProperty("debug");
        if (debug != null && !debug.isEmpty()) {
            profile.setDebugMode(debug);
        }

        TestGenProfile.instance().setIgnoreConstraints(true);
        String tiebreaker = System.getProperty("tiebreak");
        profile.setTieBreaker(tiebreaker);
    }

    @Override
    public void generation(CTModel model, TestSuite ts, Case scenario) {
        ts.suite.clear();
        List<int[]> testSet =IPOG.generation(model.name, model.t_way);
        for (int i = 0; i < testSet.size(); i++) {
            TestCase testCase = new TestCase(testSet.get(i));
            ts.suite.add(testCase);
            if(scenario.detected(testCase, model)) {
                return;
            }
        }
        Random random = new Random();
        while (true){
            int[] test = RandomSample.sample(model, random);
            TestCase testCase = new TestCase(test);
            ts.suite.add(testCase);
            if(scenario.detected(testCase, model)) {
                return;
            }
        }
    }

    @Override
    public void generation(CTModel model, TestSuite ts) {
        ts.suite.clear();
        long startTime = System.nanoTime();
        List<int[]> testSet =IPOG.generation(model.name, model.t_way);
        final long endTime = System.nanoTime();
        ts.time = endTime - startTime;
        testSet.forEach(e->ts.suite.add(new TestCase(e)));
    }

    private static List<int[]> translate(TestSet testSet) {
        List<int[]> translated = new ArrayList<>();
        int[] paramIndex = new int[testSet.getNumOfParams()];
        ArrayList<Parameter> params = testSet.getParams();
        for (int i = 0; i < paramIndex.length; i++) {
            paramIndex[i] = params.get(i).getID();
        }
        for (int[] tc: testSet.getMatrix()){
            int[] tran_tc = new int[paramIndex.length];
            for (int i = 0; i < paramIndex.length; i++) {
                tran_tc[paramIndex[i]] = tc[i];
            }
            translated.add(tran_tc);
        }
        return translated;
    }


}
