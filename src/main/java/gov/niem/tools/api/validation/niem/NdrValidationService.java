package gov.niem.tools.api.validation.niem;

import gov.niem.tools.api.core.utils.FileUtils;
import gov.niem.tools.api.validation.Test;
import gov.niem.tools.api.validation.Test.Severity;
import gov.niem.tools.api.validation.TestResult;
import gov.niem.tools.api.validation.TestResult.Status;
import gov.niem.tools.api.validation.ValidationUtils;

import com.fasterxml.jackson.core.exc.StreamReadException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.DatabindException;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.annotation.PostConstruct;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Path;
import java.util.Arrays;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import javax.xml.transform.stream.StreamSource;
import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathExpressionException;
import lombok.extern.log4j.Log4j2;
import net.sf.saxon.s9api.Processor;
import net.sf.saxon.s9api.SaxonApiException;
import net.sf.saxon.s9api.Serializer;
import net.sf.saxon.s9api.Xslt30Transformer;
import net.sf.saxon.s9api.XsltCompiler;
import net.sf.saxon.s9api.XsltExecutable;
import org.springframework.stereotype.Service;
import org.w3c.dom.Document;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;

/**
 * Validates XML Schemas against NDR validation rules.
 */
@Log4j2
@Service
public class NdrValidationService {

  private Processor processor = new Processor(false);
  private XsltCompiler compiler = this.processor.newXsltCompiler();

  private Map<String, Xslt30Transformer> transformers = new HashMap<>();

  /**
   * Initializes NDR Schematron XSL rule files after application initialization.
   */
  @PostConstruct
  public void init() {
    try {
      this.initTransformers();
      log.info("NDR validators loaded");
    }
    catch (Exception exception) {
      log.error("NDR validators failed to load");
    }
  }

  private String getNdrKey(String version, String target) {
    return String.format("%s-%s", version, target);
  }

  /**
   * Loads NDR Schematron XSL files.
   */
  private void initTransformers() throws IOException, SaxonApiException {
    if (this.transformers.size() == 0) {
      Path tempPath = FileUtils.createTempDir("validation-ndr");

      ValidationUtils.copyClasspathFiles(tempPath, "validation/ndr");

      this.buildTransformer(tempPath, "3.0", "ref");
      this.buildTransformer(tempPath, "3.0", "ext");
      this.buildTransformer(tempPath, "4.0", "ref");
      this.buildTransformer(tempPath, "4.0", "ext");
      this.buildTransformer(tempPath, "5.0", "ref");
      this.buildTransformer(tempPath, "5.0", "ext");
      this.buildTransformer(tempPath, "6.0", "ref");
      this.buildTransformer(tempPath, "6.0", "ext");
      this.buildTransformer(tempPath, "6.0", "msg");
      this.buildTransformer(tempPath, "6.0", "sub");
    }
  }

  /**
   * Compiles and loads NDR stylesheets to prepare for validation requests.
   */
  private void buildTransformer(Path tempPath, String version, String target)
      throws IOException, SaxonApiException {

    String ndrKey = this.getNdrKey(version, target);
    String filepathString = String.format("niem-ndr-rules-%s.xsl", ndrKey);
    File xslFile = FileUtils.file(tempPath + "/" + filepathString);

    XsltExecutable stylesheet = this.compiler.compile(xslFile);
    Xslt30Transformer transformer = stylesheet.load30();

    this.transformers.put(ndrKey, transformer);
  }

