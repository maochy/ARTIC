package artic.evaluation;

/**
 * Shared default settings for evaluation entries.
 */
final class EvaluationConfig {
    /** Default TS root directory. */
    static final String DEFAULT_TS_ROOT = "TS";
    /** Whether to force TS regeneration. */
    static final boolean DEFAULT_REGENERATE_TS = false;
    /** Default repeat count for SIR subjects. */
    static final int DEFAULT_SIR_REPEAT = 10000;
    /** Default repeat count for large subjects. */
    static final int DEFAULT_LARGE_REPEAT = 1000;
    /** Default interaction strengths. */
    static final int[] DEFAULT_STRENGTH = {2, 3, 4};

    /** Parameter counts of SIR subjects. */
    static final int[] SIR_PARAS = {
            9,
            9,
            14,
            11,
            10,
            7
    };

    /** SIR subject names. */
    static final String[] SIR_NAMES = {
            "flex",
            "grep",
            "gzip",
            "sed",
            "make",
            "nanoxml"
    };

    /** Parameter counts of large subjects. */
    static final int[] LARGE_PARAS = {
            47,
            68,
            104
    };

    /** Large subject names. */
    static final String[] LARGE_NAMES = {
            "drupal",
            "busybox",
            "linux"
    };

    private EvaluationConfig() {
    }
}
