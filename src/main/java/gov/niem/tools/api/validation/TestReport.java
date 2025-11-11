package gov.niem.tools.api.validation;

import gov.niem.tools.api.validation.TestResult.Status;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import java.util.LinkedList;
import java.util.List;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.RequiredArgsConstructor;

/**
 * A test report that consists of a list of tests that may (or may not) have been run.
 */
@Data
@RequiredArgsConstructor
@AllArgsConstructor
@Builder
public class TestReport {

  /**
   * Supported formats for the validation results file (e.g., json, csv)
   */
  public enum ResultsFormat {
    json,
    csv;
  }

  @Builder.Default
  public List<Test> tests = new LinkedList<Test>();

  // public String[] issuePrefixes;

  @JsonProperty("comment")
  public String comment;

  /**
   * Consolidates results from each test into a single test result list.
   */
  @JsonIgnore
  public List<TestResult> getTestResults() {
    List<TestResult> testResults = new LinkedList<TestResult>();
    for (Test test : this.tests) {
      testResults.addAll(test.results);
    }
    return testResults;
  }

  @JsonProperty("errors")
  public long getErrors() {
    return this.getCount(Status.error);
  }

  @JsonProperty("warnings")
  public long getWarnings() {
    return this.getCount(Status.warning);
  }

  @JsonProperty("info")
  public long getInfo() {
    return this.getCount(Status.info);
  }

  /**
   * Returns the number of tests with a passed result plus the number of
   * tests that ran with zero results.
   */
  @JsonProperty("passed")
  public long getPassed() {
    long count = this.getCount(Status.passed);
    count += this.tests.stream()
        .filter(test -> test.ran == true && test.results.size() == 0)
        .count();
    return count;
  }

  private long getCount(TestResult.Status status) {
    return this.getTestResults()
    .stream()
    .filter(testResult -> testResult.status.equals(status))
    .count() + 0;
  }

  public void setDefaultComment() {
    this.comment = String.format("Errors: %d.  Warnings: %d.  Info: %d.  Passed: %d.",
      this.getErrors(), this.getWarnings(), this.getInfo(), this.getPassed());
  }

}
