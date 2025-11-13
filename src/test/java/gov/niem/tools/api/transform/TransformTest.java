package gov.niem.tools.api.transform;

import gov.niem.tools.api.Application;
import gov.niem.tools.api.TestUtils;
import gov.niem.tools.api.core.config.Config;
import gov.niem.tools.api.core.exceptions.BadRequestException;
import gov.niem.tools.api.core.utils.FileUtils;
import gov.niem.tools.api.core.utils.ZipUtils;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.File;
import java.net.URL;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.function.Executable;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.web.multipart.MultipartFile;

/**
 * Tests transforms to or from CMF.
 */
@SpringBootTest(classes = {TransformService.class, Application.class, Config.class})
public class TransformTest {

  @Autowired
  TransformService transformService;

  // Full model files
  private static final String PATH_CRASH_DRIVER_CMF = "transform/CrashDriver-5.0.cmf.xml";
  private static final String PATH_CRASH_DRIVER_JSON = "transform/CrashDriver-5.0.schema.json";
  private static final String PATH_CRASH_DRIVER_RDF = "transform/CrashDriver-5.0.ttl";
  private static final String PATH_CRASH_DRIVER_XSD_ZIP = "transform/CrashDriver-5.0.zip";

  // Single namespace files
  private static final String PATH_CORE_CMF = "transform/niem-core-5.0.cmf.xml";
  private static final String PATH_CORE_XSD = "transform/niem-core-5.0.xsd";

  // Invalid CMF file
  private static final String PATH_CRASH_DRIVER_CMF_INVALID =
      "transform/CrashDriver-5.0-0.6.cmf.xml";

  /**
   * Check that a CMF file for a single namespace can be read in and regenerated
   * in the expected canonical format (e.g., sorted, regular formatting, etc.),
   */
  @Test
  public void checkCmfToCmf_single() throws Exception {
    String expected = TestUtils.getResourcesFileText(PATH_CORE_CMF);
    String actual = getTransformAsString(TransformFrom.cmf, TransformTo.cmf, PATH_CORE_CMF);
    assertEquals(expected, actual);
  }

  /**
   * Check that a CMF file for multiple namespaces can be read in and regenerated
   * in the expected canonical format (e.g., sorted, regular formatting, etc.),
   */
  @Test
  public void checkCmfToCmf_multi() throws Exception {
    String expected = TestUtils.getResourcesFileText(PATH_CRASH_DRIVER_CMF);
    String actual = getTransformAsString(TransformFrom.cmf, TransformTo.cmf, PATH_CRASH_DRIVER_CMF);
    assertEquals(expected, actual);
  }

  /**
   * Check that a single XML Schema file can be converted into the expected CMF.
   */
  @Test
  public void checkXsdToCmf() throws Exception {
    String expected = TestUtils.getResourcesFileText(PATH_CORE_CMF);
    String actual = getTransformAsString(TransformFrom.xsd, TransformTo.cmf, PATH_CORE_XSD);
    assertEquals(expected, actual);
  }

  /**
   * Check that a set of XML Schemas in a zip file can be converted into the expected CMF.
   */
  @Test
  public void checkXsdZipToCmf() throws Exception {
    String expected = TestUtils.getResourcesFileText(PATH_CRASH_DRIVER_CMF);
    String actual = getTransformAsString(TransformFrom.xsd, TransformTo.cmf, PATH_CRASH_DRIVER_XSD_ZIP);
    assertEquals(expected, actual);
  }

  /**
   * Check that a full model CMF file can be converted to the expected set of
   * XML Schemas in a zip file.
   */
  @Test
  public void checkCmfToXsdZip() throws Exception {
    // Run the CMF to XSD transform with a full model
    byte[] bytes = getTransformAsBytes(TransformFrom.cmf, TransformTo.xsd, PATH_CRASH_DRIVER_CMF);

    // Save the results of the transform to a temp file
    File actualZipFile = FileUtils.createTempFile("test-transform", "zip");
    FileUtils.saveFile(actualZipFile.toPath(), bytes);

    // Unzip the results to a temp folder
    Path actualZipDir = FileUtils.createTempDir("zip");
    ZipUtils.unzip(actualZipFile.toPath(), actualZipDir);

    // Unzip the expected output
    String expectedOutputResource = PATH_CRASH_DRIVER_XSD_ZIP;
    URL expectedOutputUrl = TestUtils.getResourcesFileUri(expectedOutputResource);
    Path expectedZipDir = FileUtils.createTempDir("zip");
    ZipUtils.unzip(Paths.get(expectedOutputUrl.toURI()), expectedZipDir);

    // Get the list of XSD file paths from the results and the expected folders
    List<Path> actualPaths = FileUtils.getFilePathsFromDirWithExtension(actualZipDir, "xsd");
    List<Path> expectedPaths = FileUtils.getFilePathsFromDirWithExtension(expectedZipDir, "xsd");

    // Check that the actual and expected results have the same number of XSD files
    assertEquals(expectedPaths.size(), actualPaths.size());

    // Check the the given file matches in each zip
    for (Path actualPath : actualPaths) {
      String expectedPathString = actualPath.toString().replace(actualZipDir.toString(), expectedZipDir.toString());

      Path expectedPath = expectedPaths
          .stream()
          .filter(path -> path.toString().equals(expectedPathString))
          .findFirst()
          .orElse(null);

      assertNotNull(expectedPath);

      String expectedText = FileUtils.getFileText(expectedPath);
      String actualText = FileUtils.getFileText(actualPath);

      assertEquals(expectedText, actualText);
    }

    // Delete temp file and folders
    FileUtils.deleteTempFile(actualZipFile);
    FileUtils.deleteTempDir(actualZipDir);
    FileUtils.deleteTempDir(expectedZipDir);
  }

