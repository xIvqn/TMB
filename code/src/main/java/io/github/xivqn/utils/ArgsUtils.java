package io.github.xivqn.utils;

import io.github.xivqn.diffmodels.DiffusionModelTypes;
import io.github.xivqn.solvers.SolverTypes;
import net.sourceforge.argparse4j.ArgumentParsers;
import net.sourceforge.argparse4j.impl.Arguments;
import net.sourceforge.argparse4j.inf.ArgumentParser;
import net.sourceforge.argparse4j.inf.ArgumentParserException;
import net.sourceforge.argparse4j.inf.Namespace;
import ch.qos.logback.classic.Level;
import ch.qos.logback.classic.Logger;
import org.slf4j.LoggerFactory;

import java.util.Arrays;
import java.util.List;

public class ArgsUtils {

    private static String instancesDirectory;
    private static String outputFile;
    private static SolverTypes solverType;
    private static boolean exportNodes;

    private static int simulations;
    private static boolean cacheRandom;
    private static boolean cacheTrees;
    private static final String TREE_STORE_PARENT_DIR = "sampleTrees/";
    private static double[] gammas;

    private static double alpha;
    private static int maxGRASPIterations;
    private static double tenureFactor;
    private static int numSamples;
    private static int timeLimit;
    private static int lsTimeLimit;

    private static boolean parallelEvaluation;
    private static DiffusionModelTypes diffusionModelType;
    private static double p;

    /**
     * Builds the arguments from the given array
     * @param args The arguments to build
     */
    public static void buildArgs(String[] args) {
        ArgumentParser parser = ArgumentParsers.newFor("TMB").build()
                .defaultHelp(true)
                .description("Execute different TMB solvers in social network instances.");

        addArgsToParser(parser);

        Namespace ns = null;
        try {
            ns = parser.parseArgs(args);
        } catch (ArgumentParserException e) {
            parser.handleError(e);
            System.exit(1);
        }

        setAttributes(ns);
    }


    /**
     * Adds the arguments to the given parser
     * @param parser The parser to add the arguments
     */
    private static void addArgsToParser(ArgumentParser parser) {
        parser.addArgument("-L", "--log-level").setDefault(String.valueOf(Level.INFO))
                .choices(Arrays.stream(org.slf4j.event.Level.values()).map(Enum::name).toArray(String[]::new))
                .help("Specify the log level to use, values must be one of: TRACE, DEBUG, INFO, WARN, ERROR and the default is INFO");
        parser.addArgument("-i", "--instances-directory")
                .required(true)
                .help("Directory path containing the social network instances");
        parser.addArgument("-o", "--output-file")
                .required(true)
                .help("File path to export the results, including the file extension. If 'null' is specified, the results will not be exported.");
        parser.addArgument("-s", "--solver")
                .required(true)
                .choices(SolverTypes.stringValues())
                .help("Specify solver type to use");
        parser.addArgument("-e", "--export-nodes")
                .action(Arguments.storeTrue())
                .help("Whether to export nodes in the solution");
        parser.addArgument("-mc", "--monte-carlo-simulations")
                .setDefault(10_000)
                .type(Integer.class)
                .help("Specify the number of Monte-Carlo simulations to perform");
        parser.addArgument("-y", "--gammas")
                .nargs("+")
                .type(Double.class)
                .setDefault(0.02, 0.04, 0.06, 0.08, 0.10)
                .help("Specify gamma values to use, indicating the percentage of the desired reduction of influence in the network.");
        parser.addArgument("-m", "--diffusion-model")
                .required(true)
                .choices(DiffusionModelTypes.stringValues())
                .help("Specify diffusion model type to use.");
        parser.addArgument("-p", "--p-value")
                .type(Double.class)
                .setDefault(0.1)
                .help("Specify p value to use in the IC diffusion model.");
        parser.addArgument("-P", "--parallel-evaluation")
                .action(Arguments.storeTrue())
                .help("Whether to parallelize the evaluation of the solutions.");
        parser.addArgument("--cache-random")
                .action(Arguments.storeTrue())
                .help("Whether to cache the random instances for seeds.");
        parser.addArgument("--cache-trees")
                .action(Arguments.storeTrue())
                .help("Whether to cache the tree instances for the STMB Solver.");
        parser.addArgument("-a", "--alpha")
                .type(Double.class)
                .setDefault(0.2)
                .help("Specify alpha values to use, indicating diversity of the GRASP construction being alpha=0 fully greedy and alpha=1 fully random. A special case is alpha=-1 which indicates a random alpha each time it is needed, which might require using the argument as -a \" -1.0\".");
        parser.addArgument("-g", "--grasp-iterations")
                .type(Integer.class)
                .setDefault(10)
                .help("Specify the number of GRASP iterations to perform.");
        parser.addArgument("-tf", "--tenure-factor")
                .type(Double.class)
                .setDefault(0.0)
                .help("Specify the size of the tenure factor for the Tabu list as a percentage of the solution size, with values between 0 and 1. The default is 0, indicating no Tabu list.");
        parser.addArgument("-n", "--num-samples")
                .type(Integer.class)
                .setDefault(500)
                .help("Specify the number of tree samples to generate from a sample tree.");
        parser.addArgument("-t", "--time-limit")
                .type(Integer.class)
                .setDefault(18_000)
                .help("Specify the time limit in seconds for the solver.");
        parser.addArgument("-lst", "--ls-time-limit")
                .type(Integer.class)
                .setDefault(3600)
                .help("Specify the time limit in seconds for the Local Search execution.");
    }

