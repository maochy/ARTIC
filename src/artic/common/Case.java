package artic.common;

import java.util.*;
import artic.combinatorial.*;

public interface Case
{
	
    int getCompleteParameter();
    
    ArrayList<int[]> getCompleteConstraint();
    
    List<String> getFaultList();
    
    CTModel getCompleteModel(final int p0);
    
    CTModel getSubModel(final int p0, final double p1, final int p2);
    
    int[] computeFaultMatrix(final ArrayList<TestCase> p0);

	int[] computeScenarioFaultMatrix(ArrayList<TestCase> test);

    int computeEM(ArrayList<TestCase> test);
	
	boolean detected(TestCase tc,CTModel model);
	
	void setFaultsList(List<String> faults,double rate);
	
	double getRate();

    default Set<String> getFaultDetected(TestCase tc){
        return null;
    }
	
}
