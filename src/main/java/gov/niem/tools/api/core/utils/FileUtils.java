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

import org.apache.commons.io.FilenameUtils;
import org.apache.commons.io.IOUtils;
import org.springframework.util.FileSystemUtils;
import org.springframework.web.multipart.MultipartFile;

import lombok.extern.log4j.Log4j2;

/**
 * File-related utilities to support the application.
 */
@Log4j2
public class FileUtils {

  private static final String TEMP_FILE_PREFIX = "niem-api-tmp-";

  /**
   * Gets the filename with extension.
   * @param multipartFile
   * @return Filename with extension
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
   * Treats ".cmf.xml" together as a double extension.
   * @param filename - Filename with an extension
   * @return Filename without the extension
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
   * Treats ".cmf.xml" as a double extension and returns both.
   * @param filename - Filename with extension
   * @return File extension
   */
  public static String getFileExtension(String filename){
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
   * @return File extension
   */
  public static String getFileExtension(Path filename) {
    return getFileExtension(filename.toString());
  }

  /**
   * Returns the extension from the given filename.
   * Treats ".cmf.xml" as a double extension and returns both.
   * @param filename - Filename with extension
   * @return File extension
   */
  public static String getFileExtension(MultipartFile multipartFile){
    String filename = getFilename(multipartFile);
    return getFileExtension(filename);
  }

  /**
   * Returns the given path normalized and formatted appropriately for
   * the current system, with a trailing separator.
   * @param path - Windows or Unix path to be normalized
   * @return Normalized path
   */
  public static String normalize(String path) {
    return FilenameUtils.normalize(path);
  }

  public static File file(String path) {
    return new File(normalize(path));
  }

  public static Path path(String path) {
    return file(path).toPath();
  }

  /**
   * Normalizes the given path and creates a new file at the location.
   * @param path - Path at which a new file should be created.
   * @return New empty file.
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
   * Normalizes the given path and creates a new file at the location.
   * @param path - Path at which a new file should be created.
   * @return New empty file.
   */
  public static File createFile(String path, byte[] bytes) throws Exception {
    File newFile = createFile(path);
    org.apache.commons.io.FileUtils.writeByteArrayToFile(newFile, bytes);
    return newFile;
  }

  /**
   * Creates a temporary file.  Use this utility to delete the file when done.
   * @param prefix - Text to use as part of the file name before a
   * random number is added to ensure uniqueness.
   * @param extension - File extension for the temporary file.
   * @return New empty file.
   */
  public static File createTempFile(String prefix, String extension) throws Exception {
    return createTempFilePath(prefix + "-", extension).toFile();
  }

  /**
   * Creates a temporary file.  Use this utility to delete the file when done.
   * @param prefix - Text to use as part of the file name before a
   * random number is added to ensure uniqueness.
   * @param extension - File extension for the temporary file.
   * @return Path for new empty file.
   */
  public static Path createTempFilePath(String prefix, String extension) throws Exception {
    return Files.createTempFile(TEMP_FILE_PREFIX + prefix + "-", "." + extension);
  }

  public static Path createDir(Path path) throws IOException {
    Files.createDirectories(path);
    return path;
  }

  /**
   * Creates a temporary directory.  Use this utility to delete the directory
   * when done.
   * @param prefix - Text to use as part of the directory name before a random
   * number is added to ensure uniqueness.
   * @return Path for the new temporary directory.
   */
  public static Path createTempDir(String prefix) throws IOException {
    return Files.createTempDirectory(TEMP_FILE_PREFIX + prefix + "-");
  }

  /**
   * Deletes the given file if it was created as a temp file by this utility.
   * @param file - Temporary file to be deleted
   */
  public static void deleteTempFile(File file) throws IOException {
    deleteTempFile(file.toPath());
  }

  /**
   * Deletes the given file if it was created as a temp file by this utility.
   * @param path - Temporary file to be deleted
   */
  public static void deleteTempFile(Path path) throws IOException {
    if (path.getFileName().toString().contains(TEMP_FILE_PREFIX)) {
      Files.deleteIfExists(path);
    }
  }

  /**
   * Recursively deletes the given directory if it was created as a temp
   * directory by this utility.
   * @param pathFile - Temporary directory path to be deleted
   */
  public static void deleteTempDir(File pathFile) throws Exception {
    deleteTempDir(pathFile.toPath());
  }

  /**
   * Recursively deletes the given directory if it was created as a temp
   * directory by this utility.
   * @param path - Temporary directory path to be deleted
   */
  public static void deleteTempDir(Path path) throws Exception {
    if (path != null && path.toString().contains(TEMP_FILE_PREFIX)) {
      FileSystemUtils.deleteRecursively(path);
    }
  }

  /**
   * Moves the temporary file to the new location if it was originally
   * created by this utility.  Overwrites the destination file if it already
   * exists.
   * @param oldFile - Original temporary file with contents.
   * @param newFile - New destination for the file, to be overwritten if it
   * already exists.
   */
  public static void moveTempFile(File oldFile, File newFile) throws Exception {
    if (oldFile.toString().contains(TEMP_FILE_PREFIX)) {
      org.apache.commons.io.FileUtils.copyFile(oldFile, newFile);
      oldFile.delete();
    }
  }

  /**
   * Moves the temporary file to the new location if it was originally
   * created by this utility.  Overwrites the destination file if it already
   * exists.
   * @param oldFile - Original temporary file with contents.
   * @param newFile - New destination for the file, to be overwritten if it
   * already exists.
   */
  public static void moveTempFile(Path oldFile, Path newFile) throws Exception {
    moveTempFile(oldFile.toFile(), newFile.toFile());
  }

  /**
   * Moves the old directory to the new path if the old directory was created
   * by this utility.
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
   * @param multipartFile - File contents to save.
   * @return Path of the new temporary file.
   * @throws Exception
   */
  public static Path saveFile(MultipartFile multipartFile) throws Exception {
    String extension = getFileExtension(multipartFile);
    Path tempFilePath = createTempFilePath("multipart-file", extension);
    multipartFile.transferTo(tempFilePath);
    return tempFilePath;
  }

  /**
   * Saves a multipart file to the given path.
   * @param multipartFile - File contents to save.
   * @param dir - Directory to save the given file to.
   * @throws Exception
   */
  public static Path saveFile(MultipartFile multipartFile, Path dir) throws Exception {
    Path path = path(FileUtils.normalize(dir.toString() + "/" + multipartFile.getOriginalFilename()));
    multipartFile.transferTo(path);
    return path;
  }

  public static void saveFile(Path path, byte[] bytes) throws IOException {
    org.apache.commons.io.FileUtils.writeByteArrayToFile(path.toFile(), bytes);
  }

  public static String getFileText(Path path) throws Exception {
    return Files.readString(path, StandardCharsets.UTF_8);
  }

  /**
   * Returns file and directory paths from the given directory.
   */
  public static List<Path> getPathsFromDir(Path dir) throws IOException {
    Stream<Path> walk = Files.walk(dir);
    List<Path> paths = walk.collect(Collectors.toList());
    walk.close();
    return paths;
  }

  public static List<Path> getFilePathsFromDir(Path dir) throws IOException {
    List<Path> paths = getPathsFromDir(dir);
    return paths
      .stream()
      .filter(path -> !Files.isDirectory(path))
      .collect(Collectors.toList());
  }

  public static List<Path> getDirPathsFromDir(Path dir) throws IOException {
    List<Path> paths = getPathsFromDir(dir);
    return paths
      .stream()
      .filter(path -> Files.isDirectory(path))
      .collect(Collectors.toList());
  }

  /**
   * Returns file paths from the given directory that match the given extension.
   */
  public static List<Path> getFilePathsFromDirWithExtension(Path dir, String ext) throws IOException {
    List<Path> filepaths = getFilePathsFromDir(dir);
    return filepaths
      .stream()
      .filter(path -> path.toString().endsWith(ext))
      .collect(Collectors.toList());
  }

  /**
   * Returns File list from the given directory that match the given extension.
   */
  public static List<File> getFilesFromDirWithExtension(Path dir, String ext) throws IOException {
    List<Path> filepaths = getFilePathsFromDir(dir);
    return filepaths
      .stream()
      .filter(path -> path.toString().endsWith(ext))
      .map(path -> path.toFile())
      .collect(Collectors.toList());
  }

  public static File[] getFileArrayFromDirWithExtension(Path dir, String ext) throws IOException {
    List<File> fileList = FileUtils.getFilesFromDirWithExtension(dir, ext);
    return fileList.toArray(new File[fileList.size()]);
  }

  /**
   * Returns a list of files given a list of paths.
   */
  public static List<File> getFiles(List<Path> paths) {
    List<File> files = paths
    .stream()
    .map(path -> FileUtils.file(path.toString()))
    .collect(Collectors.toList());
    return files;
  }

  /**
   * Returns file paths from the given directory that match the given filename.
   */
  public static List<Path> getFilePathsFromDirWithFilename(Path dir, String filename) throws IOException {
    List<Path> filepaths = getFilePathsFromDir(dir);
    return filepaths
      .stream()
      .filter(path -> path.toString().equals(filename))
      .collect(Collectors.toList());
  }

  public static boolean filesMatch(Path file1, Path file2) throws IOException {
    BufferedReader reader1 = Files.newBufferedReader(file1);
    BufferedReader reader2 = Files.newBufferedReader(file2);
    return IOUtils.contentEquals(reader1, reader2);
  }

  public static boolean dirsFileCountsMatch(Path dir1, Path dir2) throws IOException {

    List<Path> filepaths1 = getPathsFromDir(dir1);
    List<Path> filepaths2 = getPathsFromDir(dir2);

    // Check that both directories contain the same number of files
    if (filepaths1.size() == filepaths2.size()) {
      log.debug(String.format("Directory %s has %d files.  Directory %s has %d files.", dir1.toString(), filepaths1.size(), dir2.toString(), filepaths2.size()));
      return true;
    }

    return false;

  }

}
