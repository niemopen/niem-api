package gov.niem.tools.api.validation.niem;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.nio.file.Path;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.xml.transform.stream.StreamSource;
import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathExpressionException;

import org.springframework.stereotype.Service;
import org.w3c.dom.Document;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;

import gov.niem.tools.api.core.utils.FileUtils;
import gov.niem.tools.api.validation.Test;
import gov.niem.tools.api.validation.Test.Severity;
import gov.niem.tools.api.validation.TestResult;
import gov.niem.tools.api.validation.TestResult.Status;
import gov.niem.tools.api.validation.ValidationUtils;
import jakarta.annotation.PostConstruct;
import lombok.extern.log4j.Log4j2;
import net.sf.saxon.s9api.Processor;
import net.sf.saxon.s9api.SaxonApiException;
import net.sf.saxon.s9api.Serializer;
import net.sf.saxon.s9api.Xslt30Transformer;
import net.sf.saxon.s9api.XsltCompiler;
import net.sf.saxon.s9api.XsltExecutable;

@Log4j2
@Service
public class NdrValidationService {

  private Processor processor = new Processor(false);
  private XsltCompiler compiler = this.processor.newXsltCompiler();

  private Map<String, Xslt30Transformer> transformers = new HashMap<>();

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
    }
  }

  private void buildTransformer(Path tempPath, String version, String target) throws IOException, SaxonApiException {

    String ndrKey = this.getNdrKey(version, target);
    String filepathString = String.format("niem-ndr-rules-%s.xsl", ndrKey);
    File xslFile = FileUtils.file(tempPath + "/" + filepathString);

    XsltExecutable stylesheet = this.compiler.compile(xslFile);
    Xslt30Transformer transformer = stylesheet.load30();

    this.transformers.put(ndrKey, transformer);
  }

  private String getXsdNdrKey(String conformanceTargets) {

    String regex = "http://reference.niem.gov/niem/specification/naming-and-design-rules/(.*)/#(.*)SchemaDocument";
    Pattern pattern = Pattern.compile(regex);
    Matcher matcher = pattern.matcher(conformanceTargets);

    if (!matcher.find()) {
      return null;
    }

    String version = matcher.group(1);
    String target = matcher.group(2);

    switch (target) {
      case "Reference":
        target = "ref";
        break;
      case "Extension":
        target = "ext";
        break;
      default:
        return null;
    }

    return String.format("%s-%s", version, target);

  }

  private void skipTest(List<Test> tests, Test test, File file, Severity severity, Status status, String message, String comment) {
    log.info(String.format("%s - %s", file.getName(), message));
    TestResult result = new TestResult(test.id);
    result.status = status;
    result.message = message;
    result.comment = comment == null ? "" : comment;
    result.location = file.getName();
    test.results.add(result);
    test.endTest();
    test.ran = false;
    tests.add(test);
  }

  public List<Test> validateXsdWithNdr(List<File> files, Boolean skipNiem) throws Exception {

    List<Test> tests = new LinkedList<>();

    if (skipNiem == null) {
      skipNiem = true;
    }

    XPath xPath = ValidationUtils.getXpath();

    List<File> xsdFiles = files.stream().filter(file -> file.getName().endsWith(".xsd")).toList();

    for (File file : xsdFiles) {
      Test test = new Test("validate-ndr", "Validate NIEM XML schemas against NDR conformance rules");
      test.startTest();
      test.results = new LinkedList<>();

      Document document = ValidationUtils.getDocument(file);

      String targetNamespace = ValidationUtils.getXsdRootAttributeValue(document, xPath, "targetNamespace");

      if (file.getName().endsWith("structures.xsd") || file.getName().endsWith("code-lists-instance.xsd") || file.getName().endsWith("code-lists-schema-appinfo.xsd") || file.getName().endsWith("conformanceTargets.xsd") || file.getName().endsWith("appinfo.xsd")) {
        String message = "Skipped validation on NIEM utility schema.";
        this.skipTest(tests, test, file, Severity.info, Status.info, message, null);
        continue;
      }

      if (targetNamespace == null) {
        // TODO: Handle no target namespace
        String message = "No target namespace found.  This attribute is required for NIEM conformant schemas.";
        String comment = "This warning can be ignored for external standards that are properly handled via NIEM adapter types";
        this.skipTest(tests, test, file, Severity.warning, Status.warning, message, comment);
        continue;
      }

      if (skipNiem == true && targetNamespace.startsWith("http://release.niem.gov/niem/")) {
        String message = "Skipped validation on schema with a reserved NIEM uri ('http://release.niem.gov/niem/').";
        this.skipTest(tests, test, file, Severity.info, Status.info, message, null);
        continue;
      }

      String conformanceTargets = ValidationUtils.getXsdRootAttributeValue(document, xPath, "conformanceTargets");

      String ndrKey = this.getXsdNdrKey(conformanceTargets);
      Xslt30Transformer transformer = this.transformers.get(ndrKey);

      if (ndrKey == null || transformer == null) {
        // TODO: Report no transformer found
        String message = "NO CONFORMANCE TARGET FOUND.  UNABLE TO RUN VALIDATION TESTS.";
        String comment = "Unless this is an external standard, NIEM schemas should contain a conformance target which indicates which NDR rule set to use for conformance validation.  See https://niem.github.io/reference/concepts/namespace/#conformance-targets-1 for more.";
        this.skipTest(tests, test, file, Severity.warning, Status.warning, message, comment);
        continue;
      }

      File tempResultsFiles = FileUtils.createTempFile("validation-ndr", "xml");
      // Serializer out = this.processor.newSerializer(new File(file.getName() + "-results.xml"));
      Serializer out = this.processor.newSerializer(tempResultsFiles);
      out.setOutputProperty(Serializer.Property.METHOD, "xml");
      out.setOutputProperty(Serializer.Property.INDENT, "yes");
      transformer.transform(new StreamSource(file), out);

      List<Test> ndrResults = this.processResults(tempResultsFiles, file.getName(), ndrKey, document, xPath);
      FileUtils.deleteTempFile(tempResultsFiles);

      tests.addAll(ndrResults);
    }

    return tests;

  }

  private List<Test> processResults(File file, String filename, String ndrKey, Document document, XPath xPath) throws IOException, XPathExpressionException {

    List<Test> tests = new LinkedList<>();

    BufferedReader reader = new BufferedReader(new FileReader(file));
    String line = reader.readLine();

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

      Test test = new Test("validate-ndr-" + ruleId);
      test.ruleNumber = ruleId;
      test.results = new LinkedList<>();
      test.notes = String.format("Validated %s with NDR %s rules", filename, ndrKey);

      // Results
      line = reader.readLine();

      while (line!= null && !line.startsWith("   <svrl:active-pattern ")) {

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
          this.getLocation(result, expression, document, xPath);
        }
        else if (line.startsWith("   <svrl:successful-report")) {
          // Report warning
          line = reader.readLine();
          String role = line;

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
          this.getLocation(result, expression, document, xPath);
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

  private void getLocation(TestResult result, String location, Document document, XPath xPath) throws XPathExpressionException {

    String expression = location
        .replaceFirst("^ *location=\"", "")
        .replace("\">", "");

    // TODO: Fix URI awareness
    expression = expression
        .replaceAll(" and namespace-uri\\(\\)='http:\\/\\/www.w3.org\\/2001\\/XMLSchema'", "");
    log.debug(result.location);
    log.debug(expression);

    Node node = ValidationUtils.getXpathResult(document, xPath, expression);
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
