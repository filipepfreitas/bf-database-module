module agenda {

    requires javafx.base;
    requires javafx.graphics;
    requires javafx.controls;
    requires javafx.fxml;
    requires javafx.media;
    requires javafx.swing;
    requires javafx.web;

    requires ormlite.jdbc;

    requires java.sql;
    requires io.github.classgraph;
    requires jakarta.persistence;

    opens io.github.filsynyster.blockforge.app.models to ormlite.jdbc;
    opens agenda.app.view.controller to javafx.fxml;

    exports io.github.filsynyster.blockforge.app;
    exports io.github.filsynyster.blockforge.database.core;
    exports io.github.filsynyster.blockforge.database.providers.ormlite;
    exports io.github.filsynyster.blockforge.database.sql;
    exports io.github.filsynyster.blockforge.database.providers.ormlite.configuration;
    exports io.github.filsynyster.blockforge.database.exceptions;
    exports io.github.filsynyster.blockforge.database.core.repository;
}