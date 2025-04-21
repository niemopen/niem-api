package gov.niem.tools.api.validation;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import gov.niem.tools.api.core.utils.AppUtils;
import gov.niem.tools.api.core.utils.CsvUtils;
import gov.niem.tools.api.core.utils.ResponseUtils;
import gov.niem.tools.api.validation.xml.XmlValidationService;

@Service
public class ValidationService {

  @Autowired
  XmlValidationService xmlValidationService;

  public ResponseEntity<byte[]> returnResultsAsCsv(Results results, MultipartFile file) throws Exception {
    Object[] testResults = results.getTestResults().toArray();

    // Support CSV file with header only when test results are empty
    String[] headerColumns = {"testId", "status", "entity", "entityCategory", "message", "location", "comment"};

    String csvString = CsvUtils.toString(testResults, headerColumns);
    String filename = String.format("%s-validation-report-%s.csv", file.getOriginalFilename(), AppUtils.getTimestamp()).replaceAll(" ", "-");
    return ResponseUtils.getResponseFileCsv(csvString, filename);
  }

}
