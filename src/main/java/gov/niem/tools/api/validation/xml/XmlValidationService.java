package gov.niem.tools.api.validation.xml;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Path;
import java.util.LinkedList;
import java.util.Set;

import javax.xml.XMLConstants;
import javax.xml.transform.Source;
import javax.xml.transform.stream.StreamSource;
import javax.xml.validation.Schema;
import javax.xml.validation.SchemaFactory;
import javax.xml.validation.Validator;

import org.springframework.core.io.ClassPathResource;
import org.springframework.stereotype.Service;
import org.springframework.util.ResourceUtils;
import org.springframework.web.multipart.MultipartFile;
import org.xml.sax.SAXException;

import gov.niem.tools.api.core.utils.FileUtils;
import gov.niem.tools.api.core.utils.ZipUtils;
import gov.niem.tools.api.validation.Test;
import gov.niem.tools.api.validation.TestResult;
import gov.niem.tools.api.validation.TestResult.Status;
import gov.niem.tools.api.validation.ValidationUtils;
import lombok.extern.log4j.Log4j2;

@Log4j2
@Service
public class XmlValidationService {

  private InputStream[] inputStream;

  private SchemaFactory schemaFactory;

  /**
   * Validate the given XML schema files against the W3C XML Schema specification.
   * @throws IOException
   */
  public Test validateXsd(File[] xsdFiles) throws SAXException, IOException {

    // Setup
    this.schemaFactory = this.getSchemaFactory(xsdFiles);

    try {
      // Creating a schema validates the converted input files
      Source[] sources = this.convertFilesToSource(xsdFiles);
      this.schemaFactory.newSchema(sources);
    }
    finally {
      // TODO: Release file locks
      // for (InputStream inputStream : this.inputStream) {
      //   if (inputStream != null) {
      //     inputStream.close();
      //   }
      // }
    }

    return this.endXsdTest();
  }

  /**
   * Validate the XSD files from the given zip file against the W3C XML Schema specification.
   *
   * TODO: Unknown file lock prevents temporary folder from being deleted.
   */
  public Test validateXsdZip(File zipFile) throws Exception {

    // Unzip the file to a temporary folder
    Path tempPath = ValidationUtils.createTempFolder();
    ZipUtils.unzip(zipFile.toPath(), tempPath);

    // Get each .xsd file
    File[] files = FileUtils.getFileArrayFromDirWithExtension(tempPath, ".xsd");

    // Run XSD validation
    Test test = this.validateXsd(files);

    // TODO: Cannot delete temp folder because of a file lock
    // FileUtils.deleteTempDir(tempPath);

    return test;
  }

  /**
   * Runs XML schema validation on a single XSD file or a zip file of XSDs.
   */
  public Test validateXsd(MultipartFile multipartFile) throws Exception {
    Path tempFolder = ValidationUtils.createTempFolder();
    File inputFile = FileUtils.saveFile(multipartFile, tempFolder).toFile();
    Test test;
    if (inputFile.getName().endsWith(".xsd")) {
      File[] files = {inputFile};
      test = this.validateXsd(files);
    }
    else {
      test = this.validateXsdZip(inputFile);
    }
    // TODO: Cannot delete temp folder because of a file lock
    // FileUtils.deleteTempDir(tempFolder);
    return test;
  }

  /**
   * Validate an XML file against the given XML schemas.
   * Returns early with validation errors if the given XML schemas do not validate.
   */
  public Test validateXmlOnly(File xmlFile, File[] xsdFiles, String testId, String description) throws SAXException, IOException {
    Test test = this.validateXmlOnly(xmlFile, xsdFiles);
    test.id = testId;
    test.description = description;
    return test;
  }

  /**
   * Validate an XML file against the given XML schemas.
   * Returns early with validation errors if the given XML schemas do not validate.
   */
  public Test validateXmlOnly(File xmlFile, File[] xsdFiles) throws SAXException, IOException {
    Test[] tests = this.validateXml(xmlFile, xsdFiles);
    return this.getXmlTest(tests);
  }

