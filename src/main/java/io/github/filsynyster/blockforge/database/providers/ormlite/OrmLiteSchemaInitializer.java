package io.github.filsynyster.blockforge.database.providers.ormlite;

import io.github.filsynyster.blockforge.database.core.ClassScanner;
import io.github.filsynyster.blockforge.database.providers.ormlite.configuration.ConfigFileProperties;
import com.j256.ormlite.jdbc.JdbcConnectionSource;
import com.j256.ormlite.support.ConnectionSource;
import com.j256.ormlite.table.DatabaseTable;
import com.j256.ormlite.table.TableUtils;

import java.sql.SQLException;
import java.util.List;

public class OrmLiteSchemaInitializer {

    private ConnectionSource connectionSource;
    private final ClassScanner scanner;
    private final String databaseUrl;
    private final String ddlAuto;


    public OrmLiteSchemaInitializer(ClassScanner classScanner, String databaseUrl, String ddlAuto) {
        this.scanner = classScanner;
        this.databaseUrl = databaseUrl;
        this.ddlAuto = ddlAuto;
        configSchema();
    }

    public void configSchema() {
        try {
            connectionSource = new JdbcConnectionSource(databaseUrl);
            switch (ddlAuto.toLowerCase()) {
                case ConfigFileProperties.CREATE_VALUE -> create();
                case ConfigFileProperties.ENSURE_VALUE -> ensure();
                case ConfigFileProperties.DROP_VALUE -> drop();
                case ConfigFileProperties.CLEAR_VALUE -> clear();
                case ConfigFileProperties.NONE_VALUE -> {
                    // Não executa nenhuma operação de schema.
                }
                default -> throw new IllegalArgumentException(
                        "Valor de ddl-auto inválido: " + ddlAuto
                );
            }

        } catch (SQLException e) {
            throw new RuntimeException("Erro ao configurar a conexão com o banco de dados.", e);
        }
    }

    private void clear() {
        try {
            List<Class<?>> entityClasses = scanner.findClassesWithAnnotation(DatabaseTable.class);
            for (Class<?> entityClass : entityClasses) {
                TableUtils.clearTable(connectionSource, entityClass);
            }
        } catch (SQLException e) {
            throw new RuntimeException("Erro ao limpar as tabelas do banco de dados.", e);
        }
    }

    private void drop() {
        try {
            List<Class<?>> entityClasses = scanner.findClassesWithAnnotation(DatabaseTable.class);
            for (Class<?> entityClass : entityClasses) {
                TableUtils.dropTable(connectionSource, entityClass, true);
            }
        } catch (SQLException e) {
            throw new RuntimeException("Erro ao remover as tabelas do banco de dados.", e);
        }
    }

    private void create() {
        try {
            List<Class<?>> entityClasses = scanner.findClassesWithAnnotation(DatabaseTable.class);
            for (Class<?> entityClass : entityClasses) {
                TableUtils.createTable(connectionSource, entityClass);
            }
        } catch (SQLException e) {
            throw new RuntimeException("Erro ao criar as tabelas do banco de dados.", e);
        }
    }

    /**
     * Localiza entidades (classes) anotadas com @DatabaseTable e
     * utiliza seu Class para criar as tabelas que elas representam
     * caso elas não existam.
     * <br>
     * <br>
     * 1 - Procura por classes anotadas com @DatabaseTable.
     * <br>
     * 2 - Utiliza TableUtils.createTableIfNotExists(ConnectionSource connectionSource, Class<T> dataClass) para criar as tabelas.
     *
     * <br>
     * <br>
     * <b>Obs: Falta implementar um grafo para ordenar a criação das tabelas seguindo suas relações.<b/>
     * */
    public void ensure() {
        try {
            List<Class<?>> classes = scanner.findClassesWithAnnotation(DatabaseTable.class);
            for ( Class<?> entityClass : classes ) {
                TableUtils.createTableIfNotExists(connectionSource, entityClass);
            }
        } catch (SQLException e) {
            throw new RuntimeException("Erro ao tentar criar tabelas se não existirem no banco de dados",e);
        }
    }

    public OrmLiteRepository<?,?> createSqliteRepository(Class<?> entityClass) {
        return new OrmLiteRepository<>(connectionSource, entityClass);
    }

    public ConnectionSource getConnectionSource() {
        return connectionSource;
    }
}
