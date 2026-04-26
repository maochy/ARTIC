package artic.evaluation;

import artic.combinatorial.CTModel;
import artic.combinatorial.TestCase;
import artic.combinatorial.TestSuite;
import artic.common.Case;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Utility for generating, reading, and completing TS cache files.
 */
final class SuiteRepository {
    private static final Pattern TEST_CASE_PATTERN =
            Pattern.compile("\\[(\\s*\\d+\\s*(?:,\\s*\\d+\\s*)*)]");

    private SuiteRepository() {
    }

    /** Prepares TS files used by metric evaluation. */
    static void ensureSuites(String tsRoot, boolean regenerateTS, Class<?> caseClass,
                             Class<?>[] algorithms, String subjectName, int para,
                             int strength, int repeat) throws Exception {
        for (Class<?> algorithm : algorithms) {
            File file = suiteFile(tsRoot, strength, algorithm, subjectName, para);
            if (regenerateTS) {
                resetFile(file);
            }

            int existing = countSuites(file);
            if (existing >= repeat) {
                continue;
            }

            Case subject = EvaluationUtils.createCase(caseClass, subjectName);
            File parent = file.getParentFile();
            if (parent != null && !parent.exists()) {
                parent.mkdirs();
            }
            try (BufferedWriter writer = new BufferedWriter(new FileWriter(file, true))) {
                for (int i = existing; i < repeat; i++) {
                    CTModel model = EvaluationUtils.buildModel(subject, para, strength, subjectName);
                    TestSuite ts = EvaluationUtils.generateSuite(algorithm, model);
                    writer.write(Arrays.toString(ts.suite.toArray()));
                    writer.newLine();
                }
                writer.flush();
            }
        }
    }

    /** Opens a TS round reader. */
    static SuiteRoundReader openRoundReader(String tsRoot, Class<?>[] algorithms,
                                            String subjectName, int para, int strength) throws IOException {
        return new SuiteRoundReader(tsRoot, algorithms, subjectName, para, strength);
    }

    /** Computes the truncation size used by the current evaluation. */
    static int computeGlobalMinSize(String tsRoot, Class<?>[] algorithms, String subjectName,
                                    int para, int strength, int repeat) throws IOException {
        int minSize = Integer.MAX_VALUE;
        for (Class<?> algorithm : algorithms) {
            File file = suiteFile(tsRoot, strength, algorithm, subjectName, para);
            if (!file.exists()) {
                throw new IOException("Missing TS file: " + file.getPath());
            }
            try (BufferedReader reader = new BufferedReader(new FileReader(file))) {
                String line;
                int count = 0;
                while ((line = reader.readLine()) != null && count < repeat) {
                    if (line.trim().isEmpty()) {
                        continue;
                    }
                    ArrayList<TestCase> suite = parseSuite(line, para);
                    minSize = Math.min(minSize, suite.size());
                    count++;
                }
                if (count < repeat) {
                    throw new IOException("Insufficient TS lines for " + algorithm.getSimpleName());
                }
            }
        }
        return minSize == Integer.MAX_VALUE ? 0 : minSize;
    }

    /** Returns the main TS file path. */
    private static File suiteFile(String tsRoot, int strength, Class<?> algorithm,
                                  String subjectName, int para) {
        return new File(tsRoot + "/" + strength + "/" + EvaluationUtils.algorithmName(algorithm)
                + "/" + subjectName + "/" + para + "_0.0_0.txt");
    }

    /** Resets a TS file. */
    private static void resetFile(File file) throws IOException {
        File parent = file.getParentFile();
        if (parent != null && !parent.exists()) {
            parent.mkdirs();
        }
        try (BufferedWriter writer = new BufferedWriter(new FileWriter(file, false))) {
            writer.write("");
            writer.flush();
        }
    }

    /** Counts existing TS rounds. */
    private static int countSuites(File file) throws IOException {
        if (!file.exists()) {
            return 0;
        }
        int count = 0;
        try (BufferedReader reader = new BufferedReader(new FileReader(file))) {
            String line;
            while ((line = reader.readLine()) != null) {
                if (!line.trim().isEmpty()) {
                    count++;
                }
            }
        }
        return count;
    }

    /** Parses one TS text line. */
    private static ArrayList<TestCase> parseSuite(String input, int para) {
        ArrayList<TestCase> result = new ArrayList<>();
        String content = input.substring(1, input.length() - 1).replaceAll("\\s", "");
        Matcher matcher = TEST_CASE_PATTERN.matcher(content);
        while (matcher.find()) {
            String[] nums = matcher.group(1).split(",");
            int[] row = new int[nums.length];
            for (int i = 0; i < nums.length; i++) {
                row[i] = Integer.parseInt(nums[i]);
            }
            if (row.length != para) {
                throw new IllegalStateException(
                        "Unexpected test case width " + row.length + ", expected " + para);
            }
            result.add(new TestCase(row));
        }
        return result;
    }

    /** Reads the next non-empty test suite line. */
    private static String readNextSuiteLine(BufferedReader reader) throws IOException {
        String line;
        while ((line = reader.readLine()) != null) {
            if (!line.trim().isEmpty()) {
                return line;
            }
        }
        return null;
    }

    /** Reader for aligned TS rounds. */
    static final class SuiteRoundReader implements AutoCloseable {
        private final Class<?>[] algorithms;
        private final BufferedReader[] readers;

        SuiteRoundReader(String tsRoot, Class<?>[] algorithms, String subjectName,
                         int para, int strength) throws IOException {
            this.algorithms = algorithms;
            this.readers = new BufferedReader[algorithms.length];
            for (int i = 0; i < algorithms.length; i++) {
                File file = suiteFile(tsRoot, strength, algorithms[i], subjectName, para);
                if (!file.exists()) {
                    throw new IOException("Missing TS file: " + file.getPath());
                }
                this.readers[i] = new BufferedReader(new FileReader(file));
            }
        }

        /** Reads the next TS round. */
        SuiteRound nextRound(int para) throws IOException {
            @SuppressWarnings("unchecked")
            ArrayList<TestCase>[] suites = new ArrayList[algorithms.length];
            for (int i = 0; i < algorithms.length; i++) {
                String line = readNextSuiteLine(readers[i]);
                if (line == null) {
                    throw new IOException("Insufficient TS lines for " + algorithms[i].getSimpleName());
                }
                suites[i] = parseSuite(line, para);
            }
            return new SuiteRound(suites);
        }

        @Override
        public void close() throws IOException {
            IOException failure = null;
            for (BufferedReader reader : readers) {
                try {
                    reader.close();
                } catch (IOException e) {
                    failure = e;
                }
            }
            if (failure != null) {
                throw failure;
            }
        }
    }

    /** Result for one aligned round. */
    static final class SuiteRound {
        final ArrayList<TestCase>[] suites;

        SuiteRound(ArrayList<TestCase>[] suites) {
            this.suites = suites;
        }
    }
}
