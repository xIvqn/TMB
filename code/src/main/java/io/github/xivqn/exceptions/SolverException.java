package io.github.xivqn.exceptions;

/**
 * Represents an exception thrown by the solver.
 */
public class SolverException extends Exception {

    public SolverException(String message) {
        super(message);
    }

    public SolverException(String message, Throwable cause) {
        super(message, cause);
    }

}
