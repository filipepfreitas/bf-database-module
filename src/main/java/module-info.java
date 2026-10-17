module bf.database.module {
    requires ormlite.jdbc;

    requires java.sql;
    requires io.github.classgraph;
    requires jakarta.persistence;
    requires eclipselink;
    requires jakarta.activation;

    exports io.github.blockforge.database.main;
    exports io.github.blockforge.database.providers.ormlite;
    exports io.github.blockforge.database.sql;
    exports io.github.blockforge.database.providers.ormlite.configuration;
    exports io.github.blockforge.database.exceptions;
    exports io.github.blockforge.database.contracts;
    exports io.github.blockforge.database.infra;
}