package io.github.blockforge.database.infra;

import io.github.blockforge.database.contracts.DataRepository;
import io.github.blockforge.database.sql.SQLDocument;

import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Proxy;
import java.lang.reflect.Type;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class RepositoryCreator {

    public static Class<?> getEntityParamClass(Class<?>  repositoryInterface) {
        ParameterizedType type = (ParameterizedType) repositoryInterface.getGenericInterfaces()[0];
        Type[] argTypes = type.getActualTypeArguments();
        return (Class<?>) argTypes[0];
    }

    public static DataRepository<?,?> createProxyRepository(
            Class<?> repositoryInterfaceClass,
            DataRepository<?,?> dataRepository,
            SQLDocument sqlDocument
    ) {
        //Cria a implementação dinâmica de DataRepository
        ClassLoader classLoader = repositoryInterfaceClass.getClassLoader();
        Class<?>[] interfaces = { repositoryInterfaceClass };

        return (DataRepository<?,?>) Proxy.newProxyInstance(
                classLoader,
                interfaces,
                new DataRepositoryInvocationHandler(dataRepository, sqlDocument)
        );
    }

    /**
     * Procura por subinterfaces de {@link DataRepository} (repositórios declarados)
     * e as implementa dinâmicamente.
     *
     * @param scanner Um scanner configurado para escanear o package específico.
     * @param repositoryImpl Uma implementação real de {@link DataRepository}.
     * @param sqlDocument O documento xml com os scripts sql relacionados ao repositório.
     * */
    public static Map<Class<?>, DataRepository<?,?>> findAndCreateRepositories(
            ClassScanner scanner,
            DataRepository<?,?> repositoryImpl,
            SQLDocument sqlDocument
    ) {
        //Localiza todas as subinterfaces de DataRepository
        List<Class<?>> subDataRepositories = scanner.findDataRepositoryInterfaces();

        //O map com os repositórios encontrados e carregados
        Map<Class<?>, DataRepository<?,?>> result = new HashMap<>();

        //Para cada subinterface de DataRepository encontrada...
        for (Class<?> subDataRepository : subDataRepositories ) {

            //Recupera a entidade do parâmetro genérico 'T' do DataRepository da subinterface atual
            Class<?> entityParamClass = RepositoryCreator.getEntityParamClass(subDataRepository);

            //Implementa utilizando um Proxy a subinterface atual de DataRepository
            DataRepository<?,?> repository = RepositoryCreator.createProxyRepository(
                    subDataRepository,
                    repositoryImpl,
                    sqlDocument
            );

            //Registra o repositório implementado no registro de repositórios
            result.put(entityParamClass, repository);
        }

        return result;
    }

}
