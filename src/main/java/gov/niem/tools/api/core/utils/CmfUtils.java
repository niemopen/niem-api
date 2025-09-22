package gov.niem.tools.api.core.utils;

import gov.niem.tools.api.core.config.Config;
import gov.niem.tools.api.core.config.Config.AppMediaType;
import gov.niem.tools.api.core.exceptions.BadRequestException;

import org.mitre.niem.cmf.Model;
import org.mitre.niem.cmf.ModelXMLReader;
import org.mitre.niem.cmf.ModelXMLWriter;
import org.mitre.niem.cmf.PropertyAssociation;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.nio.charset.StandardCharsets;
import java.nio.file.Path;
import lombok.extern.log4j.Log4j2;
import org.json.JSONObject;
import org.springframework.web.multipart.MultipartFile;

/**
 * CMF-related utility functions.
 */
@Log4j2
public class CmfUtils {

  /**
   * Checks that the given file contains the URI for the currently-supported
   * version of CMF, and if so, loads it into a CMF Model object.
   */
  public static Model loadCmf(MultipartFile multipartFile) throws Exception {

    // Throw exception if the given file is the supported version of CMF
    CmfUtils.checkVersion(multipartFile);

    // Load CMF model
    ModelXMLReader modelXmlReader = new ModelXMLReader();
    Path tmpCmfPath = FileUtils.saveFile(multipartFile);
    final Model[] cmfArray = {null};  // Wrapped so it can be assigned from captureLog below
    String logString = AppUtils.captureLog(() -> {
      cmfArray[0] = modelXmlReader.readFiles(tmpCmfPath.toFile());
    });

    Model cmf = cmfArray[0];

    // Throw exception with error messages if CMF did not load
    if (cmf == null) {
      log.debug("Load failed: Could not parse CMF");
      log.debug(logString);

      String[] errorMessages = logString.split("\n");
      String finalErrorMessage = "";

      for (String errorMessage : errorMessages) {
        finalErrorMessage += "Line " + errorMessage.split(".cmf.xml:")[1].trim() + ". ";
      }

      throw new BadRequestException(finalErrorMessage);
    }

    return cmf;

  }

  /**
   * Checks that the given file contains the URI for the currently-supported
   * version of CMF.
   */
  public static void checkVersion(MultipartFile multipartFile)
      throws IOException, BadRequestException {
    CmfUtils.checkFileExtension(multipartFile);
    String cmfString = FileUtils.getFileText(multipartFile);
    CmfUtils.checkVersion(cmfString);
  }

  /**
   * Checks that the given file contains the URI for the currently-supported
   * version of CMF.
   */
  public static void checkVersion(File file) throws IOException {
    CmfUtils.checkFileExtension(file);
    String cmfString = FileUtils.getFileText(file.toPath());
    CmfUtils.checkVersion(cmfString);
  }

  /**
   * Checks that the given CMF string contains the URI for the currently-supported
   * version of CMF.
   */
  public static void checkVersion(String cmfString) throws BadRequestException {
    if (!cmfString.contains(Config.cmfUri)) {
      String errorMessage = String.format("Only CMF version %s is currently supported",
          Config.cmfVersion);
      throw new BadRequestException(errorMessage);
    }
  }

  /**
   * Check that the file extension of the given file has a valid CMF extension.
   */
  public static void checkFileExtension(MultipartFile multipartFile) {
    String fileExtension = FileUtils.getFileExtension(multipartFile);
    checkFileExtension(fileExtension);
  }

  /**
   * Check that the file extension of the given file has a valid CMF extension.
   */
  public static void checkFileExtension(File file) {
    String fileExtension = FileUtils.getFileExtension(file.toPath());
    checkFileExtension(fileExtension);
  }

