package gov.niem.tools.api.db.steward;

import gov.niem.tools.api.db.ServiceHub;
import gov.niem.tools.api.db.exceptions.EntityNotFoundException;

import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import java.util.List;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

/**
 * REST controller for stewards.
 */
@Validated
@RestController
@Tag(name = "Data-1: Stewards",
    description = "A group or entity responsible for managing NIEM content.")
public class StewardController {

  @Autowired
  ServiceHub hub;

  /**
   * Get a steward.
   */
  @GetMapping("/stewards/{stewardKey}")
  @ResponseStatus(code = HttpStatus.OK)
  @ApiResponse(responseCode = "422", description = "Unprocessable Entity", content = @Content)
  public Steward getSteward(@PathVariable String stewardKey) throws EntityNotFoundException {
    return hub.stewards.findOne(stewardKey);
  }

  /**
   * Get all stewards.
   */
  @GetMapping("/stewards")
  @ResponseStatus(code = HttpStatus.OK)
  public List<Steward> getStewards() throws Exception {
    return hub.stewards.repository().findAll();
  }

  // /**
  //  * Add a steward.
  //  */
  // @PostMapping(path = "/stewards")
  // public ResponseEntity<String> postSteward(@RequestBody Steward steward) throws Exception {
  //   String message = hub.stewards.add(steward);
  //   return AppUtils.getResponseOkString(message);
  // }

  // /**
  //  * Update a steward.
  //  */
  // @PutMapping(path = "/stewards/{stewardKey}")
  // public ResponseEntity<String> putSteward(
  //     @PathVariable String stewardKey,
  //     @RequestBody Steward updatedSteward,
  //     BindingResult bindingResult
  // ) throws Exception {
  //   String message = hub.stewards.edit(stewardKey, updatedSteward);
  //   return AppUtils.getResponseOkString(message);
  // }

  // /**
  //  * Patch a steward.
  //  */
  // @PatchMapping(path = "/stewards/{stewardKey}")
  // public ResponseEntity<String> patchSteward(
  //     @PathVariable String stewardKey,
  //     Steward  updatedSteward
  // ) throws Exception {
  //   String message = hub.stewards.edit(stewardKey, updatedSteward);
  //   return AppUtils.getResponseOkString(message);
  // }

  // /**
  //  * Delete a steward.
  //  */
  // @DeleteMapping(path = "/stewards/{stewardKey}")
  // public ResponseEntity<String> deleteSteward(@PathVariable String stewardKey) throws Exception {
  //   String message = hub.stewards.delete(stewardKey);
  //   return AppUtils.getResponseOkString(message);
  // }

}
