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
 * An individual test result.
 *
 * <p>Separate tests and results are useful for cases when validators are able to create
 * a separate test for each kind of issue (e.g., a property declaration without a definition).
 * Each occurrence of that issue would be recorded as a separate result.
 *
 * <p>In some cases, tests and results might not be able to be grouped and each failed test
 * will have a single result.
 */
@Data
@RequiredArgsConstructor
@AllArgsConstructor
@Builder
public class Results {

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

  @JsonProperty("passed")
  public long getPassed() {
    return this.getCount(Status.passed);
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
