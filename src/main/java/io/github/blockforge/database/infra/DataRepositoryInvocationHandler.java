package io.github.blockforge.database.infra;

import io.github.blockforge.database.annotations.Query;
import io.github.blockforge.database.contracts.DataRepository;
import io.github.blockforge.database.exceptions.RepositoryQueryNotFoundException;
import io.github.blockforge.database.sql.SQLDocument;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;

/**
 * Manipula as chamadas realizadas sobre os proxies dinâmicos de
 * {@link DataRepository}.
 *
 * <p>Esta classe atua como um {@link InvocationHandler} da API de
 * repositórios do BlockForge, intermediando as chamadas realizadas pelo
 * desenvolvedor e determinando como cada método deve ser executado.</p>
 *
 * <p>Durante a invocação de um método, o handler verifica inicialmente se
 * existe uma implementação correspondente na implementação real de
 * {@code DataRepository}. Caso exista, a chamada é delegada diretamente
 * para essa implementação.</p>
 *
 * <p>Quando o método não pertence à implementação real, o handler o
 * interpreta como uma operação de consulta definida pelo desenvolvedor.
 * Nesse caso, o script SQL é localizado seguindo a seguinte ordem:</p>
 *
 * <ol>
 *     <li>Consulta SQL declarada diretamente por meio da anotação
 *         {@link Query};</li>
 *     <li>Script SQL associado ao método no {@link SQLDocument};</li>
 *     <li>Caso nenhuma das opções anteriores seja encontrada, uma
 *         {@link RepositoryQueryNotFoundException} é lançada.</li>
 * </ol>
 *
 * <p>Após localizar o SQL, a consulta é encaminhada para o método
 * {@link DataRepository#nativeQuery(String, java.lang.reflect.Type, String[])}
 * da implementação real. O tipo de retorno declarado pelo método do
 * repositório e os argumentos recebidos pela chamada são utilizados para
 * determinar como o resultado deve ser processado.</p>
 *
 * <p>Essa abordagem permite que uma interface de repositório contenha tanto
 * operações CRUD fornecidas pelo framework quanto métodos de consulta
 * personalizados, sem exigir uma implementação manual da interface.</p>
 *
 * <p>Exemplo:</p>
 *
 * <pre>{@code
 * public interface ContactRepository
 *         extends DataRepository<Contact, Long> {
 *
 *     @Query("SELECT * FROM contacts WHERE name = ?")
 *     List<Contact> findByName(String name);
 * }
 * }</pre>
 *
 * <p>Nesse exemplo, {@code findByName} não possui uma implementação
 * convencional. A chamada é interceptada pelo handler, a anotação
 * {@link Query} é localizada e o SQL é executado por meio da implementação
 * real do repositório.</p>
 *
 * @see InvocationHandler
 * @see DataRepository
 * @see Query
 * @see SQLDocument
 * @see RepositoryQueryNotFoundException
 */
public class DataRepositoryInvocationHandler implements InvocationHandler {

    /**
     * Implementação real do repositório responsável pela execução das
     * operações de persistência e das consultas SQL nativas.
     */
    private final DataRepository<?, ?> dataRepository;

    /**
     * Classe da implementação real do repositório.
     *
     * <p>É utilizada para verificar se um método possui uma implementação
     * concreta antes de tratá-lo como uma consulta personalizada.</p>
     */
    private final Class<?> dataRepositoryClass;

    /**
     * Documento SQL associado ao repositório.
     *
     * <p>É utilizado como fonte alternativa de scripts SQL quando o método
     * não possui uma declaração {@link Query}.</p>
     */
    private final SQLDocument sqlDocument;

    /**
     * Cria um novo handler para interceptar as chamadas de um repositório.
     *
     * @param dataRepository implementação real do repositório que receberá
     *                       as operações delegadas e executará as consultas
     *                       nativas
     * @param sqlDocument documento SQL associado ao repositório, utilizado
     *                    para localizar scripts externos aos métodos Java
     */
    public DataRepositoryInvocationHandler(
            DataRepository<?, ?> dataRepository,
            SQLDocument sqlDocument) {
        this.dataRepository = dataRepository;
        this.dataRepositoryClass = dataRepository.getClass();
        this.sqlDocument = sqlDocument;
    }

