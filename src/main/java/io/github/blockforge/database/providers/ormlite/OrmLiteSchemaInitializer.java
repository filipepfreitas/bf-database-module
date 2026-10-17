
package io.github.blockforge.database.providers.ormlite;

import io.github.blockforge.database.exceptions.PersistenceContextException;
import io.github.blockforge.database.infra.ClassScanner;
import io.github.blockforge.database.providers.ormlite.configuration.ConfigFileProperties;

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

    public OrmLiteSchemaInitializer(
            ClassScanner classScanner,
            String databaseUrl,
            String ddlAuto) {

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
                    // No schema operation is performed.
                }
                default -> throw new PersistenceContextException(
                        "Invalid ddl-auto value: " + ddlAuto
                );
            }

        } catch (PersistenceContextException e) {
            closeConnectionSource(e);
            throw e;
        } catch (SQLException e) {
            closeConnectionSource(e);
            throw new PersistenceContextException(
                    "Failed to configure the database connection.", e
            );
        } catch (RuntimeException e) {
            closeConnectionSource(e);
            throw new PersistenceContextException(
                    "Failed to configure the database schema.", e
            );
        }
    }

    private void clear() {
        try {
            List<Class<?>> entityClasses =
                    scanner.findClassesWithAnnotation(DatabaseTable.class);

            for (Class<?> entityClass : entityClasses) {
                TableUtils.clearTable(connectionSource, entityClass);
            }
        } catch (SQLException e) {
            throw new PersistenceContextException(
                    "Failed to clear database tables.", e
            );
        }
    }

    private void drop() {
        try {
            List<Class<?>> entityClasses =
                    scanner.findClassesWithAnnotation(DatabaseTable.class);

            for (Class<?> entityClass : entityClasses) {
                TableUtils.dropTable(connectionSource, entityClass, true);
            }
        } catch (SQLException e) {
            throw new PersistenceContextException(
                    "Failed to drop database tables.", e
            );
        }
    }

    private void create() {
        try {
            List<Class<?>> entityClasses =
                    scanner.findClassesWithAnnotation(DatabaseTable.class);

            for (Class<?> entityClass : entityClasses) {
                TableUtils.createTable(connectionSource, entityClass);
            }
        } catch (SQLException e) {
            throw new PersistenceContextException(
                    "Failed to create database tables.", e
            );
        }
    }

    /**
     * Finds classes annotated with @DatabaseTable and creates their
     * corresponding tables if they do not already exist.
     *
     * <p>Dependency ordering between related entities is not yet implemented.</p>
     */
    public void ensure() {
        try {
            List<Class<?>> entityClasses =
                    scanner.findClassesWithAnnotation(DatabaseTable.class);

            for (Class<?> entityClass : entityClasses) {
                TableUtils.createTableIfNotExists(
                        connectionSource, entityClass
                );
            }
        } catch (SQLException e) {
            throw new PersistenceContextException(
                    "Failed to create database tables that do not exist.", e
            );
        }
    }

    public OrmLiteRepository<?, ?> createSqliteRepository(
            Class<?> entityClass) {

        return new OrmLiteRepository<>(connectionSource, entityClass);
    }

    public ConnectionSource getConnectionSource() {
        return connectionSource;
    }

    private void closeConnectionSource(Throwable originalException) {
        if (connectionSource == null) {
            return;
        }

        try {
            connectionSource.close();
        } catch (Exception closeException) {
            originalException.addSuppressed(closeException);
        }
    }
}
