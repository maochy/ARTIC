package artic.evaluation;

import artic.combinatorial.TestCase;

import java.util.ArrayList;

/**
 * Test suite truncation utility.
 */
final class SuiteTools {
    private SuiteTools() {
    }

    /** Truncates a test suite to the given limit. */
    static ArrayList<TestCase> truncate(ArrayList<TestCase> suite, int limit) {
        int upper = Math.min(limit, suite.size());
        return new ArrayList<>(suite.subList(0, upper));
    }
}
