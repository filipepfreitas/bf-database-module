package io.github.filsynyster.blockforge.database.providers.ormlite.configuration;

import java.util.Arrays;

/**
 * Representa um documento de configuração de persistência.
 *
 * <p>O documento corresponde ao elemento raiz
 * {@code <persistence>} do arquivo XML.</p>
 *
 * <p>Um documento pode possuir um ou vários
 * {@link Context contexts}.</p>
 */
public class ConfigDocument {

    private final Context[] contexts;

    /**
     * Cria um documento de configuração.
     *
     * @param contexts contextos definidos no documento
     */
    public ConfigDocument(Context[] contexts) {
        this.contexts = contexts != null
                ? contexts.clone()
                : new Context[0];
    }

    /**
     * Retorna os nomes de todos os contextos.
     *
     * @return array contendo os nomes dos contextos
     */
    public String[] getContextNames() {

        String[] names = new String[contexts.length];

        for (int i = 0; i < contexts.length; i++) {
            names[i] = contexts[i].getName();
        }

        return names;
    }

    /**
     * Retorna todos os contextos.
     *
     * @return array de contextos
     */
    public Context[] getContexts() {
        return contexts.clone();
    }

    /**
     * Localiza um contexto pelo nome.
     *
     * @param name nome do contexto
     * @return contexto encontrado ou {@code null}
     */
    public Context getContextByName(String name) {

        if (name == null) {
            return null;
        }

        for (Context context : contexts) {

            if (name.equals(context.getName())) {
                return context;
            }
        }

        throw new ConfigurationException("Context with name " + name + " not found");
    }

    @Override
    public String toString() {
        return "ConfigDocument{" +
                "contexts=" + Arrays.toString(contexts) +
                '}';
    }
}