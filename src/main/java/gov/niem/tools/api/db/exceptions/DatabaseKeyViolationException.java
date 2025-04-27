package gov.niem.tools.api.db.exceptions;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

/**
 * Customized exception handler for database key violations.
 */
@ResponseStatus(value = HttpStatus.UNPROCESSABLE_ENTITY)
public class DatabaseKeyViolationException extends RuntimeException {

  /**
   * Throws an exception after an attempt to add an object that is not unique to the database.
   */
  public DatabaseKeyViolationException(String model, String value) {
    super(
        String.format("Your request to add a new '%s' failed. There is already one named '%s'",
        model, value));
  }

  /**
   * Throws an exception after an attempt to add an object that is not unique to the database.
   */
  @SuppressWarnings("checkstyle:linelength")
  public DatabaseKeyViolationException(String model, String value, String additionalErrors) {
    super(
        String.format("Your request to add a new %s failed. There is already one named %s. Additional Info: %s", model, value, additionalErrors));
  }

}
