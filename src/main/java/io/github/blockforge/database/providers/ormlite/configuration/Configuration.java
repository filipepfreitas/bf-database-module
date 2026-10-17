
package io.github.blockforge.database.providers.ormlite.configuration;

import io.github.blockforge.database.exceptions.ConfigurationException;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import javax.xml.XMLConstants;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;

/**
 * Loads and validates persistence configuration files.
 *
 * <p>The configuration can be loaded from a physical file or
 * from a resource available on the classpath.</p>
 *
 * <p>The XML document is parsed into a {@link ConfigDocument},
 * with structural and property validation.</p>
 */
public final class Configuration {

    private Configuration() {
    }

    /**
     * Loads a configuration file and converts it into a ConfigDocument.
     *
     * @param configFile physical file path or classpath resource
     * @return the parsed configuration document
     */
    public static ConfigDocument loadConfiguration(String configFile) {
        return new Configuration().load(configFile);
    }

    /**
     * Loads and validates a persistence configuration.
     *
     * @param file physical file path or classpath resource name
     * @return the parsed configuration document
     */
    public ConfigDocument load(String file) {
        if (file == null || file.isBlank()) {
            throw new ConfigurationException(
                    "The configuration file path must not be empty."
            );
        }

        try (InputStream input = openConfiguration(file)) {
            Document document = createDocument(input);
            Element root = document.getDocumentElement();

            validateRoot(root);

            Context[] contexts = parseContexts(root);

            return new ConfigDocument(contexts);

        } catch (ConfigurationException e) {
            throw e;

        } catch (Exception e) {
            throw new ConfigurationException(
                    "Failed to load configuration file: " + file, e
            );
        }
    }

    /**
     * Opens a configuration from the file system or the classpath.
     *
     * @param file physical file path or classpath resource name
     * @return an open input stream
     * @throws IOException if the configuration cannot be found or opened
     */
    private InputStream openConfiguration(String file) throws IOException {
        Path path;

        try {
            path = Path.of(file);
        } catch (RuntimeException e) {
            throw new ConfigurationException(
                    "Invalid configuration file path: " + file, e
            );
        }

        if (Files.isRegularFile(path)) {
            return Files.newInputStream(path);
        }

        String resourceName = file.startsWith("/")
                ? file.substring(1)
                : file;

        InputStream input = Configuration.class
                .getClassLoader()
                .getResourceAsStream(resourceName);

        if (input == null) {
            throw new ConfigurationException(
                    "Configuration file not found: " + file
            );
        }

        return input;
    }

    /**
     * Creates a DOM document from the supplied XML stream.
     * External entities and external DTD access are disabled.
     *
     * @param inputStream the XML input stream
     * @return the parsed DOM document
     * @throws Exception if the parser cannot be configured or the XML is invalid
     */
    private Document createDocument(InputStream inputStream) throws Exception {
        DocumentBuilderFactory factory =
                DocumentBuilderFactory.newInstance();

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

        factory.setAttribute(XMLConstants.ACCESS_EXTERNAL_DTD, "");
        factory.setAttribute(XMLConstants.ACCESS_EXTERNAL_SCHEMA, "");

        DocumentBuilder builder = factory.newDocumentBuilder();

        return builder.parse(inputStream);
    }

    private void validateRoot(Element root) {
        if (root == null) {
            throw new ConfigurationException(
                    "The XML document has no root element."
            );
        }

        if (!ConfigFileProperties.PERSISTENCE_TAG.equals(root.getTagName())) {
            throw new ConfigurationException(
                    "The root element must be <persistence>."
            );
        }
    }

    private Context[] parseContexts(Element root) {
        List<Element> elements = getDirectChildren(
                root,
                ConfigFileProperties.CONTEXT_TAG
        );

        List<Context> contexts = new ArrayList<>();

        for (Element element : elements) {
            contexts.add(parseContext(element));
        }

        return contexts.toArray(new Context[0]);
    }

    private Context parseContext(Element element) {
        String name = getRequiredAttribute(
                element,
                ConfigFileProperties.CONTEXT_NAME_PROPERTY
        );

        Element contextConfigElement = getSingleOptionalChild(
                element,
                ConfigFileProperties.CONTEXT_CONFIG_TAG
        );

        if (contextConfigElement == null) {
            throw new ConfigurationException(
                    "Context '" + name + "' must contain <context-config>."
            );
        }

        ContextConfig contextConfig =
                parseContextConfig(contextConfigElement);

        Element entityConfigElement = getSingleOptionalChild(
                element,
                ConfigFileProperties.ENTITY_CONFIG_TAG
        );

        EntityConfig entityConfig = entityConfigElement != null
                ? parseEntityConfig(entityConfigElement)
                : new EntityConfig(
                new EntityPackage[0],
                new EntityClass[0]
        );

        return new Context(name, contextConfig, entityConfig);
    }

