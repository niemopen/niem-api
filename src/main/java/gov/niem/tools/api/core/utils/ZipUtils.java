package gov.niem.tools.api.core.utils;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import net.lingala.zip4j.ZipFile;
import net.lingala.zip4j.exception.ZipException;

/**
 * Zipfile-related utility functions.
 */
public class ZipUtils {

  /**
   * Returns a byte array for a zipfile of the given list of files.
   */
  public static byte[] zip(List<File> files) throws Exception {
    // Set up temp zip file
    File file = FileUtils.createTempFile("migration", ".zip");
    ZipFile zipFile = new ZipFile(file);
    zipFile.addFiles(files);
    zipFile.close();

    // Convert to bytes and delete the temp file
    byte[] bytes = Files.readAllBytes(zipFile.getFile().toPath());
    FileUtils.deleteTempFile(file);
    return bytes;
  }

  /**
   * Zips the contents of the given source directory to the specified path.
   */
  public static void zip(File sourceDir, String zipFilePathString)
      throws ZipException, IOException {
    ZipFile zipFile = new ZipFile(zipFilePathString);
    zipFile.addFolder(sourceDir);
    zipFile.close();
  }

  /**
   * Adds a file to the given zip file.
   */
  public static ZipFile zip(String zipFilePathString, List<File> files)
      throws ZipException, IOException {
    ZipFile zipFile = new ZipFile(zipFilePathString);
    zipFile.addFiles(files);
    return zipFile;
  }

  /**
   * Unzips the zip file at the given path to a temporary folder.
   *
   * @return - Path to the temporary folder where the files have been extracted.
   */
  public static Path unzip(Path zipPath) throws IOException {
    Path outputDir = FileUtils.createTempDir("unzip");
    return ZipUtils.unzip(zipPath, outputDir);
  }

  /**
   * Unzips the zip file at the given path to a temporary folder.
   *
   * @return - Path to the temporary folder where the files have been extracted.
   */
  public static Path unzip(Path zipPath, Path outputDir) throws IOException {
    ZipFile zipFile = new ZipFile(zipPath.toFile());
    zipFile.extractAll(outputDir.toString());
    zipFile.close();
    return outputDir;
  }

}