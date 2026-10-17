package io.github.blockforge.database.sql;

import java.nio.file.Path;
import java.util.HashMap;
import java.util.Map;

public class SQLDocument{

    private String repositoryClass;
    private Path file;
    private final Map<String, String> scriptsMap = new HashMap<>();

    public SQLDocument(String repositoryClass, Path file,  Map<String, String> scriptsMap) {
        this.repositoryClass = repositoryClass;
        this.file = file;
        this.scriptsMap.putAll(scriptsMap);
    }

    public String getRepositoryClass() {
        return repositoryClass;
    }

    public void setRepositoryClass(String repositoryClass) {
        this.repositoryClass = repositoryClass;
    }

    public Path getFile() {
        return file;
    }

    public void setFile(Path file) {
        this.file = file;
    }

    public String getScript(String bind) {
        return scriptsMap.get(bind);
    }

    public void setScript(String bind, String script) {
        scriptsMap.put(bind, script);
    }

    public boolean containsScript(String bind) {
        return scriptsMap.containsKey(bind);
    }
}