  /**
   * Validate an XML file against the given XML schemas.
   * Returns early with validation errors if the given XML schemas do not validate.
   */
  public Test validateXmlOnly(File xmlFile, Source[] xsdSources, String testId, String description) throws SAXException, IOException {

    SchemaFactory schemaFactory = SchemaFactory.newInstance(XMLConstants.W3C_XML_SCHEMA_NS_URI);
    Schema schema = schemaFactory.newSchema(xsdSources);

    Test xmlTest = this.createTest(testId, description);

    Validator validator = schema.newValidator();
    XmlNodeMap xmlNodeMap = new XmlNodeMap();
    XmlErrorHandler xmlErrorHandler = new XmlErrorHandler(new File[] {xmlFile}, xmlNodeMap, xmlTest);
    validator.setErrorHandler(xmlErrorHandler);

    // Prepare the XML input files and validate
    Source xmlSource = new StreamSource(xmlFile);
    validator.validate(xmlSource);

    this.endXmlTest(xmlErrorHandler);
    return xmlTest;
  }

  /**
   * Validate an XML file against the given XML schemas.
   * Returns early with validation errors if the given XML schemas do not validate.
   */
  public Test[] validateXml(File xmlFile, File[] xsdFiles) throws SAXException, IOException {

    // Setup.  Start with XSD validation test.
    this.schemaFactory = this.getSchemaFactory(xsdFiles);
    Test xsdTest = this.getXsdTest();
    Source[] xsdSources = this.convertFilesToSource(xsdFiles);

    // Creating a schema validates the converted input files
    Schema schema = this.schemaFactory.newSchema(xsdSources);

    // Finalize XSD validation test
    this.endXsdTest();

    Test xmlTest = this.createTest("validate-xml", "Validate XML files against XML schemas");

    // Return test early if schemas did not validate
    if (xsdTest.status == Status.error) {
      TestResult result = new TestResult(xmlTest.id);
      result.message = "XML validation did not run because given XML schemas had validation errors.";
      result.status = Status.error;

      xmlTest.status = Status.error;
      xmlTest.ran = false;
      xmlTest.category = "validation";
      xmlTest.results.add(result);
      xmlTest.comments = result.message;
      return new Test[] {xsdTest, xmlTest};
    }

    // Switch to a new XML validation test and build the XML validator.
    Validator validator = schema.newValidator();
    XmlNodeMap xmlNodeMap = new XmlNodeMap();
    XmlErrorHandler xmlErrorHandler = new XmlErrorHandler(new File[] {xmlFile}, xmlNodeMap, xmlTest);
    validator.setErrorHandler(xmlErrorHandler);

    // Prepare the XML input files and validate
    Source xmlSource = new StreamSource(xmlFile);
    validator.validate(xmlSource);

    this.endXmlTest(xmlErrorHandler);
    return new Test[] {xsdTest, xmlTest};
  }

  public Test[] validateXml(MultipartFile multipartXmlFile, MultipartFile multipartXsdFile) throws Exception {
    Path tempPath = ValidationUtils.createTempFolder();
    File xmlFile = FileUtils.saveFile(multipartXmlFile, tempPath).toFile();
    File xsdFile = FileUtils.saveFile(multipartXsdFile, tempPath).toFile();

    File[] files;

    if (xsdFile.getName().endsWith(".xsd")) {
      // XSD input was a single .xsd file
      files = new File[]{xsdFile};
    }
    else {
      // XSD input was a zip file with multiple .xsd files
      ZipUtils.unzip(xsdFile.toPath(), tempPath);
      files = FileUtils.getFileArrayFromDirWithExtension(tempPath, ".xsd");
    }

    Test[] tests = this.validateXml(xmlFile, files);
    return tests;

  }

  public Test validateXmlCatalog(MultipartFile xml) throws Exception {
    Path tempPath = ValidationUtils.createTempFolder();
    File xmlFile = FileUtils.saveFile(xml, tempPath).toFile();
    return this.validateXmlCatalog(xmlFile);

  }

