
package io.github.blockforge.database.main;

import io.github.blockforge.database.contracts.PersistenceContext;
import io.github.blockforge.database.contracts.PersistenceContextMap;
import io.github.blockforge.database.exceptions.PersistenceContextException;
import io.github.blockforge.database.infra.ResourcesLocator;
import io.github.blockforge.database.providers.ormlite.OrmLitePersistenceContext;
import io.github.blockforge.database.providers.ormlite.configuration.ConfigDocument;
import io.github.blockforge.database.providers.ormlite.configuration.Configuration;
import io.github.blockforge.database.providers.ormlite.configuration.Context;
import io.github.blockforge.database.sql.SQLDocument;
import io.github.blockforge.database.sql.SQLFileManager;

import java.util.Map;

public class PersistenceContextFactory {

    private final PersistenceContextMap contexts;
    private final Map<Class<?>, SQLDocument> documents;
    private final ConfigDocument configuration;


    /**
     * CONSTRUTOR
     * **/
    public PersistenceContextFactory() {
        contexts = new PersistenceContextMapImpl();
        try (ResourcesLocator.ResourcePaths locator = ResourcesLocator.locate()) {
            SQLFileManager sqlFileManager = new SQLFileManager(locator.sql());
            documents = sqlFileManager.load();
            configuration = Configuration.loadConfiguration(locator.databaseConfig().toString());
        } catch (Exception e) {
            throw new PersistenceContextException("Failed to locate resources: ", e);
        }
    }

    /**
     * Retorna um {@link PersistenceContextMap} contendo todos os
     * {@link PersistenceContext} criados a partir do arquivo {@code META-INF/ormlite.xml}.
     *
     * @return - {@link PersistenceContextMap}
     * */
    public PersistenceContextMap createOrmLiteContexts() {
        return createAll();
    }

    /**
     * Cria um {@link PersistenceContext} pelo seu nome no arquivo de configuração
     * {@code META-INF/ormlite.xml} e o disponibiliza no {@link PersistenceContextMap}.
     *
     * @return - {@link PersistenceContext}
     * */
    public PersistenceContext createOrmLiteContextByName(String name) {
        return createByName(name);
    }

    // ===========================================================================

    private PersistenceContextMap getContexts() {
        return contexts;
    }

    private PersistenceContext createByName(String name) {
        if(!configuration.contains(name)) {
            throw new PersistenceContextException("Context configuration \'"+name+"\' not found.");
        }
        try {
            Context contextConfig = configuration.getContextByName(name);
            if (contextConfig != null) {
                PersistenceContext context = new OrmLitePersistenceContext(
                        configuration.getContextByName(name),
                        documents
                );
                context.initialize();
                contexts.register(name, context);
                return context;
            } else {
                return null;
            }
        } catch (Exception e) {
            throw new PersistenceContextException(
                    "Failed to load persistence contexts from configuration component '" + name + "'.",
                    e
            );
        }
    }

    private PersistenceContextMap createAll() {
        for (Context contextConfig : configuration.getContexts()) {
            String name = contextConfig.getName();
            if (!contexts.contains(name)) {
                PersistenceContext context = new OrmLitePersistenceContext(
                        configuration.getContextByName(name),
                        documents
                );
                context.initialize();
                contexts.register(name, context);
            }
        }
        return contexts;
    }
}
