package gov.niem.tools.api.app;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import gov.niem.tools.api.core.config.Config;
import io.swagger.v3.oas.annotations.tags.Tag;

/**
 * General application calls
 */
@Tag(name = "App", description = "General application calls")
@RestController
public class AppController {

  /**
   * Gets the current version of the NIEM API application.
   * @return Version
   */
  @GetMapping("/version")
  @ResponseStatus(HttpStatus.OK)
  public String version() {
    return Config.draft;
  }

}