  private String getXsdNdrKey(String conformanceTargets) {

    String regex;

    // NDR 3.0 - NDR 5.0
    if (conformanceTargets.contains("http://reference.niem.gov/niem/specification/naming-and-design-rules/")) {
      regex = "http://reference.niem.gov/niem/specification/naming-and-design-rules/(.*)/#(.*)SchemaDocument";
    }
    // NDR 6.0
    else if (conformanceTargets.contains("https://docs.oasis-open.org/niemopen/ns/specification/NDR/")) {
      regex = "https://docs.oasis-open.org/niemopen/ns/specification/NDR/(.*)/#(.*)SchemaDocument";
    }
    else {
      return null;
    }

    Pattern pattern = Pattern.compile(regex);
    Matcher matcher = pattern.matcher(conformanceTargets);

    if (!matcher.find()) {
      return null;
    }

    String version = matcher.group(1);
    String target = matcher.group(2);

    int major = Integer.parseInt(String.valueOf(version.charAt(0)));

    switch (target) {
      case "Reference":
        target = "ref";
        break;
      case "Extension":
        target = "ext";
        break;
      case "Message":
        target = major >= 6 ? "msg" : null;
        break;
      case "Subset":
        target = major >= 6 ? "sub" : null;
        break;
      default:
        return null;
    }

    return String.format("%s-%s", version, target);

  }

  private void skipTest(List<Test> tests, Test test, File file, Severity severity,
      Status status, String entityCategory, String message, String comment) {
    log.info(String.format("%s - %s", file.getName(), message));
    TestResult result = new TestResult(test.id);
    result.status = status;
    result.message = message;
    result.comment = comment == null ? "" : comment;
    result.location = file.getName();
    result.entityCategory = entityCategory;
    test.results.add(result);
    test.endTest();
    test.ran = false;
    tests.add(test);
  }

  /**
   * Validate the given files against NDR rules.
   *
   * @param skipNiem - True to skip validation on files with target namespaces
   *     from the NIEM reference model.
   */
  public List<Test> validateXsdWithNdr(List<File> files, Boolean skipNiem) throws Exception {

    List<Test> tests = new LinkedList<>();

    if (skipNiem == null) {
      skipNiem = true;
    }

    XPath xpath = ValidationUtils.getXpath();

    List<File> xsdFiles = files.stream().filter(file -> file.getName().endsWith(".xsd")).toList();

    for (File file : xsdFiles) {
      Test test = new Test("validate-ndr",
          "Validate NIEM XML schemas against NDR conformance rules");
      test.startTest();
      test.results = new LinkedList<>();

      Document document = ValidationUtils.getDocument(file);

      String targetNamespace = ValidationUtils.getXsdRootAttributeValue(document,
          xpath, "targetNamespace");

      if (file.getName().endsWith("structures.xsd")
          || file.getName().endsWith("code-lists-instance.xsd")
          || file.getName().endsWith("code-lists-schema-appinfo.xsd")
          || file.getName().endsWith("conformanceTargets.xsd")
          || file.getName().endsWith("appinfo.xsd")) {
        String message = "Skipped validation on NIEM utility schema.";
        this.skipTest(tests, test, file, Severity.info, Status.info, "utility", message, null);
        continue;
      }

      if (targetNamespace == null) {
        String message = "No target namespace found.  This attribute is required for NIEM conformant schemas.";
        String comment = "This warning can be ignored for external standards that are properly handled via NIEM adapter types";
        this.skipTest(tests, test, file, Severity.warning, Status.warning, null, message, comment);
        continue;
      }

      if (skipNiem == true && targetNamespace.startsWith("http://release.niem.gov/niem/")) {
        String message = "Skipped validation on schema with a reserved NIEM uri ('http://release.niem.gov/niem/').";
        this.skipTest(tests, test, file, Severity.info, Status.info, null, message, null);
        continue;
      }

      String conformanceTargets = ValidationUtils.getXsdRootAttributeValue(document,
          xpath, "conformanceTargets");

      String ndrKey = this.getXsdNdrKey(conformanceTargets);
      Xslt30Transformer transformer = this.transformers.get(ndrKey);

      if (ndrKey == null) {
        String message = "NO NDR CONFORMANCE TARGET FOUND.  UNABLE TO RUN VALIDATION TESTS.";
        String comment = "Unless this is an external standard, NIEM schemas should contain a conformance target which indicates which NDR rule set to use for conformance validation.  See https://niem.github.io/reference/concepts/namespace/#conformance-targets-1 for more.";
        this.skipTest(tests, test, file, Severity.warning, Status.warning,
            "no ndr", message, comment);
        continue;
      }
      else if (transformer == null) {
        String message = "NO MATCHING RULE SET FOUND.  UNABLE TO RUN VALIDATION TESTS.";
        this.skipTest(tests, test, file, Severity.warning, Status.warning, null, message, "");
        continue;
      }

      File tempResultsFiles = FileUtils.createTempFile("validation-ndr", "xml");
      // Serializer out = this.processor.newSerializer(new File(file.getName() + "-results.xml"));
      Serializer out = this.processor.newSerializer(tempResultsFiles);
      out.setOutputProperty(Serializer.Property.METHOD, "xml");
      out.setOutputProperty(Serializer.Property.INDENT, "yes");
      transformer.transform(new StreamSource(file), out);

      List<Test> ndrResults = this.processResults(tempResultsFiles,
          file.getName(), ndrKey, document, xpath);
      FileUtils.deleteTempFile(tempResultsFiles);

      tests.addAll(ndrResults);
    }

    return tests;

  }

