package io.github.filsynyster.blockforge.database.core;

import io.github.filsynyster.blockforge.database.providers.ormlite.OrmLitePersistenceContext;
import io.github.filsynyster.blockforge.database.providers.ormlite.configuration.ConfigDocument;
import io.github.filsynyster.blockforge.database.providers.ormlite.configuration.Configuration;
import io.github.filsynyster.blockforge.database.providers.ormlite.configuration.Context;

public final class PersistenceContextFactory {

    private PersistenceContextFactory() {
    }

    /**
     * Responsável por criar todos os {@link PersistenceContext} disponíveis nos
     * arquivos de configurações {@code META-INF/ormlite.xml} e {@code META-INF/persistence.xml}
     * que podem pertencer tanto ao pacote de domínio ou a pacotes de plugins externos (.jar).
     * <br/>
     * Retorna um {@link PersistenceContextMap} contendo todos os {@link PersistenceContext}
     * encontrados e carregados.
     *
     * @return - Um {@link PersistenceContextMap} com os {@link PersistenceContext} encontrados e carregados.
     * */
    public static PersistenceContextMap createPersistenceContexts() {
        return null;
    }

    public static PersistenceContext createPersistenceContext(String name) {
        return null;
    }

    public static PersistenceContextMap createOrmLiteContexts() {
        ConfigDocument config = new Configuration().load("META-INF/ormlite.xml");
        PersistenceContextMap map = new PersistenceContextMap();
        for (Context context : config.getContexts()) {
            String name = context.getName();
            if (!map.contains(name)) {
                PersistenceContext pc = new OrmLitePersistenceContext(config.getContextByName(name));
                pc.initialize();
                map.register(name, pc);
            }
        }
        return map;
    }

    public static PersistenceContext createOrmLiteContext(String name) {
        ConfigDocument config = new Configuration().load("META-INF/ormlite.xml");
        PersistenceContext context = new OrmLitePersistenceContext(config.getContextByName(name));
        context.initialize();
        return context;
    }

    public static PersistenceContext createJpaContexts() {
        return null;
    }

    public static PersistenceContext createJpaContext(String name) {
        return null;
    }

}
