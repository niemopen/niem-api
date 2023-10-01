package gov.niem.tools.api.core.exceptions;

import org.springframework.http.HttpStatus;
import org.springframework.web.server.ResponseStatusException;

public class NoContentException extends ResponseStatusException {

  public NoContentException() {
    super(HttpStatus.NO_CONTENT);
  }

}
