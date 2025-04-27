package gov.niem.tools.api.validation.xml;

import gov.niem.tools.api.core.utils.FileUtils;
import gov.niem.tools.api.validation.ValidationUtils;

import java.io.IOException;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * XML validation-related utility functions.
 */
public class XmlValidationUtils extends ValidationUtils {

  private static final String regex = TEMP_DIR_BASE + TEMP_DIR_PREFIX + "-\\d*/(.*)";
  private static final Pattern pattern = Pattern.compile(regex);

  /**
   * Normalizes the given path, removes the temporary directory from the path if present
   * and cleans up the results.
   */
  public static String getRelativePath(String pathString) throws IOException {

    String relativePath = FileUtils
        .file(pathString)
        .toString()
        .replaceAll("%20", " ")
        .replaceAll("\\\\", "/")
        .replace("file:///", "")
        .replace("file://", "")
        .replace("file:/", "");

    Matcher matcher = pattern.matcher(relativePath);
    if (matcher.find()) {
      relativePath = matcher.group(1);
    }

    return relativePath;
  }

}
