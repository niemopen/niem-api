package gov.niem.tools.api.validation;

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

import gov.niem.tools.api.core.utils.FileUtils;
import gov.niem.tools.api.validation.xml.XmlValidationUtils;

public class ValidationUtils {

  public static final String TEMP_DIR_BASE = "niem-api-tmp-";
  public static final String TEMP_DIR_PREFIX = "validation-zips";

  public static Path createTempFolder() throws IOException {
    return FileUtils.createTempDir(XmlValidationUtils.TEMP_DIR_PREFIX);
  }

  public static File convertClassPathResourceToFile(Path tempPath, String classPath, String filename) throws IOException {
    ClassPathResource resource = new ClassPathResource(classPath);
    File file = FileUtils.file(tempPath + "/" + filename);
    FileUtils.saveFile(file.toPath(), resource.getContentAsByteArray());
    return file;
  }

  public static void copyClasspathFiles(Path tempPath, String resourcesPathString) throws IOException {
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

  public static Document getDocument(File file) throws ParserConfigurationException, SAXException, IOException  {
    DocumentBuilder db = DocumentBuilderFactory.newInstance().newDocumentBuilder();
    Document document = db.parse(file);
    return document;
  }

  public static XPath getXpath() throws ParserConfigurationException, SAXException, IOException {

    SimpleNamespaceContext namespaceContext = new SimpleNamespaceContext();
    namespaceContext.bindNamespaceUri("xs", "http://www.w3.org/2001/XMLSchema");
    namespaceContext.bindNamespaceUri("ct", "http://release.niem.gov/niem/conformanceTargets/3.0/");

    XPath xpath = XPathFactory.newInstance().newXPath();
    xpath.setNamespaceContext(namespaceContext);

    return xpath;

  }

  public static String getXsdRootAttributeValue(Document document, XPath xpath, String attributeName) throws XPathExpressionException {
    // TODO: Fix namespace-aware xpath
    // String expression = "//xs:schema/@ct:conformanceTargets";
    String expression = "//*[local-name()='schema']/@*[local-name()='conformanceTargets']";
    return ValidationUtils.getXpathResultText(document, xpath, expression);
  }

  public static NodeList getXpathResults(Document document, XPath xPath, String expression) throws XPathExpressionException {
    NodeList nodeList = (NodeList) xPath.evaluate(expression, document, XPathConstants.NODESET);
    return nodeList;
  }

  public static Node getXpathResult(Document document, XPath xPath, String expression) throws XPathExpressionException {
    Node node = (Node) xPath.evaluate(expression, document, XPathConstants.NODE);
    return node;
  }

  public static String getXpathResultText(Document document, XPath xPath, String expression) throws XPathExpressionException {
    Node node = (Node) xPath.evaluate(expression, document, XPathConstants.NODE);
    return node == null ? null : node.getTextContent();
  }

}
