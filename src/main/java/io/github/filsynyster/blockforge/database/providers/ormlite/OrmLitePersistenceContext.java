package io.github.filsynyster.blockforge.database.providers.ormlite;

import io.github.filsynyster.blockforge.database.core.ClassScanner;
import io.github.filsynyster.blockforge.database.core.PersistenceContext;
import io.github.filsynyster.blockforge.database.core.repository.DataRepository;
import io.github.filsynyster.blockforge.database.core.repository.RepositoryBuilder;
import io.github.filsynyster.blockforge.database.providers.ormlite.configuration.Context;
import io.github.filsynyster.blockforge.database.providers.ormlite.configuration.ContextConfig;
import io.github.filsynyster.blockforge.database.sql.SQLDocument;
import io.github.filsynyster.blockforge.database.sql.SQLFileManager;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class OrmLitePersistenceContext implements PersistenceContext {

    private final ClassScanner scanner;
    private final Map<Class<?>, DataRepository<?,?>> repositories = new HashMap<>();
    private final OrmLiteSchemaInitializer schemaInitializer;

    public OrmLitePersistenceContext(Context config) {

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
        //Cria um SQLFileManager que procura arquivos SQL no diretório padrão 'resources/sql'
        SQLFileManager sqlFileManager = SQLFileManager.newSQLFileManager();

        //Carrega os arquivos SQL para objetos SQLDocument
        Map<Class<?>, SQLDocument> documents = sqlFileManager.load();

        //Localiza todas as sub-interfaces de DataRepository
        List<Class<?>> subDataRepositories = scanner.findDataRepositoryInterfaces();

        //Para cada subinterface de DataRepository encontrada...
        for (Class<?> subDataRepository : subDataRepositories ) {

            //Recupera a entidade do parâmetro genérico 'T' do DataRepository da subinterface atual
            Class<?> entityParamClass = RepositoryBuilder.getEntityParamClass(subDataRepository);

            //Obtém uma implementação concreta de DataRepository com o provedor de dados ORMLite
            //É isso aqui que o Proxy usa para acessar os dados do banco
            DataRepository<?,?> sqliteRepository = schemaInitializer.createSqliteRepository(entityParamClass);

            //Implementa utilizando um Proxy a subinterface atual de DataRepository
            DataRepository<?,?> repository = RepositoryBuilder.createProxyRepository(subDataRepository, sqliteRepository, documents.get(subDataRepository));

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
            throw new RuntimeException(e);
        }
    }

}
