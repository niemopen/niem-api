package gov.niem.tools.api.validation.xml;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.io.IOException;
import java.nio.file.Path;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;

import gov.niem.tools.api.core.utils.FileUtils;
import gov.niem.tools.api.validation.ValidationUtils;
import lombok.extern.log4j.Log4j2;

@Log4j2
@SpringBootTest(classes = {XmlValidationUtilsTest.class})
public class XmlValidationUtilsTest {

  /**
   * Check that the temporary directory can be removed from a file path, leaving
   * the remaining relative path.
   */
  @Test
  public void checkTemporaryPath() throws Exception {
    Path tempPath = ValidationUtils.createTempFolder();
    String relativePathString = "Crash Driver\\xsd\\extension.xsd";
    testRelativeFilePath(tempPath.toString() + "\\" + relativePathString, relativePathString.replaceAll("\\\\", "/"));
    FileUtils.deleteTempDir(tempPath);
  }

  /**
   * Check that a non-temporary path is normalized.
   */
  @Test
  public void checkNonTemporaryPath() throws Exception {
    testRelativeFilePath(
        "file:///C:/test/niem-api/xml/input/single/person-invalid.xsd",
        "C:/test/niem-api/xml/input/single/person-invalid.xsd");
  }

  private void testRelativeFilePath(String fullPathString, String expectedString) throws IOException {
    String canonicalPathString = FileUtils.file(fullPathString.replace("file:///", "")).getCanonicalPath();

    String resultString = XmlValidationUtils.getRelativePath(canonicalPathString);

    log.debug("canonical {}", canonicalPathString);
    log.debug("relative  {}", expectedString);
    log.debug("results   {}", resultString);

    assertEquals(expectedString, resultString);
  }

}
