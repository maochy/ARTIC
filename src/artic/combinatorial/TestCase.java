package artic.combinatorial;

import java.util.*;

public class TestCase
{
    public final int[] test;
    public TestResult result;
    public int cnt = 0;
    
    public TestCase(final int[] t, final TestResult r) {
        this.test = t.clone();
        this.result = r;
    }
    public TestCase(TestCase tc){
        this.test = Arrays.copyOf(tc.test, tc.test.length);
        this.result = tc.result;
    }
    
    public TestCase(final int[] t) {
        this.test = t.clone();
        this.result = TestResult.UNKNOWN;
    }
    
    @Override
    public boolean equals(final Object other) {
        if (other == null || !(other instanceof TestCase)) {
            return false;
        }
        if (other == this) {
            return true;
        }
        final TestCase tc = (TestCase)other;
        return Arrays.equals(this.test, tc.test);
    }
    
    @Override
    public String toString() {
        return Arrays.toString(this.test);
    }
}
