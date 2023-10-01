package gov.niem.tools.api.transform;

import java.io.File;
import java.io.FileInputStream;
import java.io.PrintWriter;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;

import org.mitre.niem.cmf.Model;
import org.mitre.niem.json.ModelToJSON;
import org.mitre.niem.rdf.ModelToOWL;
import org.mitre.niem.xsd.ModelFromXSD;
import org.mitre.niem.xsd.ModelToXSD;
import org.mitre.niem.xsd.ModelXMLReader;
import org.mitre.niem.xsd.ModelXMLWriter;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import gov.niem.tools.api.core.exceptions.BadRequestException;
import gov.niem.tools.api.core.utils.FileUtils;
import gov.niem.tools.api.core.utils.ZipUtils;
import lombok.extern.log4j.Log4j2;

@Service @Log4j2
public class TransformService {

  /**
   * Convert the input file from the given 'from' format to the given 'to'
   * format.  Saves the output to a temporary directory.
   *
   * @param from - A supported NIEM model format to be transformed
   * @param to - A supported NIEM model format to be transformed into
   * @param multipartInputFile - A file or files to be transformed
   */
  public byte[] transform(TransformFrom from, TransformTo to, MultipartFile multipartInputFile) throws Exception {

    log.info(String.format("Transform %s from %s to %s", FileUtils.getFilename(multipartInputFile), from, to));

    // Get the input filename base and extension
    String inputFilenameBase = FileUtils.getFilenameBase(multipartInputFile);
    String inputExtension = FileUtils.getFileExtension(multipartInputFile);

    // Verify user input
    checkInput(from, to, inputExtension);

    // Convert input to CMF
    Model cmf = this.loadInput(from, multipartInputFile, inputExtension);

    // Transformation optional step:
    // TODO: Support CMF to simple CMF transforms

    // Convert CMF to the user-selected format
    // TODO: Model should store model name
    byte[] bytes = this.generateOutput(cmf, to, inputFilenameBase);
    return bytes;

  }

  /**
   * Checks to make sure user input is valid.
   * @param from - A supported NIEM model format to be transformed
   * @param to - A supported NIEM model format to be transformed into
   * @param inputExtension - The file extension of the model to be transformed
   */
  public static void checkInput(TransformFrom from, TransformTo to, String inputExtension) throws BadRequestException {
    // Check that the input file extension is valid for the given from parameter
    checkInputFileExtension(from, inputExtension);
  }

  /**
   * Check if the input file has a valid extension for a transformation.
   * Throw a BadRequestException if the check fails.
   *
   * @param from - A supported NIEM model format to be transformed
   * @param inputExtension - The file extension of the model to be transformed
   */
  public static void checkInputFileExtension(TransformFrom from, String inputExtension) throws BadRequestException {
    switch (from) {
      case xsd:
        if (inputExtension.equals("xsd") || inputExtension.equals("zip")) {
          return;
        }
        break;
      case cmf:
        if (inputExtension.equals("cmf") || inputExtension.equals("cmf.xml")) {
          return;
        }
        break;
    }

    String msg = String.format("A file with extension .%s is not a valid input for transforming a model from %s", inputExtension, from.toString());

    throw new BadRequestException(msg);
  }

