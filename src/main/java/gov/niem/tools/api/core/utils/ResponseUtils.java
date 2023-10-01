package gov.niem.tools.api.core.utils;

import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;

public class ResponseUtils {

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

  public static ResponseEntity<byte[]> getResponseFile(byte[] bytes, String filename, MediaType mediaType) {
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

  public static ResponseEntity<byte[]> getResponseFileZip(byte[] bytes, String filename) {
    return getResponseFile(bytes, filename, MediaType.valueOf("application/zip"));
  }

  public static ResponseEntity<byte[]> getResponseFileJSON(byte[] bytes, String filename) {
    return getResponseFile(bytes, filename, MediaType.APPLICATION_JSON);
  }

  public static ResponseEntity<byte[]> getResponseFileText(byte[] bytes, String filename) {
    return getResponseFile(bytes, filename, MediaType.TEXT_PLAIN);
  }

  public static ResponseEntity<byte[]> getResponseFileXML(byte[] bytes, String filename) {
    return getResponseFile(bytes, filename, MediaType.APPLICATION_XML);
  }

  public static ResponseEntity<byte[]> getResponseFileCsv(String data, String filename) {
    // ResponseEntity<byte[]> response = getResponseFile(data.getBytes(), filename);
    // HttpHeaders headers = response.getHeaders();
    // headers.add("Content-Type", "text/csv");
    // return response;
    return ResponseUtils.getResponseFileText(data.getBytes(), filename);
  }

}