  private Map<String, String[]> loadRuleNumberMap()
      throws StreamReadException, DatabindException, IOException {
    ObjectMapper objectMapper = new ObjectMapper();
    String resourcePath = "/validation/ndr/ndr-5.0-to-6.0-ruleNumbers.json";
    // InputStream inputStream = HashMap.class.getResourceAsStream(resourcePath);
    InputStream inputStream = getClass().getResourceAsStream(resourcePath);
    return objectMapper.readValue(inputStream, new TypeReference<Map<String, String[]>>() {});
  }

  /**
   * Convert results in a SVRL format into a validation report.
   *
   * @todo Remove NDR 5.0 to 6.0 rule number mapping code once changes to support new
   *     NDR 6.0 rules are implemented and the 6.0 XSL files provide the real numbers.
   */
  private List<Test> processResults(File file, String filename, String ndrKey,
      Document document, XPath xpath) throws IOException, XPathExpressionException {

    List<Test> tests = new LinkedList<>();

    BufferedReader reader = new BufferedReader(new FileReader(file));
    String line = reader.readLine();

    Map<String, String[]> ruleNumberMap = null;
    if (ndrKey.startsWith("6.0")) {
      ruleNumberMap = this.loadRuleNumberMap();
    }

    while (line != null) {
      if (!line.startsWith("   <svrl:active-pattern ")) {
        line = reader.readLine();
        continue;
      }

      // Rule ID
      line = reader.readLine();
      String ruleId = line.split("=")[1].replaceAll("\"", "");

      // Rule title
      line = reader.readLine();
      String ruleTitle = line.split("\"")[1];

      // Handle 6.0 rule number mapping (first entry)
      String[] updatedRuleNumbers = null;
      if (ruleNumberMap != null) {
        updatedRuleNumbers = ruleNumberMap.get(ruleId);
        if (updatedRuleNumbers != null && updatedRuleNumbers.length > 0) {
          // Replace parsed rule number with first entry from the mapping
          ruleId = updatedRuleNumbers[0];
        }
      }

      Test test = new Test("validate-ndr-" + ruleId);
      test.ruleNumber = ruleId;
      test.ruleUrl = this.getRuleUrl(ndrKey, ruleId);
      test.results = new LinkedList<>();
      test.notes = String.format("Validated %s with NDR %s rules", filename, ndrKey);

      // Results
      line = reader.readLine();

      while (line != null && !line.startsWith("   <svrl:active-pattern ")) {

        TestResult result = new TestResult(test.id);
        result.message = ruleTitle;
        result.location = filename;
        result.entity = "";
        result.entityCategory = "";

        String expression = "";

        if (line.startsWith("   <svrl:failed-assert")) {
          // Report error
          result.status = Status.error;
          test.severity = Severity.error;

          line = reader.readLine();
          expression = line;

          line = reader.readLine();
          String comment = line;

          result.comment = comment.replace("      <svrl:text>", "").replace("</svrl:text>", "");
          test.ran = true;
          test.results.add(result);
          this.getLocation(result, expression, document, xpath);
        }
        else if (line.startsWith("   <svrl:successful-report")) {
          // Report warning
          line = reader.readLine();
          // String role = line;

          // Get xpath to component
          line = reader.readLine();
          expression = line;

          // Get rule description
          line = reader.readLine();
          String comment = line;

          result.status = Status.warning;
          test.severity = Severity.warning;
          result.comment = comment.replace("      <svrl:text>", "").replace("</svrl:text>", "");
          test.ran = true;
          test.results.add(result);
          this.getLocation(result, expression, document, xpath);
        }


        // Temporary special processing for 6.0 rules
        if (ndrKey.startsWith("6.0")) {
          if (updatedRuleNumbers == null) {
            // 5.0 rule number did not map to 6.0 rule set
            test.ruleNumber += " [NDR 5.0-only rule]";
          }
          else {
            if (updatedRuleNumbers != null && updatedRuleNumbers.length > 1) {
              // Note the additional 6.0 rule numbers when one 5.0 rule maps to multiple 6.0 rules
              String[] remainingRuleNumbers = Arrays.copyOfRange(updatedRuleNumbers,
                  1, updatedRuleNumbers.length);
              result.comment += String.format(" Also see NDR 6.0 rule(s) %s",
                  remainingRuleNumbers.toString());
            }
          }
        }

        line = reader.readLine();
      }

      if (test.countErrors() > 0 || test.countWarnings() > 0) {
        tests.add(test);
      }

    }

    reader.close();
    return tests;
  }

