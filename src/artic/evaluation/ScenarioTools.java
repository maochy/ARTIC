package artic.evaluation;

import artic.common.ProjectResource;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Scanner;

/**
 * Scenario file reader.
 */
final class ScenarioTools {
    private ScenarioTools() {
    }

    /** Loads all scenario definitions for one subject. */
    static List<ScenarioFault> loadScenarioFaults(String subjectName) throws IOException {
        List<ScenarioFault> faults = new ArrayList<>();
        try (Scanner scanner = new Scanner(ProjectResource.openStream("scenario/" + subjectName + ".scenario"))) {
            if (!scanner.hasNextLine()) {
                return faults;
            }

            scanner.nextLine();
            if (!scanner.hasNextLine()) {
                return faults;
            }

            String rateLine = scanner.nextLine();
            String rawRates = rateLine.substring(5).replaceAll("\\[|\\]|\\s", "");
            String[] rateParts = rawRates.split(",");
            for (String ratePart : rateParts) {
                if (!scanner.hasNextLine()) {
                    break;
                }
                String rawFaults = scanner.nextLine().replaceAll("\\[|\\]|\\s", "");
                String[] faultIds = rawFaults.isEmpty() ? new String[0] : rawFaults.split(",");
                faults.add(new ScenarioFault(faultIds, Double.parseDouble(ratePart)));
            }
        }
        return faults;
    }

    /** Fault set and failure rate of one scenario. */
    static final class ScenarioFault {
        final List<String> faults;
        final double rate;

        ScenarioFault(String[] faults, double rate) {
            this.faults = Arrays.asList(faults.clone());
            this.rate = rate;
        }
    }
}
