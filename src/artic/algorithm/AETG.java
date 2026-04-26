package artic.algorithm;

import java.util.*;
import java.util.stream.Collectors;
import artic.combinatorial.*;
import artic.common.ALG;
import artic.common.Case;
import artic.common.Generation;
import artic.common.RandomSample;

/**
 * @author: Linlin Wen
 *
 * AETG algorithm.
 *
 * It greedily estimates the newly covered t-way combinations of each candidate.
 */
public class AETG  implements Generation{
	public CTModel model;
	private int CANDIDATE;
	private final Random random;
			
	public AETG() {
		this.CANDIDATE = 10;
		this.model = null;
		this.random = new Random();
	}

	/**
	 * Generates a complete covering array for CAT-measure.
	 */
	@Override
	public void generation(CTModel model, TestSuite ts) {
		long startTime = System.nanoTime();
		(this.model = model).initialization();
		model.initLevelCover();
		ts.suite.clear();
		while (model.getCombUncovered() != 0L) {
			final int[] next = this.nextBestTestCase(this.CANDIDATE,false);
			final TestCase best = new TestCase(next);
			ts.suite.add(best);
			model.updateCombinationAndLevel(best.test);
		}
		final long endTime = System.nanoTime();
		ts.time = endTime - startTime;
	}
	
	/**
	 * Stops when the current scenario detects a failure for F-measure.
	 */
	@Override
	public void generation(CTModel model, TestSuite ts,Case scenario) {
		(this.model = model).initialization();
		ts.suite.clear();
		model.initLevelCover();
		while (model.getCombUncovered() != 0L) {
			final int[] next = this.nextBestTestCase(this.CANDIDATE,true);
			if (next == null) {
				break;
			}
			final TestCase best = new TestCase(next);
			ts.suite.add(best);
			if(scenario.detected(best, model)) { // Failure detected.
				return;
			}
			model.updateCombinationAndLevel(best.test);
		}
		while (true) {
			TestCase testCase = new TestCase(RandomSample.sample(model, random));
			ts.suite.add(testCase);
			if(scenario.detected(testCase, model)) {
				return;
			}
		}
	}


