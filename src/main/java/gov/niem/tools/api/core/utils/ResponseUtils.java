package gov.niem.tools.api.core.utils;

import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;

/**
 * HTTP response-related utility functions.
 */
public class ResponseUtils {

  /**
   * Returns a response entity with the given filename set in the header and bytes set in the body.
   */
  public static ResponseEntity<byte[]> getResponseFile(byte[] bytes, String filename) {
    return ResponseEntity
      .ok()
      .header(HttpHeaders.ACCESS_CONTROL_EXPOSE_HEADERS, "Content-Disposition")
      .header(HttpHeaders.CACHE_CONTROL, "no-cache, no-store, must-revalidate")
      .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=" + filename)
      .header(HttpHeaders.EXPIRES, "0")
      .header(HttpHeaders.PRAGMA, "no-cache")
      .body(bytes);
  }

  /**
   * Returns a response entity with the given filename set in the header, bytes set in the body,
   * and the content type set to the given media type.
   */
  public static ResponseEntity<byte[]> getResponseFile(byte[] bytes, String filename,
      MediaType mediaType) {
    return ResponseEntity
      .ok()
      .contentType(mediaType)
      .header(HttpHeaders.ACCESS_CONTROL_EXPOSE_HEADERS, "Content-Disposition")
      .header(HttpHeaders.CACHE_CONTROL, "no-cache, no-store, must-revalidate")
      .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=" + filename)
      .header(HttpHeaders.EXPIRES, "0")
      .header(HttpHeaders.PRAGMA, "no-cache")
      .body(bytes);
  }

  /**
   * Returns a response entity for a zip file.
   */
  public static ResponseEntity<byte[]> getResponseFileZip(byte[] bytes, String filename) {
    return getResponseFile(bytes, filename, MediaType.valueOf("application/zip"));
  }

  /**
   * Returns a response entity for a JSON file.
   */
  public static ResponseEntity<byte[]> getResponseFileJson(byte[] bytes, String filename) {
    return getResponseFile(bytes, filename, MediaType.APPLICATION_JSON);
  }

  /**
   * Returns a response entity for a text file.
   */
  public static ResponseEntity<byte[]> getResponseFileText(byte[] bytes, String filename) {
    return getResponseFile(bytes, filename, MediaType.TEXT_PLAIN);
  }

  /**
   * Returns a response entity for an XML file.
   */
  public static ResponseEntity<byte[]> getResponseFileXml(byte[] bytes, String filename) {
    return getResponseFile(bytes, filename, MediaType.APPLICATION_XML);
  }

  /**
   * Returns a response entity for a CSV file.
   */
  public static ResponseEntity<byte[]> getResponseFileCsv(byte[] bytes, String filename) {
    return getResponseFile(bytes, filename, new MediaType("text", "csv"));
  }

  /**
   * Returns a response entity for a CSV file.
   */
  public static ResponseEntity<byte[]> getResponseFileCsv(String data, String filename) {
    return getResponseFile(data.getBytes(), filename, new MediaType("text", "csv"));
  }

}
