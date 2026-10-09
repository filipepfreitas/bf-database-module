package io.github.filsynyster.blockforge.database.providers.ormlite.configuration;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import javax.xml.XMLConstants;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import java.io.FileNotFoundException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;

/**
 * Responsável pelo carregamento e validação dos arquivos
 * de configuração de persistência.
 *
 * <p>O parser converte o XML em um {@link ConfigDocument}.</p>
 *
 * <p>Além de realizar o parsing, esta classe valida as regras
 * estruturais da configuração.</p>
 */
public class Configuration {

    /**
     * Carrega e valida um arquivo XML de configuração.
     *
     * @param file caminho do arquivo XML
     * @return documento de configuração
     * @throws ConfigurationException caso o arquivo seja inválido
     */
    public ConfigDocument load(String file) {

        if (file == null || file.isBlank()) {
            throw new ConfigurationException(
                    "O caminho do arquivo de configuração não pode ser vazio."
            );
        }

        try {

            InputStream input = Configuration.class.getClassLoader().getResourceAsStream(file);
            if (input == null) {
                throw new FileNotFoundException("Arquivo não encontrado no classpath: " + file);
            }

            Document document = createDocument(input);

            Element root = document.getDocumentElement();

            validateRoot(root);

            Context[] contexts = parseContexts(root);

            return new ConfigDocument(contexts);

        } catch (ConfigurationException e) {
            throw e;

        } catch (Exception e) {
            throw new ConfigurationException(
                    "Não foi possível carregar o arquivo de configuração: "
                            + file,
                    e
            );
        }
    }

    /**
     * Cria o documento DOM a partir do arquivo XML.
     */
    private Document createDocument(InputStream inputStream) throws Exception {

        DocumentBuilderFactory factory =
                DocumentBuilderFactory.newInstance();

        /*
         * Proteções contra XXE e outras formas de processamento
         * externo de entidades XML.
         */
        factory.setFeature(
                "http://apache.org/xml/features/disallow-doctype-decl",
                true
        );

        factory.setFeature(
                "http://xml.org/sax/features/external-general-entities",
                false
        );

        factory.setFeature(
                "http://xml.org/sax/features/external-parameter-entities",
                false
        );

        factory.setFeature(
                "http://apache.org/xml/features/nonvalidating/load-external-dtd",
                false
        );

        factory.setXIncludeAware(false);
        factory.setExpandEntityReferences(false);

        factory.setAttribute(
                XMLConstants.ACCESS_EXTERNAL_DTD,
                ""
        );

        factory.setAttribute(
                XMLConstants.ACCESS_EXTERNAL_SCHEMA,
                ""
        );

        DocumentBuilder builder =
                factory.newDocumentBuilder();

        return builder.parse(inputStream);
    }

    /**
     * Valida o elemento raiz.
     */
    private void validateRoot(Element root) {

        if (root == null) {
            throw new ConfigurationException(
                    "O documento XML não possui elemento raiz."
            );
        }

        if (!ConfigFileProperties.PERSISTENCE_TAG
                .equals(root.getTagName())) {

            throw new ConfigurationException(
                    "O elemento raiz deve ser <persistence>."
            );
        }
    }

    /**
     * Processa todos os contextos.
     */
    private Context[] parseContexts(Element root) {

        List<Element> elements =
                getDirectChildren(
                        root,
                        ConfigFileProperties.CONTEXT_TAG
                );

        List<Context> contexts = new ArrayList<>();

        for (Element element : elements) {
            contexts.add(parseContext(element));
        }

        return contexts.toArray(new Context[0]);
    }

    /**
     * Processa um <context>.
     */
    private Context parseContext(Element element) {

        String name = getRequiredAttribute(
                element,
                ConfigFileProperties.CONTEXT_NAME_PROPERTY
        );

        Element contextConfigElement =
                getSingleOptionalChild(
                        element,
                        ConfigFileProperties.CONTEXT_CONFIG_TAG
                );

        if (contextConfigElement == null) {
            throw new ConfigurationException(
                    "O <context> '" + name +
                            "' deve possuir <context-config>."
            );
        }

        ContextConfig contextConfig =
                parseContextConfig(contextConfigElement);

        Element entityConfigElement =
                getSingleOptionalChild(
                        element,
                        ConfigFileProperties.ENTITY_CONFIG_TAG
                );

        EntityConfig entityConfig =
                entityConfigElement != null
                        ? parseEntityConfig(entityConfigElement)
                        : new EntityConfig(
                        new EntityPackage[0],
                        new EntityClass[0]
                );

        return new Context(
                name,
                contextConfig,
                entityConfig
        );
    }

