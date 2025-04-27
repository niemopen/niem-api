package gov.niem.tools.api.validation;

import gov.niem.tools.api.core.utils.AppUtils;
import gov.niem.tools.api.core.utils.CsvUtils;
import gov.niem.tools.api.core.utils.ResponseUtils;
import gov.niem.tools.api.validation.xml.XmlValidationService;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

/**
 * General operations supporting validation.
 */
@Service
public class ValidationService {

  @Autowired
  XmlValidationService xmlValidationService;

  /**
   * Return validation results as a CSV file.
   */
  public ResponseEntity<byte[]> returnResultsAsCsv(TestReport results, MultipartFile file)
      throws Exception {
    Object[] testResults = results.getTestResults().toArray();

    // Support CSV file with header only when test results are empty
    String[] headerColumns = {"testId", "status", "entity", "entityCategory",
      "message", "location", "comment"};

    String csvString = CsvUtils.toString(testResults, headerColumns);
    String filename = String.format("%s-validation-report-%s.csv", file.getOriginalFilename(), AppUtils.getTimestamp()).replaceAll(" ", "-");
    return ResponseUtils.getResponseFileCsv(csvString, filename);
  }

}
