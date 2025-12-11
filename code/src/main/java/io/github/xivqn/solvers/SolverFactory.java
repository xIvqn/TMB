package io.github.xivqn.solvers;

import io.github.xivqn.exceptions.ImportException;
import io.github.xivqn.exceptions.ModelException;
import io.github.xivqn.exceptions.SolverException;
import io.github.xivqn.entities.Instance;
import io.github.xivqn.utils.ArgsUtils;

public class SolverFactory {

    /**
     * Returns a new TMB solver of the given type
     * @param type The type of solver to create
     * @param instance The instance to solve
     * @return A new TMB solver
     * @throws SolverException If the solver type is unknown
     */
    public static Solver getSolver(SolverTypes type, Instance instance, double gamma) throws SolverException, ModelException, ImportException {
        switch (type) {
            case RANDOM:
                return new RandomSolver(instance, gamma);
            case DEGREE:
                return new DegreeSolver(instance, gamma);
            case GREEDY:
                return new GreedySolver(instance, gamma);
            case PAGERANK:
                return new PageRankSolver(instance, gamma);
            case STMB:
                return STMBFactory.getSolver(instance, gamma);
            case GRASP:
                return new GRASPSolver(instance, gamma);
            default:
                throw new SolverException("Unknown solver type");
        }
    }

    public static String getSolverDescription(SolverTypes type) throws SolverException {
        switch (type) {
            case RANDOM:
            case DEGREE:
            case GREEDY:
            case PAGERANK:
            case STMB:
                return "";
            case GRASP:
                return String.format("_a=%s_it=%s_tf=%s", ArgsUtils.getAlpha(), ArgsUtils.getGRASPIterations(), ArgsUtils.getTenureFactor());
            default:
                throw new SolverException("Unknown solver type");
        }
    }

}
