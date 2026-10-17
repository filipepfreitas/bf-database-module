package io.github.blockforge.database.exceptions;

import java.io.Serial;

/**
 * Exceção lançada quando um arquivo de configuração
 * de persistência é inválido.
 */
public class ConfigurationException extends PersistenceContextException {

    @Serial
    private static final long serialVersionUID = 1L;

    public ConfigurationException(String message) {
        super(message);
    }

    public ConfigurationException(String message, Throwable cause) {
        super(message, cause);
    }

    public ConfigurationException(Throwable cause) {
        super(cause);
    }
}