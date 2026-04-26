package artic.combinatorial;

import java.util.*;

public class TestSuite
{
    public ArrayList<TestCase> suite;
    public long time;
    
    public TestSuite() {
        this.suite = new ArrayList<TestCase>();
    }
    
    public int getTestSuiteSize() {
        return this.suite.size();
    }
    
    public long getTestSuiteTime() {
        return this.time;
    }
}
