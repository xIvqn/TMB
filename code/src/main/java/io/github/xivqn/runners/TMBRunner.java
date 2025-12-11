package io.github.xivqn.runners;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Comparator;
import java.util.stream.Stream;

import io.github.xivqn.diffmodels.DiffusionModelFactory;
import io.github.xivqn.exceptions.*;
import io.github.xivqn.exporters.Exporter;
import io.github.xivqn.exporters.ExporterFactory;
import io.github.xivqn.importers.PageRankImporter;
import io.github.xivqn.importers.TMBEdgesImporter;
import io.github.xivqn.entities.Instance;
import io.github.xivqn.entities.Solution;
import io.github.xivqn.solvers.Solver;
import io.github.xivqn.solvers.SolverFactory;
import io.github.xivqn.solvers.SolverTypes;
import io.github.xivqn.utils.ArgsUtils;
import io.github.xivqn.utils.TimeUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


public class TMBRunner {

    private static final Logger logger = LoggerFactory.getLogger(TMBRunner.class);

    private final String instancesDirectory;
    private final SolverTypes solverType;
    private final String outputFilePath;

    long startTime = System.currentTimeMillis();
    final int[] progress = {0, 0};

    /**
     * Create a new TMBRunner
     * @param outputFilePath The path to the output file
     * @throws ExportException if an error occurs while creating the output file
     */
    private TMBRunner(String instancesDirectory, String outputFilePath, SolverTypes solverType) throws SolverException {
        this.instancesDirectory = instancesDirectory;
        this.solverType = solverType;

        String extension = outputFilePath.substring(outputFilePath.lastIndexOf('.'));
        String description = DiffusionModelFactory.getModelDescription(ArgsUtils.getDiffusionModelType()) +
                SolverFactory.getSolverDescription(ArgsUtils.getSolverType());
        this.outputFilePath = outputFilePath.replace(extension, description + extension);
    }

    public static void process() throws ExportException, ImportException, SolverException {
        TMBRunner runner = new TMBRunner(
                ArgsUtils.getInstancesDirectory(),
                ArgsUtils.getOutputFile(),
                ArgsUtils.getSolverType()
        );

        for (double gamma : ArgsUtils.getGammas()) {
            runner.run(gamma);
        }
    }

    /**
     * Run TMB with all instances in the given directory
     */
    private void run(double gamma) throws ImportException {
        //  Initialize exporters
        Exporter exporter;
        try {
            String extension = outputFilePath.substring(outputFilePath.lastIndexOf('.'));
            String gammaOutFilePath = outputFilePath.replace(extension, "_y=" + gamma + extension);
            exporter = ExporterFactory.getExporter(gammaOutFilePath, solverType);
            exporter.initialize();
        } catch (ExportException e) {
            throw new TMBException("Output file could not be created", e);
        }

        startTime = System.currentTimeMillis();
        logger.info("Running {} with instances from {}", solverType, instancesDirectory);

        //  Count the number of instances in the directory
        Path instancesPath = Paths.get(instancesDirectory);
        if (Files.isDirectory(instancesPath)) {
            try (Stream<Path> files = Files.list(instancesPath)
                    .filter(path -> path.toString().endsWith(TMBEdgesImporter.getSupportedExt()))
            ) {
                progress[1] = (int) files.count();
            } catch (IOException e) {
                throw new TMBException("Instances directory not accessible", e);
            }
        } else {
            progress[1] = 1;
        }

        runSequential(gamma, exporter);
    }

    private void runSequential(double gamma, Exporter exporter) {
        logger.info("Running in sequential mode");
        //  Run TMB with all instances in the given directory
        Path instancesPath = Paths.get(instancesDirectory);
        if (Files.isDirectory(instancesPath)) {
            PageRankImporter.setInstancesDirectory(instancesPath);

            // Process the directory
            try (Stream<Path> filepath = Files.walk(instancesPath)
                    .filter(path -> path.toString().endsWith(TMBEdgesImporter.getSupportedExt()))
                    .sorted(Comparator.comparing(path -> path.getFileName().toString()))
            ) {
                filepath.forEach(path -> {
                    // Skip directories
                    if (path.toFile().isDirectory()) return;

                    processInstance(path, gamma, exporter);
                });
            } catch (IOException e) {
                throw new TMBException("Instances directory not accessible", e);
            }
        } else {
            PageRankImporter.setInstancesDirectory(instancesPath.getParent());
            // Process the single file
            processInstance(instancesPath, gamma, exporter);
        }

        logger.info("Finished running all instances in {}!", TimeUtils.formatTime(System.currentTimeMillis() - startTime));
    }

    private void processInstance(Path instancePath, double gamma, Exporter exporter) {
        try {
            Solution solution = runInstance(instancePath.toAbsolutePath(), gamma);
            exporter.exportSolution(solution);
            System.gc();
        } catch (IOException e) {
            logger.error("Error importing graph: {}", e.getMessage());
        } catch (InvalidSizeException e) {
            logger.warn("Skipping file {}: {}", instancePath, e.getMessage());
        } catch (SolverException | ExportException | ImportException | ModelException e) {
            logger.error("{} : {}", e.getMessage(), instancePath.getFileName().toString());
        }
    }

    /**
     * Run TMB with the given instance
     * @param path The path to the instance file
     * @return The name of the instance
     * @throws IOException if an error occurs while reading the instance file
     * @throws SolverException if an error occurs while solving the instance
     * @throws ExportException if an error occurs while exporting the solution
     * @throws ImportException if an error occurs while importing the instance
     */
    private Solution runInstance(Path path, double gamma) throws IOException, SolverException, ExportException, ImportException, ModelException {
        //  Import instance
        Instance instance = new TMBEdgesImporter(path).importInstance();

        logger.info("Processing instance {}", instance.getName());

        //  Solve instance
        Solver solver = SolverFactory.getSolver(solverType, instance, gamma);
        Solution solution = solver.solve();

        progress[0]++;
        logger.info("[{}\t/{}\t] Successfully processed instance {} in {}", progress[0], progress[1], solution.getName(), TimeUtils.formatTime(solution.getExecutionTime()));
        logger.info("Total elapsed time: {}\t| Memory usage: {} MB", TimeUtils.formatTime(System.currentTimeMillis() - startTime), (Runtime.getRuntime().totalMemory() - Runtime.getRuntime().freeMemory()) / 1024 / 1024);

        return solution;
    }

}
