package io.github.blockforge.database.exceptions;

import java.io.Serial;

public class MissingXmlAttributeException extends PersistenceContextException {

    @Serial
    private static final long serialVersionUID = 1L;

    public MissingXmlAttributeException(String message) {
        super(message);
    }

    public MissingXmlAttributeException(String message, Throwable cause) {
        super(message, cause);
    }

    public MissingXmlAttributeException(Throwable cause) {
        super(cause);
    }
}