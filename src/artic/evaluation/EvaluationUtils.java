package artic.evaluation;

import artic.combinatorial.CTModel;
import artic.combinatorial.TestSuite;
import artic.common.Case;
import artic.common.Generation;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.List;
import java.util.Locale;

/**
 * Common helper methods for evaluation entries.
 */
final class EvaluationUtils {
    private EvaluationUtils() {
    }

    /** Creates a subject instance by name. */
    static Case createCase(Class<?> caseClass, String name) throws Exception {
        return (Case) caseClass.getConstructor(String.class).newInstance(name);
    }

    /** Builds a CTModel for the subject, strength, and parameter count. */
    static CTModel buildModel(Case subject, int para, int strength, String name) {
        CTModel model = subject.getSubModel(para, 0, strength);
        model.name = name;
        return model;
    }

    /** Creates an algorithm instance by reflection. */
    static Generation createGeneration(Class<?> algorithmClass) throws Exception {
        return (Generation) algorithmClass.getConstructor().newInstance();
    }

    /** Normalizes the algorithm name used in output directories. */
    static String algorithmName(Class<?> algorithmClass) {
        return algorithmClass.getSimpleName().toLowerCase(Locale.ROOT);
    }

    /** Generates a complete test suite. */
    static TestSuite generateSuite(Class<?> algorithmClass, CTModel model) throws Exception {
        Generation generation = createGeneration(algorithmClass);
        TestSuite ts = new TestSuite();
        generation.generation(model, ts);
        return ts;
    }

    /** Generates a test suite until a failure is detected. */
    static TestSuite generateSuite(Class<?> algorithmClass, CTModel model, Case scenario) throws Exception {
        Generation generation = createGeneration(algorithmClass);
        TestSuite ts = new TestSuite();
        generation.generation(model, ts, scenario);
        return ts;
    }

    /** Generates a complete test suite and records generation time. */
    static TimedSuite generateSuiteWithTime(Class<?> algorithmClass, CTModel model) throws Exception {
        Generation generation = createGeneration(algorithmClass);
        TestSuite ts = new TestSuite();
        long startTime = System.nanoTime();
        generation.generation(model, ts);
        long elapsed = System.nanoTime() - startTime;
        ts.time = elapsed;
        return new TimedSuite(ts, elapsed);
    }

    /** Generates a test suite until failure detection and records generation time. */
    static TimedSuite generateSuiteWithTime(Class<?> algorithmClass, CTModel model, Case scenario) throws Exception {
        Generation generation = createGeneration(algorithmClass);
        TestSuite ts = new TestSuite();
        long startTime = System.nanoTime();
        generation.generation(model, ts, scenario);
        long elapsed = System.nanoTime() - startTime;
        ts.time = elapsed;
        return new TimedSuite(ts, elapsed);
    }

    /** Appends one metric value to its result file. */
    static void appendMetric(String rootDir, int strength, Class<?> algorithmClass, String subjectName,
                             int para, int scenarioIndex, Object value) throws IOException {
        File file;
        String algorithmName = algorithmName(algorithmClass);
        if (scenarioIndex < 0) {
            file = new File(rootDir + "/" + strength + "/" + algorithmName + "/" + subjectName + "/" + para + "_0.0.txt");
        } else {
            file = new File(rootDir + "/" + strength + "/" + algorithmName + "/" + subjectName + "/" + para + "_0.0_" + scenarioIndex + ".txt");
        }
        if (!file.getParentFile().exists()) {
            file.getParentFile().mkdirs();
        }
        try (FileWriter writer = new FileWriter(file, true)) {
            writer.write(value + "\r\n");
            writer.flush();
        }
    }

    /** Appends one line to a given file. */
    static void appendLine(String fileName, Object value) throws IOException {
        File file = new File(fileName);
        if (!file.getParentFile().exists()) {
            file.getParentFile().mkdirs();
        }
        try (FileWriter writer = new FileWriter(file, true)) {
            writer.write(value + "\r\n");
            writer.flush();
        }
    }

    /** Computes the average of metric values. */
    static double average(List<? extends Number> values) {
        if (values.isEmpty()) {
            return 0.0;
        }
        double sum = 0.0;
        for (Number value : values) {
            sum += value.doubleValue();
        }
        return sum / values.size();
    }

    /** Holds a generated test suite and its measured time. */
    static final class TimedSuite {
        final TestSuite suite;
        final long time;

        TimedSuite(TestSuite suite, long time) {
            this.suite = suite;
            this.time = time;
        }
    }
}
