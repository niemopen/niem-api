package gov.niem.tools.api.transform;

import gov.niem.tools.api.core.exceptions.BadRequestException;
import gov.niem.tools.api.core.utils.FileUtils;
import gov.niem.tools.api.core.utils.ResponseUtils;

import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RequestPart;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

/**
 * REST controller for model transformations.
 */
@RestController
@Tag(name = "Transforms", description = "Transformations")
public class TransformController {

  @Autowired
  TransformService transformService;

  /**
   * Transform a model from one supported NIEM format to another.
   *
   * @param from [REQUEST BODY PARAMETER] The current model format of the input file.
   * @param to [REQUEST BODY PARAMETER] The requested output format for the transformation.
   * @param file A file with a NIEM model.
   */
  @PostMapping(value = "/transforms/models", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
  @ApiResponse(responseCode = "200", description = "Success", content = {
    @Content(mediaType = "application/json"),
    @Content(mediaType = "application/xml"),
    @Content(mediaType = "application/zip"),
    @Content(mediaType = "text/turtle")
  })
  @ApiResponse(responseCode = "400", description = "Bad request", content = @Content)
  @ApiResponse(responseCode = "415", description = "Unsupported Media Type", content = @Content)
  @ApiResponse(responseCode = "500", description = "Error", content = @Content)
  public ResponseEntity<byte[]> transformModel(
      @RequestParam TransformFrom from,
      @RequestParam TransformTo to,
      @RequestPart MultipartFile file
  ) throws BadRequestException, Exception {

    // Get filename
    String filenameBase = FileUtils.getFilenameBase(file);
    String filename = transformService.getOutputFilename(to, filenameBase);

    // Get response media type
    org.springframework.http.MediaType mediaType = transformService.getOutputMediaType(to);

    // Run transformation and get output file data
    byte[] bytes = transformService.transform(from, to, file);

    return ResponseUtils.getResponseFile(bytes, filename, mediaType);

  }

}