    /**
     * Sets the attributes from the given namespace
     * @param ns The namespace to set the attributes
     */
    private static void setAttributes(Namespace ns) {
        Logger root = (Logger) LoggerFactory.getLogger(org.slf4j.Logger.ROOT_LOGGER_NAME);
        root.setLevel(Level.valueOf(ns.getString("log_level")));

        instancesDirectory = escape(ns.getString("instances_directory"));
        outputFile = escape(ns.getString("output_file"));
        solverType = SolverTypes.valueOf(ns.getString("solver"));
        exportNodes = ns.getBoolean("export_nodes");
        simulations = ns.getInt("monte_carlo_simulations");
        cacheRandom = ns.getBoolean("cache_random");
        cacheTrees = ns.getBoolean("cache_trees");

        List<Double> gammaList = ns.getList("gammas");
        gammas = new double[gammaList.size()];
        for (int i = 0; i < gammaList.size(); i++) {
            gammas[i] = gammaList.get(i);
        }

        alpha = ns.getDouble("alpha");
        maxGRASPIterations = ns.getInt("grasp_iterations");
        tenureFactor = ns.getDouble("tenure_factor");
        numSamples = ns.getInt("num_samples");
        timeLimit = ns.getInt("time_limit");
        lsTimeLimit = ns.getInt("ls_time_limit");

        parallelEvaluation = ns.getBoolean("parallel_evaluation");
        diffusionModelType = DiffusionModelTypes.valueOf(ns.getString("diffusion_model"));
        p = ns.getDouble("p_value");
    }

    /**
     * Escapes the given string
     * @param str The string to escape
     * @return The escaped string
     */
    private static String escape(String str) {
        return str.replace("\"", "").replace("'", "");
    }

    public static boolean isExportNodes() {
        return exportNodes;
    }

    public static SolverTypes getSolverType() {
        return solverType;
    }

    public static String getInstancesDirectory() {
        return instancesDirectory;
    }

    public static String getOutputFile() {
        return outputFile;
    }

    public static double[] getGammas() {
        return gammas;
    }

    public static int getSimulations() {
        return simulations;
    }

    public static boolean isCacheRandom() {
        return cacheRandom;
    }
    public static String getTreeStoreParentDir() {
        return TREE_STORE_PARENT_DIR;
    }

    public static boolean isCacheTrees() {
        return cacheTrees;
    }

    public static double getAlpha() {
        return alpha;
    }

    public static int getGRASPIterations() {
        return maxGRASPIterations;
    }

    public static double getTenureFactor() {
        return tenureFactor;
    }

    public static int getNumSamples() {
        return numSamples;
    }

    public static int getTimeLimit() {
        return timeLimit;
    }

    public static int getLsTimeLimit() {
        return lsTimeLimit;
    }

    public static boolean isParallelEvaluation() {
        return parallelEvaluation;
    }

    public static DiffusionModelTypes getDiffusionModelType() {
        return diffusionModelType;
    }

    public static double getP() {
        return p;
    }
}
