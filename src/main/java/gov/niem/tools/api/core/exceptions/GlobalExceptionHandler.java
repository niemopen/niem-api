package gov.niem.tools.api.core.exceptions;

import gov.niem.tools.api.db.exceptions.DatabaseKeyViolationException;
import gov.niem.tools.api.db.exceptions.EntityNotFoundException;
import gov.niem.tools.api.db.exceptions.EntityNotUniqueException;
import gov.niem.tools.api.db.exceptions.FieldNotFoundException;

import java.sql.Timestamp;
import java.util.LinkedHashMap;
import java.util.Map;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.context.request.WebRequest;
import org.springframework.web.servlet.mvc.method.annotation.ResponseEntityExceptionHandler;

/**
 * Adds a response header for exceptions.
 */
@ControllerAdvice
public class GlobalExceptionHandler extends ResponseEntityExceptionHandler {

  @ExceptionHandler(value = {
    Exception.class,
    BadRequestException.class,
    NoContentException.class,
    DatabaseKeyViolationException.class,
    EntityNotFoundException.class,
    EntityNotUniqueException.class,
    FieldNotFoundException.class
  })
  protected ResponseEntity<Object> handleGenericException(Exception exception, WebRequest request) {

    // Add headers for error message
    HttpHeaders headers = new HttpHeaders();
    headers.add("X-Error-Message", exception.getMessage());
    headers.add("Content-Type", MediaType.APPLICATION_PROBLEM_JSON_VALUE);

    HttpStatus status;
    String exceptionClassName = exception.getClass().getName();

    // Set http status code for custom application exceptions
    if (exceptionClassName.contains("gov.niem.tools.api.db.exceptions")) {
      status = HttpStatus.UNPROCESSABLE_ENTITY;
    }
    else if (exceptionClassName.contains("BadRequestException")) {
      status = HttpStatus.BAD_REQUEST;
    }
    else if (exceptionClassName.contains("NoContentException")) {
      status = HttpStatus.NO_CONTENT;
    }
    else {
      status = HttpStatus.valueOf(exception.getClass().getName());
    }

    Map<String, Object> error = new LinkedHashMap<>();
    error.put("timestamp", new Timestamp(System.currentTimeMillis()).toString());
    error.put("status", status.value());
    error.put("title", status.getReasonPhrase());  // or key "error"
    error.put("detail", exception.getMessage()     // or key "message"
        .replaceAll("\"", "")
        .replaceAll("400 BAD_REQUEST ", ""));
    error.put("instance", request.getDescription(false).replace("uri=", ""));  // or key "path"

    return handleExceptionInternal(exception, error, headers, status, request);

  }

}
