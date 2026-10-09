package io.github.filsynyster.blockforge.database.providers.ormlite;

import io.github.filsynyster.blockforge.database.core.repository.DataRepository;
import com.j256.ormlite.dao.Dao;
import com.j256.ormlite.dao.DaoManager;
import com.j256.ormlite.dao.GenericRawResults;
import com.j256.ormlite.support.ConnectionSource;
import jakarta.persistence.PersistenceException;

import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.sql.SQLException;
import java.util.*;
import java.util.stream.Stream;

public class OrmLiteRepository<T, ID> implements DataRepository<T, ID> {

    private Dao<T, ID> dao;

    public OrmLiteRepository(ConnectionSource connectionSource, Class<?> clazz) {
        try {
            dao = DaoManager.createDao(connectionSource, (Class<T>)clazz);
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public List<T> findAll() {
        try {
            return dao.queryForAll();
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public T findById(ID id) {
        try {
            return (T) dao.queryForId(id);
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public T create(T entity) {
        try {
            dao.create(entity);
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
        return entity;
    }

    @Override
    public T update(T entity) {
        try {
            dao.update(entity);
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
        return entity;
    }

    @Override
    public boolean deleteById(ID id) {
        try {
            dao.deleteById(id);
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
        return true;
    }

    private Class<?> resolveEntityType(Type returnType) {
        // ==============================================
        // Contact
        // Account
        // String
        // Integer
        // etc.
        // ==============================================
        if (returnType instanceof Class<?> clazz) {
            return clazz;
        }

        // ==============================================
        // List<Contact>
        // Set<Contact>
        // Collection<Contact>
        // Optional<Contact>
        // Stream<Contact>
        // ==============================================

        if (returnType instanceof ParameterizedType parameterizedType) {

            Type[] arguments = parameterizedType.getActualTypeArguments();

            if (arguments.length != 1) {
                throw new PersistenceException("BlockForge says: It was not possible to determine the entity: \"" + returnType+"\"");
            }

            Type entityType = arguments[0];

            if (entityType instanceof Class<?> clazz) {
                return clazz;
            }

            throw new PersistenceException("BlockForge says: The entity type is not a class: \"" + entityType+"\"");
        }

        throw new PersistenceException("BlockForge says: Return type not supported: \"" +returnType+"\"");
    }

    /**
     * Executes a native SQL query and converts its result into the return type
     * requested by the caller.
     *
     * <p>The return type is determined from the {@link java.lang.reflect.Type}
     * provided by the caller, allowing the method to support both simple and
     * parameterized return types.</p>
     *
     * <p>Supported return types are:</p>
     * <ul>
     *     <li>{@code T}</li>
     *     <li>{@code List<T>}</li>
     *     <li>{@code Set<T>}</li>
     *     <li>{@code Optional<T>}</li>
     *     <li>{@code Stream<T>}</li>
     * </ul>
     *
     * <p>The method is provider-independent. Each persistence provider is
     * responsible for translating the native SQL query and its parameters
     * according to its own API and capabilities.</p>
     *
     * @param sql the native SQL query to execute
     * @param returnType the {@link java.lang.reflect.Type} describing the
     *                   expected result type
     * @param args the parameters to bind to the query placeholders
     *
     * @param <R> the type of the query result
     *
     * @return the query result converted to the requested return type
     *
     * @throws PersistenceException if the query cannot be executed or if the
     *                              requested return type is not supported
     */
    @Override
    public <R> R nativeQuery(String sql, Type returnType, String... args) {

        GenericRawResults<T> results = null;

        try {

            // ==================================================
            // Executa a query
            // ==================================================

            if (args == null || args.length == 0 || args[0] == null) {
                results = dao.queryRaw(sql, dao.getRawRowMapper());
            } else {
                results = dao.queryRaw(sql, dao.getRawRowMapper(), args);
            }

            // ==================================================
            // Obtém os resultados
            // ==================================================

            List<T> rawResults = results.getResults();

            // ==================================================
            // Tipo simples
            //
            // Contact
            // Account
            // String
            // Integer
            // Long
            // etc.
            // ==================================================

            if (returnType instanceof Class<?> retType) {

                if (rawResults.isEmpty()) {
                    return null;
                }

                return (R) rawResults.getFirst();
            }

            // ==================================================
            // Tipos parametrizados
            //
            // List<Contact>
            // Set<Contact>
            // Optional<Contact>
            // Stream<Contact>
            // ==================================================

            if (returnType instanceof ParameterizedType parameterizedType) {

                Type rawType = parameterizedType.getRawType();

                if (!(rawType instanceof Class<?> retType)) {
                    throw new PersistenceException("BlockForge says: It was not possible to determine the return type \"" + returnType + "\"");
                }

                // ==================================================
                // List<T>
                // ==================================================

                if (List.class.isAssignableFrom(retType)) {
                    return (R) new ArrayList<>(rawResults);
                }

                // ==================================================
                // Set<T>
                // ==================================================

                if (Set.class.isAssignableFrom(retType)) {
                    return (R) new HashSet<>(rawResults);
                }

                // ==================================================
                // Optional<T>
                // ==================================================
                if (Optional.class.isAssignableFrom(retType)) {
                    if (rawResults.isEmpty()) {
                        return (R) Optional.empty();
                    }
                    return (R) Optional.ofNullable(
                            rawResults.getFirst()
                    );
                }

                // ==================================================
                // Stream<T>
                // ==================================================
                if (Stream.class.isAssignableFrom(retType)) {
                    return (R) rawResults.stream();
                }

                throw new PersistenceException("BlockForge says: Return type not supported \"" + returnType + "\"");
            }

            // ==================================================
            // Tipo desconhecido
            // ==================================================

            throw new PersistenceException("BlockForge says: Invalid return type \"" + returnType + "\"");

        } catch (SQLException e) {

            throw new PersistenceException("BlockForge says: Error on executing query: " + sql, e);
        } finally {

            if (results != null) {
                try {
                    results.close();
                } catch (Exception e) {
                    throw new PersistenceException("BlockForge says: Error on closing GenericRawResults: " + sql, e);
                }
            }
        }
    }

}
