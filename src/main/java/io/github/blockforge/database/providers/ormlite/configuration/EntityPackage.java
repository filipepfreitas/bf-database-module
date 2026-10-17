package io.github.blockforge.database.providers.ormlite.configuration;

/**
 * Representa uma configuração de escaneamento por pacote.
 *
 * <p>Cada instância corresponde a um elemento
 * {@code <package>} dentro de {@code <entity-config>}.</p>
 */
public class EntityPackage {

    private final String value;

    /**
     * Cria uma configuração de pacote.
     *
     * @param value nome do pacote ou valor especial de configuração
     */
    public EntityPackage(String value) {
        this.value = value;
    }

    /**
     * Retorna o valor do pacote.
     *
     * @return nome do pacote
     */
    public String getValue() {
        return value;
    }

    /**
     * Verifica se este objeto representa o pacote especial
     * {@code default}.
     *
     * @return {@code true} caso seja o pacote padrão
     */
    public boolean isDefault() {
        return "default".equals(value);
    }

    @Override
    public String toString() {
        return "EntityPackage{" +
                "value='" + value + '\'' +
                '}';
    }
}