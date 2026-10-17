package io.github.blockforge.database.exceptions;

import java.io.Serial;

public class RepositoryOperationException extends PersistenceContextException {

    @Serial
    private static final long serialVersionUID = 1L;

    public RepositoryOperationException(String message) {
        super(message);
    }

    public RepositoryOperationException(String message, Throwable cause) {
        super(message, cause);
    }

    public RepositoryOperationException(Throwable cause) {
        super(cause);
    }
}