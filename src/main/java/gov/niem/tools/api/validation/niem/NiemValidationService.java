package gov.niem.tools.api.validation.niem;

import gov.niem.tools.api.core.exceptions.BadRequestException;
import gov.niem.tools.api.core.utils.CmfUtils;
import gov.niem.tools.api.core.utils.FileUtils;
import gov.niem.tools.api.core.utils.ZipUtils;
import gov.niem.tools.api.validation.Test;
import gov.niem.tools.api.validation.TestResult;
import gov.niem.tools.api.validation.TestResult.Status;
import gov.niem.tools.api.validation.ValidationUtils;
import gov.niem.tools.api.validation.xml.XmlValidationService;

import java.io.File;
import java.io.IOException;
import java.net.URISyntaxException;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import javax.xml.transform.Source;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import org.xml.sax.SAXException;

/**
 * Validates artifacts against NIEM rules.
 */
@Service
public class NiemValidationService {

  @Autowired
  XmlValidationService xmlValidationService;

  @Autowired
  NdrValidationService ndrValidationService;

  private enum Version {
    v3,
    v5;
  }

  /**
   * Validate the given zip file against NIEM message specification rules and guidance.
   */
  public List<Test> validateMessageSpecification(MultipartFile multipartFile) throws Exception {

    List<Test> tests = new LinkedList<Test>();

    // Unzip message specification input
    Path tempPath = ValidationUtils.createTempFolder();
    List<File> files = this.loadFiles(tempPath, multipartFile);
    List<File> xsdFiles = files.stream().filter(file -> file.getName().endsWith(".xsd")).toList();

    // File[] fileArray = files.toArray(File[]::new);
    File[] xsdFileArray = xsdFiles.toArray(File[]::new);

    // Check for required IEPD catalog
    File messageCatalogFile = this.findMessageCatalog(files);

    // Validate message catalog
    this.testMessageCatalogValidation(tests, messageCatalogFile);

    // TODO: [TEST] Validate sample instances and XSD in one step if present

    // TODO: [TEST] If no sample instances, validate XSDs only
    this.testXsdValidation(tests, xsdFileArray);

    // Validate XML catalogs
    this.testXmlCatalogsValidation(tests, files);

    // Run NDR validation
    List<Test> ndrTests = this.ndrValidationService.validateXsdWithNdr(files, true);
    tests.addAll(ndrTests);

    // TODO: [TEST] Resolve XML catalog paths and URIs

    // TODO: [Test] Check for required artifacts

    // TODO: Cannot delete temp folder
    // FileUtils.deleteTempDir(tempPath);

    return tests;

  }

  /**
   * Validate the given file against NIEM message catalog rules.
   */
  public Test validateMessageCatalog(MultipartFile multipartFile) throws Exception {
    Path tempPath = ValidationUtils.createTempFolder();
    File file = FileUtils.saveFile(multipartFile, tempPath).toFile();
    return this.validateMessageCatalog(file);
  }

  /**
   * Validate the given file against NIEM message catalog rules.
   */
  public Test validateMessageCatalog(File xmlFile)
      throws SAXException, IOException, URISyntaxException {
    Version version = this.getVersionFromMessageCatalog(xmlFile);
    String testId = "validate-message-catalog";
    String description = "Validate a MPD or IEPD catalog against the NIEM message catalog schema";
    if (version == null) {
      String message = "No message catalog with a recognized name found";
      String comments = "Recognized message catalog names are mpd-catalog.xml (for version 3.0 of the IEPD specification) or iepd-catalog.xml (for version 5.0 of the IEPD specification)";
      return this.failTest(testId, description, Status.error, message, comments);
    }

    String xsdPathString = "validation/message-catalog/" + version.toString();
    Source[] xsdSources = ValidationUtils.getClasspathXsdSources(xsdPathString);
    Test test = xmlValidationService.validateXmlOnly(xmlFile, xsdSources, testId, description);
    return test;
  }

  /**
   * Creates a new test to record the outcome of a message catalog validation for the
   * given file and adds it to the given list of tests.
   */
  public Test testMessageCatalogValidation(List<Test> tests, File xmlFile)
      throws SAXException, IOException, URISyntaxException {
    Test test = this.validateMessageCatalog(xmlFile);
    tests.add(test);
    return test;
  }

  /**
   * Validates the given file against the CMF schemas.
   */
  public Test validateCmf(MultipartFile multipartFile) throws Exception {
    Path tempPath = ValidationUtils.createTempFolder();
    File file = FileUtils.saveFile(multipartFile, tempPath).toFile();
    return this.validateCmf(file);
  }

  /**
   * Validates the given file against the CMF schemas.
   */
  public Test validateCmf(File cmfFile) throws IOException, SAXException, BadRequestException {
    CmfUtils.checkVersion(cmfFile);
    Source[] xsdSources = ValidationUtils.getClasspathXsdSources("validation/cmf/v1.0/model.xsd");
    String description = "Validate a CMF against the NIEM Common Model Format Specification";
    Test test = xmlValidationService.validateXmlOnly(cmfFile, xsdSources,
        "validate-cmf", description);
    return test;
  }

