package gov.niem.tools.api.db.exceptions;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

/**
 * Custom exception handler for entity not unique exceptions.
 */
@ResponseStatus(value = HttpStatus.UNPROCESSABLE_CONTENT)
public class EntityNotUniqueException extends RuntimeException {

  public EntityNotUniqueException(String entityKind, String label) {
    super(String.format("%s [%s] already exists", entityKind, label));
  }

}