  /**
   * Check that the given CMF file has a valid file extension.
   */
  public static void checkFileExtension(String fileExtension) throws BadRequestException {
    if (!fileExtension.equals("xml") && !fileExtension.equals("cmf.xml")) {
      String message = String.format("[.%s] is not an accepted file extension for CMF files (.cmf or .cmf.xml)", fileExtension);
      throw new BadRequestException(message);
    }
  }

  /**
   * Generate the given CMF model as a CMF XML or JSON string.
   */
  public static String generateString(Model model, AppMediaType mediaType) throws Exception {
    ModelXMLWriter modelXmlWriter = new ModelXMLWriter();
    ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
    OutputStreamWriter outputStreamWriter = new OutputStreamWriter(outputStream,
        StandardCharsets.UTF_8);

    modelXmlWriter.writeXML(model, outputStreamWriter);
    outputStreamWriter.flush();
    outputStreamWriter.close();
    String xml = outputStream.toString();

    // TODO: Resolve CMF XML error at source in CMF tool
    xml = xml.replace("cmf/1.0/\">", "cmf/1.0/\"");

    if (mediaType == AppMediaType.json) {
      JSONObject json = JsonUtils.xmlToJson(xml);
      refactorContext(json);
      return json.toString(2);
    }

    return xml;
  }

  /**
   * Generate the given CMF model as a CMF XML string.
   */
  public static String generateString(Model model) throws Exception {
    return generateString(model, AppMediaType.xml);
  }

  /**
   * Write a CMF model to an XML file.
   *
   * @param cmfModel CMF model
   * @param path Directory to save the file.
   * @param filenameBase Filename without the extension.
   */
  public static File saveCmfModel(org.mitre.niem.cmf.Model cmfModel, Path path, String filenameBase)
      throws Exception {

    // Set up the new file in the given directory
    String filepathString = String.format("%s/%s.cmf.xml", path.toString(), filenameBase);
    File file = FileUtils.file(filepathString);
    // file.createNewFile();

    // Write the CMF model to a string
    String cmfString = CmfUtils.generateString(cmfModel);

    // Write the CMF string to a file
    FileUtils.saveFile(file.toPath(), cmfString.getBytes());

    return file;

  }

  private static void refactorContext(JSONObject json) {
    JSONObject model = json.getJSONObject("Model");
    JSONObject context = new JSONObject();

    context.put("xmlns", model.getString("xmlns"));
    context.put("xmlns:cmf", model.getString("xmlns:cmf"));
    context.put("xmlns:xsi", model.getString("xmlns:xsi"));
    context.put("xmlns:structures", model.getString("xmlns:structures"));

    model.remove("xmlns");
    model.remove("xmlns:cmf");
    model.remove("xmlns:xsi");
    model.remove("xmlns:structures");

    model.put("@context", context);
  }

  /**
   * Get the subproperty min as a string.
   */
  public static String subpropertyMin(PropertyAssociation propertyAssociation) {
    return String.valueOf(propertyAssociation.minOccurs());
  }

  /**
   * Get subproperty max as a string with either a numeric value or "unbounded".
   */
  public static String subpropertyMax(PropertyAssociation propertyAssociation) {
    if (propertyAssociation.isMaxUnbounded()) {
      return "unbounded";
    }
    return String.valueOf(propertyAssociation.maxOccurs());
  }

  /**
   * Returns the augmentation type name for the given type name being augmented.
   *
   * <p>For example, returns "PersonAugmentationType" when given "PersonType".
   */
  public static String getAugmentationClassName(String augmentedTypeName) {
    return augmentedTypeName.replace("Type", "AugmentationType");
  }

  /**
   * Returns the qualified augmentation type name with the given namespace prefix as is, and
   * the given augmentedTypeName converted to an augmentation type name.
   *
   * <p>For example, given prefix "j" and type name "PersonType", returns
   * "j:PersonAugmentationType".
   */
  public static String getAugmentationClassQname(String augmentationPrefix,
      String augmentedTypeName) {
    return augmentationPrefix + ":" + CmfUtils.getAugmentationClassName(augmentedTypeName);
  }

}
