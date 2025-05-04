package gov.niem.tools.api.search;

import gov.niem.tools.api.core.config.Config.AppMediaType;
import gov.niem.tools.api.core.exceptions.NoContentException;
import gov.niem.tools.api.core.utils.CmfUtils;
import gov.niem.tools.api.db.namespace.Namespace;
import gov.niem.tools.api.db.property.Property;
import gov.niem.tools.api.db.type.Type;

import io.swagger.v3.oas.annotations.Hidden;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.log4j.Log4j2;
import org.hibernate.search.engine.search.query.SearchResult;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

/**
 * REST controller for database searches for NIEM and community model content.
 */
@RestController
@Tag(name = "Search", description = "Search for properties and types")
@Log4j2
public class SearchController {

  @Autowired
  SearchService searchService;

  @GetMapping("/search/index")
  @Hidden
  public void index() {
    searchService.runIndexer();
  }

  private static final int LIMIT = 100;

  /**
   * Search properties based on the given criteria.
   *
   * @param niemVersionNumber A base NIEM version number (e.g., "5.2") to search for
   *     NIEM and community content. Defaults to the current NIEM version if not provided.
   *
   * @param token Search for full tokens in component names and definitions with stemming.
   *     Example: "arm" returns property names with "Arm", "Armed", and "Arming" but does
   *     not return "Alarm", "Firearm", "Harm", etc.
   *
   * @param substring Search for partial text in component names and definitions.
   *     Example: "arm" returns property names with "Arm", "Armed", "Arming", "Alarm",
   *     "Firearm", "Harm", etc.
   *
   * @param prefix Filter results on the given prefix(es)
   *
   * @param type Filter results by substring matching on one of the given types.
   *     Example: ["text", "boolean"] matches properties with types that include
   *     nc:TextType and niem-xs:boolean.
   *
   * @param isAbstract True if only abstract properties should be returned; false if only
   *     non-abstract (concrete) properties should be returned.
   *     Omit parameter to return either kind.
   *
   * @param isElement True if only element properties should be returned; false if
   *     only attribute properties should be returned.
   *     Omit parameter to return either kind.
   *
   * @param page Page number of results to return.  Defaults to 0 (first page).
   */
  @GetMapping("/search/properties")
  @ResponseStatus(code = HttpStatus.OK)
  @ApiResponse(responseCode = "204", description = "No Content", content = @Content)
  @ApiResponse(responseCode = "400", description = "Bad Request", content = @Content)
  public Page<Property> getPropertySearch(
      HttpServletRequest request,
      HttpServletResponse response,
      @RequestParam(required = false) String niemVersionNumber,
      @RequestParam(required = false) String[] token,
      @RequestParam(required = false) String[] substring,
      @RequestParam(required = false) String[] prefix,
      @RequestParam(required = false) String[] type,
      // @RequestParam(required = false) String[] group,
      // @RequestParam(required = false) String[] steward,
      // @RequestParam(required = false) String[] model,
      @RequestParam(required = false) Boolean isAbstract,
      @RequestParam(required = false) Boolean isElement,
      // @RequestParam(required = false) Namespace.Category[] namespaceCategory,
      @RequestParam(required = false, defaultValue = "0") int page) {

    String[] group = null;
    String[] steward = null;
    String[] model = null;
    Namespace.Category[] namespaceCategory = null;

    SearchResult<Property> result = searchService.searchProperty(
        niemVersionNumber,
        token,
        substring,
        prefix,
        type,
        group,
        steward,
        model,
        isAbstract,
        isElement,
        namespaceCategory,
        page,
        LIMIT
    );

    if (result.hits().isEmpty()) {
      throw new NoContentException();
    }

    searchService.setResponseHeaders(response, result, page, LIMIT,
        request.getRequestURL() + "?" + request.getQueryString());

    Pageable pageable = PageRequest.of(page, LIMIT);
    long total = result.total().hitCount();
    return new PageImpl<Property>(result.hits(), pageable, total);

  }

  /**
   * Search properties in the database based on the given criteria and return results in CMF.
   *
   * @param niemVersionNumber A base NIEM version number (e.g., "5.2") to search for NIEM
   *     and community content. Defaults to the current NIEM version if not provided.
   *
   * @param token Search for full tokens in component names and definitions with stemming.
   *     Example: "arm" returns property names with "Arm", "Armed", and "Arming" but does not
   *     return "Alarm", "Firearm", "Harm", etc.
   *
   * @param substring Search for partial text in component names and definitions.
   *     Example: "arm" returns property names with "Arm", "Armed", "Arming", "Alarm",
   *     "Firearm", "Harm", etc.
   *
   * @param prefix Filter results on the given prefix(es)
   *
   * @param type Filter results by substring matching on one of the given types.
   *     Example: ["text", "boolean"] matches properties with types that include
   *     nc:TextType and niem-xs:boolean.
   *
   * @param isAbstract True if only abstract properties should be returned; false if only
   *     non-abstract (concrete) properties should be returned.
   *     Omit parameter to return either kind.
   *
   * @param isElement True if only element properties should be returned; false if only
   *     attribute properties should be returned.
   *     Omit parameter to return either kind.
   *
   * @param page Page number of results to return.  Defaults to 0.
   */
  @GetMapping("/search.cmf/properties")
  @ResponseStatus(code = HttpStatus.OK)
  @ApiResponse(responseCode = "204", description = "No Content", content = @Content)
  @ApiResponse(responseCode = "400", description = "Bad Request", content = @Content)
  public Object getPropertySearchCmf(
      HttpServletRequest request,
      HttpServletResponse response,
      @RequestParam(required = false) String niemVersionNumber,
      @RequestParam(required = false) String[] token,
      @RequestParam(required = false) String[] substring,
      @RequestParam(required = false) String[] prefix,
      @RequestParam(required = false) String[] type,
      // @RequestParam(required = false) String[] group,
      // @RequestParam(required = false) String[] steward,
      // @RequestParam(required = false) String[] model,
      @RequestParam(required = false) Boolean isAbstract,
      @RequestParam(required = false) Boolean isElement,
      // @RequestParam(required = false) Namespace.Category[] namespaceCategory,
      @RequestParam(required = false) Integer page,
      @RequestParam(required = false, defaultValue = "json") AppMediaType mediaType
  ) throws Exception {

    String[] group = null;
    String[] steward = null;
    String[] model = null;
    Namespace.Category[] namespaceCategory = null;

    SearchResult<Property> result = searchService.searchProperty(
        niemVersionNumber,
        token,
        substring,
        prefix,
        type,
        group,
        steward,
        model,
        isAbstract,
        isElement,
        namespaceCategory,
        page,
        LIMIT
    );

    if (result.hits().isEmpty()) {
      throw new NoContentException();
    }

    searchService.setResponseHeaders(response, result, page, LIMIT,
        request.getRequestURL() + "?" + request.getQueryString());

    org.mitre.niem.cmf.Model cmfModel = new org.mitre.niem.cmf.Model();
    for (Property property : result.hits()) {
      log.info(property.getIdLabel());
      cmfModel.addComponent(property.toCmf());
    }

    return CmfUtils.generateString(cmfModel, mediaType);

  }

