package gov.niem.tools.api.validation;

import java.util.Arrays;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import gov.niem.tools.api.db.property.Property;
import gov.niem.tools.api.db.type.Type;
import gov.niem.tools.api.validation.Test.Severity;
import gov.niem.tools.api.validation.niem.NiemValidationService;
import gov.niem.tools.api.validation.xml.XmlValidationService;
import io.swagger.v3.oas.annotations.Hidden;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.extern.log4j.Log4j2;

@Log4j2
@RestController
@RequestMapping("/validation")
@Tag(name = "Validation", description = "ALPHA 4 PREVIEW: Run XML, JSON, and NDR validation on instances, schemas, message specifications, ")
public class ValidationController {

  @Autowired
  ValidationService validationService;

  @Autowired
  XmlValidationService xmlValidationService;

  @Autowired
  NiemValidationService niemValidationService;

  /**
   * Validate one or more XML Schemas.
   *
   * @param file XML Schema or zip file
   * @throws Exception
   */
  @PostMapping(value = "schemas/xml", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
  @ResponseStatus(code = HttpStatus.OK)
  @ApiResponse(responseCode = "415", description = "Unsupported Media Type", content = @Content)
  @ApiResponse(responseCode = "500", description = "Error", content = @Content)
  public Object getXsdValidation(
    @RequestParam MultipartFile file,
    @RequestParam(defaultValue = "json") String mediaType)
    throws Exception {

    Results results = new Results();
    Test test = xmlValidationService.validateXsd(file);
    results.tests.add(test);
    return this.handleResults(results, mediaType, file);
  }

  /**
   * Validate one or more XML instances against XML schemas.
   *
   * @param xml An XML instance file or set of XML instance files in a zip file.
   * @param xsd An XML schema or set of XML schemas in a zip file.
   * @throws Exception
   */
  @PostMapping(value = "instances/xml", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
  @ResponseStatus(code = HttpStatus.OK)
  @ApiResponse(responseCode = "415", description = "Unsupported Media Type", content = @Content)
  @ApiResponse(responseCode = "500", description = "Error", content = @Content)
  public Object getXMLValidation(
    @RequestParam MultipartFile xml,
    @RequestParam MultipartFile xsd,
    @RequestParam(defaultValue = "json") String mediaType
  ) throws Exception {

    Results results = new Results();
    Test[] tests = this.xmlValidationService.validateXml(xml, xsd);
    results.tests.addAll(Arrays.asList(tests));
    return this.handleResults(results, mediaType, xml);
  }

  /**
   * Validate a XML catalog against the OASIS catalog schema.
   *
   * @param file An XML catalog file.
   */
  @PostMapping(value = "xml-catalog", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
  @ResponseStatus(code = HttpStatus.OK)
  @ApiResponse(responseCode = "415", description = "Unsupported Media Type", content = @Content)
  @ApiResponse(responseCode = "500", description = "Error", content = @Content)
  public Object getXmlCatalogXmlValidation(@RequestParam MultipartFile file,
    @RequestParam(defaultValue = "json") String mediaType
  ) throws Exception {
    Results results = new Results();
    Test test = this.xmlValidationService.validateXmlCatalog(file);
    results.tests.add(test);
    return this.handleResults(results, mediaType, file);
  }

  /**
   * Validate an IEPD / message catalog instance file against the NIEM IEPD /
   * message catalog schema.
   *
   * @param file An IEPD / message catalog instance file
   * @throws Exception
   */
  @PostMapping(value = "message-catalog", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
  @ResponseStatus(code = HttpStatus.OK)
  @ApiResponse(responseCode = "415", description = "Unsupported Media Type", content = @Content)
  @ApiResponse(responseCode = "500", description = "Error", content = @Content)
  public Object getMessageCatalogXMLValidation(
    @RequestParam MultipartFile file,
    @RequestParam(defaultValue = "json") String mediaType
  ) throws Exception {

    Results results = new Results();
    Test test = niemValidationService.validateMessageCatalog(file);
    results.tests.add(test);
    return this.handleResults(results, mediaType, file);
  }

  /**
   * Validate a CMF XML file against the NIEM CMF XML schema.
   * @param file A CMF XML file.
   */
  @PostMapping(value = "cmf/xml", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
  @ResponseStatus(code = HttpStatus.OK)
  @ApiResponse(responseCode = "415", description = "Unsupported Media Type", content = @Content)
  @ApiResponse(responseCode = "500", description = "Error", content = @Content)
  public Object getCmfXMLValidation(
    @RequestParam MultipartFile file,
    @RequestParam(defaultValue = "json") String mediaType
  ) throws Exception {

    Results results = new Results();
    Test test = niemValidationService.validateCmf(file);
    results.tests.add(test);
    return this.handleResults(results, mediaType, file);
  }

  /**
   * Validate a message specification or IEPD zip file against NIEM message
   * specification / IEPD conformance rules.
   *
   * @param file A message specification or IEPD zip file.
   */
  @PostMapping(value = "message-specification", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
  @ResponseStatus(code = HttpStatus.OK)
  @ApiResponse(responseCode = "415", description = "Unsupported Media Type", content = @Content)
  @ApiResponse(responseCode = "500", description = "Error", content = @Content)
  public Object getMessageSpecificationValidation(
    @RequestParam MultipartFile file,
    @RequestParam(defaultValue = "json") String mediaType
  ) throws Exception {

    Results results = new Results();
    List<Test> tests = niemValidationService.validateMessageSpecification(file);
    results.tests.addAll(tests);
    return this.handleResults(results, mediaType, file);
  }

  /**
   * Validate one or more XML Schemas against the NIEM Naming and Design Rules (NDR).
   *
   * @param file XML Schema or zip file
   * @throws Exception
   */
  @PostMapping(value = "schemas/ndr", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
  @ResponseStatus(code = HttpStatus.OK)
  @ApiResponse(responseCode = "415", description = "Unsupported Media Type", content = @Content)
  @ApiResponse(responseCode = "500", description = "Error", content = @Content)
  public Object getSchemaNDRValidation(
    @RequestParam MultipartFile file,
    @RequestParam(defaultValue = "json") String mediaType
  ) throws Exception {

    Results results = new Results();
    List<Test> tests = niemValidationService.validateXsdWithNdr(file);
    results.tests.addAll(tests);
    return this.handleResults(results, mediaType, file);
  }

  /**
   * Validate a JSON Schema.
   *
   * @param file A JSON Schema file
   */
  @PostMapping(value = "schemas/json", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
  @ResponseStatus(code = HttpStatus.OK)
  @ApiResponse(responseCode = "415", description = "Unsupported Media Type", content = @Content)
  @ApiResponse(responseCode = "500", description = "Error", content = @Content)
  public Results getSchemaJsonValidation(@RequestParam MultipartFile file) {
    // Validate JSON Schema
    return null;
  }

  @Hidden
  @PostMapping(value = "schemas/qa", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
  @Operation(summary = "Validate a XML schema against NIEM qa rules and best practices")
  public Results getSchemaQaValidation(@RequestParam MultipartFile file) {
    return null;
  }

  /**
   * Validate one or more JSON instances against a JSON Schema.
   *
   * @param json       A JSON instance file or set of JSON instance files in a zip
   *                   file.
   * @param jsonSchema A JSON Schema file.
   */
  @PostMapping(value = "instances/json", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
  @ResponseStatus(code = HttpStatus.OK)
  @ApiResponse(responseCode = "415", description = "Unsupported Media Type", content = @Content)
  @ApiResponse(responseCode = "500", description = "Error", content = @Content)
  public Results getInstanceJSONValidation(@RequestParam MultipartFile json, @RequestParam MultipartFile jsonSchema) {
    // Validate JSON Instance

    Results results = new Results();

    Test test1 = Test.builder()
    .description("Missing property \"j:Crash\".")
    .severity(Severity.warning)
    .build();

    TestResult issue1 = TestResult.builder()
    .location("CrashDriver.json")
    .line("11")
    .build();

    test1.results.add(issue1);
    results.tests.add(test1);


    Test test2 = Test.builder()
    .description("Incorrect type. Expected \"object\".")
    .severity(Severity.warning)
    .build();

    TestResult issue2 = TestResult.builder()
    .location("CrashDriver.json")
    .line("15")
    .build();

    test2.results.add(issue2);
    results.tests.add(test2);


    return results;
  }

  @Hidden
  @PostMapping(value = "cmf/ndr", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
  @Operation(summary = "Validate a CMF XML file against NIEM CMF NDR rules")
  public Results getCmfNDRValidation(@RequestParam MultipartFile file) {
    return null;
  }

  @Hidden
  @PostMapping(value = "cmf/qa", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
  @Operation(summary = "Validate a CMF XML file against NIEM QA rules and best practices")
  public Results getCmfQaValidation(@RequestParam MultipartFile file) {
    return null;
  }

  /**
   * Check a property for basic NDR conformance issues.
   *
   * @param property          A NIEM property
   * @param niemVersionNumber Applicable base NIEM version number. Defaults to
   *                          the current version if not provided.
   */
  @PostMapping(value = "properties/qa", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
  @ResponseStatus(code = HttpStatus.OK)
  @ApiResponse(responseCode = "500", description = "Error", content = @Content)
  public Results getPropertyQaValidation(@RequestParam Property property, @PathVariable(required = false) String niemVersionNumber) {
    // Validate property
    return null;
  }

  /**
   * Check a property for basic NDR conformance issues.
   *
   * @param type              A NIEM type
   * @param niemVersionNumber Applicable base NIEM version number. Defaults to
   *                          the current version if not provided.
   */
  @PostMapping(value = "types/qa", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
  @ResponseStatus(code = HttpStatus.OK)
  @ApiResponse(responseCode = "500", description = "Error", content = @Content)
  public Results getTypeQaValidation(@RequestParam Type type, @PathVariable(required = false) String niemVersionNumber) {
    // Validate type
    return null;
  }

  private Object handleResults(Results results, String mediaType, MultipartFile file) throws Exception {

    results.setDefaultComment();

    if (mediaType.equals("csv")) {
      return validationService.returnResultsAsCsv(results, file);
    }

    return results;

  }

}
