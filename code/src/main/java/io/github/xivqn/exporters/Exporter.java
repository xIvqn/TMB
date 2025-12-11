package io.github.xivqn.exporters;

import io.github.xivqn.exceptions.ExportException;
import io.github.xivqn.entities.Solution;

public interface Exporter {

        /**
         * Returns the path where the solution will be exported.
         *
         * @return The path where the solution will be exported.
         */
        String getPath();

        /**
         * Initializes the exporter.
         *
         * @throws ExportException If an error occurs during the initialization.
         */
        void initialize() throws ExportException;

        /**
         * Exports a solution.
         *
         * @param solution The solution to be exported.
         * @throws ExportException If an error occurs during the export.
         */
        void exportSolution(Solution solution) throws ExportException;

}
