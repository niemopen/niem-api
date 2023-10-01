package gov.niem.tools.api.validation.xml;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.io.File;
import java.io.IOException;
import java.util.List;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.xml.sax.SAXException;

import gov.niem.tools.api.core.utils.FileUtils;
import gov.niem.tools.api.validation.TestResult;
import lombok.extern.log4j.Log4j2;

@Log4j2
@SpringBootTest(classes={XmlValidationService.class})
public class XmlValidationServiceTest {

  @Autowired
  XmlValidationService xmlValidationService;

  public final static String curDir = "./src/test/java/gov/niem/tools/api/validation/xml/";

  /**
   * Test XML schema validation of a single, small, valid XSD file
   * returns a passed test result.
   */
  @Test
  public void checkXsdValidation_Single_Valid() throws IOException, SAXException {
    File xsdFile = FileUtils.file(curDir + "input/single/person.xsd");
    File[] xsdFiles = {xsdFile};

    gov.niem.tools.api.validation.Test test = xmlValidationService.validateXsd(xsdFiles);

    log.debug("{}", test);

    assertEquals(1, test.countPassed(), "The valid XSD file should have a passing test result");
    assertEquals(0, test.countErrors(), "The valid XSD file should have 0 errors.");
  }

  /**
   * Test XML schema validation of a single, small, invalid XSD file
   * returns errors.
   */
  @Test
  public void checkXsdValidation_Single_Invalid() throws IOException, SAXException {
    File xsdFile = FileUtils.file(curDir + "input/single/person-invalid.xsd");
    File[] xsdFiles = {xsdFile};

    gov.niem.tools.api.validation.Test test = xmlValidationService.validateXsd(xsdFiles);
    xmlValidationService.logTestResults(test);

    log.debug("{}", test);

    // Check passed and error results
    assertEquals(0, test.countPassed(), "The invalid XSD file should not have passing results");
    assertEquals(3, test.countErrors(), "The invalid XSD file should have 3 errors.");

    // Check error result has entity category and component name
    TestResult result = test.results.get(0);
    assertEquals("xs:element", result.entityCategory);
    assertEquals("city", result.entity);
  }

  /**
   * Test XML schema validation of multiple valid XSD files
   * returns a passed test result.
   * @throws Exception
   */
  @Test
  public void checkXsdValidation_Multi_Valid() throws Exception {
    File zipFile = FileUtils.file(curDir + "input/multi/Crash Driver IEPD.zip");

    gov.niem.tools.api.validation.Test test = xmlValidationService.validateXsdZip(zipFile);
    xmlValidationService.logTestResults(test);

    assertEquals(1, test.countPassed());
    assertEquals(0, test.countErrors());
  }

  /**
   * Test XML schema validation of multiple invalid XSD files
   * returns errors.
   * @throws Exception
   */
  @Test
  public void checkXsdValidation_Multi_Invalid() throws Exception {
    File zipFile = FileUtils.file(curDir + "input/multi/Crash Driver IEPD-invalid.zip");

    gov.niem.tools.api.validation.Test test = xmlValidationService.validateXsdZip(zipFile);
    xmlValidationService.logTestResults(test);

    assertEquals(0, test.countPassed());
    assertEquals(1, test.countErrors());

    // Check error result has entity category and component name
    TestResult result = test.results.get(0);
    assertEquals("xs:element", result.entityCategory);
    assertEquals("LicenseAugmentation", result.entity);
  }

  /**
   * Check for two passing tests for a valid XML instance and a valid XML schema.
   */
  @Test
  public void checkXmlValidation_Single_Valid() throws IOException, SAXException {
    File xmlFile = FileUtils.file(curDir + "input/single/person.xml");
    File xsdFile = FileUtils.file(curDir + "input/single/person.xsd");

    this.testXmlValidation(xmlFile, xsdFile, 0, 0);
  }

  /**
   * Check for a passing XML schema validation test and an XML instance validation
   * test with errors.
   */
  @Test
  public void checkXmlValidation_Single_Invalid() throws IOException, SAXException {
    File xmlFile = FileUtils.file(curDir + "input/single/person-invalid.xml");
    File xsdFile = FileUtils.file(curDir + "input/single/person.xsd");

    gov.niem.tools.api.validation.Test[] tests = this.testXmlValidation(xmlFile, xsdFile, 0, 3);

    List<TestResult> results = tests[1].results;

    // Check tag names on error results
    assertEquals("zip", results.get(0).entity);
    assertEquals("zip", results.get(1).entity);
    assertEquals("extra", results.get(2).entity);
  }

  @Test
  public void checkXmlValidation_Single_Valid_InvalidSchema() throws SAXException, IOException {
    File xmlFile = FileUtils.file(curDir + "input/single/person.xml");
    File xsdFile = FileUtils.file(curDir + "input/single/person-invalid.xsd");

    gov.niem.tools.api.validation.Test[] tests = this.testXmlValidation(xmlFile, xsdFile, 3, 1);

  }

  private gov.niem.tools.api.validation.Test[] testXmlValidation(File xmlFile, File xsdFile, int expectedXsdErrorCount, int expectedXmlErrorCount) throws SAXException, IOException {

    gov.niem.tools.api.validation.Test[] tests = xmlValidationService.validateXml(xmlFile, new File[] {xsdFile});

    assertEquals(2, tests.length);

    gov.niem.tools.api.validation.Test xsdTest = tests[0];
    gov.niem.tools.api.validation.Test xmlTest = tests[1];

    log.debug("{}", xsdTest);
    log.debug("{}", xmlTest);

    if (expectedXsdErrorCount == 0) {
      // Check XSD validation result for a valid XSD
      assertEquals(1, xsdTest.countPassed(), "XSD validation test should have one passing result");
      assertEquals(0, xsdTest.countErrors(), "XSD validation test should not have error results");
    }

    if (expectedXsdErrorCount > 0) {
      // XSD invalid.  XML should also return one error result.
      assertEquals(expectedXsdErrorCount, xsdTest.countErrors(), "An invalid XSD file should return error results.");
      assertEquals(1, xmlTest.countErrors(), "Validation should fail for an XML file with an invalid schema");
    }
    else if (expectedXmlErrorCount == 0) {
      // XSD valid.  For XML, expect 1 pass result, 0 error results
      assertEquals(1, xmlTest.countPassed(), "A valid XML file should have one passing result");
      assertEquals(0, xmlTest.countErrors(), "A valid XML file should have 0 error results.");
    }
    else {
      // XSD valid.  For XML, expect 0 pass results, given number of error results
      assertEquals(0, xmlTest.countPassed(), "An invalid XML file should have no passing results.");
      assertEquals(expectedXmlErrorCount, xmlTest.countErrors(), "An invalid XML file should have the expected number of error results");
    }

    return tests;

  }

}
