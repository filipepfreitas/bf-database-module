package io.github.blockforge.database.providers.ormlite.configuration;

public final class ConfigFileProperties {

    private ConfigFileProperties() {
    }

    // ============================================================
    // Root
    // ============================================================

    public static final String PERSISTENCE_TAG = "persistence";


    // ============================================================
    // Context
    // ============================================================

    public static final String CONTEXT_TAG = "context";

    public static final String CONTEXT_NAME_PROPERTY = "name";


    // ============================================================
    // Context Configuration
    // ============================================================

    public static final String CONTEXT_CONFIG_TAG = "context-config";

    public static final String VALUE_PROPERTY = "value";


    // ============================================================
    // Database Connection
    // ============================================================

    public static final String URL_PROPERTY = "url";

    public static final String DATABASE_PROVIDER_PROPERTY = "database-provider";

    public static final String SQLITE_VALUE = "sqlite";

    public static final String SQLCIPHER_VALUE = "sqlcipher";


    // ============================================================
    // Schema / DDL
    // ============================================================

    public static final String DDL_AUTO_PROPERTY = "ddl-auto";

    public static final String CREATE_VALUE = "create";

    public static final String ENSURE_VALUE = "ensure";

    public static final String DROP_VALUE = "drop";

    public static final String CLEAR_VALUE = "clear";

    public static final String NONE_VALUE = "none";


    // ============================================================
    // Application Scanning
    // ============================================================

    public static final String PACKAGE_PROPERTY = "package";


    // ============================================================
    // Connection Pool
    // ============================================================

    public static final String POOL_SIZE_PROPERTY = "pool-size";

    public static final String CONNECTION_TIMEOUT_PROPERTY = "connection-timeout";


    // ============================================================
    // Entity Scanning
    // ============================================================

    public static final String ENTITY_CONFIG_TAG = "entity-config";

    public static final String ENTITY_CLASS_PROPERTY = "class";

    public static final String DEFAULT_PACKAGE_VALUE = "default";

}