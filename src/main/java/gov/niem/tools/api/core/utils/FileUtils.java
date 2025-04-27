package gov.niem.tools.api.core.utils;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import lombok.extern.log4j.Log4j2;
import org.apache.commons.io.FilenameUtils;
import org.apache.commons.io.IOUtils;
import org.springframework.util.FileSystemUtils;
import org.springframework.web.multipart.MultipartFile;

/**
 * File-related utilities to support the application.
 */
@Log4j2
public class FileUtils {

  private static final String TEMP_FILE_PREFIX = "niem-api-tmp-";

  /**
   * Gets the filename with extension.
   */
  public static String getFilename(MultipartFile multipartFile) {
    String filename = multipartFile.getOriginalFilename();
    if (filename == null) {
      filename = "file";
    }
    filename.replace("..", "").replace("/", "").replace("\\", "");
    return filename;
  }

  /**
   * Gets the filename without the extension.
   * Note: Treats ".cmf.xml" together as a double extension.
   *
   * @param filename - Filename with an extension
   */
  public static String getFilenameBase(String filename) {
    if (filename.endsWith(".cmf.xml")) {
      return filename.replace(".cmf.xml", "");
    }
    return FilenameUtils.removeExtension(filename);
  }

  /**
   * Gets the filename without the extension.
   *
   * @param multipartFile - Filename with an extension
   * @return Filename without the extension
   */
  public static String getFilenameBase(MultipartFile multipartFile) {
    String filename = getFilename(multipartFile);
    return getFilenameBase(filename);
  }

  /**
   * Returns the extension from the given filename.
   * Note: Treats ".cmf.xml" as a double extension and returns both.
   *
   * @param filename - Filename with extension
   */
  public static String getFileExtension(String filename) {
    if (filename.endsWith(".cmf.xml")) {
      return "cmf.xml";
    }
    return FilenameUtils.getExtension(filename);
  }

  /**
   * Returns the extension from the given filename.
   * Treats ".cmf.xml" as a double extension and returns both.
   *
   * @param filename - Path to file with extension
   */
  public static String getFileExtension(Path filename) {
    return getFileExtension(filename.toString());
  }

  /**
   * Returns the extension from the given filename.
   * Note: Treats ".cmf.xml" as a double extension and returns both.
   *
   * @param multipartFile - Uploaded input file
   */
  public static String getFileExtension(MultipartFile multipartFile) {
    String filename = getFilename(multipartFile);
    return getFileExtension(filename);
  }

  /**
   * Returns the given Windows or Unix path normalized and formatted appropriately for
   * the current system, with a trailing separator.
   */
  public static String normalize(String path) {
    return FilenameUtils.normalize(path);
  }

  /**
   * Returns a File object with the given path normalized.
   */
  public static File file(String path) {
    return new File(normalize(path));
  }

  /**
   * Returns a Path object with the given path normalized.
   */
  public static Path path(String path) {
    return file(path).toPath();
  }

  /**
   * Normalizes the given path and creates a new file at the location.
   */
  public static File createFile(String path) throws Exception {
    File file = file(path);
    Boolean success = file.createNewFile();
    if (!success) {
      throw new Exception("File already exists at " + path);
    }
    return file;
  }

  /**
   * Normalizes the given path and creates a new file at the location with the given bytes.
   */
  public static File createFile(String path, byte[] bytes) throws Exception {
    File newFile = createFile(path);
    org.apache.commons.io.FileUtils.writeByteArrayToFile(newFile, bytes);
    return newFile;
  }

  /**
   * Creates an empty temporary file.
   * Note: Use this utility to delete the temporary file when done.
   *
   * @param prefix - Descriptive text for the file name that will precede the random number.
   */
  public static File createTempFile(String prefix, String extension) throws Exception {
    return createTempFilePath(prefix + "-", extension).toFile();
  }

  /**
   * Creates a temporary file.
   * Note: Use this utility to delete the temporary file when done.
   *
   * @param prefix - Descriptive text for the file name that will precede the random number.
   */
  public static Path createTempFilePath(String prefix, String extension) throws Exception {
    return Files.createTempFile(TEMP_FILE_PREFIX + prefix + "-", "." + extension);
  }

