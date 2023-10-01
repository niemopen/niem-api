package gov.niem.tools.api.core.utils;

import java.text.SimpleDateFormat;
import java.util.Date;

import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;

import gov.niem.tools.api.db.exceptions.FieldNotFoundException;
import lombok.extern.java.Log;

@Log
public class AppUtils {

  public static String log(String message) {
    log.info(message);
    return message;
  }

  public static String log(String messageBase, String label) {
    String message = String.format("%s [%s]", messageBase, label);
    return AppUtils.log(message);
  }

  public static ResponseEntity<String> getResponseOkString(String message) {
    return ResponseEntity.ok().contentType(MediaType.TEXT_PLAIN).body(message);
  }

  public static ResponseEntity<String> getResponseOkJSON(String message) {
    return ResponseEntity.ok().contentType(MediaType.APPLICATION_JSON).body(message);
  }

  public static ResponseEntity<String> getResponseUnprocessableString(String message) {
    return ResponseEntity.unprocessableEntity().contentType(MediaType.TEXT_PLAIN).body(message);
  }

  public static ResponseEntity<String> getResponseErrorJSON(String errorMessage) throws FieldNotFoundException {
    return ResponseEntity.ok().contentType(MediaType.APPLICATION_JSON).body(errorMessage);
  }

  public static String getTimestamp() {
    return new SimpleDateFormat("yyyy-MM-dd-HHmm").format(new Date());
  }

}