package io.github.xivqn.exporters;

import io.github.xivqn.exceptions.ExportException;
import io.github.xivqn.solvers.SolverTypes;

import java.util.ArrayList;
import java.util.List;

public class ExporterFactory {

    /**
     * Returns an instance importer for the given path.
     *
     * @param filename the path of the file to export
     * @return an instance importer for the given path
     * @throws ExportException if the file format is unknown
     */
    public static Exporter getExporter(String filename, SolverTypes solverType) throws ExportException {
        int dotIndex = filename.lastIndexOf('.');
        String newFilename = dotIndex != -1 ?
                filename.substring(0, dotIndex) + "_" + solverType + filename.substring(dotIndex) :
                filename + "_" + solverType;

        String extension = newFilename.substring(newFilename.lastIndexOf('.') + 1);

        switch (extension) {
            case "csv":
                return new CsvExporter(newFilename);
            case "xlsx":
            case "xls":
                return new ExcelExporter(newFilename);
            default:
                throw new IllegalArgumentException("Unknown file format");
        }
    }

}
