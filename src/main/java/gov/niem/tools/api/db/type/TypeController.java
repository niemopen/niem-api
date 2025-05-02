package gov.niem.tools.api.db.type;

import gov.niem.tools.api.core.config.Config.AppMediaType;
import gov.niem.tools.api.core.utils.CmfUtils;
import gov.niem.tools.api.db.ServiceHub;
import gov.niem.tools.api.db.exceptions.EntityNotFoundException;

import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
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
 * REST controller for types.
 */
@RestController
@RequestMapping("stewards/{stewardKey}/models/{modelKey}/versions/{versionNumber}")
@Tag(
    name = "Data-6: Types",
    description = "A type defines a structure - an allowable set of values. A type might describe a simple value (e.g., a string, a number) or a complex object (e.g., PersonType).")
public class TypeController {

  @Autowired
  ServiceHub hub;

  /**
   * Gets the type with the given fields.
   */
  @GetMapping("/types/{qname}")
  @ResponseStatus(code = HttpStatus.OK)
  @ApiResponse(responseCode = "422", description = "Unprocessable Entity", content = @Content)
  public Type getType(
      @PathVariable String stewardKey,
      @PathVariable String modelKey,
      @PathVariable String versionNumber,
      @PathVariable String qname) throws Exception {
    return hub.types.findOne(stewardKey, modelKey, versionNumber, qname);
  }

  /**
   * Gets the type with the given fields in CMF.
   */
  @GetMapping("/types.cmf/{qname}")
  @ResponseStatus(code = HttpStatus.OK)
  @ApiResponse(responseCode = "422", description = "Unprocessable Entity", content = @Content)
  public Object getTypeCmf(
      @PathVariable String stewardKey,
      @PathVariable String modelKey,
      @PathVariable String versionNumber,
      @PathVariable String qname,
      @RequestParam(required = false, defaultValue = "json") AppMediaType mediaType)
      throws Exception {
    Type type = hub.types.findOne(stewardKey, modelKey, versionNumber, qname);
    org.mitre.niem.cmf.Model cmfModel = new org.mitre.niem.cmf.Model();
    cmfModel.addComponent(type.toCmf());
    return CmfUtils.generateString(cmfModel, mediaType);
  }

  /**
   * Gets all types in the version matching the given fields.
   */
  @GetMapping("/types")
  @ResponseStatus(code = HttpStatus.OK)
  @ApiResponse(responseCode = "422", description = "Unprocessable Entity", content = @Content)
  public Page<Type> getVersionTypes(
      @PathVariable String stewardKey,
      @PathVariable String modelKey,
      @PathVariable String versionNumber,
      @PageableDefault(sort = {"namespace.prefix", "name"}) Pageable pageable)
      throws Exception {
    return hub.types.findByVersion(stewardKey, modelKey, versionNumber, pageable);
  }

  /**
   * Gets all types from a namespace.
   */
  @GetMapping("/namespaces/{prefix}/types")
  @ResponseStatus(code = HttpStatus.OK)
  @ApiResponse(responseCode = "422", description = "Unprocessable Entity", content = @Content)
  public Page<Type> getNamespaceTypes(
      @PathVariable String stewardKey,
      @PathVariable String modelKey,
      @PathVariable String versionNumber,
      @PathVariable String prefix,
      @PageableDefault(sort = {"namespace.prefix", "name"}) Pageable pageable)
      throws Exception {
    return hub.types.findByNamespace(stewardKey, modelKey, versionNumber, prefix, pageable);
  }

  /**
   * Gets all types in CMF from a namespace.
   */
  @GetMapping("/namespaces.cmf/{prefix}/types")
  @ResponseStatus(code = HttpStatus.OK)
  @ApiResponse(responseCode = "422", description = "Unprocessable Entity", content = @Content)
  public Object getNamespaceTypesCmf(
      @PathVariable String stewardKey,
      @PathVariable String modelKey,
      @PathVariable String versionNumber,
      @PathVariable String prefix,
      @PageableDefault(sort = {"namespace.categorySortOrder", "prefix", "name"}) Pageable pageable,
      @RequestParam(required = false, defaultValue = "json") AppMediaType mediaType)
      throws Exception {
    Page<Type> types = hub.types.findByNamespace(stewardKey, modelKey, versionNumber,
        prefix, pageable);
    org.mitre.niem.cmf.Model cmfModel = new org.mitre.niem.cmf.Model();
    for (Type type : types) {
      type.addToCmfModel(cmfModel);
    }
    return CmfUtils.generateString(cmfModel, mediaType);
  }