  public Test validateXmlCatalog(File xmlFile) throws Exception {
    Path tempPath = ValidationUtils.createTempFolder();
    File xsdFile = ValidationUtils.convertClassPathResourceToFile(tempPath, "validation/catalog.xsd", "catalog.xsd");

    // Validation should return two results: XSD validation and XML validation
    Test[] resultTests = this.validateXml(xmlFile, new File[] {xsdFile});

    // Only return the result for the user's catalog file.
    return this.getXmlTest(resultTests, "validate-xml-catalog", "Validate an XML catalog file against the OASIS specification");
  }


  /**
   * Assumes array with a XSD validation test and a XML validation test.
   * Returns the XML validation test or null.
   */
  private Test getXmlTest(Test[] tests) {
    if (tests.length == 2) {
      return tests[1];
    }
    return null;
  }

  /**
   * Assumes array with a XSD validation test and a XML validation test.
   * Returns the XML validation test or null.
   */
  private Test getXmlTest(Test[] tests, String testId, String description) {
    Test xmlTest = this.getXmlTest(tests);
    if (xmlTest == null) {
      return null;
    }
    xmlTest.id = testId;
    xmlTest.description = description;
    for (TestResult result : xmlTest.results) {
      result.testId = testId;
    }
    return xmlTest;
  }

  /**
   * Returns a SchemaFactory with a custom error handler that catches and logs
   * all exception if possible instead of exiting on the first encountered exception.
   * @throws IOException
   */
  private SchemaFactory getSchemaFactory(File[] xsdFiles) throws IOException {
    SchemaFactory schemaFactory = SchemaFactory.newInstance(XMLConstants.W3C_XML_SCHEMA_NS_URI);

    XsdNodeMap xsdNodeMap = new XsdNodeMap();

    // Set up a custom error handler in order to catch and log all exceptions
    // instead of throwing a SAXException on the first exception encountered.
    Test xsdTest = this.createXsdTest();
    XmlErrorHandler xmlErrorHandler = new XmlErrorHandler(xsdFiles, xsdNodeMap, xsdTest);
    schemaFactory.setErrorHandler(xmlErrorHandler);

    return schemaFactory;
  }

  private XmlErrorHandler getXsdErrorHandler() {
    return (XmlErrorHandler) this.schemaFactory.getErrorHandler();
  }

  private Test createTest(String id, String description) {
    Test test = new Test(id, description);
    test.results = new LinkedList<>();
    test.category = "validation";
    return test;
  }

  /**
   * Gets a new XML Schema validation test.
   */
  private Test createXsdTest() {
    return this.createTest("validate-xsd", "Validate XML schema files against the W3C specification");
  }

  private Test getXsdTest() {
    return this.getXsdErrorHandler().getTest();
  }

  /**
   * Convert the given XML files into an array of Source objects to be used
   * by the validator.
   * @throws IOException
   */
  private Source[] convertFilesToSource(File[] xmlFiles) throws IOException {

    Source[] sources = new Source[xmlFiles.length];
    this.inputStream = new InputStream[xmlFiles.length];
    int i = 0;

    // Convert the input XML schema files into an array of Source objects
    for (File xmlFile : xmlFiles) {
      InputStream inputStream = new FileInputStream(xmlFile);
      Source source = new StreamSource(inputStream, xmlFile.getCanonicalPath());
      this.inputStream[i] = inputStream;
      sources[i++] = source;
    }

    return sources;

  }

  /**
   * Stops the test run clock and returns the test with finalized results.
   */
  private Test endXsdTest() {
    XmlErrorHandler xsdErrorHandler = this.getXsdErrorHandler();
    return xsdErrorHandler.endTest();
  }

  private Test endXmlTest(XmlErrorHandler xmlErrorHandler) {
    return xmlErrorHandler.endTest();
  }

  /**
   * Log XML validation test results to the console.
   */
  public void logTestResults(Test test) {
    log.info("TEST {}: {}", test.id, test.description);
    log.info("  {} errors.  {}  Ran in {} secs.", test.countErrors(), test.comments, test.runtime);

    for (TestResult result : test.results) {
      log.error("{}: {}", result.status, result.message);
      log.error("  {}", result.location);
      log.error("  line:{} col:{} - {} '{}'", result.line, result.column, result.entityCategory, result.entity);
    }
  }

}
