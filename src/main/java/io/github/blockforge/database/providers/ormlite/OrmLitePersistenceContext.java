package io.github.blockforge.database.providers.ormlite;

import io.github.blockforge.database.exceptions.PersistenceContextException;
import io.github.blockforge.database.infra.ClassScanner;
import io.github.blockforge.database.contracts.PersistenceContext;
import io.github.blockforge.database.contracts.DataRepository;
import io.github.blockforge.database.infra.RepositoryCreator;
import io.github.blockforge.database.providers.ormlite.configuration.Context;
import io.github.blockforge.database.providers.ormlite.configuration.ContextConfig;
import io.github.blockforge.database.sql.SQLDocument;
import io.github.blockforge.database.sql.SQLFileManager;

import java.nio.file.Path;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class OrmLitePersistenceContext implements PersistenceContext {

    private final ClassScanner scanner;
    private final Map<Class<?>, DataRepository<?,?>> repositories = new HashMap<>();
    private final OrmLiteSchemaInitializer schemaInitializer;
    private final Map<Class<?>, SQLDocument> sqlDocuments;

    public OrmLitePersistenceContext(Context config, Map<Class<?>, SQLDocument> sqlDocuments) {

        this.sqlDocuments = sqlDocuments;
        ContextConfig contextConfig = config.getContextConfig();
        scanner = new ClassScanner(config);
        schemaInitializer = new OrmLiteSchemaInitializer(
                scanner,
                contextConfig.getUrl(),
                contextConfig.getDdlAuto()
        );
        initialize();
    }

    @Override
    public void initialize() {

        //Localiza todas as sub-interfaces de DataRepository
        List<Class<?>> subDataRepositories = scanner.findDataRepositoryInterfaces();

        //Para cada subinterface de DataRepository encontrada...
        for (Class<?> subDataRepository : subDataRepositories ) {

            //Recupera a entidade do parâmetro genérico 'T' do DataRepository da subinterface atual
            Class<?> entityParamClass = RepositoryCreator.getEntityParamClass(subDataRepository);

            //Obtém uma implementação concreta de DataRepository com o provedor de dados ORMLite
            //É isso aqui que o Proxy usa para acessar os dados do banco
            DataRepository<?,?> sqliteRepository = schemaInitializer.createSqliteRepository(entityParamClass);

            SQLDocument document = null;
            if (sqlDocuments != null) {
                document = sqlDocuments.get(subDataRepository);
            }

            //Implementa utilizando um Proxy a subinterface atual de DataRepository
            DataRepository<?,?> repository = RepositoryCreator.createProxyRepository(
                    subDataRepository,
                    sqliteRepository,
                    document
            );

            //Registra o repositório implementado no registro de repositórios
            register(entityParamClass, repository);
        }
    }

    @Override
    public void register(Class<?> entity, DataRepository<?,?> repository) {
        repositories.put(entity, repository);
    }

    @Override
    public <R extends DataRepository<?,?>> R getRepository(Class<?> entity) {
        return (R) repositories.get(entity);
    }

    @Override
    public void close() {
        try {
            schemaInitializer.getConnectionSource().close();
        } catch (Exception e) {
            throw new PersistenceContextException(e);
        }
    }

}
