package gov.niem.tools.api.validation;

import java.util.LinkedList;
import java.util.List;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;

import gov.niem.tools.api.validation.TestResult.Status;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.RequiredArgsConstructor;

@Data
@RequiredArgsConstructor
@AllArgsConstructor
@Builder
public class Results {

  @Builder.Default
  public List<Test> tests = new LinkedList<Test>();

  // public String[] issuePrefixes;

  @JsonProperty("comment")
  public String comment;

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
    this.comment = String.format("Errors: %d.  Warnings: %d.  Informative messages: %d.  Passed: %d.", this.getErrors(), this.getWarnings(), this.getInfo(), this.getPassed());
  }

}