    private ContextConfig parseContextConfig(Element element) {
        Element urlElement = getSingleRequiredChild(
                element,
                ConfigFileProperties.URL_PROPERTY
        );

        String url = getRequiredAttribute(
                urlElement,
                ConfigFileProperties.VALUE_PROPERTY
        );

        Element providerElement = getSingleRequiredChild(
                element,
                ConfigFileProperties.DATABASE_PROVIDER_PROPERTY
        );

        String databaseProvider = getRequiredAttribute(
                providerElement,
                ConfigFileProperties.VALUE_PROPERTY
        );

        validateDatabaseProvider(databaseProvider);

        Element ddlElement = getSingleRequiredChild(
                element,
                ConfigFileProperties.DDL_AUTO_PROPERTY
        );

        String ddlAuto = getRequiredAttribute(
                ddlElement,
                ConfigFileProperties.VALUE_PROPERTY
        );

        validateDdlAuto(ddlAuto);

        Element packageElement = getSingleRequiredChild(
                element,
                ConfigFileProperties.PACKAGE_PROPERTY
        );

        String rootPackage = getRequiredAttribute(
                packageElement,
                ConfigFileProperties.VALUE_PROPERTY
        );

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

    private EntityConfig parseEntityConfig(Element element) {
        List<EntityPackage> packages = parseEntityPackages(element);
        List<EntityClass> classes = parseEntityClasses(element);

        return new EntityConfig(
                packages.toArray(new EntityPackage[0]),
                classes.toArray(new EntityClass[0])
        );
    }

    private List<EntityPackage> parseEntityPackages(Element entityConfig) {
        List<Element> elements = getDirectChildren(
                entityConfig,
                ConfigFileProperties.PACKAGE_PROPERTY
        );

        List<EntityPackage> packages = new ArrayList<>();

        for (Element element : elements) {
            String value = getRequiredAttribute(
                    element,
                    ConfigFileProperties.VALUE_PROPERTY
            );

            packages.add(new EntityPackage(value));
        }

        return packages;
    }

    private List<EntityClass> parseEntityClasses(Element entityConfig) {
        List<Element> elements = getDirectChildren(
                entityConfig,
                ConfigFileProperties.ENTITY_CLASS_PROPERTY
        );

        List<EntityClass> classes = new ArrayList<>();

        for (Element element : elements) {
            String value = getRequiredAttribute(
                    element,
                    ConfigFileProperties.VALUE_PROPERTY
            );

            classes.add(new EntityClass(value));
        }

        return classes;
    }

    private void validateDatabaseProvider(String provider) {
        if (!ConfigFileProperties.SQLITE_VALUE.equals(provider)
                && !ConfigFileProperties.SQLCIPHER_VALUE.equals(provider)) {

            throw new ConfigurationException(
                    "Invalid database provider: '" + provider
                            + "'. Allowed values: sqlite, sqlcipher."
            );
        }
    }

    private void validateDdlAuto(String value) {
        if (!ConfigFileProperties.CREATE_VALUE.equals(value)
                && !ConfigFileProperties.ENSURE_VALUE.equals(value)
                && !ConfigFileProperties.DROP_VALUE.equals(value)
                && !ConfigFileProperties.CLEAR_VALUE.equals(value)
                && !ConfigFileProperties.NONE_VALUE.equals(value)) {

            throw new ConfigurationException(
                    "Invalid ddl-auto value: '" + value
                            + "'. Allowed values: create, ensure, drop, clear, none."
            );
        }
    }

    private int parsePositiveInt(String value, String property) {
        final int result;

        try {
            result = Integer.parseInt(value);
        } catch (NumberFormatException e) {
            throw new ConfigurationException(
                    "Invalid integer value for property '" + property
                            + "': " + value,
                    e
            );
        }

        if (result <= 0) {
            throw new ConfigurationException(
                    "Property '" + property + "' must be greater than zero."
            );
        }

        return result;
    }

    private long parsePositiveLong(String value, String property) {
        final long result;

        try {
            result = Long.parseLong(value);
        } catch (NumberFormatException e) {
            throw new ConfigurationException(
                    "Invalid long value for property '" + property
                            + "': " + value,
                    e
            );
        }

        if (result <= 0) {
            throw new ConfigurationException(
                    "Property '" + property + "' must be greater than zero."
            );
        }

        return result;
    }

    private Element getSingleRequiredChild(Element parent, String tag) {
        List<Element> elements = getDirectChildren(parent, tag);

        if (elements.isEmpty()) {
            throw new ConfigurationException(
                    "Required element <" + tag + "> is missing."
            );
        }

        if (elements.size() > 1) {
            throw new ConfigurationException(
                    "Element <" + tag + "> must appear only once."
            );
        }

        return elements.get(0);
    }

    private Element getSingleOptionalChild(Element parent, String tag) {
        List<Element> elements = getDirectChildren(parent, tag);

        if (elements.size() > 1) {
            throw new ConfigurationException(
                    "Element <" + tag + "> must appear at most once."
            );
        }

        return elements.isEmpty() ? null : elements.get(0);
    }

    private List<Element> getDirectChildren(Element parent, String tag) {
        List<Element> result = new ArrayList<>();
        NodeList children = parent.getChildNodes();

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

    private String getRequiredAttribute(Element element, String attribute) {
        if (!element.hasAttribute(attribute)) {
            throw new ConfigurationException(
                    "Element <" + element.getTagName()
                            + "> must have attribute '" + attribute + "'."
            );
        }

        String value = element.getAttribute(attribute).trim();

        if (value.isEmpty()) {
            throw new ConfigurationException(
                    "Attribute '" + attribute + "' on element <"
                            + element.getTagName() + "> must not be empty."
            );
        }

        return value;
    }
}
