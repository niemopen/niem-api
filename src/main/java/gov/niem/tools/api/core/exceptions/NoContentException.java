package gov.niem.tools.api.core.exceptions;

import org.springframework.http.HttpStatus;
import org.springframework.web.server.ResponseStatusException;

/**
 * Exception handler for requests in which no content was found for the given criteria.
 */
public class NoContentException extends ResponseStatusException {

  public NoContentException() {
    super(HttpStatus.NO_CONTENT);
  }

}
