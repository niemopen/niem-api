package gov.niem.tools.api.db.exceptions;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

/**
 * Custom exception handler for entity not found exceptions.
 */
@ResponseStatus(value = HttpStatus.UNPROCESSABLE_ENTITY)
public class ArgumentNotValidException extends RuntimeException {

  public ArgumentNotValidException(String fieldName, String message) {
    super(String.format("[%s] is not a valid argument. " + message, fieldName));
  }
}