  /**
   * Creates a test to record the outcome of a failed validation and adds it to the
   * given list of tests.
   */
  private Test failTest(List<Test> tests, String testId, String description,
      Status status, String message, String comment) {
    Test test = this.failTest(testId, description, status, message, comment);
    tests.add(test);
    return test;
  }

  /**
   * Creates a test to record the outcome of a failed validation.
   */
  private Test failTest(String testId, String description, Status status,
      String message, String comment) {
    Test test = new Test(testId, description);
    test.category = "validation";
    test.ran = true;
    test.status = status;
    test.results = new LinkedList<TestResult>();
    TestResult result = new TestResult(testId, description);
    result.testId = testId;
    result.status = status;
    result.message = message;
    result.comment = comment;
    test.results.add(result);
    return test;
  }

  private Version getVersionFromMessageCatalog(File messageCatalogFile) {
    if (messageCatalogFile == null) {
      return null;
    }
    String filename = messageCatalogFile.getName();
    if (filename.equals("mpd-catalog.xml")) {
      return Version.v3;
    }
    else if (filename.equals("iepd-catalog.xml")) {
      return Version.v5;
    }
    return null;
  }

  /**
   * Creates a test to record the outcome of a XML Schema validation and adds it to
   * the given list of tests.
   */
  private void testXsdValidation(List<Test> tests, File[] files) throws SAXException, IOException {
    try {
      Test test = xmlValidationService.validateXsd(files);
      tests.add(test);
    }
    catch (SAXException exception) {
      String message = "The validator encountered a fatal exception";
      this.failTest(tests, "xsd-validation", "Validate XML schemas", Status.error,
          message, exception.getLocalizedMessage());
    }

  }

  /**
   * Creates a test to record the outcome of the validation of a XML catalog and adds it
   * to the given list of tests.
   */
  private void testXmlCatalogsValidation(List<Test> tests, List<File> files) throws Exception {
    List<File> catalogFiles = this.findFilenameList(files, "xml-catalog.xml");
    for (File catalogFile : catalogFiles) {
      Test test = xmlValidationService.validateXmlCatalog(catalogFile);
      tests.add(test);
    }
  }

  /**
   * Validates the given XML Schema or zip file against NDR rules.
   * Returns separate tests for each failed rule, with each occurrence as its own
   * result under that test.
   */
  public List<Test> validateXsdWithNdr(MultipartFile multipartFile) throws Exception {
    Path tempPath = ValidationUtils.createTempFolder();
    File inputFile = FileUtils.saveFile(multipartFile, tempPath).toFile();
    List<File> files = new ArrayList<>();

    String extension = FileUtils.getFileExtension(multipartFile);

    if (extension.equals("zip")) {
      // Process zip file
      ZipUtils.unzip(inputFile.toPath(), tempPath);
      List<Path> paths = FileUtils.getFilePathsFromDirWithExtension(tempPath, ".xsd");
      files = paths.stream().map(Path::toFile).toList();
    }
    else if (extension.equals("xsd")) {
      // Process single XSD file
      Path xsdPath = FileUtils.saveFile(multipartFile, tempPath);
      files.add(xsdPath.toFile());
    }
    else {
      throw new BadRequestException("A zip file or a XSD file is required.");
    }

    // Run validation tests and clean up temp files
    List<Test> tests = ndrValidationService.validateXsdWithNdr(files, true);
    FileUtils.deleteTempDir(tempPath);
    return tests;
  }

  // TODO: Component QA
  // public List<Test> validateProperty(Property property) {
  //   List<Test> tests = new LinkedList<Test>();

  //   if (property.getPrefix() == null) {

  //   }

  //   return tests;
  // }

  // private void componentQaResult(Test test, Status status, String message, String comment,
  //     String field, String prefix, String entity, String problemValue) {
  //   TestResult result = new TestResult(test.id, status, prefix, entity, "property",
  //       message, null, null, null, comment, problemValue);
  //   test.results.add(result);
  // }

  /**
   * Unzips the given file to the given path and returns the list of files.
   */
  private List<File> loadFiles(Path path, MultipartFile multipartFile) throws Exception {
    File inputFile = FileUtils.saveFile(multipartFile, path).toFile();
    ZipUtils.unzip(inputFile.toPath(), path);

    List<Path> paths = FileUtils.getFilePathsFromDir(path);
    List<File> files = paths.stream().map(Path::toFile).toList();
    return files;
  }

  /**
   * Finds the message catalog file in the given list of files.
   */
  private File findMessageCatalog(List<File> files) {
    // Try MPD Specification 3.0
    File file = this.findFilename(files, "mpd-catalog.xml");
    if (file != null) {
      return file;
    }

    // Try IEPD Specification 5.0
    file = this.findFilename(files, "iepd-catalog.xml");
    if (file != null) {
      return file;
    }

    // Not found
    return null;
  }

  /**
   * Finds a file in the given list of files with the given filename.
   */
  private File findFilename(List<File> files, String filename) {
    return files
        .stream()
        .filter(f -> f.getName().equals(filename))
        .findAny()
        .orElse(null);
  }

  /**
   * Finds all files in the given list of files with the given filename.
   */
  private List<File> findFilenameList(List<File> files, String filename) {
    return files.stream().filter(f -> f.getName().equals(filename)).toList();
  }


}
