package io.github.xivqn.exporters;

import io.github.xivqn.exceptions.ExportException;
import io.github.xivqn.entities.Solution;
import io.github.xivqn.utils.ArgsUtils;
import io.github.xivqn.utils.TimeUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Objects;
import java.util.stream.Collectors;

public class ExcelExporter implements Exporter {

    private static final Logger logger = LoggerFactory.getLogger(ExcelExporter.class);

    private final String path;

    private final String templatePath;

    /**
     * Create a new CSV exporter.
     *
     * @param path the path to the output CSV file
     * @throws ExportException if an error occurs while creating the export file
     */
    public ExcelExporter(String path) throws ExportException {
        this.path = path;
        this.templatePath = "results.xlsx";

        logger.info("Exporting to {} using template {}.", path, templatePath.split("/")[templatePath.split("/").length - 1]);
    }

    /**
     * Get the path to the output CSV file
     *
     * @return the path to the output CSV file
     */
    @Override
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

        try {
            Files.copy(Objects.requireNonNull(ClassLoader.getSystemResourceAsStream(templatePath)), Paths.get(path), java.nio.file.StandardCopyOption.REPLACE_EXISTING);
        } catch (IOException e) {
            throw new ExportException("Error copying template to output file", e);
        } catch (NullPointerException e) {
            throw new ExportException("Template file not found", e);
        }

        try (ExcelData excelData = new ExcelData(path, "Results")) {
            excelData.appendRow(new String[]{"Name", "Objective Value", "Selection Size", "Execution Time (s)", "Execution Time"});
        } catch (Exception e) {
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
        try (ExcelData excelData = new ExcelData(path, "Results")) {
            Object[] row = new Object[]{
                    // Only admits String and Double
                    solution.getName(),
                    solution.getOf(),
                    (double) solution.getSelectionSize(),
                    (double) solution.getExecutionTime() / 1000.0,
                    TimeUtils.formatTime(solution.getExecutionTime()),
                    (ArgsUtils.isExportNodes()) ? solution.getSelection().stream().map(Object::toString).collect(Collectors.joining(" ")) : ""
            };

            excelData.appendRow(row);
        } catch (Exception e) {
            throw new ExportException("Error creating export file", e);
        }
    }
}
