package artic.algorithm;

import artic.combinatorial.CTModel;
import artic.combinatorial.TestCase;
import artic.combinatorial.TestSuite;
import artic.common.Case;
import artic.common.Generation;
import artic.common.RandomSample;

import java.util.*;

/**
 * @author: Linlin Wen
 *
 * DPSO algorithm.
 *
 * It applies particle swarm optimization to combinatorial test generation.
 */
public class DPSO implements Generation {

    final static int maxIterations = 50;

    final static int swarmSize = 20;
    /**
     * Inertia Weight
     */
    final static double w = 0.9;
    /**
     * Acceleration Constant
     */
    final static double c = 1.3;
    /**
     *  velocity probability
     */
    final static double pro1=0.5;
    /**
     *  level probability
     */
    final static double pro2=0.5;
    /**
     *  mutation probability
     */
    final static double pro3=0.5;

    Random random;

    private TestCase gBest;

    private TestCase[] pBest;

    private CTModel model;

    private TestSuite ts;

    private TestCase[] swarm;


    public DPSO(){
        this.random = new Random();
        this.gBest = null;
        this.pBest = new TestCase[swarmSize];
        swarm = new TestCase[this.swarmSize];
    }

    private TestCase[] initSwarm(){
        long best = Long.MIN_VALUE;
        List<TestCase> bestList = new ArrayList<>();
        for (int i = 0; i < swarm.length; i++ ){
            //init swarm
            int[] tc = RandomSample.sample(model, this.random);
            swarm[i] = new TestCase(model.fitnessValue(tc),tc);
            pBest[i] = new TestCase(swarm[i]);
            if(best < swarm[i].uncover){
                bestList.clear();
                best = swarm[i].uncover;
                bestList.add(swarm[i]);
            } else if (best == swarm[i].uncover) {
                bestList.add(swarm[i]);
            }
        }
        if (bestList.size() == 1){
            gBest = new TestCase(bestList.get(0));
            return swarm;
        }
        // Additional Evaluation of gbest
        double minDist = Double.MAX_VALUE;
        int index = -1;
        for (int i = 0; i < bestList.size(); i++) {
            double dist = hammingDist(bestList.get(i).testcase);
            if (minDist > dist){
                index = i;
                minDist = dist;
            }
        }
        gBest = new TestCase(bestList.get(index));
        return swarm;
    }


    public void updateVelocity(TestCase[] swarm) {
        for (int i = 0; i < swarm.length; i++) {
            swarm[i].updateVelocity(pBest[i]);
        }
    }

    public void updatePosition(TestCase[] swarm) {
        for (TestCase testCase:swarm) {
            testCase.updatePosition();
        }
        List<TestCase> bestList = new ArrayList<>();
        double best = gBest.uncover;
        for (int i = 0; i <swarm.length ; i++) {
            if (pBest[i].uncover < swarm[i].uncover){
                pBest[i] = new TestCase(swarm[i]);
            }
            if(best < swarm[i].uncover){
                bestList.clear();
                bestList.add(swarm[i]);
                best = swarm[i].uncover;
            } else if (best == swarm[i].uncover) {
                bestList.add(swarm[i]);
            }
        }
        if (bestList.isEmpty()){
            return;
        }
        if(bestList.size() == 1){
            gBest = new TestCase(bestList.get(0));
        }
        // Additional Evaluation of gbest
        double minDist = Double.MAX_VALUE;
        int index = -1;
        for (int i = 0; i < bestList.size(); i++) {
            double dist = hammingDist(bestList.get(i).testcase);
            if (minDist > dist){
                index = i;
                minDist = dist;
            }
        }
        gBest = new TestCase(bestList.get(index));
    }

    static void plus(Map<String,Velocity> v1, Map<String,Velocity> v2, Map<String, Velocity> v3){
        v2.forEach((key,val)->v1.merge(key,val,(val1,val2)-> {
            double p = val2.prob*c*Math.random();
            p = p > 1?1:p;
            val1.prob *= w;
            val1.prob += p;
            val1.prob = val1.prob>=1?1:val1.prob;
            return val1;
        }));

        v3.forEach((key,val)->v1.merge(key,val,(val1,val2)-> {
            double p = val2.prob*c*Math.random();
            p = p>1?1:p;
            val1.prob += p;
            val1.prob = val1.prob>=1?1:val1.prob;
            return val1;
        }));
        List<String> removeKeys = new ArrayList<>();
        for (Map.Entry<String,Velocity> entry:v1.entrySet()) {
            if(entry.getValue().prob < pro1){
                removeKeys.add(entry.getKey());
            }
        }
        for (String key:removeKeys) {
            v1.remove(key);
        }
    }

    static Map<String, Velocity> sub(TestCase t1, TestCase t2,List<int[]> allPc){
        HashMap<String,Velocity> velocitySet = new HashMap<>();
        for (int[] pos:allPc) {
            for (int i = 0; i < pos.length; i++) {
                if (t1.testcase[i] != t2.testcase[i]){
                    int[] v = new int[pos.length * 2];
                    for (int j = 0,k=0; j < v.length; j+=2,k++) {
                        v[j] = pos[k];
                        v[j+1] = t1.testcase[v[j]];
                    }
                    Velocity velocity = new Velocity(v,0.5);
                    velocitySet.put(Arrays.toString(v),velocity);
                }
            }
        }
        return velocitySet;
    }

