package gov.niem.tools.api.core.exceptions;

import org.springframework.http.HttpStatus;
import org.springframework.web.server.ResponseStatusException;

public class BadRequestException extends ResponseStatusException {

  public BadRequestException(String reason) {
    super(HttpStatus.BAD_REQUEST, reason);
  }

}
