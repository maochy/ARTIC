package artic.handler;

import artic.combinatorial.*;

public interface ValidityChecker
{
    void init(final CTModel p0);
    
    /**
     * Checks whether a test case satisfies all constraints.
     * @param test test case
     * @return
     */
    boolean isValid(final int[] test);
    
    /**
     * Checks whether a partial tuple satisfies all constraints.
     * @param position parameter indexes
     * @param schema value indexes
     * @return
     */
    boolean isValid(final int[] position, final int[] schema);
}
