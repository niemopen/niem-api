package gov.niem.tools.api.db.namespace;

import gov.niem.tools.api.core.config.Config.AppMediaType;
import gov.niem.tools.api.core.utils.CmfUtils;
import gov.niem.tools.api.db.ServiceHub;
import gov.niem.tools.api.db.exceptions.EntityNotFoundException;

import org.mitre.niem.cmf.Model;

import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import java.util.ArrayList;
import java.util.List;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

/**
 * REST controller for namespaces.
 */
@Validated
@RestController
@RequestMapping("/stewards/{stewardKey}/models/{modelKey}/versions/{versionNumber}")
@Tag(
    name = "Data-4: Namespace",
    description = "A collection of properties and types managed by an authoritative source.")
public class NamespaceController {

  @Autowired
  ServiceHub hub;

  /**
   * Get a namespace with the given fields.
   *
   * @example http://tools.niem.gov/api/v2/stewards/niem/models/model/versions/5.2/namespaces/nc
   */
  @GetMapping("/namespaces/{prefix}")
  @ResponseStatus(code = HttpStatus.OK)
  @ApiResponse(responseCode = "422", description = "Unprocessable Entity", content = @Content)
  public Namespace getNamespace(
      @PathVariable String stewardKey,
      @PathVariable String modelKey,
      @PathVariable String versionNumber,
      @PathVariable String prefix) throws Exception {
    return hub.namespaces.findOne(stewardKey, modelKey, versionNumber, prefix);
  }

  /**
   * Get a namespace in CMF with the given fields.
   *
   * @example http://tools.niem.gov/api/v2/stewards/niem/models/model/versions/5.2/namespaces.cmf/nc
   */
  @GetMapping("/namespaces.cmf/{prefix}")
  @ResponseStatus(code = HttpStatus.OK)
  @ApiResponse(responseCode = "422", description = "Unprocessable Entity", content = @Content)
  public Object getNamespaceCmf(
      @PathVariable String stewardKey,
      @PathVariable String modelKey,
      @PathVariable String versionNumber,
      @PathVariable String prefix,
      @RequestParam(required = false, defaultValue = "json") AppMediaType mediaType)
      throws Exception {
    Namespace namespace = hub.namespaces.findOne(stewardKey, modelKey, versionNumber, prefix);
    org.mitre.niem.cmf.Model cmfModel = new Model();
    namespace.addToCmfModel(cmfModel);
    return CmfUtils.generateString(cmfModel, mediaType);
  }

  /**
   * Get all namespaces from a version of a model.
   *
   * @example http://tools.niem.gov/api/v2/stewards/niem/models/model/versions/5.2/namespaces
   */
  @GetMapping("/namespaces")
  @ResponseStatus(code = HttpStatus.OK)
  @ApiResponse(responseCode = "422", description = "Unprocessable Entity", content = @Content)
  public List<Namespace> getVersionNamespaces(
      @PathVariable String stewardKey,
      @PathVariable String modelKey,
      @PathVariable String versionNumber) throws Exception {

    return new ArrayList<Namespace>(hub.namespaces.findByKeys(stewardKey, modelKey, versionNumber));

  }

  /**
   * Get all namespaces as CMF from a version of a model.
   *
   * @example http://tools.niem.gov/api/v2/stewards/niem/models/model/versions/5.2/namespaces.cmf
   */
  @GetMapping("/namespaces.cmf")
  @ResponseStatus(code = HttpStatus.OK)
  @ApiResponse(responseCode = "422", description = "Unprocessable Entity", content = @Content)
  public Object getVersionNamespacesCmf(
      @PathVariable String stewardKey,
      @PathVariable String modelKey,
      @PathVariable String versionNumber,
      @RequestParam(required = false, defaultValue = "json") AppMediaType mediaType)
      throws Exception {

    // Get results
    ArrayList<Namespace> namespaces = new ArrayList<Namespace>(
        hub.namespaces.findByKeys(stewardKey, modelKey, versionNumber));

    // Convert results to CMF
    org.mitre.niem.cmf.Model cmfModel = new Model();
    for (Namespace namespace : namespaces) {
      namespace.addToCmfModel(cmfModel);
    }
    return CmfUtils.generateString(cmfModel, mediaType);
  }

  /**
   * Count all namespaces in the version with the given fields.
   */
  @GetMapping("/namespaces/count")
  @ResponseStatus(code = HttpStatus.OK)
  public long countNamespaces(
      @PathVariable String stewardKey,
      @PathVariable String modelKey,
      @PathVariable String versionNumber,
      @RequestParam(required = false) Namespace.Category category) throws EntityNotFoundException {
    return hub.namespaces.count(stewardKey, modelKey, versionNumber, category);
  }

  // @PostMapping("/namespaces")
  // public ResponseEntity<String> postNamespace(@PathVariable String stewardKey,
  //     @PathVariable String modelKey, @PathVariable String versionNumber, Namespace namespace)
  //     throws Exception {
  //   String message = hub.namespaces.add(stewardKey, modelKey, versionNumber, namespace);
  //   return AppUtils.getResponseOkString(message);
  // }

}
