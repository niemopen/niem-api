package gov.niem.tools.api.transform;

import org.apache.commons.io.IOUtils;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.function.Executable;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.web.multipart.MultipartFile;

import gov.niem.tools.api.core.exceptions.BadRequestException;
import gov.niem.tools.api.core.utils.FileUtils;
import gov.niem.tools.api.core.utils.ZipUtils;
import gov.niem.tools.api.migrate.MigrationService;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.nio.file.Files;
import java.nio.file.Path;

/**
 * Tests transforms to or from CMF.
 */
@SpringBootTest(classes={TransformService.class})
public class TransformTest {

  @Autowired
  TransformService transformService;

  String curDir = "./src/test/java/gov/niem/tools/api/transform/";

  @Test
  public void checkCMFtoCMF() throws Exception {

    checkTransform(
      TransformFrom.cmf,
      TransformTo.cmf,
      "data/CrashDriver.cmf.xml",
      "output/cmf-to-cmf/CrashDriver.cmf.xml",
      "data/CrashDriver.cmf.xml"
    );

  }

  /**
   * Check for empty line at top of file
   */
  @Disabled
  @Test
  public void checkXSDtoCMF() throws Exception {

    checkTransform(
      TransformFrom.xsd,
      TransformTo.cmf,
      "data/niem-core.xsd",
      "output/xsd-to-cmf/niem-core.cmf.xml",
      "data/niem-core.cmf.xml"
    );

  }

  @Test
  public void checkMigrationSubset() throws Exception {

    // Transform the input
    MultipartFile inputFile = this.getMultipartFile(curDir + "data/subset-migration-3.0.zip");
    byte[] bytes = transformService.transform(TransformFrom.xsd, TransformTo.cmf, inputFile);

  }

  /**
   * Check for empty line at top of file and SchemaDocument paths without the xsd/ folder
   */
  @Disabled
  @Test
  public void checkXSD_ZIPtoCMF() throws Exception {

    checkTransform(
      TransformFrom.xsd,
      TransformTo.cmf,
      "data/CrashDriver.zip",
      "output/xsd.zip-to-cmf/CrashDriver.cmf.xml",
      "data/CrashDriver.cmf.xml"
    );

  }

  @Disabled
  public void checkCMFtoXSD() throws Exception {

    checkTransform(
      TransformFrom.cmf,
      TransformTo.xsd,
      "data/niem-core.cmf.xml",
      "output/cmf-to-xsd/niem-core.xsd",
      "data/niem-core.xsd"
    );

  }

  @Test
  public void checkCMFtoXSD_ZIP() throws Exception {

    // Get the results of a CMF to XSD transform
    Path outputFile = transformAndSave(
      TransformFrom.cmf,
      TransformTo.xsd,
      "data/CrashDriver.cmf.xml",
      "output/cmf-to-xsd.zip/CrashDriver.zip"
    );

    ZipUtils.unzip(outputFile, FileUtils.path(curDir + "output/cmf-to-xsd"));

    // TODO: Check output

  }

  @Test
  public void checkCMFtoJSONSchema() throws Exception {

    checkTransform(
      TransformFrom.cmf,
      TransformTo.json_schema,
      "data/CrashDriver.cmf.xml",
      "output/cmf-to-json-schema/CrashDriver.schema.json",
      "data/CrashDriver.schema.json"
    );

  }

  /**
   * Check for empty line at top of file
   */
  @Disabled
  @Test
  public void checkCMFtoOWL() throws Exception {

    checkTransform(
      TransformFrom.cmf,
      TransformTo.owl,
      "data/CrashDriver.cmf.xml",
      "output/cmf-to-owl/CrashDriver.owl.ttl",
      "data/CrashDriver.owl.ttl"
    );

  }

  private Path transformAndSave(TransformFrom from, TransformTo to, String inputFilePathString, String outputFilePathString) throws Exception {

    // Transform the input
    MultipartFile inputFile = this.getMultipartFile(curDir + inputFilePathString);
    byte[] bytes = transformService.transform(from, to, inputFile);

    // Save the output
    Path outputFilePath = FileUtils.path(curDir + outputFilePathString);
    FileUtils.saveFile(outputFilePath, bytes);

    return outputFilePath;

  }

  /**
   * Helper method to test a transform
   */
  private void checkTransform(TransformFrom from, TransformTo to, String inputFilePathString, String outputFilePathString, String expectedFilePathString) throws Exception {

    // Transform the input and save the output
    Path outputFilePath = transformAndSave(from, to, inputFilePathString, outputFilePathString);

    // Get the expected results from the test data directory
    Path expectedFilePath = FileUtils.path(curDir + expectedFilePathString);

    String outputText = FileUtils.getFileText(outputFilePath);
    String expectedText = FileUtils.getFileText(expectedFilePath);

    // Compare the expected and actual output files
    // assertTrue(FileUtils.filesMatch(expectedFilePath, outputFilePath));
    assertEquals(expectedText, outputText);

  }

  /**
   * Reads the given file from the test's data directory and returns it as
   * a mock multipart file for the transform service calls.
   */
  private MultipartFile getMultipartFile(String path) throws Exception {

    File inputFile = FileUtils.file(path);
    FileInputStream inputStream = new FileInputStream(inputFile);
    String contentType = Files.probeContentType(inputFile.toPath());
    return new MockMultipartFile(inputFile.getName(), inputFile.getName(), contentType, inputStream);

  }

  /**
   * Asserts the given two files match.
   */
  private void assertFilesMatch(Path file1, Path file2, String message) throws Exception {
    BufferedReader reader1 = Files.newBufferedReader(file1);
    BufferedReader reader2 = Files.newBufferedReader(file2);
    assertTrue(IOUtils.contentEquals(reader1, reader2));
  }

  /**
   * Test if file extensions are valid based on the given "from" values
   */
  @Test
  public void checkInputFileExtension() {

    // from "xsd" tests
    fileExtensionShouldPass("xsd", "xsd");
    fileExtensionShouldPass("xsd", "zip");

    fileExtensionShouldFail("xsd", "json");
    fileExtensionShouldFail("xsd", "owl");
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
   * Calls the testFileExtension helper, with shouldPassTest = true.
   * @param from - Specified format of the model being transformed.
   * @param extension - File extension that should be obtained from the input.
   */
  private void fileExtensionShouldPass(String from, String extension) {
    testFileExtension(from, extension, true);
  }

  /**
   * Calls the testFileExtension helper, with shouldPassTest = false.
   * @param from - Specified format of the model being transformed.
   * @param extension - File extension that should be obtained from the input.
   */
  private void fileExtensionShouldFail(String from, String extension) {
    testFileExtension(from, extension, false);
  }

  /**
   * Test if the file extension is valid based on the given "from" value.
   * @param from - Specified format of the model being transformed.
   * @param extension - File extension that should be obtained from the input.
   * @param shouldPassTest - True if the test is expected to pass; false
   * if the test is expected to fail.
   */
  private void testFileExtension(String from, String extension, Boolean shouldPassTest) {

    String message = String.format("from %s with file extension %s", from, extension);

    Executable test = () -> TransformService.checkInputFileExtension(TransformFrom.valueOf(from), extension);

    if (shouldPassTest) {
      assertDoesNotThrow(test, message + " should pass");
    }
    else {
      assertThrows(BadRequestException.class, test, message + " should fail");
    }

  }

}
