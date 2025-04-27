package gov.niem.tools.api.app;

import gov.niem.tools.api.core.config.Config;
import gov.niem.tools.api.core.utils.AppUtils;

import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * General application calls.
 */
@RestController
@Tag(name = "App", description = "General application calls")
@ApiResponse(responseCode = "200", description = "Success", content = {
  @Content(mediaType = MediaType.TEXT_PLAIN_VALUE)
})
public class AppController {

  /**
   * Get the current version of the NIEM API application.
   */
  @GetMapping("/version")
  public ResponseEntity<String> version() {
    return AppUtils.getResponseOkString(Config.draft);
  }

  /**
   * Get the current version of the CMF Specification supported by the NIEM API.
   */
  @GetMapping("/version/cmf")
  public ResponseEntity<String> cmfVersion() {
    return AppUtils.getResponseOkString(Config.cmfVersion);
  }

  /**
   * Get the current version of the CMF Tool used by the NIEM API.
   */
  @GetMapping("/version/cmftool")
  public ResponseEntity<String> cmftoolVersion() {
    return AppUtils.getResponseOkString(Config.cmftoolVersion);
  }

}
