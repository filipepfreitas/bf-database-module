package io.github.filsynyster.blockforge.database.providers.ormlite.configuration;

/**
 * Representa as configurações de infraestrutura de um contexto
 * de persistência.
 *
 * <p>Esta classe corresponde ao elemento
 * {@code <context-config>} do XML.</p>
 *
 * <p>São armazenadas configurações relacionadas ao banco de dados,
 * esquema, escaneamento da aplicação e pool de conexões.</p>
 */
public class ContextConfig {

    private final String url;
    private final String databaseProvider;
    private final String ddlAuto;
    private final String rootPackage;
    private final int poolSize;
    private final long connectionTimeout;

    /**
     * Cria uma configuração de contexto.
     *
     * @param url URL JDBC do banco de dados
     * @param databaseProvider provedor do banco de dados
     * @param ddlAuto estratégia de inicialização do esquema
     * @param rootPackage pacote raiz da aplicação
     * @param poolSize quantidade de conexões do pool
     * @param connectionTimeout tempo máximo de espera por conexão
     */
    public ContextConfig(
            String url,
            String databaseProvider,
            String ddlAuto,
            String rootPackage,
            int poolSize,
            long connectionTimeout
    ) {
        this.url = url;
        this.databaseProvider = databaseProvider;
        this.ddlAuto = ddlAuto;
        this.rootPackage = rootPackage;
        this.poolSize = poolSize;
        this.connectionTimeout = connectionTimeout;
    }

    /**
     * Retorna a URL JDBC do banco.
     *
     * @return URL do banco
     */
    public String getUrl() {
        return url;
    }

    /**
     * Retorna o provedor do banco de dados.
     *
     * @return provedor configurado
     */
    public String getDatabaseProvider() {
        return databaseProvider;
    }

    /**
     * Retorna a estratégia de inicialização do esquema.
     *
     * @return estratégia DDL
     */
    public String getDdlAuto() {
        return ddlAuto;
    }

    /**
     * Retorna o pacote raiz da aplicação.
     *
     * @return pacote raiz
     */
    public String getRootPackage() {
        return rootPackage;
    }

    /**
     * Retorna a quantidade máxima de conexões do pool.
     *
     * @return tamanho do pool
     */
    public int getPoolSize() {
        return poolSize;
    }

    /**
     * Retorna o tempo máximo de espera por uma conexão.
     *
     * <p>O valor é expresso em milissegundos.</p>
     *
     * @return timeout em milissegundos
     */
    public long getConnectionTimeout() {
        return connectionTimeout;
    }

    @Override
    public String toString() {
        return "ContextConfig{" +
                "url='" + url + '\'' +
                ", databaseProvider='" + databaseProvider + '\'' +
                ", ddlAuto='" + ddlAuto + '\'' +
                ", rootPackage='" + rootPackage + '\'' +
                ", poolSize=" + poolSize +
                ", connectionTimeout=" + connectionTimeout +
                '}';
    }
}