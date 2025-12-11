package io.github.xivqn.exceptions;

/**
 * Represents an exception thrown during the import of an instance.
 */
public class ImportException extends Exception {

    public ImportException(String message) {
        super(message);
    }

    public ImportException(String message, Throwable cause) {
        super(message, cause);
    }

}
