package gov.niem.tools.api.migrate;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import gov.niem.tools.api.core.utils.AppUtils;
import gov.niem.tools.api.core.utils.FileUtils;
import gov.niem.tools.api.core.utils.ResponseUtils;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;

@RestController
@RequestMapping("/migration")
@Tag(name = "Migrations", description = "Migrate to a newer NIEM version")
public class MigrationController {

  @Autowired
  MigrationService migrationService;

  /**
   * Migrate NIEM subset content in a CMF file to a newer version of NIEM.
   *
   * @param from The version of the NIEM model represented in the CMF file.
   * @param to The more recent version of NIEM to which the CMF file should be migrated.
   * @param file A CMF file containing NIEM properties and types to be migrated.
   * @param stewardKey Steward identifier.  If not provided, defaults to the NIEM steward identifier.
   * @param modelKey Model identifier.  If not provided, defaults to the NIEM data model identifier.
   *
   * @return Zip file with updated CMF file and a JSON file with issues encountered during the migration.
   */
  @PostMapping(value = "cmf", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
  @ResponseStatus(code = HttpStatus.OK)
  @ApiResponse(responseCode = "200", content = {@Content(mediaType = "application/zip")})
  @ApiResponse(responseCode = "415", description = "Unsupported Media Type", content = @Content)
  @ApiResponse(responseCode = "500", description = "Error", content = @Content)
  public ResponseEntity<byte[]> migrateCMF(
    @RequestParam String from,
    @RequestParam String to,
    @RequestParam MultipartFile file,
    @RequestParam(required = false, defaultValue = "niem") String stewardKey,
    @RequestParam(required = false, defaultValue = "model") String modelKey
  ) throws Exception {

    // Get filename
    String filenameBase = FileUtils.getFilenameBase(file);

    // Run the migration
    byte[] bytes = migrationService.migrateCmf(stewardKey, modelKey, from, to, file);

    // Return a named zip file with the migrated CMF and a migration report
    String zipFilename = String.format("%s-migration-%s-to-%s-%s.zip", filenameBase, from, to, AppUtils.getTimestamp());
    return ResponseUtils.getResponseFileZip(bytes, zipFilename);

  }

}
