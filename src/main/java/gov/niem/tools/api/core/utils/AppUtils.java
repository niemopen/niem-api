package gov.niem.tools.api.core.utils;

import java.io.ByteArrayOutputStream;
import java.io.PrintStream;
import java.text.SimpleDateFormat;
import java.util.Date;
import lombok.extern.java.Log;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;

/**
 * General utility functions for the application.
 */
@Log
public class AppUtils {

  /**
   * Logs the given message.
   */
  public static String log(String message) {
    log.info(message);
    return message;
  }

  /**
   * Logs the given arguments as a formatted message.
   */
  public static String log(String messageBase, String label) {
    String message = String.format("%s [%s]", messageBase, label);
    return AppUtils.log(message);
  }

  /**
   * Returns an OK plain text response.
   */
  public static ResponseEntity<String> getResponseOkString(String message) {
    return ResponseEntity.ok().contentType(MediaType.TEXT_PLAIN).body(message);
  }

  /**
   * Returns an OK JSON response.
   */
  public static ResponseEntity<String> getResponseOkJson(String message) {
    return ResponseEntity.ok().contentType(MediaType.APPLICATION_JSON).body(message);
  }

  /**
   * Returns an Unprocessable Entity plain text response.
   */
  public static ResponseEntity<String> getResponseUnprocessableString(String message) {
    return ResponseEntity.unprocessableContent().contentType(MediaType.TEXT_PLAIN).body(message);
  }

  /**
   * Gets the current date and time with formatting.
   */
  public static String getTimestamp() {
    return new SimpleDateFormat("yyyy-MM-dd-HHmm").format(new Date());
  }

  /**
   * Capture console output for the given runnable and return as a string.
   */
  public static String captureLog(Runnable runnable) {
    ByteArrayOutputStream capturedOut = new ByteArrayOutputStream();
    PrintStream originalOut = System.out;
    PrintStream originalErr = System.err;

    System.setOut(new PrintStream(capturedOut));
    System.setErr(new PrintStream(capturedOut));

    try {
      runnable.run();
    }
    finally {
      System.setOut(originalOut);
      System.setErr(originalErr);
    }

    return capturedOut.toString();
  }

}