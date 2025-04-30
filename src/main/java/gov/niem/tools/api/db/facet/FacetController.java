package gov.niem.tools.api.db.facet;

import gov.niem.tools.api.core.config.Config.AppMediaType;
import gov.niem.tools.api.core.utils.CmfUtils;
import gov.niem.tools.api.db.ServiceHub;
import gov.niem.tools.api.db.exceptions.EntityNotFoundException;
import gov.niem.tools.api.db.facet.Facet.Category;

import io.swagger.v3.oas.annotations.tags.Tag;
import java.util.Set;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

/**
 * REST controller for facet information.
 */
@RestController
@RequestMapping("stewards/{stewardKey}/models/{modelKey}/versions/{versionNumber}")
@Tag(
      name = "Data-8: Facets",
      description = "A facet is a code, pattern, length, minimum or maximum value, or other such kind of constraint on a data value, such as a string or number.")
public class FacetController {

  @Autowired
  ServiceHub hub;

  /**
   * Gets a facet with the given criteria.
   */
  @GetMapping("/types/{qname}/facets/{category}={value}")
  public Facet getFacet(
      @PathVariable String stewardKey,
      @PathVariable String modelKey,
      @PathVariable String versionNumber,
      @PathVariable String qname,
      @PathVariable Category category,
      @PathVariable String value) {
    return hub.facets.findOne(stewardKey, modelKey, versionNumber, qname, category, value);
  }

  /**
   * Gets a facet in CMF with the given criteria.
   */
  @GetMapping("/types.cmf/{qname}/facets/{category}={value}")
  public Object getFacetCmf(
      @PathVariable String stewardKey,
      @PathVariable String modelKey,
      @PathVariable String versionNumber,
      @PathVariable String qname,
      @PathVariable Category category,
      @PathVariable String value,
      @RequestParam(required = false, defaultValue = "json") AppMediaType mediaType)
      throws Exception {
    Facet facet = hub.facets.findOne(stewardKey, modelKey, versionNumber, qname, category, value);
    org.mitre.niem.cmf.Model cmfModel = new org.mitre.niem.cmf.Model();
    facet.addToCmfModel(cmfModel);
    return CmfUtils.generateString(cmfModel, mediaType);
  }

  /**
   * Gets all facets from the type with the given criteria.
   */
  @GetMapping("/types/{qname}/facets")
  public Set<Facet> getFacets(
      @PathVariable String stewardKey,
      @PathVariable String modelKey,
      @PathVariable String versionNumber,
      @PathVariable String qname) {
    return hub.facets.find(stewardKey, modelKey, versionNumber, qname);
  }

  /**
   * Gets all facets in CMF from the type with the given criteria.
   */
  @GetMapping("/types.cmf/{qname}/facets")
  public Object getFacetsCmf(
      @PathVariable String stewardKey,
      @PathVariable String modelKey,
      @PathVariable String versionNumber,
      @PathVariable String qname,
      @RequestParam(required = false, defaultValue = "json") AppMediaType mediaType)
      throws Exception {
    Set<Facet> facets = hub.facets.find(stewardKey, modelKey, versionNumber, qname);
    org.mitre.niem.cmf.Model cmfModel = new org.mitre.niem.cmf.Model();
    for (Facet facet : facets) {
      facet.addToCmfModel(cmfModel);
    }
    return CmfUtils.generateString(cmfModel, mediaType);
  }

  /**
   * Count all facets in the version with the given fields.
   */
  @GetMapping("/facets/count")
  public long countVersionFacets(
      @PathVariable String stewardKey,
      @PathVariable String modelKey,
      @PathVariable String versionNumber) throws EntityNotFoundException {
    return hub.facets.countByVersion(stewardKey, modelKey, versionNumber);
  }

  /**
   * Count all facets in the namespace with the given fields.
   */
  @GetMapping("/namespaces/{prefix}/facets/count")
  public long countNamespaceFacets(
      @PathVariable String stewardKey,
      @PathVariable String modelKey,
      @PathVariable String versionNumber,
      @PathVariable String prefix) throws EntityNotFoundException {
    return hub.facets.countByNamespace(stewardKey, modelKey, versionNumber, prefix);
  }

  /**
   * Count all facets in the type with the given fields.
   */
  @GetMapping("/types/{qname}/facets/count")
  public long countTypeFacets(
      @PathVariable String stewardKey,
      @PathVariable String modelKey,
      @PathVariable String versionNumber,
      @PathVariable String qname) throws EntityNotFoundException {
    return hub.facets.countByType(stewardKey, modelKey, versionNumber, qname);
  }

  /**
   * Count all facets in the type of the property with the given fields.
   */
  @GetMapping("/properties/{qname}/facets/count")
  public long countPropertyFacets(
      @PathVariable String stewardKey,
      @PathVariable String modelKey,
      @PathVariable String versionNumber,
      @PathVariable String qname) throws EntityNotFoundException {
    return hub.facets.countByProperty(stewardKey, modelKey, versionNumber, qname);
  }

}
