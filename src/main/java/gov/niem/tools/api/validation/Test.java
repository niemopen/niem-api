package gov.niem.tools.api.validation;

import java.util.LinkedList;
import java.util.List;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonInclude;

import gov.niem.tools.api.validation.TestResult.Status;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.RequiredArgsConstructor;

@Data
@RequiredArgsConstructor
@AllArgsConstructor
@Builder
@JsonInclude(JsonInclude.Include.NON_NULL)
public class Test {

  public enum Severity {
    error,
    warning,
    info
  };

  @Builder.Default
  public Status status = Status.unset;

  public String id;
  public String description;
  public Severity severity;
  public String category;
  public String component;
  public String field;
  public String comments;
  public String scope;
  public String source;
  public String specId;
  public String specUrl;
  public String ruleNumber;
  public String ruleUrl;
  public String exampleValid;
  public String exampleInvalid;
  public String exceptions;
  public String exceptionLabels;
  public String notes;
  public boolean ran;

  /** Test runtime in seconds */
  public float runtime;

  @JsonIgnore
  private long start;

  @Builder.Default
  public List<TestResult> results = new LinkedList<TestResult>();

  public Test(String id) {
    this.id = id;
  }

  public Test(String id, String description) {
    this.id = id;
    this.description = description;
  }

  public void startTest() {
    this.start = System.currentTimeMillis();
  }

  public void startTest(Severity severity) {
    this.startTest();
    this.severity = severity;
  }

  public void endTest() {
    long end = System.currentTimeMillis();
    this.ran = true;
    this.runtime = (end - start) / 1000F;
  }

  public long countWarnings() {
    return this.getCount(Status.warning);
  }

  public long countErrors() {
    return this.getCount(Status.error);
  }

  public long countInfo() {
    return this.getCount(Status.info);
  }

  public long countPassed() {
    return this.getCount(Status.passed);
  }

  private long getCount(Status status) {
    return this.results
    .stream()
    .filter(result -> result.status.equals(status))
    .count() + 0;
  }

}