    private double hammingDist(int[] tc){
        long sum = 0;
        for (int i = 0; i < ts.getTestSuiteSize(); i++) {
            int[] test = ts.suite.get(i).test;
            for (int j = 0; j < test.length; j ++){
                sum += tc[j] == test[j]?0:1;
            }
        }
        return sum/(ts.getTestSuiteSize()*1.0);
    }





    @Override
    public void generation(CTModel model, TestSuite ts, Case scenario) {
        (this.model = model).initialization();
        this.ts = ts;
        ts.suite.clear();
        final int[] test = RandomSample.sample(model, this.random);
        ts.suite.add(new artic.combinatorial.TestCase(test));
        if(scenario.detected(new artic.combinatorial.TestCase(test), model)) { // Failure detected.
            return;
        }
        model.updateCombination(test);
        while (model.getCombUncovered() != 0L && ts.getTestSuiteSize() < 50/scenario.getRate()) {
            final artic.combinatorial.TestCase best = new artic.combinatorial.TestCase(nextTestCase());
            if (best.test == null) {
                break;
            }
            ts.suite.add(best);
            if(scenario.detected(best, model)) { // Failure detected.
                return;
            }
            model.updateCombination(test);
        }
    }


    @Override
    public void generation(CTModel model, TestSuite ts) {
        final long startTime = System.nanoTime();
        (this.model = model).initialization();
        this.ts = ts;
        ts.suite.clear();
        final int[] test = RandomSample.sample(model, this.random);
        ts.suite.add(new artic.combinatorial.TestCase(test));
        model.updateCombination(test);
        while (model.getCombUncovered() != 0L) {
            final artic.combinatorial.TestCase tc = new artic.combinatorial.TestCase(nextTestCase());
            ts.suite.add(tc);
            model.updateCombination(tc.test);
        }
        final long endTime = System.nanoTime();
        ts.time = endTime - startTime;
    }

    private int[] nextTestCase() {
        TestCase[] swarms = initSwarm();
        for (int i = 0; i < maxIterations; i++) {
            updateVelocity(swarms);
            updatePosition(swarms);
        }
        return gBest.testcase;
    }

    class TestCase{
        long uncover;
        int[] testcase;
        Map<String,Velocity> velocitySet;

        public TestCase(long uncover, int[] testcase) {
            this.uncover = uncover;
            this.testcase = testcase;
            this.velocitySet = new HashMap();
            initVelocity();
        }

        void initVelocity(){
            for (int j = 0; j < model.getTestCaseCoverMax(); j ++){
                int[] pos = model.allPc.get(j);
                int[] v = new int[model.t_way*2];
                for (int k = 0,m=0; k < v.length; k++,m++) {
                        v[k++] = pos[m];
                        v[k] = random.nextInt(model.value[pos[m]]);
                    }
                    Velocity velocity = new Velocity(v,random.nextDouble());
                velocitySet.put(Arrays.toString(v),velocity);
            }
        }

        public TestCase(TestCase testCase) {
            this.uncover = testCase.uncover;
            this.testcase = testCase.testcase;
        }

        public void updatePosition() {
            //Particle Reinitialization
            if (velocitySet.isEmpty()){
                this.testcase = RandomSample.sample(model, random);
                this.uncover = model.fitnessValue(testcase);
                initVelocity();
                return;
            }
            int[] tc = new int[testcase.length];
            Arrays.fill(tc,-1);
            ArrayList<Map.Entry<String, Velocity>> entries = new ArrayList<>(velocitySet.entrySet());
            entries.sort((o1,o2)->Double.compare(o2.getValue().prob,o1.getValue().prob));
            for (Map.Entry<String,Velocity> entry:entries){
                double r1 = random.nextDouble();
                if (r1 < entry.getValue().prob){
                    int[] tuple = entry.getValue().tuple;
                    for (int i = 0; i < tuple.length; i+=2) {
                        double r2 = random.nextDouble();
                        if (r2 < pro2 && tc[tuple[i]] == -1){
                            tc[tuple[i]] = tuple[i+1];
                        }
                    }
                }
            }
            for (int i = 0; i < tc.length; i++) {
                if (tc[i] == -1){
                    tc[i] = testcase[i];
                }
            }
            double r3 = random.nextDouble();
            if (r3 < pro3){
                int mui = random.nextInt(tc.length);
                tc[mui] = random.nextInt(model.value[mui]);
            }
            testcase = tc;
            try {
                uncover = model.fitnessValue(testcase);
            }
            catch (Exception e){
                uncover = model.fitnessValue(testcase);
            }
        }

        public void updateVelocity(TestCase pBest) {
            Map<String, Velocity> v1 = sub(pBest, this, model.allPc);
            Map<String, Velocity> v2 = sub(gBest, this, model.allPc);
            plus(velocitySet,v1,v2);
        }
    }

    static class Velocity{
        int[] tuple;
        double prob;

        public Velocity(int[] tuple, double prob) {
            this.tuple = tuple;
            this.prob = prob;
        }

        @Override
        public int hashCode() {
            return Arrays.hashCode(tuple);
        }

        @Override
        public boolean equals(Object obj) {
            if (this == obj)
                return true;
            if (obj instanceof Velocity && Arrays.equals(this.tuple,((Velocity) obj).tuple)){
                return true;
            }
            return false;
        }
    }
}
