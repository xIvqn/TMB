package io.github.xivqn.solvers;

import java.util.Arrays;

public enum SolverTypes {

    RANDOM,
    DEGREE,
    GREEDY,
    PAGERANK,
    STMB,
    GRASP,
    ;

    /**
     * Returns the string values of the enum
     * @return The string values of the enum
     */
    public static String[] stringValues() {
        return Arrays.stream(SolverTypes.values())
                .map(Enum::name)
                .toArray(String[]::new);
    }

}
