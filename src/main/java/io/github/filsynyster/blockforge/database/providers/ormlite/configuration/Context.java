package io.github.filsynyster.blockforge.database.providers.ormlite.configuration;

/**
 * Representa um contexto de persistência.
 *
 * <p>Um contexto agrupa todas as configurações necessárias
 * para uma unidade lógica de persistência.</p>
 *
 * <p>Um contexto possui:</p>
 *
 * <ul>
 *     <li>{@link ContextConfig} — configuração de infraestrutura;</li>
 *     <li>{@link EntityConfig} — configuração das entidades.</li>
 * </ul>
 */
public class Context {

    private final String name;
    private final ContextConfig contextConfig;
    private final EntityConfig entityConfig;

    /**
     * Cria um contexto de persistência.
     *
     * @param name nome do contexto
     * @param contextConfig configuração do contexto
     * @param entityConfig configuração das entidades
     */
    public Context(
            String name,
            ContextConfig contextConfig,
            EntityConfig entityConfig
    ) {
        this.name = name;
        this.contextConfig = contextConfig;
        this.entityConfig = entityConfig;
    }

    /**
     * Retorna o nome do contexto.
     *
     * @return nome do contexto
     */
    public String getName() {
        return name;
    }

    /**
     * Retorna a configuração de infraestrutura do contexto.
     *
     * @return configuração do contexto
     */
    public ContextConfig getContextConfig() {
        return contextConfig;
    }

    /**
     * Retorna a configuração das entidades.
     *
     * @return configuração das entidades
     */
    public EntityConfig getEntityConfig() {
        return entityConfig;
    }

    @Override
    public String toString() {
        return "Context{" +
                "name='" + name + '\'' +
                ", contextConfig=" + contextConfig +
                ", entityConfig=" + entityConfig +
                '}';
    }
}