  /**
   * Search types based on given criteria.
   *
   * @param niemVersionNumber A base NIEM version number (e.g., "5.2") to search
   *     for NIEM and community content.
   *     Defaults to the current NIEM version if not provided.
   *
   * @param token Search for full tokens in component names and definitions with stemming.
   *     Example: "arm" returns property names with "Arm", "Armed", and "Arming"
   *     but does not return "Alarm", "Firearm", "Harm", etc.
   *
   * @param substring Search for partial text in component names and definitions.
   *     Example: "arm" returns property names with "Arm", "Armed", "Arming", "Alarm",
   *     "Firearm", "Harm", etc.
   *
   * @param prefix Filter results on the given prefix(es)
   *
   * @param page Page number of results to return.  Defaults to 0.
   */
  @GetMapping("/search/types")
  @ResponseStatus(code = HttpStatus.OK)
  @ApiResponse(responseCode = "204", description = "No Content", content = @Content)
  @ApiResponse(responseCode = "400", description = "Bad Request", content = @Content)
  public Page<Type> getTypeSearch(
      HttpServletRequest request,
      HttpServletResponse response,
      @RequestParam(required = false) String niemVersionNumber,
      @RequestParam(required = false) String[] token,
      @RequestParam(required = false) String[] substring,
      @RequestParam(required = false) String[] prefix,
      @RequestParam(required = false, defaultValue = "0") int page) {

    SearchResult<Type> result = searchService.searchType(
        niemVersionNumber,
        token,
        substring,
        prefix,
        page,
        LIMIT);

    if (result.hits().isEmpty()) {
      throw new NoContentException();
    }

    searchService.setResponseHeaders(response, result, page, LIMIT,
        request.getRequestURL() + "?" + request.getQueryString());


    Pageable pageable = PageRequest.of(page, LIMIT);
    long total = result.total().hitCount();
    return new PageImpl<Type>(result.hits(), pageable, total);

  }

  /**
   * Search types based on given criteria and returns results in CMF.
   *
   * @param niemVersionNumber A base NIEM version number (e.g., "5.2") to search
   *     for NIEM and community content.
   *     Defaults to the current NIEM version if not provided.
   *
   * @param token Search for full tokens in component names and definitions with stemming.
   *     Example: "arm" returns property names with "Arm", "Armed", and "Arming"
   *     but does not return "Alarm", "Firearm", "Harm", etc.
   *
   * @param substring Search for partial text in component names and definitions.
   *     Example: "arm" returns property names with "Arm", "Armed", "Arming", "Alarm",
   *     "Firearm", "Harm", etc.
   *
   * @param prefix Filter results on the given prefix(es)
   *
   * @param page Page number of results to return.  Defaults to 0.
   */
  @GetMapping("/search.cmf/types")
  @ResponseStatus(code = HttpStatus.OK)
  @ApiResponse(responseCode = "204", description = "No Content", content = @Content)
  @ApiResponse(responseCode = "400", description = "Bad Request", content = @Content)
  public Object getTypeSearchCmf(
      HttpServletRequest request,
      HttpServletResponse response,
      @RequestParam(required = false) String niemVersionNumber,
      @RequestParam(required = false) String[] token,
      @RequestParam(required = false) String[] substring,
      @RequestParam(required = false) String[] prefix,
      @RequestParam(required = false) Integer page,
      @RequestParam(required = false, defaultValue = "json") AppMediaType mediaType
  ) throws Exception {

    SearchResult<Type> result = searchService.searchType(
        niemVersionNumber,
        token,
        substring,
        prefix,
        page,
        LIMIT);

    if (result.hits().isEmpty()) {
      throw new NoContentException();
    }

    searchService.setResponseHeaders(response, result, page, LIMIT,
        request.getRequestURL() + "?" + request.getQueryString());

    org.mitre.niem.cmf.Model cmfModel = new org.mitre.niem.cmf.Model();
    for (Type type : result.hits()) {
      log.info(type.getIdLabel());
      cmfModel.addComponent(type.toCmf());
    }

    return CmfUtils.generateString(cmfModel, mediaType);

  }

}
