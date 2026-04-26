package artic.algorithm;

import artic.combinatorial.CTModel;
import artic.combinatorial.TestSuite;
import artic.common.Case;
import artic.common.Generation;
import artic.common.RandomSample;

import java.util.*;

/**
 * @author: Linlin Wen
 *
 * GSA algorithm.
 *
 * It applies gravitational search optimization to combinatorial test generation.
 */
public class GSA implements Generation {

    private final static int TMAX = 10;

    /**
     * population size
     */
    private final static int N = 300;

    private final static double reduce = (N-1)/(TMAX-1.0);

    private final double epsilon = 1e-4;

    private final static double G0 = 10;

    private final static double alpha = 20;

    /**
     * 0 is ceil，1 is round，2 is floor
     */
    private final static int method = 1;

    private CTModel model;

    private Agent[] pop;

    private Random random;

    private long best;

    private long worst;


    public GSA(){
        random = new Random();
    }

    private void initPopulation(){
        pop = new Agent[N];
        worst = Long.MAX_VALUE;
        best = Long.MIN_VALUE;
        for (int i = 0; i < N; i++) {
            int[] tc = RandomSample.sample(model,random);
            long fit = model.fitnessValue(tc);
            pop[i] = new Agent(tc,fit);
            if (worst > fit){
                worst = fit;
            }
            if (best < fit){
                best = fit;
            }
        }
    }

    private void updateMass(){
        double sub = best-worst;
        double sum = 0;
        for (int i = 0; i < N; i++) {
            pop[i].mass = (pop[i].fitness-worst)/sub;
            sum += pop[i].mass;
        }
        for (int i = 0; i < N; i++) {
            pop[i].mass /= sum;
        }
    }
    
    private Double dist(int[] t1, int[] t2){
        double dist =0;
        for (int i = 0; i < t1.length; i++) {
            double sub = t1[i]-t2[i];
            dist += sub*sub;
        }
        return Math.sqrt(dist);
    }

    private int[] evlution(){
        initPopulation();
        for (int iter = 0; iter < TMAX; iter++) {
            double[][] force = updateForce(iter);
            updatePositionAndFitness(force);
        }
        List<int[]> list = new ArrayList<>();
        for (int i = 0; i < N; i++) {
            if (pop[i].fitness == best){
                list.add(pop[i].testcase);
            }
        }
        return list.get(random.nextInt(list.size()));
    }

    private void updatePositionAndFitness(double[][] force) {
        updateMass();
        worst = Long.MAX_VALUE;
        best = Long.MIN_VALUE;
        for (int i = 0; i < N; i++) {
            double r = random.nextDouble();
            for (int j = 0; j < model.parameter; j++) {
                pop[i].velocity[j] = r * pop[i].velocity[j] + (force[i][j]);
                double pos = pop[i].testcase[j] + pop[i].velocity[j];
                switch (method){
                    case 0:
                        pop[i].testcase[j] = (int) Math.ceil(pos);
                        break;
                    case 1:
                        pop[i].testcase[j] = (int) Math.round(pos);
                        break;
                    case 2:
                        pop[i].testcase[j] = (int) Math.floor(pos);
                        break;
                }
            }
            checkSpace(pop[i]);
            pop[i].fitness = model.fitnessValue(pop[i].testcase);
            if (worst > pop[i].fitness){
                worst = pop[i].fitness;
            }
            if (best < pop[i].fitness){
                best = pop[i].fitness;
            }
        }
    }

    private void checkSpace(Agent agent) {
        boolean flag = false;
        for (int i = 0; i < model.parameter; i++) {
            if (agent.testcase[i] < 0 || agent.testcase[i] >= model.value[i]){
                flag = true;
                break;
            }
        }
        if (flag){ // out of space,reinitialized
            agent.testcase = RandomSample.sample(model,random);
            agent.velocity= new double[model.parameter];
        }
    }

    private double[][] updateForce(int iter) {
        double[][] force = new double[N][model.parameter];
        int kbest = (int)(N-iter*reduce);
        Agent[] kbestList = findKthBigest(pop, kbest);
        double Gt = G0 * Math.pow(Math.E,-(alpha*iter/(TMAX*1.0)));
        for (int i = 0; i < N; i++) {
            double r = random.nextDouble();
            for (int j = 0; j < kbest; j++) {
                if (pop[i] != kbestList[j]){
                    double F = r*(Gt * kbestList[j].mass)/(dist(pop[i].testcase,kbestList[j].testcase)+epsilon);
                    for (int k = 0; k < model.parameter; k++) {
                        force[i][k] += F * (kbestList[j].testcase[k]-pop[i].testcase[k]);
                    }
                }
            }
        }
        return force;
    }


