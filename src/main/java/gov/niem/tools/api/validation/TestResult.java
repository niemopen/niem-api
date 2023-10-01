package gov.niem.tools.api.validation;

import com.fasterxml.jackson.annotation.JsonInclude;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.RequiredArgsConstructor;

@Data
@RequiredArgsConstructor
@AllArgsConstructor
@Builder
@JsonInclude(JsonInclude.Include.NON_NULL)
public class TestResult {

  public enum Status {
    error,
    warning,
    info,
    passed,
    unset
  }

  public String testId;

  @Builder.Default
  public Status status = Status.unset;

  public String prefix;
  public String entity;
  public String entityCategory;
  public String message;
  public String location;
  public String line;
  public String column;
  public String comment;
  public String problemValue;

  public TestResult(String testId) {
    this.testId = testId;
  }

  public TestResult(String testId, String label) {
    this.testId = testId;
    this.entity = label;
  }

  public TestResult(String testId, String label, String prefix, String problemValue) {
    this.testId = testId;
    this.entity = label;
    this.prefix = prefix;
    this.problemValue = problemValue;
  }

  public void setLine(int line) {
    this.line = Integer.toString(line);
  }

  public void setLine(String line) {
    this.line = line;
  }

  public void setColumn(int col) {
    this.column = Integer.toString(col);
  }

  public void setColumn(String col) {
    this.column = col;
  }

}
