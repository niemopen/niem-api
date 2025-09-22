package gov.niem.tools.api.db.property;

import gov.niem.tools.api.core.config.Config.AppMediaType;
import gov.niem.tools.api.core.utils.CmfUtils;
import gov.niem.tools.api.db.ServiceHub;
import gov.niem.tools.api.db.base.AddModelReason;
import gov.niem.tools.api.db.exceptions.EntityNotFoundException;

import org.mitre.niem.cmf.Model;

import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import java.util.List;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
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
  @GetMapping("/properties/{qname}/formats/cmf")
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
    property.addToCmfModel(cmfModel, false, AddModelReason.REPRESENTATION, null);
    return CmfUtils.generateString(cmfModel, mediaType);
  }

  /**
   * Gets all properties from the version with the given fields.
   */
  @GetMapping("/properties")
  @ResponseStatus(code = HttpStatus.OK)
  @ApiResponse(responseCode = "422", description = "Unprocessable Entity", content = @Content)
  public Page<Property> getVersionProperties(
      @PathVariable String stewardKey,
      @PathVariable String modelKey,
      @PathVariable String versionNumber,
      @PageableDefault(sort = {"prefix", "name"}) Pageable pageable)
      throws Exception {
    return hub.properties.findByVersion(stewardKey, modelKey, versionNumber, pageable);
  }

  /**
   * Gets all properties from the namespace with the given fields.
   * Default sort is namespace rank (e.g., Core first), prefix, and then name.
   */
  @GetMapping("/namespaces/{prefix}/properties")
  @ResponseStatus(code = HttpStatus.OK)
  @ApiResponse(responseCode = "422", description = "Unprocessable Entity", content = @Content)
  public Page<Property> getNamespaceProperties(
      @PathVariable String stewardKey,
      @PathVariable String modelKey,
      @PathVariable String versionNumber,
      @PathVariable String prefix,
      @PageableDefault(sort = {"prefix", "name"}) Pageable pageable)
      throws Exception {
    return hub.properties.findByNamespace(stewardKey, modelKey, versionNumber, prefix, pageable);
  }

  /**
   * Gets a list of the top 10 properties with the keyword from the version with the given fields.
   *
   * @param keyword - A keyword in a property name.  May be qualified with a namespace prefix
   *      or with an empty string to treat the keyword as a starts-with search vs leading and
   *      trailing wildcards.  Note that spaces in the keyword are not supported.
   */
  @GetMapping("/properties/keyword/{keyword}")
  @ResponseStatus(code = HttpStatus.OK)
  @ApiResponse(responseCode = "422", description = "Unprocessable Entity", content = @Content)
  public List<Property> getKeywordProperties(
      @PathVariable String stewardKey,
      @PathVariable String modelKey,
      @PathVariable String versionNumber,
      @PathVariable String keyword) throws Exception {
    return hub.properties.findByKeyword(stewardKey, modelKey, versionNumber, keyword);
  }

  /**
   * Gets all properties from a namespace with the given fields in CMF.
   */
  @GetMapping("/namespaces/{prefix}/properties/formats/cmf")
  @ResponseStatus(code = HttpStatus.OK)
  @ApiResponse(responseCode = "422", description = "Unprocessable Entity", content = @Content)
  public Object getNamespacePropertiesCmf(
      @PathVariable String stewardKey,
      @PathVariable String modelKey,
      @PathVariable String versionNumber,
      @PathVariable String prefix,
      @PageableDefault(sort = {"namespace.prefix", "name"}) Pageable pageable,
      @RequestParam(required = false, defaultValue = "json") AppMediaType mediaType)
      throws Exception {
    Page<Property> properties = hub.properties.findByNamespace(stewardKey,
        modelKey, versionNumber, prefix, pageable);
    org.mitre.niem.cmf.Model cmfModel = new Model();
    for (Property property : properties) {
      property.addToCmfModel(cmfModel, false, AddModelReason.REPRESENTATION, null);
    }
    return CmfUtils.generateString(cmfModel, mediaType);
  }

  /**
   * Count all properties in the version with the given fields.
   */
  @GetMapping("/properties/count")
  @ResponseStatus(code = HttpStatus.OK)
  public long countVersionProperties(
      @PathVariable String stewardKey,
      @PathVariable String modelKey,
      @PathVariable String versionNumber,
      @RequestParam(required = false) Property.Category category) throws EntityNotFoundException {
    return hub.properties.countByVersion(stewardKey, modelKey, versionNumber, category);
  }

  /**
   * Count all properties in the namespace with the given fields.
   */
  @GetMapping("/namespaces/{prefix}/properties/count")
  @ResponseStatus(code = HttpStatus.OK)
  public long countNamespaceProperties(
      @PathVariable String stewardKey,
      @PathVariable String modelKey,
      @PathVariable String versionNumber,
      @PathVariable String prefix,
      @RequestParam(required = false) Property.Category category) throws EntityNotFoundException {
    return hub.properties.countByNamespace(stewardKey, modelKey, versionNumber, prefix, category);
  }

  /**
   * Get the previous version of the property with the given fields.
   *
   * @param includePreRelease - True to return a result from a pre-release if applicable;
   *     false to iterate until an official version is reached.
   */
  @GetMapping("/properties/{qname}/prev")
  @ResponseStatus(code = HttpStatus.OK)
  public Property getPrev(
      @PathVariable String stewardKey,
      @PathVariable String modelKey,
      @PathVariable String versionNumber,
      @PathVariable String qname,
      @RequestParam(required = false, defaultValue = "false") boolean includePreRelease)
      throws Exception {
    Property property = hub.properties.findOne(stewardKey, modelKey, versionNumber, qname);
    return hub.properties.getPrev(property, includePreRelease);
  }

  /**
   * Get the next version of the property with the given fields.
   *
   * @param includePreRelease - True to return a result from a pre-release if applicable;
   *     false to iterate until an official version is reached.
   */
  @GetMapping("/properties/{qname}/next")
  @ResponseStatus(code = HttpStatus.OK)
  public Property getNext(
      @PathVariable String stewardKey,
      @PathVariable String modelKey,
      @PathVariable String versionNumber,
      @PathVariable String qname,
      @RequestParam(required = false, defaultValue = "false") boolean includePreRelease)
      throws Exception {
    Property property = hub.properties.findOne(stewardKey, modelKey, versionNumber, qname);
    return hub.properties.getNext(property, includePreRelease);
  }

  /**
   * Get the current version of the property with the given fields.
   */
  @GetMapping("/properties/{qname}/current")
  @ResponseStatus(code = HttpStatus.OK)
  public Property getCurrent(
      @PathVariable String stewardKey,
      @PathVariable String modelKey,
      @PathVariable String versionNumber,
      @PathVariable String qname) throws Exception {
    Property property = hub.properties.findOne(stewardKey, modelKey, versionNumber, qname);
    return property.getCurrent();
  }

  /**
   * Get the history of the property with the given fields.
   *
   * @param includePreRelease - True to return a result from a pre-release if applicable;
   *     false to iterate until an official version is reached.
   */
  @GetMapping("/properties/{qname}/history")
  @ResponseStatus(code = HttpStatus.OK)
  public List<Property> getHistory(
      @PathVariable String stewardKey,
      @PathVariable String modelKey,
      @PathVariable String versionNumber,
      @PathVariable String qname,
      @RequestParam(required = false, defaultValue = "false") boolean includePreRelease)
      throws Exception {
    Property property = hub.properties.findOne(stewardKey, modelKey, versionNumber, qname);
    return hub.properties.getHistory(property, includePreRelease);
  }

  /**
   * Get the list of substitutions for the property with the given fields.
   */
  @GetMapping("/properties/{qname}/substitutions")
  @ResponseStatus(code = HttpStatus.OK)
  public List<Property> getSubstitutions(
      @PathVariable String stewardKey,
      @PathVariable String modelKey,
      @PathVariable String versionNumber,
      @PathVariable String qname) throws Exception {
    return hub.properties.getSubstitutions(stewardKey, modelKey, versionNumber, qname);
  }

  /**
   * Get the list of substitution group heads for the property with the given fields.
   *
   * <p>Note that for substitutable properties, this is usually a single abstract property
   * on its own.  Occasionally, this abstract property may also belong to its own substitution
   * group. This chain of substitution groups may be needed when dealing with property
   * dependencies.
   */
  @GetMapping("/properties/{qname}/groups")
  @ResponseStatus(code = HttpStatus.OK)
  public List<Property> getSubstitutionGroups(
      @PathVariable String stewardKey,
      @PathVariable String modelKey,
      @PathVariable String versionNumber,
      @PathVariable String qname) throws Exception {
    return hub.properties.getSubstitutionGroups(stewardKey, modelKey, versionNumber, qname);
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
