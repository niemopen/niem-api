package gov.niem.tools.api.db.facet;

import gov.niem.tools.api.core.config.Config.AppMediaType;
import gov.niem.tools.api.core.utils.CmfUtils;
import gov.niem.tools.api.db.ServiceHub;
import gov.niem.tools.api.db.exceptions.EntityNotFoundException;
import gov.niem.tools.api.db.facet.Facet.Category;

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
  @GetMapping("/types/{qname}/facets/{category}/{value}")
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
   * Gets all facets from the version with the given criteria.
   */
  @GetMapping("/facets")
  public Page<Facet> getVersionFacets(
      @PathVariable String stewardKey,
      @PathVariable String modelKey,
      @PathVariable String versionNumber,
      @PageableDefault(sort = {"typeName", "typePrefix", "category", "value"}, size = 100)
          Pageable pageable) {
    return hub.facets.findByVersion(stewardKey, modelKey, versionNumber, pageable);
  }

  /**
   * Gets all facets from the namespace with the given criteria.
   */
  @GetMapping("/namespaces/{prefix}/facets")
  public Page<Facet> getNamespaceFacets(
      @PathVariable String stewardKey,
      @PathVariable String modelKey,
      @PathVariable String versionNumber,
      @PathVariable String prefix,
      @PageableDefault(sort = {"typeName", "category", "value"}, size = 100) Pageable pageable) {
    return hub.facets.findByNamespace(stewardKey, modelKey, versionNumber, prefix, pageable);
  }

  /**
   * Gets all facets from the type with the given criteria.
   */
  @GetMapping("/types/{qname}/facets")
  public Page<Facet> getTypeFacets(
      @PathVariable String stewardKey,
      @PathVariable String modelKey,
      @PathVariable String versionNumber,
      @PathVariable String qname,
      @PageableDefault(sort = {"category", "value"}, size = 100) Pageable pageable) {
    return hub.facets.findByType(stewardKey, modelKey, versionNumber, qname, pageable);
  }

  /**
   * Gets all facets from the type of the property with the given criteria.
   */
  @GetMapping("/properties/{qname}/facets")
  public Page<Facet> getPropertyFacets(
      @PathVariable String stewardKey,
      @PathVariable String modelKey,
      @PathVariable String versionNumber,
      @PathVariable String qname,
      @PageableDefault(sort = {"category", "value"}, size = 100) Pageable pageable) {
    return hub.facets.findByProperty(stewardKey, modelKey, versionNumber, qname, pageable);
  }

  /**
   * Gets all facets in CMF from the type with the given criteria.
   */
  @GetMapping("/types/{qname}/facets/formats/cmf")
  public Object getTypeFacetsCmf(
      @PathVariable String stewardKey,
      @PathVariable String modelKey,
      @PathVariable String versionNumber,
      @PathVariable String qname,
      @PageableDefault(sort = {"category", "value"}) Pageable pageable,
      @RequestParam(required = false, defaultValue = "json") AppMediaType mediaType)
      throws Exception {
    Page<Facet> facets = hub.facets.findByType(stewardKey, modelKey, versionNumber,
        qname, pageable);
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

  /**
   * Get the previous version of the facet with the given fields.
   *
   * @param includePreRelease - True to return a result from a pre-release if applicable;
   *     false to iterate until an official version is reached.
   */
  @GetMapping("/types/{qname}/facets/{category}/{value}/prev")
  @ResponseStatus(code = HttpStatus.OK)
  public Facet getPrev(
      @PathVariable String stewardKey,
      @PathVariable String modelKey,
      @PathVariable String versionNumber,
      @PathVariable String qname,
      @PathVariable Category category,
      @PathVariable String value,
      @RequestParam(required = false, defaultValue = "false") boolean includePreRelease)
      throws Exception {
    Facet facet = hub.facets.findOne(stewardKey, modelKey, versionNumber,
        qname, category, value);
    return hub.facets.getPrev(facet, includePreRelease);
  }

  /**
   * Get the next version of the facet with the given fields.
   *
   * @param includePreRelease - True to return a result from a pre-release if applicable;
   *     false to iterate until an official version is reached.
   */
  @GetMapping("/types/{qname}/facets/{category}/{value}/next")
  @ResponseStatus(code = HttpStatus.OK)
  public Facet getNext(
      @PathVariable String stewardKey,
      @PathVariable String modelKey,
      @PathVariable String versionNumber,
      @PathVariable String qname,
      @PathVariable Category category,
      @PathVariable String value,
      @RequestParam(required = false, defaultValue = "false") boolean includePreRelease)
      throws Exception {
    Facet facet = hub.facets.findOne(stewardKey, modelKey, versionNumber,
        qname, category, value);
    return hub.facets.getNext(facet, includePreRelease);
  }

  /**
   * Get the current version of the facet with the given fields.
   */
  @GetMapping("/types/{qname}/facets/{category}/{value}/current")
  @ResponseStatus(code = HttpStatus.OK)
  public Facet getCurrent(
      @PathVariable String stewardKey,
      @PathVariable String modelKey,
      @PathVariable String versionNumber,
      @PathVariable String qname,
      @PathVariable Category category,
      @PathVariable String value) throws Exception {
    Facet facet = hub.facets.findOne(stewardKey, modelKey, versionNumber,
        qname, category, value);
    return facet.getCurrent();
  }

  /**
   * Get the history of the facet with the given fields.
   *
   * @param includePreRelease - True to return a result from a pre-release if applicable;
   *     false to iterate until an official version is reached.
   */
  @GetMapping("/types/{qname}/facets/{category}/{value}/history")
  @ResponseStatus(code = HttpStatus.OK)
  public List<Facet> getHistory(
      @PathVariable String stewardKey,
      @PathVariable String modelKey,
      @PathVariable String versionNumber,
      @PathVariable String qname,
      @PathVariable Category category,
      @PathVariable String value,
      @RequestParam(required = false, defaultValue = "false") boolean includePreRelease)
      throws Exception {
    Facet facet = hub.facets.findOne(stewardKey, modelKey, versionNumber,
        qname, category, value);
    return hub.facets.getHistory(facet, includePreRelease);
  }

}