  /**
   * Creates a new directory at the given path.
   */
  public static Path createDir(Path path) throws IOException {
    Files.createDirectories(path);
    return path;
  }

  /**
   * Creates a temporary directory.
   * Note: Use this utility to delete the temporary directory when done.
   *
   * @param prefix - Descriptive text for the directory name that will precede the random number.
   */
  public static Path createTempDir(String prefix) throws IOException {
    return Files.createTempDirectory(TEMP_FILE_PREFIX + prefix + "-");
  }

  /**
   * Deletes the given file if it was created as a temporary file by this utility.
   */
  public static void deleteTempFile(File file) throws IOException {
    deleteTempFile(file.toPath());
  }

  /**
   * Deletes the file at the given path if it was created as a temporary file by this utility.
   */
  public static void deleteTempFile(Path path) throws IOException {
    if (path.getFileName().toString().contains(TEMP_FILE_PREFIX)) {
      Files.deleteIfExists(path);
    }
  }

  /**
   * Recursively deletes the given directory if it was created as a temporary
   * directory by this utility.
   */
  public static void deleteTempDir(File pathFile) throws Exception {
    deleteTempDir(pathFile.toPath());
  }

  /**
   * Recursively deletes the given directory if it was created as a temporary
   * directory by this utility.
   */
  public static void deleteTempDir(Path path) throws Exception {
    if (path != null && path.toString().contains(TEMP_FILE_PREFIX)) {
      FileSystemUtils.deleteRecursively(path);
    }
  }

  /**
   * Moves the temporary file to the new location if it was originally created by this utility.
   * Overwrites the destination file if it already exists.
   *
   * @param oldFile - Original temporary file with contents.
   * @param newFile - New destination for the file, to be overwritten if it already exists.
   */
  public static void moveTempFile(File oldFile, File newFile) throws Exception {
    if (oldFile.toString().contains(TEMP_FILE_PREFIX)) {
      org.apache.commons.io.FileUtils.copyFile(oldFile, newFile);
      oldFile.delete();
    }
  }

  /**
   * Moves the temporary file to the new location if it was originally created by this utility.
   * Overwrites the destination file if it already exists.
   *
   * @param oldFile - Original temporary file with contents.
   * @param newFile - New destination for the file, to be overwritten if it already exists.
   */
  public static void moveTempFile(Path oldFile, Path newFile) throws Exception {
    moveTempFile(oldFile.toFile(), newFile.toFile());
  }

  /**
   * Moves the old directory to the new path if the old directory was created by this utility.
   *
   * @param oldPath - Original temporary directory with contents.
   * @param newPath - New destination for the original directory.
   */
  public static void moveTempDir(File oldPath, File newPath) throws Exception {
    if (oldPath.toString().contains(TEMP_FILE_PREFIX)) {
      org.apache.commons.io.FileUtils.moveDirectory(oldPath, newPath);
    }
  }

  /**
   * Saves a multipart file to a temporary file.
   */
  public static Path saveFile(MultipartFile multipartFile) throws Exception {
    String extension = getFileExtension(multipartFile);
    Path tempFilePath = createTempFilePath("multipart-file", extension);
    multipartFile.transferTo(tempFilePath);
    return tempFilePath;
  }

  /**
   * Saves a multipart file to the given path.
   */
  public static Path saveFile(MultipartFile multipartFile, Path dir) throws Exception {
    String pathString = dir.toString() + "/" + multipartFile.getOriginalFilename();
    String pathStringNormalized = FileUtils.normalize(pathString);
    Path path = path(pathStringNormalized);
    multipartFile.transferTo(path);
    return path;
  }

  /**
   * Saves the given bytes to the given file path.
   */
  public static void saveFile(Path path, byte[] bytes) throws IOException {
    org.apache.commons.io.FileUtils.writeByteArrayToFile(path.toFile(), bytes);
  }