  /**
   * Count all types in the version with the given fields.
   */
  @GetMapping("/types/count")
  @ResponseStatus(code = HttpStatus.OK)
  public long countVersionTypes(
      @PathVariable String stewardKey,
      @PathVariable String modelKey,
      @PathVariable String versionNumber,
      @RequestParam(required = false) Type.Category category) throws EntityNotFoundException {
    return hub.types.countByVersion(stewardKey, modelKey, versionNumber, category);
  }

  /**
   * Count all types in the namespace with the given fields.
   */
  @GetMapping("/namespaces/{prefix}/types/count")
  @ResponseStatus(code = HttpStatus.OK)
  public long countNamespaceTypes(
      @PathVariable String stewardKey,
      @PathVariable String modelKey,
      @PathVariable String versionNumber,
      @PathVariable String prefix,
      @RequestParam(required = false) Type.Category category) throws EntityNotFoundException {
    return hub.types.countByNamespace(stewardKey, modelKey, versionNumber, prefix, category);
  }

  // /**
  //  * Get all CMF datatypes matching the given fields.
  //  */
  // @GetMapping("/datatypes")
  // @ApiResponses(value = {
  //     @ApiResponse(responseCode = "200", description = "Success", content = {
  //       @Content(
  //         mediaType = "application/json",
  //         array = @ArraySchema(schema =
  //             @Schema(implementation = org.mitre.niem.cmf.Datatype.class)))
  //     }),
  //     @ApiResponse(responseCode = "404", description = "Not Found", content = {
  //         @Content(mediaType = "application/json", schema = @Schema(type = "object"))
  //     })
  // })
  // public List<Datatype> getAllDataTypes(@PathVariable String stewardKey,
  //     @PathVariable String modelKey, @PathVariable String versionKey) throws Exception {
  //   // return hub.types.findByRelease(stewardKey, modelKey, versionNumber);
  //   return new ArrayList<Datatype>();
  // }

  // /**
  //  * Get all CMF classes matching the given fields
  //  */
  // @GetMapping("/classes")
  // @ApiResponses(value = {
  //   @ApiResponse(responseCode = "200", description = "Success", content = {
  //     @Content(
  //       mediaType = "application/json",
  //       array = @ArraySchema(schema =
  //           @Schema(implementation = org.mitre.niem.cmf.ClassType.class)))
  //   }),
  //   @ApiResponse(responseCode = "404", description = "Not Found", content = {
  //       @Content(mediaType = "application/json", schema = @Schema(type = "object"))
  //   })
  // })
  // public List<ClassType> getAllClassTypes(@PathVariable String stewardKey,
  //     @PathVariable String modelKey, @PathVariable String versionKey) throws Exception {
  //   // return hub.types.findByRelease(stewardKey, modelKey, versionNumber);
  //   return new ArrayList<ClassType>();
  // }

  // /**
  //  * Get a CMF datatype matching the given fields.
  //  */
  // @GetMapping("/datatypes/{qname}")
  // @ApiResponses(value = {
  //     @ApiResponse(responseCode = "200", description = "Success", content = {
  //       @Content(
  //         mediaType = "application/json",
  //         schema = @Schema(implementation = org.mitre.niem.cmf.Datatype.class))
  //     }),
  //     @ApiResponse(responseCode = "404", description = "Not Found", content = {
  //         @Content(mediaType = "application/json", schema = @Schema(type = "object"))
  //     })
  // })
  // public Datatype getDataType(@PathVariable String stewardKey, @PathVariable String modelKey,
  //     @PathVariable String versionNumber, @PathVariable String qname) throws Exception {
  //   // return hub.types.findOneByQname(stewardKey, modelKey, versionNumber, qname);
  //   return new Datatype();
  // }

  // /**
  //  * Get the CMF class matching the given fields
  //  */
  // @GetMapping("/classes/{qname}")
  // @ApiResponses(value = {
  //     @ApiResponse(responseCode = "200", description = "Success", content = {
  //       @Content(
  //         mediaType = "application/json",
  //         schema = @Schema(implementation = org.mitre.niem.cmf.ClassType.class))
  //     }),
  //     @ApiResponse(responseCode = "404", description = "Not Found", content = {
  //         @Content(mediaType = "application/json", schema = @Schema(type = "object"))
  //     })
  // })
  // public ClassType getClassType(@PathVariable String stewardKey, @PathVariable String modelKey,
  //     @PathVariable String versionNumber, @PathVariable String qname) throws Exception {
  // // return hub.types.findOneByQname(stewardKey, modelKey, versionNumber, qname);
  //   return new ClassType();
  // }

  // @PostMapping("/types")
  // public ResponseEntity<String> postType(@PathVariable String stewardKey,
  //     @PathVariable String modelKey, @PathVariable String versionNumber, Type type)
  //     throws Exception {
  //   String message = hub.types.add(stewardKey, modelKey, versionNumber, type);
  //   return AppUtils.getResponseOkString(message);
  // }

}
