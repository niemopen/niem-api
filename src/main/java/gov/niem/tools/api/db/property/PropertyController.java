package gov.niem.tools.api.db.property;

import gov.niem.tools.api.core.config.Config.AppMediaType;
import gov.niem.tools.api.core.utils.CmfUtils;
import gov.niem.tools.api.db.ServiceHub;

import org.mitre.niem.cmf.Model;

import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import java.util.List;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

/**
 * REST controller for properties.
 */
@RestController
@RequestMapping("stewards/{stewardKey}/models/{modelKey}/versions/{versionNumber}")
@Tag(
    name = "Data-5: Properties",
    description = "A property represents a concept, idea, or thing. It defines specific semantics and appears in exchanges as the tag or label for a field.")
public class PropertyController {

  @Autowired
  ServiceHub hub;

  /**
   * Gets a property with the given fields.
   */
  @GetMapping("/properties/{qname}")
  @ResponseStatus(code = HttpStatus.OK)
  @ApiResponse(responseCode = "422", description = "Unprocessable Entity", content = @Content)
  public Property getProperty(
      @PathVariable String stewardKey,
      @PathVariable String modelKey,
      @PathVariable String versionNumber,
      @PathVariable String qname) throws Exception {
    return hub.properties.findOne(stewardKey, modelKey, versionNumber, qname);
  }

  /**
   * Gets a property in CMF with the given fields.
   */
  @GetMapping("/properties.cmf/{qname}")
  @ResponseStatus(code = HttpStatus.OK)
  @ApiResponse(responseCode = "422", description = "Unprocessable Entity", content = @Content)
  public Object getPropertyCmf(
      @PathVariable String stewardKey,
      @PathVariable String modelKey,
      @PathVariable String versionNumber,
      @PathVariable String qname,
      @RequestParam(required = false, defaultValue = "json") AppMediaType mediaType)
      throws Exception {
    Property property = hub.properties.findOne(stewardKey, modelKey, versionNumber, qname);
    org.mitre.niem.cmf.Model cmfModel = new Model();
    property.addToCmfModel(cmfModel);
    return CmfUtils.generateString(cmfModel, mediaType);
  }

  /**
   * Gets all properties from the version with the given fields.
   * Note: Currently returns null until pagination support is added.
   *
   * @todo Add pagination support for version properties and return results.
   */
  @GetMapping("/properties")
  @ResponseStatus(code = HttpStatus.OK)
  @ApiResponse(responseCode = "422", description = "Unprocessable Entity", content = @Content)
  public List<Property> getAllProperties(
      @PathVariable String stewardKey,
      @PathVariable String modelKey,
      @PathVariable String versionNumber) throws Exception {
    // return hub.properties.findByVersion(stewardKey, modelKey, versionNumber);
    return null;
  }

  // /**
  //  * Add a new property.
  //  */
  // @PostMapping("/properties")
  // @ResponseStatus(code = HttpStatus.OK)
  // @ApiResponse(responseCode = "422", description = "Unprocessable Entity", content = @Content)
  // @SecurityRequirement(name = "bearerAuthentication")
  // public Property addProperty(@PathVariable String stewardKey,
  //  @PathVariable String modelKey, @PathVariable String versionNumber, Property property)
  //      throws Exception {
  //     return null;
  //   // String message = hub.properties.add(stewardKey, modelKey, versionNumber, property);
  //   // return AppUtils.getResponseOkString(message);
  // }

  // /**
  //  * Update an existing property.
  //  */
  // @PutMapping("/properties")
  // @ResponseStatus(code = HttpStatus.OK)
  // @ApiResponse(responseCode = "422", description = "Unprocessable Entity", content = @Content)
  // @SecurityRequirement(name = "bearerAuthentication")
  // public Property editProperty(@PathVariable String stewardKey,
  //  @PathVariable String modelKey, @PathVariable String versionNumber, String currentQname,
  //      Property property) throws Exception {
  //     return null;
  //   // String message = hub.properties.add(stewardKey, modelKey, versionNumber, property);
  //   // return AppUtils.getResponseOkString(message);
  // }

  // /**
  //  * Delete a property.
  //  */
  // @DeleteMapping("/properties")
  // @ResponseStatus(code = HttpStatus.OK)
  // @ApiResponse(responseCode = "422", description = "Unprocessable Entity", content = @Content)
  // @SecurityRequirement(name = "bearerAuthentication")
  // public Property deleteProperty(@PathVariable String stewardKey,
  //   @PathVariable String modelKey, @PathVariable String versionNumber, String qname) {
  //   return null;
  // }

}
