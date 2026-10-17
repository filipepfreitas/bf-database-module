package io.github.blockforge.database.contracts;

/**
 * Representa um contexto de persistência responsável por gerenciar
 * as entidades pertencentes a uma determinada unidade de persistência,
 * incluindo suas operações de acesso, armazenamento e sincronização
 * com a fonte de dados através de {@link DataRepository}.
 * <br/>
 * Pode ser implementada por vários tipos diferentes de provedores de dados como {@code OrmLite} e
 * {@code JPA EclipseLink}.
 */
public interface PersistenceContext {

    public void initialize();

    public void register(Class<?> entity, DataRepository<?,?> repository);

    public <R extends DataRepository<?,?>> R getRepository(Class<?> entity);

    public void close();
}
