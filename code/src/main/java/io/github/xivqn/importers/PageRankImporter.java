package io.github.xivqn.importers;

import io.github.xivqn.solvers.PageRankSolver;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;

public class PageRankImporter {

    private static final Logger log = LoggerFactory.getLogger(PageRankImporter.class);

    private static String instancesDirectory;

    /**
     * Loads a list of doubles from a file containing N doubles in a single line.
     * @param instanceName Name of the instance to read its pagerank information.
     * @return List<Double> containing the N doubles in order.
     * @throws IOException If file reading fails.
     * @throws NumberFormatException If any token is not a valid double.
     */
    public static List<Double> importPageRanks(String instanceName, int numNodes) throws IOException {
        List<Double> doubleList = new ArrayList<>(numNodes);

        // Read the first line
        Path filePath = Paths.get(instancesDirectory, instanceName + ".pagerank");
        String line = Files.readAllLines(filePath).get(0);

        // Split by whitespace
        String[] tokens = line.trim().split("\\s+");

        for (int i = 0; i < numNodes; i++) {
            doubleList.add(Double.parseDouble(tokens[i]));
        }

        log.info("Successfully imported pagerank for {} nodes in {}", numNodes, instanceName);

        return doubleList;
    }

    public static void setInstancesDirectory(String instancesDirectory) {
        PageRankImporter.instancesDirectory = instancesDirectory;
    }

    public static void setInstancesDirectory(Path instancesDirectory) {
        setInstancesDirectory(instancesDirectory.toString());
    }
}
