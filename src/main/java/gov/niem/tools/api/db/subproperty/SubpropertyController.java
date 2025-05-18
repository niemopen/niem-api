package gov.niem.tools.api.db.subproperty;

import gov.niem.tools.api.core.config.Config.AppMediaType;
import gov.niem.tools.api.core.utils.CmfUtils;
import gov.niem.tools.api.db.ServiceHub;

import io.swagger.v3.oas.annotations.tags.Tag;
import java.util.List;
import java.util.Set;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

/**
 * REST controller for subproperties.
 */
@RestController
@RequestMapping("stewards/{stewardKey}/models/{modelKey}/versions/{versionNumber}")
@Tag(
    name = "Data-7: Subproperties",
    description = "A subproperty is the occurrence of a property as it appears when contained by a type.  It carries additional characteristics in this situation, such as cardinality constraints.")
public class SubpropertyController {

  @Autowired
  ServiceHub hub;

  /**
   * Gets a subproperty from the database with the type with the given type fields and
   * the property with the given qualified name.
   */
  @GetMapping("/types/{typeQname}/subproperties/{propertyQname}")
  public Subproperty getSubproperty(
      @PathVariable String stewardKey,
      @PathVariable String modelKey,
      @PathVariable String versionNumber,
      @PathVariable String typeQname,
      @PathVariable String propertyQname) throws Exception {
    return hub.subproperties.findOne(stewardKey, modelKey, versionNumber, typeQname, propertyQname);
  }

  /**
   * Gets a subproperty from the database with the type with the given type fields and
   * the property with the given qualified name, converted to CMF.
   */
  @GetMapping("/types/{typeQname}/subproperties/{propertyQname}/formats/cmf")
  public Object getSubpropertyCmf(
      @PathVariable String stewardKey,
      @PathVariable String modelKey,
      @PathVariable String versionNumber,
      @PathVariable String typeQname,
      @PathVariable String propertyQname,
      @RequestParam(required = false, defaultValue = "json") AppMediaType mediaType)
      throws Exception {
    Subproperty subproperty = hub.subproperties.findOne(stewardKey, modelKey,
        versionNumber, typeQname, propertyQname);
    org.mitre.niem.cmf.Model cmfModel = new org.mitre.niem.cmf.Model();
    subproperty.addToCmfModel(cmfModel);
    return CmfUtils.generateString(cmfModel, mediaType);
  }

  /**
   * Gets all subproperties from the database in the version with the given fields.
   */
  @GetMapping("/subproperties")
  public Set<Subproperty> getAllSubproperties(
      @PathVariable String stewardKey,
      @PathVariable String modelKey,
      @PathVariable String versionNumber) throws Exception {
    return hub.subproperties.findByVersion(stewardKey, modelKey, versionNumber);
  }

  /**
   * Gets all subproperties from the database in the type with the give fields.
   */
  @GetMapping("/types/{typeQname}/subproperties")
  public Set<Subproperty> getTypeSubproperties(
      @PathVariable String stewardKey,
      @PathVariable String modelKey,
      @PathVariable String versionNumber,
      @PathVariable String typeQname) throws Exception {
    return hub.subproperties.findByType(stewardKey, modelKey, versionNumber, typeQname);
  }

  /**
   * Gets all subproperties from the database in the type with the given fields, in CMF.
   */
  @GetMapping("/types/{typeQname}/subproperties/formats/cmf")
  public Object getTypeSubpropertiesCmf(
      @PathVariable String stewardKey,
      @PathVariable String modelKey,
      @PathVariable String versionNumber,
      @PathVariable String typeQname,
      @RequestParam(required = false, defaultValue = "json") AppMediaType mediaType)
      throws Exception {
    Set<Subproperty> subproperties = hub.subproperties.findByType(stewardKey,
        modelKey, versionNumber, typeQname);
    org.mitre.niem.cmf.Model cmfModel = new org.mitre.niem.cmf.Model();
    for (Subproperty subproperty : subproperties) {
      subproperty.addToCmfModel(cmfModel);
    }
    return CmfUtils.generateString(cmfModel, mediaType);
  }

  /**
   * Gets all subproperties from the database with the property with the given fields.
   */
  @GetMapping("/properties/{propertyQname}/subproperties")
  public Set<Subproperty> getPropertySubproperties(
      @PathVariable String stewardKey,
      @PathVariable String modelKey,
      @PathVariable String versionNumber,
      @PathVariable String propertyQname) throws Exception {
    return hub.subproperties.findByProperty(stewardKey, modelKey, versionNumber, propertyQname);
  }

  /**
   * Gets all subproperties from the database in the given namespace fields.
   */
  @GetMapping("/namespaces/{prefix}/subproperties")
  public Set<Subproperty> getNamespaceSubproperties(
      @PathVariable String stewardKey,
      @PathVariable String modelKey, @PathVariable String versionNumber,
      @PathVariable String prefix) throws Exception {
    return hub.subproperties.findByTypePrefix(stewardKey, modelKey, versionNumber, prefix);
  }

  /**
   * Count all subproperties in a version with the given fields.
   */
  @GetMapping("/subproperties/count")
  public long countVersionSubproperties(
      @PathVariable String stewardKey,
      @PathVariable String modelKey,
      @PathVariable String versionNumber) throws Exception {
    return hub.subproperties.countByVersion(stewardKey, modelKey, versionNumber);
  }

