package io.github.xivqn.exceptions;

/**
 * Represents an exception thrown during the export of a solution.
 */
public class ExportException extends Exception {

    public ExportException(String message) {
        super(message);
    }

    public ExportException(String message, Throwable cause) {
        super(message, cause);
    }
}
