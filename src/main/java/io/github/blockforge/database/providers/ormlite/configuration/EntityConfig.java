package io.github.blockforge.database.providers.ormlite.configuration;

import java.util.Arrays;

/**
 * Representa a configuração de descoberta e registro de entidades.
 *
 * <p>Corresponde ao elemento {@code <entity-config>} do XML.</p>
 *
 * <p>A configuração pode especificar entidades através de:</p>
 *
 * <ul>
 *     <li>pacotes;</li>
 *     <li>classes.</li>
 * </ul>
 */
public class EntityConfig {

    private final EntityPackage[] entityPackages;
    private final EntityClass[] entityClasses;

    /**
     * Cria uma configuração de entidades.
     *
     * @param entityPackages pacotes utilizados no escaneamento
     * @param entityClasses classes registradas diretamente
     */
    public EntityConfig(
            EntityPackage[] entityPackages,
            EntityClass[] entityClasses
    ) {
        this.entityPackages = entityPackages != null
                ? entityPackages.clone()
                : new EntityPackage[0];

        this.entityClasses = entityClasses != null
                ? entityClasses.clone()
                : new EntityClass[0];
    }

    /**
     * Retorna os pacotes configurados.
     *
     * @return pacotes de entidades
     */
    public EntityPackage[] getEntityPackages() {
        return entityPackages.clone();
    }

    /**
     * Retorna as classes configuradas.
     *
     * @return classes de entidades
     */
    public EntityClass[] getEntityClasses() {
        return entityClasses.clone();
    }

    public boolean isPackagesEmpty() {
        if (entityPackages == null) {
            return true;
        }
        return entityPackages.length == 0;
    }

    public boolean isClassesEmpty() {
        if (entityClasses == null) {
            return true;
        }
        return entityClasses.length == 0;
    }

    @Override
    public String toString() {
        return "EntityConfig{" +
                "entityPackages=" + Arrays.toString(entityPackages) +
                ", entityClasses=" + Arrays.toString(entityClasses) +
                '}';
    }
}