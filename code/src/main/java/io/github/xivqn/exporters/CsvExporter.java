package io.github.xivqn.exporters;

import io.github.xivqn.exceptions.ExportException;
import io.github.xivqn.entities.Solution;
import io.github.xivqn.utils.ArgsUtils;
import io.github.xivqn.utils.TimeUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.stream.Collectors;

public class CsvExporter implements Exporter {

    private static final Logger logger = LoggerFactory.getLogger(CsvExporter.class);

    private final String path;

    /**
     * Create a new CSV exporter.
     *
     * @param path the path to the output CSV file
     */
    public CsvExporter(String path) {
        this.path = path;

        logger.info("Exporting to {}.", path);
    }

    /**
     * Get the path to the output CSV file
     *
     * @return the path to the output CSV file
     */
    public String getPath() {
        return path;
    }

    /**
     * Initialize the output CSV file with the header
     *
     * @throws ExportException if an error occurs while creating the export file
     */
    public void initialize() throws ExportException {
        Path parent = Paths.get(path).getParent();
        if (parent != null) {
            try {
                Files.createDirectories(parent);
            } catch (IOException e) {
                throw new ExportException("Error creating parent directories", e);
            }
        }
        
        try (FileWriter csvWriter = new FileWriter(path)) {
            csvWriter.append("Name");
            csvWriter.append(";");
            csvWriter.append("Objective Value");
            csvWriter.append(";");
            csvWriter.append("Selection Size");
            csvWriter.append(";");
            csvWriter.append("Execution Time (s)");
            csvWriter.append(";");
            csvWriter.append("Execution Time");
            if (ArgsUtils.isExportNodes()) {
                csvWriter.append(";");
                csvWriter.append("Chosen Nodes");
            }
            csvWriter.append("\n");

            csvWriter.flush();
        } catch (IOException e) {
            throw new ExportException("Error creating export file", e);
        }
    }

    /**
     * Export a solution to the output CSV file
     *
     * @param solution the solution to be exported
     * @throws ExportException if an error occurs while exporting the solution
     */
    public void exportSolution(Solution solution) throws ExportException {
        try (FileWriter csvWriter = new FileWriter(path, true)) {
            csvWriter.append(solution.getName());
            csvWriter.append(";");
            csvWriter.append(String.format("%.2f", solution.getOf()));
            csvWriter.append(";");
            csvWriter.append(String.valueOf(solution.getSelectionSize()));
            csvWriter.append(";");
            csvWriter.append(String.format("%.2f", solution.getExecutionTime() / 1000.0));
            csvWriter.append(";");
            csvWriter.append(TimeUtils.formatTime(solution.getExecutionTime()));
            if (ArgsUtils.isExportNodes()) {
                csvWriter.append(";");
                csvWriter.append(solution.getSelection().stream().map(Object::toString).collect(Collectors.joining(" ")));
            }
            csvWriter.append("\n");

            csvWriter.flush();
        } catch (IOException e) {
            throw new ExportException("Error creating export file", e);
        }
    }

}
