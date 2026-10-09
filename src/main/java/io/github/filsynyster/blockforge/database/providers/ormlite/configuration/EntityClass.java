package io.github.filsynyster.blockforge.database.providers.ormlite.configuration;

/**
 * Representa uma configuração de escaneamento ou registro
 * de uma entidade.
 *
 * <p>Cada instância corresponde a um elemento
 * {@code <class>} dentro de {@code <entity-config>}.</p>
 *
 * <p>O valor pode representar:</p>
 *
 * <ul>
 *     <li>somente o nome simples da classe;</li>
 *     <li>o nome totalmente qualificado da classe.</li>
 * </ul>
 */
public class EntityClass {

    private final String value;

    /**
     * Cria uma configuração de classe.
     *
     * @param value nome simples ou nome totalmente qualificado
     */
    public EntityClass(String value) {
        this.value = value;
    }

    /**
     * Retorna o valor configurado.
     *
     * @return nome da classe
     */
    public String getValue() {
        return value;
    }

    /**
     * Verifica se o valor representa um nome totalmente qualificado.
     *
     * @return {@code true} caso contenha um pacote
     */
    public boolean isQualified() {
        return value != null && value.contains(".");
    }

    @Override
    public String toString() {
        return "EntityClass{" +
                "value='" + value + '\'' +
                '}';
    }
}