  private String getRuleUrl(String ndrKey, String ruleId) {
    String version = ndrKey.substring(0, 3);
    switch (version) {
      case "3.0":
        return "https://reference.niem.gov/niem/specification/naming-and-design-rules/3.0/niem-ndr-3.0.html#" + ruleId;
      case "4.0":
        return "https://reference.niem.gov/niem/specification/naming-and-design-rules/4.0/niem-ndr-4.0.html#" + ruleId;
      case "5.0":
        return "https://reference.niem.gov/niem/specification/naming-and-design-rules/5.0/niem-ndr-5.0.html#" + ruleId;
      case "6.0":
        return "https://niemopen.github.io/niem-naming-design-rules/ndr-v6.0-psd01.html#" + ruleId;
      default:
        return "";
    }
  }

  private void getLocation(TestResult result, String location, Document document, XPath xpath)
      throws XPathExpressionException {

    String expression = location
        .replaceFirst("^ *location=\"", "")
        .replace("\">", "");

    // TODO: Fix URI awareness
    expression = expression
        .replaceAll(" and namespace-uri\\(\\)='http:\\/\\/www.w3.org\\/2001\\/XMLSchema'", "");
    log.debug(result.location);
    log.debug(expression);

    Node node = null;

    try {
      node = ValidationUtils.getXpathResult(document, xpath, expression);
    }
    catch (Exception exception) {
      log.error(exception.getMessage());
    }

    if (node != null) {
      result.entityCategory = node.getNodeName();
      log.info(result.entityCategory);

      if (result.entityCategory.equals("xs:documentation")) {
        node = node.getParentNode().getParentNode();
        result.entityCategory = node.getNodeName();
      }

      NamedNodeMap nodeMap = node.getAttributes();
      if (nodeMap != null) {
        Node nameNode = nodeMap.getNamedItem("name");
        if (nameNode != null) {
          result.entity = nameNode.getNodeValue();
        }
      }

      log.info(result.entityCategory + " - " + result.entity);

    }
  }

}
