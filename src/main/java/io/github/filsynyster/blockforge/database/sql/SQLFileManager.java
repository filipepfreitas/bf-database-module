package io.github.filsynyster.blockforge.database.sql;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;

import javax.xml.parsers.DocumentBuilderFactory;
import java.io.IOException;
import java.net.URISyntaxException;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * Representa um documento SQL associado a uma interface de repositório.
 *
 * <p>Um {@code SQLDocument} mantém em memória os scripts SQL definidos para
 * um determinado repositório, permitindo que cada script seja identificado
 * por uma chave de vinculação ({@code bind}). Essa chave é utilizada pelo
 * mecanismo de persistência para associar um método do repositório ao
 * respectivo script SQL.</p>
 *
 * <p>Os documentos SQL são carregados pelo mecanismo de gerenciamento de
 * arquivos SQL e posteriormente utilizados durante a construção e execução
 * dos repositórios. Dessa forma, o desenvolvedor pode manter as consultas
 * SQL separadas do código Java, organizadas em arquivos de recursos.</p>
 *
 * <p>Um documento possui:</p>
 * <ul>
 *     <li>o nome qualificado da interface de repositório à qual está associado;</li>
 *     <li>o caminho do arquivo SQL de origem;</li>
 *     <li>um conjunto de scripts identificados por suas respectivas chaves
 *         de vinculação ({@code bind}).</li>
 * </ul>
 *
 * <p>Exemplo de associação:</p>
 *
 * <pre>{@code
 * <repository class="agenda.app.repositories.ContactRepository">
 *
 *     <script bind="findByName">
 *         SELECT * FROM contacts WHERE name = ?
 *     </script>
 *
 * </repository>
 * }</pre>
 *
 * <p>Nesse exemplo, o valor {@code findByName} funciona como identificador
 * do script e pode ser utilizado pelo framework para localizar a instrução
 * SQL correspondente ao método do repositório.</p>
 *
 * <p>A classe não é responsável pela execução dos scripts SQL. Sua
 * responsabilidade é representar e fornecer acesso aos comandos SQL
 * carregados, deixando a execução a cargo da camada de persistência.</p>
 *
 * @see SQLFileManager
 */
public class SQLFileManager {

    private final Path sqlDirectory;

    public static SQLFileManager newSQLFileManager() {
        return new SQLFileManager(Objects.requireNonNull(SQLFileManager.class.getClassLoader().getResource("sql")));
    }

    public SQLFileManager(URL sqlDirectory) {
        try {
            this.sqlDirectory = Paths.get(sqlDirectory.toURI());
        } catch (URISyntaxException e) {
            throw new IllegalArgumentException("BlockForge says: inválid SQL directory: " + sqlDirectory, e);
        }
    }

    public Map<Class<?>, SQLDocument> load() {
        if (!Files.isDirectory(sqlDirectory)) {
            throw new IllegalStateException("Diretório de SQL não encontrado: " + sqlDirectory);
        }

        try (var files = Files.list(sqlDirectory)) {

            return files
                    .filter(Files::isRegularFile)
                    .filter(path -> path.toString().endsWith(".xml"))
                    .map(this::parse)
                    .collect(Collectors.toMap(
                            document -> loadClass(document.getRepositoryClass()),
                            Function.identity()
                    ));

        } catch (IOException e) {
            throw new RuntimeException("Erro ao listar diretório de SQL: " + sqlDirectory, e);
        }
    }

    private Class<?> loadClass(String className) {
        try {
            return Class.forName(className);
        } catch (ClassNotFoundException e) {
            throw new IllegalArgumentException(
                    "BlockForge says: Repository not found: " + className, e);
        }
    }

    private SQLDocument parse(Path file) {
        try {
            var factory = DocumentBuilderFactory.newInstance();
            // Evita XXE
            factory.setFeature(
                    "http://apache.org/xml/features/disallow-doctype-decl",
                    true
            );

            var builder = factory.newDocumentBuilder();
            Document document = builder.parse(file.toFile());
            Element root = document.getDocumentElement();

            if (!"sql".equals(root.getTagName())) {
                throw new IllegalArgumentException("Invalid root element in: " + file);
            }

            String repository = root.getAttribute("class");
            if (repository == null || repository.isBlank()) {
                throw new IllegalArgumentException("Attribute 'class' not provided in: " + file);
            }

            NodeList queryNodes = root.getElementsByTagName("query");
            Map<String, String> scriptMap = new HashMap<>();

            for (int i = 0; i < queryNodes.getLength(); i++) {
                Element queryElement = (Element) queryNodes.item(i);
                String bind = queryElement.getAttribute("bind");
                if (bind == null || bind.isBlank()) {
                    throw new IllegalArgumentException("The 'bind' attribute is not provided in: " + file);
                }
                String sql = queryElement.getTextContent().trim();
                if (sql.isBlank()) {
                    throw new IllegalArgumentException("Empty SQL for '" + bind + "' in: " + file);
                }
                scriptMap.put(bind, sql);
            }

            return new SQLDocument(repository, file, scriptMap);

        } catch (Exception e) {
            throw new RuntimeException("BlockForge says: Error processing SQL file.: " + file, e);
        }
    }

}