  /**
   * Gets UTF-8 text from the file at the given path.
   */
  public static String getFileText(Path path) throws IOException {
    return Files.readString(path, StandardCharsets.UTF_8);
  }

  /**
   * Gets UTF-8 text from the given multipart file.
   */
  public static String getFileText(MultipartFile multipartFile) throws IOException {
    return new String(multipartFile.getBytes(), StandardCharsets.UTF_8);
  }

  /**
   * Walks the given path to return a list of all file and directory paths.
   */
  public static List<Path> getPathsFromDir(Path dir) throws IOException {
    Stream<Path> walk = Files.walk(dir);
    List<Path> paths = walk.collect(Collectors.toList());
    walk.close();
    return paths;
  }

  /**
   * Walks the given path to return a list of all file paths.
   */
  public static List<Path> getFilePathsFromDir(Path dir) throws IOException {
    List<Path> paths = getPathsFromDir(dir);
    return paths
      .stream()
      .filter(path -> !Files.isDirectory(path))
      .collect(Collectors.toList());
  }

  /**
   * Walks the given path to return a list of all directory paths.
   */
  public static List<Path> getDirPathsFromDir(Path dir) throws IOException {
    List<Path> paths = getPathsFromDir(dir);
    return paths
      .stream()
      .filter(path -> Files.isDirectory(path))
      .collect(Collectors.toList());
  }

  /**
   * Walks the given path to return a list of all file paths that match the given extension.
   */
  public static List<Path> getFilePathsFromDirWithExtension(Path dir, String ext)
      throws IOException {
    List<Path> filepaths = getFilePathsFromDir(dir);
    return filepaths
      .stream()
      .filter(path -> path.toString().endsWith(ext))
      .collect(Collectors.toList());
  }

  /**
   * Walks the given path to return a list of all file that match the given extension.
   */
  public static List<File> getFilesFromDirWithExtension(Path dir, String ext) throws IOException {
    List<Path> filepaths = getFilePathsFromDir(dir);
    return filepaths
      .stream()
      .filter(path -> path.toString().endsWith(ext))
      .map(path -> path.toFile())
      .collect(Collectors.toList());
  }

  /**
   * Walks the given path to return an array of all files that match the given extension.
   */
  public static File[] getFileArrayFromDirWithExtension(Path dir, String ext) throws IOException {
    List<File> fileList = FileUtils.getFilesFromDirWithExtension(dir, ext);
    return fileList.toArray(new File[fileList.size()]);
  }

  /**
   * Converts the given list of path objects to a list of file objects.
   */
  public static List<File> getFiles(List<Path> paths) {
    List<File> files = paths
        .stream()
        .map(path -> FileUtils.file(path.toString()))
        .collect(Collectors.toList());
    return files;
  }

  /**
   * Returns a list of file paths from the given directory that match the given filename.
   */
  public static List<Path> getFilePathsFromDirWithFilename(Path dir, String filename)
      throws IOException {
    List<Path> filepaths = getFilePathsFromDir(dir);
    return filepaths
      .stream()
      .filter(path -> path.toString().equals(filename))
      .collect(Collectors.toList());
  }

  /**
   * Returns true if the UTF-8 contents of file 1 match the contents of file 2.
   */
  public static boolean filesMatch(Path file1, Path file2) throws IOException {
    BufferedReader reader1 = Files.newBufferedReader(file1);
    BufferedReader reader2 = Files.newBufferedReader(file2);
    return IOUtils.contentEquals(reader1, reader2);
  }

  /**
   * Returns true if the two given paths contain the same number of files and subdirectories.
   */
  public static boolean dirsFileCountsMatch(Path dir1, Path dir2) throws IOException {

    List<Path> filepaths1 = getPathsFromDir(dir1);
    List<Path> filepaths2 = getPathsFromDir(dir2);

    // Check that both directories contain the same number of files
    if (filepaths1.size() == filepaths2.size()) {
      log.debug(
          String.format("Directory %s has %d files.  Directory %s has %d files.",
          dir1.toString(), filepaths1.size(), dir2.toString(), filepaths2.size()));
      return true;
    }

    return false;

  }

}