  /**
   * Check that a CMF file can be converted to the expected JSON Schema file.
   */
  @Test
  public void checkCmfToJsonSchema() throws Exception {
    String expected = TestUtils.getResourcesFileText(PATH_CRASH_DRIVER_JSON);
    String actual = getTransformAsString(TransformFrom.cmf, TransformTo.json_schema, PATH_CRASH_DRIVER_CMF);
    assertEquals(expected, actual);
  }

  /**
   * Check that a CMF file can be converted to the expected RDF file.
   */
  @Test
  public void checkCmfToRdf() throws Exception {
    String expected = TestUtils.getResourcesFileText(PATH_CRASH_DRIVER_RDF);
    String actual = getTransformAsString(TransformFrom.cmf, TransformTo.rdf, PATH_CRASH_DRIVER_CMF);
    assertEquals(expected, actual);
  }

  /**
   * Check that an invalid version of a  CMF file returns a helpful error message.
   */
  @Test
  public void checkInvalidCmf() throws Exception {
    BadRequestException exception = assertThrows(BadRequestException.class,
        () -> getTransformAsString(TransformFrom.cmf,
            TransformTo.cmf, PATH_CRASH_DRIVER_CMF_INVALID));

    assertTrue(exception.getMessage().contains("Only CMF version"));
  }

  /**
   * Check if input file extensions are valid based on the given transformation "from" values.
   */
  @Test
  public void checkInputFileExtension() {

    // from "xsd" tests
    fileExtensionShouldPass("xsd", "xsd");
    fileExtensionShouldPass("xsd", "zip");

    fileExtensionShouldFail("xsd", "json");
    fileExtensionShouldFail("xsd", "rdf");
    fileExtensionShouldFail("xsd", "cmf");

    // from "cmf" tests
    fileExtensionShouldPass("cmf", "cmf");
    fileExtensionShouldPass("cmf", "cmf.xml");

    fileExtensionShouldFail("cmf", "xml");
    fileExtensionShouldFail("cmf", "xsd");
    fileExtensionShouldFail("cmf", "txt");
    fileExtensionShouldFail("cmf", "zip");
  }

  /**
   * Run the transform and return the results as a byte array, which can be converted
   * to a string or saved as a zip file.
   */
  private byte[] getTransformAsBytes(TransformFrom from, TransformTo to, String inputResourcePath)
      throws Exception {
    MultipartFile inputFile = TestUtils.getMultipartFile(inputResourcePath);
    return transformService.transform(from, to, inputFile);
  }

  /**
   * Run the transform and convert the results to a string.  Can be uses for transforms that
   * return a single text file (as opposed to a zip file).
   */
  private String getTransformAsString(TransformFrom from, TransformTo to, String inputResourcePath)
      throws Exception {
    byte[] bytes = getTransformAsBytes(from, to, inputResourcePath);
    return new String(bytes);
  }

  /**
   * Calls the testFileExtension helper, with shouldPassTest = true.
   *
   * @param from - Specified format of the model being transformed.
   * @param extension - File extension that should be obtained from the input.
   */
  private void fileExtensionShouldPass(String from, String extension) {
    testFileExtension(from, extension, true);
  }

  /**
   * Calls the testFileExtension helper, with shouldPassTest = false.
   *
   * @param from - Specified format of the model being transformed.
   * @param extension - File extension that should be obtained from the input.
   */
  private void fileExtensionShouldFail(String from, String extension) {
    testFileExtension(from, extension, false);
  }

  /**
   * Test if the file extension is valid based on the given "from" value.
   *
   * @param from - Specified format of the model being transformed.
   * @param extension - File extension that should be obtained from the input.
   * @param shouldPassTest - True if the test is expected to pass; false
   *     if the test is expected to fail.
   */
  private void testFileExtension(String from, String extension, Boolean shouldPassTest) {

    String message = String.format("from %s with file extension %s", from, extension);

    Executable test = () -> TransformService.checkInputFileExtension(
          TransformFrom.valueOf(from), extension);

    if (shouldPassTest) {
      assertDoesNotThrow(test, message + " should pass");
    }
    else {
      assertThrows(BadRequestException.class, test, message + " should fail");
    }

  }

}
