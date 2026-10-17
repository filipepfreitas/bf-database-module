package io.github.blockforge.database.annotations;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Define uma consulta SQL a ser executada por um método de repositório.
 *
 * <p>A anotação {@code @Query} permite que o desenvolvedor defina
 * explicitamente a instrução SQL associada a um método de um
 * {@code DataRepository}. A instrução SQL definida pela propriedade
 * {@link #value()} será utilizada pelo framework quando o método
 * anotado for invocado.</p>
 *
 * <p>Exemplo:</p>
 *
 * <pre>{@code
 * @Query("SELECT * FROM contacts WHERE name = ?")
 * List<Contact> findByName(String name);
 * }</pre>
 *
 * <p>Quando um método de repositório possui a anotação {@code @Query},
 * a instrução SQL declarada na anotação é utilizada diretamente pelo
 * BlockForge.</p>
 *
 * <p>Caso o método não possua {@code @Query}, o BlockForge poderá
 * procurar a definição SQL correspondente nos arquivos XML localizados
 * no diretório {@code resources/sql}. Nesse modelo, a propriedade
 * {@code class} identifica o repositório e a propriedade {@code bind}
 * associa o script SQL ao método correspondente.</p>
 *
 * <p>A anotação possui retenção em tempo de execução para que a
 * infraestrutura de persistência do BlockForge possa identificá-la
 * através de Reflection durante a construção dos metadados do
 * repositório e a resolução das consultas SQL.</p>
 *
 * @see #value()
 */
@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
public @interface Query {

    /**
     * Define a instrução SQL que será executada pelo método de
     * repositório anotado.
     *
     * @return a instrução SQL
     */
    String value();
}