  /**
   * Count all subproperties in a namespace with the given fields.
   */
  @GetMapping("/namespaces/{prefix}/subproperties/count")
  public long countNamespaceSubproperties(
      @PathVariable String stewardKey,
      @PathVariable String modelKey,
      @PathVariable String versionNumber,
      @PathVariable String prefix) throws Exception {
    return hub.subproperties.countByNamespace(stewardKey, modelKey, versionNumber, prefix);
  }

  /**
   * Count all subproperty occurrences for a property with the given fields.
   */
  @GetMapping("/properties/{qname}/subproperties/count")
  public long countPropertySubproperties(
      @PathVariable String stewardKey,
      @PathVariable String modelKey,
      @PathVariable String versionNumber,
      @PathVariable String qname) throws Exception {
    return hub.subproperties.countByProperty(stewardKey, modelKey, versionNumber, qname);
  }

  /**
   * Count all subproperties of a type with the given fields.
   */
  @GetMapping("/types/{qname}/subproperties/count")
  public long countTypeSubproperties(
      @PathVariable String stewardKey,
      @PathVariable String modelKey,
      @PathVariable String versionNumber,
      @PathVariable String qname) throws Exception {
    return hub.subproperties.countByType(stewardKey, modelKey, versionNumber, qname);
  }


  /**
   * Get the previous version of the subproperty with the given fields.
   *
   * @param includePreRelease - True to return a result from a pre-release if applicable;
   *     false to iterate until an official version is reached.
   */
  @GetMapping("/types/{typeQname}/subproperties/{propertyQname}/prev")
  @ResponseStatus(code = HttpStatus.OK)
  public Subproperty getPrev(
      @PathVariable String stewardKey,
      @PathVariable String modelKey,
      @PathVariable String versionNumber,
      @PathVariable String typeQname,
      @PathVariable String propertyQname,
      @RequestParam(required = false, defaultValue = "false") boolean includePreRelease)
      throws Exception {
    Subproperty subproperty = hub.subproperties.findOne(stewardKey, modelKey,
        versionNumber, typeQname, propertyQname);
    return hub.subproperties.getPrev(subproperty, includePreRelease);
  }

  /**
   * Get the next version of the subproperty with the given fields.
   *
   * @param includePreRelease - True to return a result from a pre-release if applicable;
   *     false to iterate until an official version is reached.
   */
  @GetMapping("/types/{typeQname}/subproperties/{propertyQname}/next")
  @ResponseStatus(code = HttpStatus.OK)
  public Subproperty getNext(
      @PathVariable String stewardKey,
      @PathVariable String modelKey,
      @PathVariable String versionNumber,
      @PathVariable String typeQname,
      @PathVariable String propertyQname,
      @RequestParam(required = false, defaultValue = "false") boolean includePreRelease)
      throws Exception {
    Subproperty subproperty = hub.subproperties.findOne(stewardKey, modelKey,
        versionNumber, typeQname, propertyQname);
    return hub.subproperties.getNext(subproperty, includePreRelease);
  }

  /**
   * Get the next current of the subproperty with the given fields.
   */
  @GetMapping("/types/{typeQname}/subproperties/{propertyQname}/current")
  @ResponseStatus(code = HttpStatus.OK)
  public Subproperty getCurrent(
      @PathVariable String stewardKey,
      @PathVariable String modelKey,
      @PathVariable String versionNumber,
      @PathVariable String typeQname,
      @PathVariable String propertyQname) throws Exception {
    Subproperty subproperty = hub.subproperties.findOne(stewardKey, modelKey,
        versionNumber, typeQname, propertyQname);
    return subproperty.getCurrent();
  }

  /**
   * Get the history of the subproperty with the given fields.
   *
   * @param includePreRelease - True to return a result from a pre-release if applicable;
   *     false to iterate until an official version is reached.
   */
  @GetMapping("/types/{typeQname}/subproperties/{propertyQname}/history")
  @ResponseStatus(code = HttpStatus.OK)
  public List<Subproperty> getHistory(
      @PathVariable String stewardKey,
      @PathVariable String modelKey,
      @PathVariable String versionNumber,
      @PathVariable String typeQname,
      @PathVariable String propertyQname,
      @RequestParam(required = false, defaultValue = "false") boolean includePreRelease)
      throws Exception {
    Subproperty subproperty = hub.subproperties.findOne(stewardKey, modelKey,
        versionNumber, typeQname, propertyQname);
    return hub.subproperties.getHistory(subproperty, includePreRelease);
  }

  // @PostMapping("/subproperties")
  // public ResponseEntity<String> postSubproperty(@PathVariable String stewardKey,
  //     @PathVariable String modelKey, @PathVariable String versionNumber,
  //     Subproperty subproperty) throws Exception {
  //   String message = hub.subproperties.add(stewardKey, modelKey, versionNumber,
  //       subproperty.getTypeQname(), subproperty.getPropertyQname(), subproperty.getMin(),
  //       subproperty.getMax());
  //   return AppUtils.getResponseOkString(message);
  // }

}