    /**
     * Verifica se uma classe declara um método compatível com a assinatura
     * fornecida.
     *
     * <p>A comparação considera o nome do método e os tipos dos parâmetros,
     * permitindo identificar corretamente métodos sobrecarregados.</p>
     *
     * @param clazz classe na qual o método será procurado
     * @param name nome do método
     * @param parameterTypes tipos dos parâmetros do método
     * @return {@code true} se a classe declarar um método com o nome e a
     *         assinatura informados; {@code false} caso contrário
     */
    public boolean containsMethod(Class<?> clazz, String name, Class<?>... parameterTypes) {
        try {
            clazz.getDeclaredMethod(name, parameterTypes);
            return true;
        } catch (NoSuchMethodException e) {
            return false;
        }
    }

    /**
     * Converte os argumentos de uma chamada de método para um array de
     * {@link String}.
     *
     * <p>Os valores são convertidos utilizando
     * {@link String#valueOf(Object)}, permitindo que diferentes tipos de
     * argumentos sejam encaminhados para a execução de uma consulta SQL
     * nativa.</p>
     *
     * @param args argumentos recebidos pela chamada do método
     * @return um array contendo os argumentos convertidos para
     *         {@code String}, ou {@code null} quando não existem argumentos
     */
    public String[] toStringParamArray(Object[] args) {
        if (args == null || args.length == 0 || args[0] == null) {
            return null;
        }

        String[] parameters = new String[args.length];

        for (int i = 0; i < args.length; i++) {
            parameters[i] = String.valueOf(args[i]);
        }

        return parameters;
    }

    /**
     * Intercepta e processa uma chamada realizada sobre o proxy do
     * repositório.
     *
     * <p>Quando o método possui uma implementação na implementação real de
     * {@code DataRepository}, a chamada é delegada diretamente para ela.</p>
     *
     * <p>Caso contrário, o método é tratado como uma consulta personalizada.
     * O SQL é obtido primeiro da anotação {@link Query} e, na ausência da
     * anotação, do {@link SQLDocument} associado ao repositório.</p>
     *
     * <p>Depois de localizado o SQL, a consulta é executada por meio de
     * {@link DataRepository#nativeQuery(String, java.lang.reflect.Type, String[])}.
     * O tipo de retorno genérico declarado pelo método é encaminhado para que
     * a camada de persistência possa determinar o formato do resultado.</p>
     *
     * @param proxy instância do proxy dinâmico que recebeu a chamada
     * @param method método da interface do repositório que foi invocado
     * @param args argumentos fornecidos à chamada do método
     * @return resultado da execução do método ou da consulta SQL
     * @throws Throwable caso a execução do método delegado ou da consulta
     *                   resulte em uma exceção
     * @throws RepositoryQueryNotFoundException caso o método não possua
     *         implementação na implementação real e nenhum script SQL seja
     *         encontrado por meio de {@link Query} ou {@link SQLDocument}
     */
    @Override
    public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {

        if (!containsMethod(dataRepositoryClass, method.getName(), method.getParameterTypes())) {

            String sql = null;
            String[] params = null;

            if (method.isAnnotationPresent(Query.class)) {
                sql = method.getAnnotation(Query.class).value();

            } else if (sqlDocument != null && sqlDocument.containsScript(method.getName())) {
                sql = sqlDocument.getScript(method.getName());

            } else {
                throw new RepositoryQueryNotFoundException("No script found for method: " + method.getName());
            }

            return dataRepository.nativeQuery(sql, method.getGenericReturnType(), toStringParamArray(args));
        }

        Method dataRepositoryMethod = dataRepositoryClass.getMethod(method.getName(), method.getParameterTypes());
        return dataRepositoryMethod.invoke(dataRepository, args);
    }
}
