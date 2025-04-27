package gov.niem.tools.api.migrate;

import gov.niem.tools.api.core.utils.AppUtils;
import gov.niem.tools.api.core.utils.FileUtils;
import gov.niem.tools.api.core.utils.ResponseUtils;
import gov.niem.tools.api.db.model.Model;
import gov.niem.tools.api.db.steward.Steward;

import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RequestPart;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

/**
 * REST controller for migrations.
 */
@RestController
@RequestMapping("/migration")
@Tag(name = "Migrations", description = "Migrate to a newer NIEM version")
public class MigrationController {

  @Autowired
  MigrationService migrationService;

  /**
   * Migrate a version of a supported model in a CMF file to a newer version.
   *
   * <p>Note: Only models stored via the NIEM API and that have migration rules are supported.
   *
   * @param from [REQUEST BODY PARAMETER]
   *     The version of the data model represented in the CMF file.
   *
   * @param to [REQUEST BODY PARAMETER]
   *     A more recent version of the data model to which the CMF file should be migrated.
   *
   * @param file A CMF file containing properties and types to be migrated.
   *
   * @param stewardKey [REQUEST BODY PARAMETER]
   *     A steward identifier. Defaults to the NIEM steward identifier to migrate a NIEM subset.
   *
   * @param modelKey [REQUEST BODY PARAMETER]
   *     A model identifier for the CMF file. Defaults to the NIEM data model to migrate
   *     a NIEM subset.
   *
   * @return Zip file with updated CMF file and a JSON file with issues encountered
   *     during the migration.
   */
  @PostMapping(value = "cmf", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
  @ResponseStatus(code = HttpStatus.OK)
  @ApiResponse(responseCode = "200", content = {@Content(mediaType = "application/zip")})
  @ApiResponse(responseCode = "415", description = "Unsupported Media Type", content = @Content)
  @ApiResponse(responseCode = "500", description = "Error", content = @Content)
  public ResponseEntity<byte[]> migrateCmf(
      @RequestParam(required = true) @Parameter(example = "3.1") String from,
      @RequestParam(required = true) @Parameter(example = "5.2") String to,
      @RequestPart(required = true) MultipartFile file,
      @RequestParam(required = false) @Parameter(example = "niem") String stewardKey,
      @RequestParam(required = false) @Parameter(example = "model") String modelKey
  ) throws Exception {

    // Set default values if needed
    stewardKey = stewardKey == null ? Steward.niemStewardKey : stewardKey;
    modelKey = modelKey == null ? Model.niemModelKey : modelKey;

    // Get filename
    String filenameBase = FileUtils.getFilenameBase(file);

    // Run the migration
    byte[] bytes = migrationService.migrateCmf(stewardKey, modelKey, from, to, file);

    // Return a named zip file with the migrated CMF and a migration report
    String zipFilename = String.format("%s-migration-%s-to-%s-%s.zip",
        filenameBase, from, to, AppUtils.getTimestamp());
    return ResponseUtils.getResponseFileZip(bytes, zipFilename);

  }

}