    /**
     * Processa <context-config>.
     */
    private ContextConfig parseContextConfig(Element element) {

        /*
         * ---------------------------------------------------------
         * URL
         * ---------------------------------------------------------
         *
         * Obrigatória.
         * Apenas uma ocorrência.
         */
        Element urlElement = getSingleRequiredChild(
                element,
                ConfigFileProperties.URL_PROPERTY
        );

        String url = getRequiredAttribute(
                urlElement,
                ConfigFileProperties.VALUE_PROPERTY
        );


        /*
         * ---------------------------------------------------------
         * DATABASE PROVIDER
         * ---------------------------------------------------------
         *
         * Obrigatório.
         * Apenas uma ocorrência.
         */
        Element providerElement = getSingleRequiredChild(
                element,
                ConfigFileProperties.DATABASE_PROVIDER_PROPERTY
        );

        String databaseProvider = getRequiredAttribute(
                providerElement,
                ConfigFileProperties.VALUE_PROPERTY
        );

        validateDatabaseProvider(databaseProvider);


        /*
         * ---------------------------------------------------------
         * DDL AUTO
         * ---------------------------------------------------------
         *
         * Obrigatório.
         * Apenas uma ocorrência.
         */
        Element ddlElement = getSingleRequiredChild(
                element,
                ConfigFileProperties.DDL_AUTO_PROPERTY
        );

        String ddlAuto = getRequiredAttribute(
                ddlElement,
                ConfigFileProperties.VALUE_PROPERTY
        );

        validateDdlAuto(ddlAuto);


        /*
         * ---------------------------------------------------------
         * ROOT PACKAGE
         * ---------------------------------------------------------
         *
         * <package> dentro de context-config.
         *
         * Obrigatório.
         * Apenas uma ocorrência.
         */
        Element packageElement = getSingleRequiredChild(
                element,
                ConfigFileProperties.PACKAGE_PROPERTY
        );

        String rootPackage = getRequiredAttribute(
                packageElement,
                ConfigFileProperties.VALUE_PROPERTY
        );


        /*
         * ---------------------------------------------------------
         * POOL SIZE
         * ---------------------------------------------------------
         *
         * Opcional.
         * No máximo uma ocorrência.
         */
        Element poolElement = getSingleOptionalChild(
                element,
                ConfigFileProperties.POOL_SIZE_PROPERTY
        );

        int poolSize = 0;

        if (poolElement != null) {

            String value = getRequiredAttribute(
                    poolElement,
                    ConfigFileProperties.VALUE_PROPERTY
            );

            poolSize = parsePositiveInt(
                    value,
                    ConfigFileProperties.POOL_SIZE_PROPERTY
            );
        }


        /*
         * ---------------------------------------------------------
         * CONNECTION TIMEOUT
         * ---------------------------------------------------------
         *
         * Opcional.
         * No máximo uma ocorrência.
         */
        Element timeoutElement = getSingleOptionalChild(
                element,
                ConfigFileProperties.CONNECTION_TIMEOUT_PROPERTY
        );

        long connectionTimeout = 0;

        if (timeoutElement != null) {

            String value = getRequiredAttribute(
                    timeoutElement,
                    ConfigFileProperties.VALUE_PROPERTY
            );

            connectionTimeout = parsePositiveLong(
                    value,
                    ConfigFileProperties.CONNECTION_TIMEOUT_PROPERTY
            );
        }

        return new ContextConfig(
                url,
                databaseProvider,
                ddlAuto,
                rootPackage,
                poolSize,
                connectionTimeout
        );
    }

    /**
     * Processa <entity-config>.
     */
    private EntityConfig parseEntityConfig(Element element) {

        List<EntityPackage> packages =
                parseEntityPackages(element);

        List<EntityClass> classes =
                parseEntityClasses(element);

        return new EntityConfig(
                packages.toArray(new EntityPackage[0]),
                classes.toArray(new EntityClass[0])
        );
    }

    /**
     * Processa os <package> dentro de <entity-config>.
     *
     * <p>Podem existir zero, um ou vários.</p>
     */
    private List<EntityPackage> parseEntityPackages(
            Element entityConfig
    ) {

        List<Element> elements =
                getDirectChildren(
                        entityConfig,
                        ConfigFileProperties.PACKAGE_PROPERTY
                );

        List<EntityPackage> packages =
                new ArrayList<>();

        for (Element element : elements) {

            String value = getRequiredAttribute(
                    element,
                    ConfigFileProperties.VALUE_PROPERTY
            );

            packages.add(
                    new EntityPackage(value)
            );
        }

        return packages;
    }

    /**
     * Processa os <class> dentro de <entity-config>.
     *
     * <p>Podem existir zero, uma ou várias.</p>
     */
    private List<EntityClass> parseEntityClasses(
            Element entityConfig
    ) {

        List<Element> elements =
                getDirectChildren(
                        entityConfig,
                        ConfigFileProperties.ENTITY_CLASS_PROPERTY
                );

        List<EntityClass> classes =
                new ArrayList<>();

        for (Element element : elements) {

            String value = getRequiredAttribute(
                    element,
                    ConfigFileProperties.VALUE_PROPERTY
            );

            classes.add(
                    new EntityClass(value)
            );
        }

        return classes;
    }

