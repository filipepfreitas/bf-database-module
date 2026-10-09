package io.github.filsynyster.blockforge.database.core.repository;

import java.lang.reflect.Type;
import java.util.List;

/**
 * Define o contrato de acesso aos dados de uma entidade.
 *
 * <p>O desenvolvedor utiliza esta interface para realizar operações de
 * persistência sem precisar lidar diretamente com conexões, instruções SQL
 * ou detalhes específicos do provedor de banco de dados.</p>
 *
 * <p>Para o framework, esta interface representa o contrato utilizado para
 * associar um Repository à sua entidade e executar as operações de
 * persistência correspondentes.</p>
 *
 * <p>O parâmetro de tipo {@code T} representa a entidade manipulada pelo
 * Repository, enquanto {@code ID} representa o tipo do identificador
 * dessa entidade.</p>
 *
 * <p>As operações padrão ({@code findAll}, {@code findById},
 * {@code create}, {@code update} e {@code deleteById}) fornecem uma API
 * de persistência independente da implementação utilizada pelo framework.</p>
 *
 * <h3>Repositories definidos pelo desenvolvedor</h3>
 *
 * <p>Quando o desenvolvedor estende esta interface para criar um Repository
 * específico de uma entidade, o framework permite definir operações de
 * consulta adicionais de duas formas:</p>
 *
 * <ul>
 *     <li>
 *         <b>Consulta explícita com {@code @Query}:</b>
 *         o desenvolvedor declara o método no Repository e utiliza a
 *         anotação {@code @Query} para informar diretamente o script SQL
 *         que deve ser executado pelo framework.
 *     </li>
 *     <li>
 *         <b>Consulta definida em arquivo XML:</b>
 *         quando o método não possui {@code @Query}, o framework procura
 *         o script SQL correspondente nos arquivos XML localizados no
 *         diretório {@code resources/sql}.
 *     </li>
 * </ul>
 *
 * <p>Os arquivos XML permitem associar um conjunto de scripts SQL a um
 * Repository por meio da propriedade {@code class}. Cada script é então
 * associado a um método do Repository por meio da propriedade {@code bind}.
 * Dessa forma, o framework consegue localizar o arquivo correspondente ao
 * Repository e vincular cada método à instrução SQL que deverá ser executada.</p>
 *
 * <p>Quando um Repository possui um método adicional sem {@code @Query},
 * o framework utiliza essa configuração para localizar e executar o script
 * SQL correspondente, mantendo o código Java separado das instruções SQL.</p>
 *
 * <p>Além das operações padrão, {@code nativeQuery} permite executar uma
 * consulta SQL nativa. Nesse caso, o framework recebe a instrução SQL,
 * o tipo esperado para o resultado e os parâmetros da consulta, sendo
 * responsável por executar a consulta e converter o resultado para o
 * tipo solicitado.</p>
 *
 * <p>Dessa forma, o desenvolvedor trabalha com uma API orientada à entidade,
 * podendo escolher entre consultas SQL declaradas diretamente no código
 * através de {@code @Query} ou consultas externas organizadas em arquivos
 * XML. O framework fica responsável por localizar os scripts, associá-los
 * aos métodos, executar as consultas, converter os resultados e gerenciar
 * a comunicação com o banco de dados.</p>
 *
 * @param <T>  tipo da entidade manipulada pelo Repository
 * @param <ID> tipo do identificador da entidade
 */
public interface DataRepository<T, ID> {

    public List<T> findAll();

    public T findById(ID id);

    public T create(T entity);

    public T update(T entity);

    public boolean deleteById(ID id);

    /**
     * Executes a native query and returns its result according to the
     * specified return type.
     *
     * @param sql the native query to be executed
     * @param returnType the type expected for the query result
     * @param args the parameters used by the query
     * @param <R> the result type
     * @return the query result
     * @throws jakarta.persistence.PersistenceException if the query cannot be executed
     *         or the requested return type is not supported
     */
    public <R> R nativeQuery(String sql, Type returnType, String...args);


}
