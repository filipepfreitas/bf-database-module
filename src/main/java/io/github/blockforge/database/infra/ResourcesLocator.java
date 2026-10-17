
package io.github.blockforge.database.infra;

import io.github.classgraph.ClassGraph;
import io.github.classgraph.Resource;
import io.github.classgraph.ScanResult;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.util.HashSet;
import java.util.Set;

/**
 * Localiza recursos no classpath da aplicação e os extrai para um
 * diretório temporário, disponibilizando-os por meio de caminhos físicos.
 *
 * <p>Os recursos processados são a configuração
 * {@code META-INF/ormlite.xml} e os arquivos localizados sob
 * o diretório {@code sql/}.</p>
 *
 * <p>Os arquivos temporários são removidos em caso de falha durante
 * a extração ou quando o objeto {@link ResourcePaths} é fechado.</p>
 *
 * @author BlockForge
 * @since 0.1.0
 */
public final class ResourcesLocator {

    /**
     * Caminho relativo da configuração de persistência obrigatória.
     */
    private static final String CONFIG = "META-INF/ormlite.xml";

    /**
     * Impede a instanciação desta classe utilitária.
     */
    private ResourcesLocator() {
    }

    /**
     * Localiza os recursos suportados no classpath e os extrai para
     * um diretório temporário.
     *
     * @return os caminhos dos recursos extraídos e do diretório temporário
     * @throws IOException se ocorrer um erro durante a extração,
     *         se forem encontrados recursos duplicados ou se a configuração
     *         obrigatória não for encontrada
     */
    public static ResourcePaths locate() throws IOException {

        Path root = Files.createTempDirectory("blockforge-host-resources-");

        Path metaInfDir = root.resolve("META-INF");
        Path sqlDir = root.resolve("sql");

        boolean configFound = false;
        Set<String> copiedResources = new HashSet<>();

        try (ScanResult scanResult = new ClassGraph()
                .acceptPaths("sql", "META-INF")
                .scan()) {

            for (Resource resource : scanResult.getAllResources()) {
                String pathStr = resource.getPath();

                boolean isConfig = pathStr.equals(CONFIG);
                boolean isSqlFile = pathStr.startsWith("sql/");

                if (!isConfig && !isSqlFile) {
                    continue;
                }

                if (!copiedResources.add(pathStr)) {
                    throw new IOException(
                            "BlockForge Database says: Duplicate resource found in classpath: "
                                    + pathStr
                    );
                }

                Path target = root.resolve(pathStr).normalize();

                if (!target.startsWith(root)) {
                    throw new IOException(
                            "BlockForge Database says: Invalid resource path: "
                                    + pathStr
                    );
                }

                Files.createDirectories(target.getParent());

                try (var inputStream = resource.open()) {
                    Files.copy(
                            inputStream,
                            target,
                            StandardCopyOption.REPLACE_EXISTING
                    );
                }

                if (isConfig) {
                    configFound = true;
                }
            }

            Path configPath = root.resolve(CONFIG);

            if (!configFound || !Files.isRegularFile(configPath)) {
                throw new IOException(
                        "BlockForge Database says: Required configuration not found: "
                                + CONFIG
                );
            }

            return new ResourcePaths(
                    metaInfDir,
                    sqlDir,
                    configPath,
                    root
            );

        } catch (IOException | RuntimeException e) {
            try {
                deleteDirectory(root);
            } catch (IOException cleanupException) {
                e.addSuppressed(cleanupException);
            }

            throw e;
        }
    }

    /**
     * Remove recursivamente um diretório e todo o seu conteúdo.
     *
     * @param root caminho do diretório a ser removido
     * @throws IOException se ocorrer um erro durante a exclusão
     */
    private static void deleteDirectory(Path root) throws IOException {
        if (!Files.exists(root)) {
            return;
        }

        try (var paths = Files.walk(root)) {
            for (Path path : paths
                    .sorted(java.util.Comparator.reverseOrder())
                    .toList()) {
                Files.deleteIfExists(path);
            }
        }
    }

    /**
     * Representa os caminhos dos recursos extraídos e gerencia
     * o ciclo de vida do diretório temporário.
     *
     * @param metaInf caminho do diretório META-INF extraído
     * @param sql caminho do diretório dos arquivos SQL extraídos
     * @param databaseConfig caminho completo da configuração de persistência
     * @param tempDirectory caminho raiz do diretório temporário
     */
    public record ResourcePaths(Path metaInf, Path sql, Path databaseConfig, Path tempDirectory) implements AutoCloseable {

        /**
         * Remove o diretório temporário e todos os recursos extraídos.
         *
         * @throws IOException se ocorrer um erro durante a exclusão
         */
        @Override
        public void close() throws IOException {
            deleteDirectory(tempDirectory);
        }
    }
}
