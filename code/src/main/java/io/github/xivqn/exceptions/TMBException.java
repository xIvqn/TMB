package io.github.xivqn.exceptions;

public class TMBException extends RuntimeException {

    public TMBException(String message) {
        super(message);
    }

    public TMBException(String message, Throwable cause) {
        super(message, cause);
    }

}