  /**
   * Convert the input file from the given `from` format to the given
   * `to` format.
   */
  public Model loadInput(TransformFrom from, MultipartFile multipartInputFile, String inputExtension) throws Exception {

    Path tempInputFolder = FileUtils.createTempDir("transform-load-input");

    // Save the input multipart file to a new temporary file
    Path inputFile = FileUtils.saveFile(multipartInputFile, tempInputFolder);
    log.debug("User input saved to " + inputFile.toAbsolutePath().toString());

    Model cmf;

    switch (from) {
      case xsd:
        // Read one or more XML schemas and load into a new CMF model.
        List<Path> files = new ArrayList<Path>();

        // Extract to a new temporary folder if input is a zip file
        if (inputExtension.equals("zip")) {
          tempInputFolder = ZipUtils.unzip(inputFile);
        }

        switch (inputExtension) {
          case "xsd":
            // Read a single XSD file into the CMF model
            files.add(inputFile);
            break;
          case "zip":
            // Read a folder with multiple XML schemas into the CMF model
            files = FileUtils.getFilePathsFromDirWithExtension(tempInputFolder, "xsd");
            files.removeIf(file -> file.getFileName().toString().equals("localTerminology.xsd"));

            // Zip folder may also contain xml-catalog.xml files
            List<Path> catalogs = FileUtils.getFilePathsFromDirWithFilename(tempInputFolder, "xml-catalog");
            files.addAll(catalogs);
            break;
          default:
            // Handle unexpected input format for a transformation from XSD
            throw new BadRequestException(String.format("%s is not supported as an input format for a NIEM transformation from XSD", inputExtension));
        }

        ModelFromXSD modelFromXSD = new ModelFromXSD();
        cmf = modelFromXSD.createModel(files.stream()
          .map(path -> path.toString())
          .toArray(String[]::new)
        );

        break;

      case cmf:
        // Read a given CMF file and load into a new CMF model.
        FileInputStream inputStream = new FileInputStream(inputFile.toFile());
        ModelXMLReader modelReader = new ModelXMLReader();
        cmf = modelReader.readXML(inputStream);
        if (cmf == null) {
          log.info("Load input failed: Could not parse CMF");
          modelReader.getMessages().forEach(message -> log.info(message));
          throw new BadRequestException(String.join(", ", modelReader.getMessages()));
        }
        break;

      default:
        // Handle unexpected input cases
        throw new BadRequestException(String.format("%s is not supported as a NIEM transformation input", from));
    }

    FileUtils.deleteTempDir(tempInputFolder);

    return cmf;

  }

  /**
   * Second pass of transformation. Convert the input file from the given
   * `from` format to the given `to` format.
   */
  public byte[] generateOutput(Model cmf, TransformTo to, String filenameBase) throws Exception {

    Path tempDir = FileUtils.createTempDir("transform-output");

    String outputFilePathString = tempDir.toString() + "/" + this.getOutputFilename(to, filenameBase);

    File outputFile = FileUtils.file(outputFilePathString);

    // Handle multiple schemas and the zip file separately
    if (to.equals(TransformTo.xsd)) {

      Path xsdDir = FileUtils.createDir(FileUtils.path(tempDir.toString() + "/" + filenameBase));
      ModelToXSD modelToXSD = new ModelToXSD(cmf);
      modelToXSD.writeXSD(xsdDir.toFile());
      ZipUtils.zip(xsdDir.toFile(), outputFilePathString);

    }
    else {

      PrintWriter writer = new PrintWriter(outputFile);

      switch (to) {
        case cmf:
          ModelXMLWriter cmfWriter = new ModelXMLWriter();
          cmfWriter.writeXML(cmf, writer);
          break;

        case owl:
          ModelToOWL m2o = new ModelToOWL(cmf);
          m2o.writeRDF(writer);
          break;

        case xsd:
          break;

        case json_schema:
          ModelToJSON modelToJSON = new ModelToJSON(cmf);
          modelToJSON.writeJSON(writer);
          break;

        default:
          writer.close();
          throw new Exception();
      }

      writer.close();

    }

    byte[] bytes = Files.readAllBytes(outputFile.toPath());
    FileUtils.deleteTempDir(tempDir);

    return bytes;

  }

  public String getOutputFilename(TransformTo to, String filenameBase) throws Exception {
    switch (to) {
      case cmf:
        return filenameBase + ".cmf.xml";
      case owl:
        return filenameBase + ".owl.ttl";
      case xsd:
        return filenameBase + ".zip";
      case json_schema:
        return filenameBase + ".schema.json";
    }
    throw new Exception("Unknown transformation format");
  }

  public MediaType getOutputMediaType(TransformTo to) throws Exception {
    switch (to) {
      case cmf:
        return MediaType.APPLICATION_XML;
      case json_schema:
        return MediaType.APPLICATION_JSON;
      case owl:
        return MediaType.TEXT_PLAIN;
      case xsd:
        return MediaType.valueOf("application/zip");
    }
    throw new Exception("Unknown transformation format");
  }

}
