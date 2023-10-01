package gov.niem.tools.api.validation.xml;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;

import org.xml.sax.ErrorHandler;
import org.xml.sax.SAXException;
import org.xml.sax.SAXParseException;

import gov.niem.tools.api.validation.Test;
import gov.niem.tools.api.validation.Test.Severity;
import gov.niem.tools.api.validation.TestResult;
import gov.niem.tools.api.validation.TestResult.Status;
import lombok.Getter;

/**
 * Custom error handler to catch and log SAX exceptions.  This overrides the default
 * exception handler which would stop validation on the first exception encountered.
 */
public class XmlErrorHandler implements ErrorHandler {

  private List<SAXParseException> exceptions;

  private HashMap<String, XmlNodeMap> fileMap = new HashMap<>();

  private String lastRelativePath = "";

  @Getter
  private Test test;

  public XmlErrorHandler(File[] files, XmlNodeMap nodeMapTemplate, Test test) throws IOException {
    this.exceptions = new ArrayList<>();
    this.test = test;

    this.initTest();

    for (File file : files) {
      XmlNodeMap nodeMap = nodeMapTemplate.create();
      nodeMap.load(file);
      String key = XmlValidationUtils.getRelativePath(file.getCanonicalPath());
      this.fileMap.put(key, nodeMap);

      if (files.length == 1) {
        this.lastRelativePath = key;
      }
    }

  }

  private void initTest() {
    this.test.results = new LinkedList<>();
    this.test.startTest();
  }

  public List<SAXParseException> getExceptions() {
    return exceptions;
  }

  /**
   * Stop the test run clock and summarize the test status in the comments.
   */
  public Test endTest() {
    if (this.exceptions.size() == 0) {
      // Log that the validation test passed successfully
      this.test.comments = "Validation succeeded";
      this.test.severity = Severity.info;
      this.test.status = Status.passed;

      TestResult result = new TestResult(this.test.id);
      result.status = Status.passed;
      result.message = "";
      result.location = this.lastRelativePath;
      result.line = "";
      result.column = "";
      result.entity = "";
      result.entityCategory = "";
      result.comment = "";
      this.test.results.add(result);
    }
    else if (this.test.countErrors() > 0) {
      this.test.severity = Severity.error;
      this.test.status = Status.error;
      this.test.comments = "Validation failed";
    }
    else if (this.test.countWarnings() > 0) {
      this.test.severity = Severity.warning;
      this.test.status = Status.warning;
      this.test.comments = "Validation encountered warnings";
    }
    this.test.endTest();
    return this.test;
  }

  private void logIssue(TestResult.Status status, SAXParseException exception)  {
    TestResult result = new TestResult(this.test.id);
    result.status = status;
    result.message = exception.getLocalizedMessage();
    result.setLine(exception.getLineNumber());
    result.setColumn(exception.getColumnNumber());
    result.testId = this.test.id;

    if (result.column == null) {
      result.column = "n/a";
    }

    try {
      String systemId = exception.getSystemId();
      String relativePath = XmlValidationUtils.getRelativePath(systemId);
      result.location = relativePath;
    }
    catch (IOException ioException) {
      result.location = exception.getSystemId().replaceFirst("file:/", "");
    }

    XmlNodeMap nodeMap = this.fileMap.get(result.location);
    if (nodeMap != null) {
      XmlNode node = nodeMap.get(exception.getLineNumber());
      if (node != null) {
        result.entityCategory = node.style;
        result.entity = node.name;
      }
    }

    this.test.results.add(result);
  }

  @Override
  public void warning(SAXParseException exception) throws SAXException {
    this.exceptions.add(exception);
    this.logIssue(TestResult.Status.warning, exception);
  }

  @Override
  public void error(SAXParseException exception) throws SAXException {
    this.exceptions.add(exception);
    this.logIssue(TestResult.Status.error, exception);
  }

  @Override
  public void fatalError(SAXParseException exception) throws SAXException {
    this.exceptions.add(exception);
    this.logIssue(TestResult.Status.error, exception);
  }

}
