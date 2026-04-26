package artic.common;

import artic.combinatorial.CTModel;
import artic.combinatorial.TestSuite;
import artic.handler.MultipleArray;
import artic.matrix.ArrayMatrix;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * @author: Linlin Wen
 *
 * Test generation interface and shared redundancy-removal helpers.
 */
public interface Generation {
    public default void generation(CTModel model, TestSuite ts, Case scenario) {
    }

    public default Map<Integer,Long> generation(CTModel model, TestSuite ts, int[] size) {
		return null;
	}

	public default void generation(CTModel model, TestSuite ts) {
	}

	public default void removeRepeatTC(TestSuite ts, MultipleArray<Integer> paraVector,int[] paramIndex,ArrayList<int[]> pairs){
		ArrayList<Integer> rem = new ArrayList<>();
		for (int i = 0; i < ts.getTestSuiteSize(); i++) {
			boolean flag = false;
			int[] tc = ts.suite.get(i).test;
			for (int[] pair:pairs){
				if(paraVector.getObject(paramIndex,tc,pair) == 1){
					flag = true;
					break;
				}
			}
			if(!flag){
				rem.add(i);
				for (int[] pair:pairs){
					try {
						paraVector.getAndDescend(paramIndex,tc,pair);
					}
					catch (Exception e){
						e.printStackTrace();
					}
				}
			}
		}
		for (int i = rem.size()-1; i >= 0; i--) {
			ts.suite.remove((int)rem.get(i));
		}
	}

	public default void removeRepeatTC(TestSuite ts, List<MultipleArray> uncoverArray, ArrayList<int[]> pairs){
		ArrayList<Integer> rem = new ArrayList<>();
		for (int i = 0; i < ts.getTestSuiteSize(); i++) {
			boolean flag = false;
			int[] tc = ts.suite.get(i).test;
			int cnt = 0;
			for (int[] pair:pairs){
				if (uncoverArray.get(cnt).getValue(pair,tc) == 1){
					flag = true;
					break;
				}
				cnt ++;
			}
			if(!flag){
				rem.add(i);
				cnt = 0;
				for (int[] pair:pairs){
					try {
						uncoverArray.get(cnt).getValueDescend(pair,tc);
					}
					catch (Exception e){
						e.printStackTrace();
					}
					cnt ++;
				}
			}

		}
		for (int i = rem.size()-1; i >= 0; i--) {
			ts.suite.remove((int)rem.get(i));
		}
	}



	public default void removeRepeatTC(TestSuite ts, ArrayMatrix paraVector, int[] paramIndex, ArrayList<int[]> pairs){
		ArrayList<Integer> rem = new ArrayList<>();
		for (int i = 0; i < ts.getTestSuiteSize(); i++) {
			boolean flag = false;
			int[] tc = ts.suite.get(i).test;
			for (int[] pair:pairs){
				if(paraVector.get(pair,tc) == 1){
					flag = true;
					break;
				}
			}
			if(!flag){
				rem.add(i);
				for (int j = 0; j < pairs.size(); j++) {
					int[] pair = pairs.get(j);
					paraVector.getAndDecrease(pair,tc);
				}
			}
		}
		for (int i = rem.size()-1; i >= 0; i--) {
			ts.suite.remove((int)rem.get(i));
		}
	}
}
