package io.github.blockforge.database.exceptions;

import java.io.Serial;

public class RepositoryQueryNotFoundException extends PersistenceContextException {

    @Serial
    private static final long serialVersionUID = 1L;

    public RepositoryQueryNotFoundException(String message) {
        super(message);
    }

    public RepositoryQueryNotFoundException(String message, Throwable cause) {
        super(message, cause);
    }

    public RepositoryQueryNotFoundException(Throwable cause) {
        super(cause);
    }
}