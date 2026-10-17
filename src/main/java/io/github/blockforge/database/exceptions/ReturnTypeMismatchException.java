package io.github.blockforge.database.exceptions;

import java.io.Serial;

public class ReturnTypeMismatchException extends PersistenceContextException {

    @Serial
    private static final long serialVersionUID = 1L;

    public ReturnTypeMismatchException(String message) {
        super(message);
    }

    public ReturnTypeMismatchException(String message, Throwable cause) {
        super(message, cause);
    }

    public ReturnTypeMismatchException(Throwable cause) {
        super(cause);
    }
}