	public int[] nextBestTestCase(final int N,boolean muti) {
		if (muti){
			int[][] cands = new int[N][];
			long[] fitness = new long[N];
			Thread[] threads = new Thread[N];
			for (int i = 0; i < N; i++) {
				int finalI = i;
				threads[i] = new Thread(()-> {
					cands[finalI] = nextTestCase();
					fitness[finalI] = this.model.fitnessValue(cands[finalI]);
				});
				threads[i].start();
			}
			for (int i = 0; i < N; i++) {
				try {
					threads[i].join();
				} catch (InterruptedException e) {
					throw new RuntimeException(e);
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
			return cands[maxIndex];

		}
		else {
			final int[] best = this.nextTestCase();
			if (best == null || N == 1) {
				return best;
			}
			long covBest = this.model.fitnessValue(best);
			for (int x = 1; x < N; ++x) {
				final int[] temp = this.nextTestCase();
				final long covTemp = this.model.fitnessValue(temp);
				if (covTemp == this.model.getTestCaseCoverMax()) {
					System.arraycopy(temp, 0, best, 0, this.model.parameter);
					break;
				}
				if (covTemp > covBest) {
					System.arraycopy(temp, 0, best, 0, this.model.parameter);
					covBest = covTemp;
				}
			}
			return best;
		}
	}

	/**
	  * Generates one test case.
	 */
	private int[] nextTestCase() {
		int[] maxCover = model.getMaxPar(); // Most frequent parameter-value pair among uncovered t-way combinations.
		int[] tc = new int[model.parameter];
		for (int i = 0; i < tc.length; i++) {
			if(i == maxCover[0]) {
				tc[i] = maxCover[1];
			}
			tc[i] = -1;
		}
		final List<Integer> permutation = new ArrayList<Integer>();
		
		// Collect unassigned parameters.
		for (int k = 0; k < this.model.parameter; ++k) {
			if (tc[k] == -1) {
				permutation.add(k);
			}
		}
		
		Collections.shuffle(permutation); // Randomize parameter order.
		for (final int par : permutation) {
			tc[par] = this.selectBestValue(tc, par);
		}
		return tc;
	}

	private boolean isConstraintSatisfied(final int[] test, final int p, final int v) {
		final int old = test[p];
		test[p] = v;
		final boolean satisfied = this.model.isValid(test);
		test[p] = old;
		return satisfied;
	}

	/**
	 * Selects the value with the largest additional coverage.
	 * @param test current partial test case
	 * @param par parameter index
	 * @return selected parameter value
	 */
	private int selectBestValue(final int[] test, final int par) {
		final ArrayList<Pair> vs = new ArrayList<Pair>();
		for (int i = 0; i < this.model.value[par]; ++i) {
			if (this.isConstraintSatisfied(test, par, i)) {
				final int num = this.coveredSchemaNumberFast(test, par, i);
				vs.add(new Pair(i, num));
			}
		}
		Collections.sort(vs);
		final int max = vs.get(0).number;
		final List<Pair> filtered = vs.stream().filter(p -> p.number == max).collect(Collectors.toList());
		final int r = this.random.nextInt(filtered.size());
		return filtered.get(r).index;
	}

	/**
	 * @param test current partial test case
	 * @param par parameter index
	 * @param val parameter value
	 * @return coverage value of the candidate assignment
	 */
	private int coveredSchemaNumberFast(final int[] test, final int par, final int val) {
		int fit = 0;
		int count = 0;
		final int[] new_test = new int[this.model.parameter];
		for (int i = 0; i < this.model.parameter; ++i) {
			new_test[i] = test[i];
			if (test[i] != -1) {
				++count;
			}
		}
		
		new_test[par] = val;
		final int assigned = count; // Number of assigned parameters.
		final int required = this.model.t_way - 1;
		
		final int[] fp = new int[assigned];	// Assigned parameter indexes except par.
		final int[] fv = new int[assigned];	// Assigned parameter values except par.
		int j = 0;
		int k = 0;
		while (j < this.model.parameter) {
			if (new_test[j] != -1 && j != par) {
				fp[k] = j;
				fv[k++] = new_test[j];
			}
			++j;
		}
		final int[] pp = { par };
		final int[] vv = { val };
		for (final int[] each : ALG.allCombination(assigned, required)) {
			final int[] pos = new int[required];
			final int[] sch = new int[required];
			for (int l = 0; l < required; ++l) {
				pos[l] = fp[each[l]];
				sch[l] = fv[each[l]];
			}
			final int[] position = new int[this.model.t_way];
			final int[] schema = new int[this.model.t_way];
			mergeArray(pos, sch, pp, vv, position, schema);
			if (!this.model.covered(position, schema, 0)) {
				++fit;
			}
		}
		return fit;
	}

	private static void mergeArray(final int[] p1, final int[] v1, final int[] p2, final int[] v2, final int[] pos,
			final int[] sch) {
		int i = 0;
		int j = 0;
		int k = 0;
		while (i < p1.length && j < p2.length) {
			if (p1[i] < p2[j]) {
				pos[k] = p1[i];
				sch[k++] = v1[i++];
			} else {
				pos[k] = p2[j];
				sch[k++] = v2[j++];
			}
		}
		if (i < p1.length) {
			while (i < p1.length) {
				pos[k] = p1[i];
				sch[k] = v1[i];
				++i;
				++k;
			}
		}
		if (j < p2.length) {
			while (j < p2.length) {
				pos[k] = p2[j];
				sch[k] = v2[j];
				++j;
				++k;
			}
		}
	}

	private class Pair implements Comparable<Pair> {
		public int index;
		public int number;

		private Pair(final int i, final int n) {
			this.index = i;
			this.number = n;
		}

		@Override
		public int compareTo(final Pair B) {
			return -Integer.compare(this.number, B.number);
		}

		@Override
		public String toString() {
			return String.valueOf(this.index) + " (" + String.valueOf(this.number) + ")";
		}
	}
}
