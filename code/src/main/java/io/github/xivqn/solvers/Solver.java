package io.github.xivqn.solvers;

import io.github.xivqn.exceptions.SolverException;
import io.github.xivqn.entities.Solution;

public interface Solver {

    /**
     * Solves the TMB problem for the given graph
     * @return The solution to the TMB problem
     */
    Solution solve() throws SolverException;

}
