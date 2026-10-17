
package io.github.blockforge.database.exceptions;

import java.io.Serial;

public class PersistenceContextException extends RuntimeException {

    @Serial
    private static final long serialVersionUID = 1L;

    public PersistenceContextException(String message) {
        super("BlockForge Database says: " + message);
    }

    public PersistenceContextException(String message, Throwable cause) {
        super("BlockForge Database says: " + message, cause);
    }

    public PersistenceContextException(Throwable cause) {
        super("BlockForge Database says: An unexpected persistence context error occurred.",
                cause);
    }
}
