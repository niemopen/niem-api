package gov.niem.tools.api.validation;

import gov.niem.tools.api.db.property.Property;
import gov.niem.tools.api.db.type.Type;
import gov.niem.tools.api.validation.TestReport.ResultsFormat;
import gov.niem.tools.api.validation.niem.NiemValidationService;
import gov.niem.tools.api.validation.xml.XmlValidationService;

import io.swagger.v3.oas.annotations.Hidden;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RequestPart;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

/**
 * REST controller for validation.
 */
@RestController
@RequestMapping(value = "/validation", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
@Tag(name = "Validation", description = "Validate models and artifacts.")
@ApiResponse(responseCode = "200", description = "Success", content = {
  @Content(mediaType = "application/json"),
  @Content(mediaType = "text/csv"),
})
@ApiResponse(responseCode = "400", description = "Bad Request", content = @Content)
@ApiResponse(responseCode = "415", description = "Unsupported Media Type", content = @Content)
@ApiResponse(responseCode = "500", description = "Error", content = @Content)
public class ValidationController {

  @Autowired
  ValidationService validationService;

  @Autowired
  XmlValidationService xmlValidationService;

  @Autowired
  NiemValidationService niemValidationService;

  // TODO: Resolve request body vs query parameter issue.  See issue #64.

  /**
   * Validate one or more XML Schemas.
   *
   * @param file REQUEST BODY PARAMETER.  Single XML Schema or a zip file containing
   *     multiple XML Schemas.
   * @param mediaType File format for the validation results.
   */
  @PostMapping(value = "schemas/xml")
  public Object getXsdValidation(
      @RequestPart MultipartFile file,
      // TODO: @RequestPart(required = false) @Value("json") ResultsFormat mediaType
      @RequestParam(required = false, defaultValue = "json") ResultsFormat mediaType
  ) throws Exception {

    TestReport results = new TestReport();
    Test test = xmlValidationService.validateXsd(file);
    results.tests.add(test);
    return this.handleResults(results, mediaType, file);
  }

  /**
   * Validate one or more XML instances against XML schemas.
   *
   * @param xml An XML instance file or set of XML instance files in a zip file.
   * @param xsd An XML schema or set of XML schemas in a zip file.
   * @param mediaType REQUEST BODY PARAMETER. File format for the validation results.
   */
  @PostMapping(value = "instances/xml")
  public Object getXmlValidation(
      @RequestPart MultipartFile xml,
      @RequestPart MultipartFile xsd,
      @RequestParam(required = false, defaultValue = "json") ResultsFormat mediaType
  ) throws Exception {

    TestReport results = new TestReport();
    Test[] tests = this.xmlValidationService.validateXml(xml, xsd);
    results.tests.addAll(Arrays.asList(tests));
    return this.handleResults(results, mediaType, xml);
  }

  /**
   * Validate a XML catalog against the OASIS catalog schema.
   *
   * @param file An XML catalog file.
   * @param mediaType REQUEST BODY PARAMETER. File format for the validation results.
   */
  @PostMapping(value = "xml-catalog")
  public Object getXmlCatalogXmlValidation(
      @RequestPart MultipartFile file,
      @RequestParam(required = false, defaultValue = "json") ResultsFormat mediaType
  ) throws Exception {
    TestReport results = new TestReport();
    Test test = this.xmlValidationService.validateXmlCatalog(file);
    results.tests.add(test);
    return this.handleResults(results, mediaType, file);
  }

  /**
   * Validate an IEPD / message catalog instance file against the NIEM IEPD /
   * message catalog schema.
   *
   * @param file An IEPD / message catalog instance file
   * @param mediaType REQUEST BODY PARAMETER. File format for the validation results.
   */
  @PostMapping(value = "message-catalog")
  public Object getMessageCatalogXmlValidation(
      @RequestPart MultipartFile file,
      @RequestParam(required = false, defaultValue = "json") ResultsFormat mediaType
  ) throws Exception {

    TestReport results = new TestReport();
    Test test = niemValidationService.validateMessageCatalog(file);
    results.tests.add(test);
    return this.handleResults(results, mediaType, file);
  }

  /**
   * Validate a CMF XML file against the NIEM CMF XML schema.

   * @param file A CMF XML file.
   * @param mediaType REQUEST BODY PARAMETER. File format for the validation results.
   */
  @PostMapping(value = "cmf/xml")
  public Object getCmfXmlValidation(
      @RequestPart MultipartFile file,
      @RequestParam(required = false, defaultValue = "json") ResultsFormat mediaType
  ) throws Exception {

    TestReport results = new TestReport();
    Test test = niemValidationService.validateCmf(file);
    results.tests.add(test);
    return this.handleResults(results, mediaType, file);
  }

  /**
   * Validate a message specification or IEPD zip file against NIEM message
   * specification / IEPD conformance rules.
   *
   * <p>Runs the following checks:
   *
   * <ul>
   *   <li>Validates the MPD or IEPD catalog.</li>
   *   <li>Validates XML catalogs</li>
   *   <li>Validates XML schemas</li>
   *   <li>Validates XML schemas against NDR conformance rules</li>
   * </ul>
   *
   * @param file A message specification or IEPD zip file.
   * @param mediaType REQUEST BODY PARAMETER. File format for the validation results.
   */
  @PostMapping(value = "message-specification")
  public Object getMessageSpecificationValidation(
      @RequestPart MultipartFile file,
      @RequestParam(required = false, defaultValue = "json") ResultsFormat mediaType
  ) throws Exception {

    TestReport results = new TestReport();
    List<Test> tests = niemValidationService.validateMessageSpecification(file);
    results.tests.addAll(tests);
    return this.handleResults(results, mediaType, file);
  }

  /**
   * Validate one or more XML Schemas against the NIEM Naming and Design Rules (NDR).
   *
   * @param file XML Schema or zip file
   * @param mediaType REQUEST BODY PARAMETER. File format for the validation results.
   */
  @PostMapping(value = "schemas/ndr")
  @ApiResponse(responseCode = "400", description = "Bad Request", content = @Content)
  public Object getSchemaNdrValidation(
      @RequestPart MultipartFile file,
      @RequestParam(required = false, defaultValue = "json") ResultsFormat mediaType
  ) throws Exception {

    TestReport testReport = new TestReport();
    List<Test> tests = niemValidationService.validateXsdWithNdr(file);
    testReport.tests.addAll(tests);
    return this.handleResults(testReport, mediaType, file);
  }

  /**
   * Validate a JSON Schema.
   *
   * @param file A JSON Schema file
   */
  @Hidden
  @PostMapping(value = "schemas/json")
  public TestReport getSchemaJsonValidation(@RequestPart MultipartFile file) {
    // TODO: Validate JSON Schema
    return null;
  }

  /**
   * Validate a XML schema against NIEM qa rules and best practices.
   */
  @Hidden
  @PostMapping(value = "schemas/qa")
  public TestReport getSchemaQaValidation(@RequestPart MultipartFile file) {
    // TODO: Run QA on schemas
    return null;
  }

  /**
   * Validate one or more JSON instances against a JSON Schema.
   *
   * @param json A JSON instance file or set of JSON instance files in a zip file.
   * @param jsonSchema A JSON Schema file.
   */
  @Hidden
  @PostMapping(value = "instances/json")
  public TestReport getInstanceJsonValidation(
      @RequestPart MultipartFile json,
      @RequestPart MultipartFile jsonSchema) {
    // TODO: Validate JSON Instance
    return null;
  }

  /**
   * Validate a CMF XML file against NIEM CMF NDR rules.
   */
  @Hidden
  @PostMapping(value = "cmf/ndr")
  public TestReport getCmfNdrValidation(@RequestPart MultipartFile file) {
    // TODO: Validate a CMF file against model-related NDR rules.
    return null;
  }

  /**
   * Validate a CMF XML file against NIEM QA rules and best practices.
   */
  @Hidden
  @PostMapping(value = "cmf/qa")
  public TestReport getCmfQaValidation(@RequestPart MultipartFile file) {
    // TODO: Run QA against a CMF file.
    return null;
  }

  /**
   * Check a property for basic NDR conformance issues.
   *
   * @param property A NIEM property
   * @param niemVersionNumber Applicable base NIEM version number. Defaults to
   *     the current version if not provided.
   */
  @Hidden
  @PostMapping(value = "properties/qa")
  public TestReport getPropertyQaValidation(
      @RequestPart Property property,
      @PathVariable(required = false) String niemVersionNumber) {
    // TODO: Validate property
    return null;
  }

  /**
   * Check a property for basic NDR conformance issues.
   *
   * @param type A NIEM type
   * @param niemVersionNumber Applicable base NIEM version number. Defaults to
   *     the current version if not provided.
   */
  @Hidden
  @PostMapping(value = "types/qa")
  public TestReport getTypeQaValidation(
      @RequestPart Type type,
      @PathVariable(required = false) String niemVersionNumber) {
    // Validate type
    return null;
  }

  /**
   * Return validation results as JSON or a CSV.
   */
  private Object handleResults(TestReport testReport, ResultsFormat mediaType,
      MultipartFile file) throws Exception {

    testReport.setDefaultComment();

    if (mediaType.equals(ResultsFormat.csv)) {
      return validationService.returnResultsAsCsv(testReport, file);
    }

    // TODO: Fix duplicate tests in NDR validation test report
    Set<Test> testSet = new HashSet<>(testReport.tests);
    List<Test> testList = new LinkedList<>(testSet);
    Collections.sort(testList, Comparator.comparing(Test::getId));
    testReport.tests = testList;

    // TODO: Fix duplicate results in NDR validation test report
    for (Test test : testReport.tests) {
      Set<TestResult> testResultSet = new HashSet<>(test.results);
      test.results = new LinkedList<>(testResultSet);
    }

    // Default results as JSON
    return testReport;

  }

}