    /**
     * Valida o provedor de banco.
     */
    private void validateDatabaseProvider(
            String provider
    ) {

        if (!ConfigFileProperties.SQLITE_VALUE.equals(provider)
                && !ConfigFileProperties.SQLCIPHER_VALUE.equals(provider)) {

            throw new ConfigurationException(
                    "Database provider inválido: '" +
                            provider +
                            "'. Valores permitidos: sqlite, sqlcipher."
            );
        }
    }

    /**
     * Valida o valor de ddl-auto.
     */
    private void validateDdlAuto(String value) {

        if (!ConfigFileProperties.CREATE_VALUE.equals(value)
                && !ConfigFileProperties.ENSURE_VALUE.equals(value)
                && !ConfigFileProperties.DROP_VALUE.equals(value)
                && !ConfigFileProperties.CLEAR_VALUE.equals(value)
                && !ConfigFileProperties.NONE_VALUE.equals(value)) {

            throw new ConfigurationException(
                    "Valor inválido para ddl-auto: '" +
                            value +
                            "'. Valores permitidos: create, ensure, drop, clear."
            );
        }
    }

    /**
     * Converte uma String para inteiro positivo.
     */
    private int parsePositiveInt(
            String value,
            String property
    ) {

        try {

            int result = Integer.parseInt(value);

            if (result <= 0) {
                throw new ConfigurationException(
                        "O valor de '" + property +
                                "' deve ser maior que zero."
                );
            }

            return result;

        } catch (NumberFormatException e) {

            throw new ConfigurationException(
                    "Valor inválido para '" +
                            property +
                            "': " +
                            value,
                    e
            );
        }
    }

    /**
     * Converte uma String para long positivo.
     */
    private long parsePositiveLong(
            String value,
            String property
    ) {

        try {

            long result = Long.parseLong(value);

            if (result <= 0) {
                throw new ConfigurationException(
                        "O valor de '" + property +
                                "' deve ser maior que zero."
                );
            }

            return result;

        } catch (NumberFormatException e) {

            throw new ConfigurationException(
                    "Valor inválido para '" +
                            property +
                            "': " +
                            value,
                    e
            );
        }
    }

    /**
     * Obtém um elemento obrigatório e único.
     */
    private Element getSingleRequiredChild(
            Element parent,
            String tag
    ) {

        List<Element> elements =
                getDirectChildren(parent, tag);

        if (elements.isEmpty()) {

            throw new ConfigurationException(
                    "A tag <" + tag +
                            "> é obrigatória."
            );
        }

        if (elements.size() > 1) {

            throw new ConfigurationException(
                    "A tag <" + tag +
                            "> só pode existir uma vez."
            );
        }

        return elements.get(0);
    }

    /**
     * Obtém um elemento opcional e único.
     */
    private Element getSingleOptionalChild(
            Element parent,
            String tag
    ) {

        List<Element> elements =
                getDirectChildren(parent, tag);

        if (elements.size() > 1) {

            throw new ConfigurationException(
                    "A tag <" + tag +
                            "> só pode existir uma vez."
            );
        }

        return elements.isEmpty()
                ? null
                : elements.get(0);
    }

    /**
     * Obtém todos os filhos diretos com determinado nome.
     *
     * <p>Somente filhos diretos são considerados. Isso é importante
     * para diferenciar, por exemplo, o <package> de
     * <context-config> do <package> de <entity-config>.</p>
     */
    private List<Element> getDirectChildren(
            Element parent,
            String tag
    ) {

        List<Element> result = new ArrayList<>();

        NodeList children =
                parent.getChildNodes();

        for (int i = 0; i < children.getLength(); i++) {

            Node node = children.item(i);

            if (node.getNodeType() != Node.ELEMENT_NODE) {
                continue;
            }

            Element element = (Element) node;

            if (tag.equals(element.getTagName())) {
                result.add(element);
            }
        }

        return result;
    }

    /**
     * Obtém um atributo obrigatório.
     */
    private String getRequiredAttribute(
            Element element,
            String attribute
    ) {

        if (!element.hasAttribute(attribute)) {

            throw new ConfigurationException(
                    "A tag <" +
                            element.getTagName() +
                            "> deve possuir o atributo '" +
                            attribute +
                            "'."
            );
        }

        String value =
                element.getAttribute(attribute).trim();

        if (value.isEmpty()) {

            throw new ConfigurationException(
                    "O atributo '" +
                            attribute +
                            "' da tag <" +
                            element.getTagName() +
                            "> não pode ser vazio."
            );
        }

        return value;
    }

    @Override
    public String toString() {
        return "Configuration{}";
    }
}