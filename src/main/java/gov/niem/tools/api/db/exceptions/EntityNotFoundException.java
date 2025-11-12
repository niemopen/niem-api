package gov.niem.tools.api.db.exceptions;

import gov.niem.tools.api.db.base.BaseEntity;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

/**
 * Custom exception handler for entity not found exceptions.
 */
@ResponseStatus(value = HttpStatus.UNPROCESSABLE_CONTENT)
public class EntityNotFoundException extends RuntimeException {

  public EntityNotFoundException(String entityKind, String label) {
    super(String.format("%s [%s] not found", entityKind, label));
  }

  public EntityNotFoundException(BaseEntity entity, String label) {
    super(String.format("%s [%s] not found", entity.getClassName(), label));
  }

  public EntityNotFoundException(BaseEntity entity) {
    super(String.format("%s [%s] not found", entity.getClassName(), entity.getIdLabel()));
  }

}
