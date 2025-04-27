package gov.niem.tools.api.validation;

import gov.niem.tools.api.core.utils.FileUtils;
import gov.niem.tools.api.validation.xml.XmlValidationUtils;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Path;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.Source;
import javax.xml.transform.stream.StreamSource;
import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathConstants;
import javax.xml.xpath.XPathExpressionException;
import javax.xml.xpath.XPathFactory;
import org.springframework.core.io.ClassPathResource;
import org.springframework.core.io.Resource;
import org.springframework.core.io.support.PathMatchingResourcePatternResolver;
import org.springframework.util.xml.SimpleNamespaceContext;
import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

/**
 * Utility functions supporting validation.
 */
public class ValidationUtils {

  public static final String TEMP_DIR_BASE = "niem-api-tmp-";
  public static final String TEMP_DIR_PREFIX = "validation-zips";

  public static Path createTempFolder() throws IOException {
    return FileUtils.createTempDir(XmlValidationUtils.TEMP_DIR_PREFIX);
  }

  /**
   * Saves the file in the resources directory with the given class path to
   * the given temporary path and returns the file.
   */
  public static File convertClassPathResourceToFile(Path tempPath, String classPath,
      String filename) throws IOException {
    ClassPathResource resource = new ClassPathResource(classPath);
    File file = FileUtils.file(tempPath + "/" + filename);
    FileUtils.saveFile(file.toPath(), resource.getContentAsByteArray());
    return file;
  }

  /**
   * Saves all files (may be nested) at the given path in the resources directory to the
   * given temporary path.
   */
  public static void copyClasspathFiles(Path tempPath, String resourcesPathString)
      throws IOException {
    PathMatchingResourcePatternResolver scanner = new PathMatchingResourcePatternResolver();
    Resource[] resources = scanner.getResources(resourcesPathString + "/**/*");

    if (resources == null || resources.length == 0) {
      throw new IOException(resourcesPathString + " resources not found");
    }

    for (Resource resource : resources) {
      File file = FileUtils.file(tempPath + "/" + resource.getFilename());
      FileUtils.saveFile(file.toPath(), resource.getContentAsByteArray());
    }

  }

  /**
   * Converts XSD files at the given path in the resources directory to source objects
   * that can be used for validation.
   */
  public static Source[] getClasspathXsdSources(String resourcesPathString) throws IOException {
    PathMatchingResourcePatternResolver scanner = new PathMatchingResourcePatternResolver();
    Resource[] resources = scanner.getResources(resourcesPathString + "/**/*.xsd");

    if (resources == null || resources.length == 0) {
      throw new IOException(resourcesPathString + " resources not found");
    }

    int i = 0;
    Source[] sources = new Source[resources.length];

    for (Resource resource : resources) {
      InputStream inputStream = resource.getInputStream();
      sources[i] = new StreamSource(inputStream);
      sources[i++].setSystemId(resource.getURI().toString());
    }

    return sources;
  }

  /**
   * Converts an XML text file to a DOM document with nodes and other DOM objects.
   */
  public static Document getDocument(File file)
      throws ParserConfigurationException, SAXException, IOException  {
    DocumentBuilder db = DocumentBuilderFactory.newInstance().newDocumentBuilder();
    Document document = db.parse(file);
    return document;
  }

  /**
   * Gets an object that can be used to evaluate xPath expressions that binds
   * namespace prefixes `xs` and `ct` to the URIs for XML Schema and the NIEM
   * conformance targets attribute.
   *
   * @todo Fix conformance target support for 6.0
   */
  public static XPath getXpath() throws ParserConfigurationException, SAXException, IOException {

    SimpleNamespaceContext namespaceContext = new SimpleNamespaceContext();
    namespaceContext.bindNamespaceUri("xs", "http://www.w3.org/2001/XMLSchema");
    namespaceContext.bindNamespaceUri("ct", "http://release.niem.gov/niem/conformanceTargets/3.0/");

    XPath xpath = XPathFactory.newInstance().newXPath();
    xpath.setNamespaceContext(namespaceContext);

    return xpath;

  }

  /**
   * Gets the values of the given schema-level attribute.
   *
   * @todo Fix expression hardcoded to conformance targets.
   */
  public static String getXsdRootAttributeValue(Document document, XPath xpath,
      String attributeName) throws XPathExpressionException {
    // TODO: Fix namespace-aware xpath
    // String expression = "//xs:schema/@ct:conformanceTargets";
    String expression = "//*[local-name()='schema']/@*[local-name()='conformanceTargets']";
    return ValidationUtils.getXpathResultText(document, xpath, expression);
  }

  /**
   * Gets a NodeList for all nodes matching the given xPath expression.
   */
  public static NodeList getXpathResults(Document document, XPath xpath, String expression)
      throws XPathExpressionException {
    NodeList nodeList = (NodeList) xpath.evaluate(expression, document, XPathConstants.NODESET);
    return nodeList;
  }

  /**
   * Gets the node at the given xPath expression.
   */
  public static Node getXpathResult(Document document, XPath xpath, String expression)
      throws XPathExpressionException {
    Node node = (Node) xpath.evaluate(expression, document, XPathConstants.NODE);
    return node;
  }

  /**
   * Gets the text content of the node at the given xPath expression.
   */
  public static String getXpathResultText(Document document, XPath xpath, String expression)
      throws XPathExpressionException {
    Node node = (Node) xpath.evaluate(expression, document, XPathConstants.NODE);
    return node == null ? null : node.getTextContent();
  }

}
