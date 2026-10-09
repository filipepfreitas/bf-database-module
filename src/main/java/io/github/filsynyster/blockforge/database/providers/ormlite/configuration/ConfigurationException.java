package io.github.filsynyster.blockforge.database.providers.ormlite.configuration;

/**
 * Exceção lançada quando um arquivo de configuração
 * de persistência é inválido.
 */
public class ConfigurationException extends RuntimeException {

    public ConfigurationException(String message) {
        super(message);
    }

    public ConfigurationException(
            String message,
            Throwable cause
    ) {
        super(message, cause);
    }
}