    @Override
    public void generation(CTModel model, TestSuite ts, Case scenario) {
        (this.model = model).initialization();
        ts.suite.clear();
        final int[] test = RandomSample.sample(model, this.random);
        ts.suite.add(new artic.combinatorial.TestCase(test));
        if(scenario.detected(new artic.combinatorial.TestCase(test), model)) { // Failure detected.
            return;
        }
        model.updateCombination(test);
        while (model.getCombUncovered() != 0L && ts.getTestSuiteSize() < 50/scenario.getRate()) {
            final artic.combinatorial.TestCase best = new artic.combinatorial.TestCase(evlution());
            if (best.test == null) {
                break;
            }
            ts.suite.add(best);
            if(scenario.detected(best, model)) { // Failure detected.
                return;
            }
            model.updateCombinationAndLevel(best.test);
        }
        while (ts.getTestSuiteSize() < 50/scenario.getRate()) {
            artic.combinatorial.TestCase testCase = new artic.combinatorial.TestCase(RandomSample.sample(model, random));
            ts.suite.add(testCase);
            if(scenario.detected(testCase, model)) {
                return;
            }
        }
    }

    @Override
    public Map<Integer,Long> generation(CTModel model, TestSuite ts, int[] sizes) {
        final long startTime = System.nanoTime();
        Map<Integer,Long> map = new HashMap<>();
        (this.model = model).initialization();
        ts.suite.clear();
        final int[] test = RandomSample.sample(model, this.random);
        ts.suite.add(new artic.combinatorial.TestCase(test));
        model.updateCombination(test);
        int cnt = 0;
        int size = sizes[cnt];
        while (ts.getTestSuiteSize() < sizes[sizes.length-1]) {
            final artic.combinatorial.TestCase tc = new artic.combinatorial.TestCase(evlution());
            ts.suite.add(tc);
            if (ts.getTestSuiteSize() == size) {
                map.put(ts.getTestSuiteSize(), System.nanoTime()-startTime);
                cnt ++;
                size = sizes[cnt];
            }
            model.updateCombination(tc.test);
        }
        final long endTime = System.nanoTime();
        ts.time = endTime - startTime;
        return map;
    }

    @Override
    public void generation(CTModel model, TestSuite ts) {
        final long startTime = System.nanoTime();
        (this.model = model).initialization();
        ts.suite.clear();
        final int[] test = RandomSample.sample(model, this.random);
        ts.suite.add(new artic.combinatorial.TestCase(test));
        model.updateCombination(test);
        while (model.getCombUncovered() != 0L) {
            final artic.combinatorial.TestCase tc = new artic.combinatorial.TestCase(evlution());
            ts.suite.add(tc);
            model.updateCombination(tc.test);
        }
        final long endTime = System.nanoTime();
        ts.time = endTime - startTime;
    }

    class Agent{
        int[] testcase;
        long fitness;
        double[] velocity;
        double mass;

        public Agent(int[] testcase, long fitness) {
            this.testcase = testcase;
            this.fitness = fitness;
            mass = 0;
            velocity = new double[testcase.length];
        }
    }


    public static Agent[] findKthBigest(Agent[] nums, int k) {
        // Build a max heap.
        Agent[] arr = new Agent[k];
        for (int i = 0; i < k; ++i) {
            arr[i] = nums[i];
        }
        for (int i = arr.length / 2; i >= 0; --i) {
            adjustHeap(arr, i, arr.length);
        }

        for (int i = k; i < nums.length; ++i) {
            if (nums[i].fitness <= arr[0].fitness)
                continue;
            arr[0] = nums[i];
            adjustHeap(arr, 0, arr.length);
        }
        return arr;
    }

    // Build, select, and adjust the heap.
    public static void adjustHeap(Agent[] arr, int i, int len) {
        Agent tmp = arr[i];
        for (int k = 2*i+1; k < len; k = k*2+1) {
            if (k < len-1 && arr[k].fitness >= arr[k+1].fitness)
                k++;
            if (arr[k].fitness < tmp.fitness) {
                arr[i] = arr[k];
                i = k;
            }
            else
                break;
        }
        arr[i] = tmp;
    }

}
