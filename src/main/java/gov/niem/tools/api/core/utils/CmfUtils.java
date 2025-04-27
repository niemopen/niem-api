package gov.niem.tools.api.core.utils;

import gov.niem.tools.api.core.config.Config;
import gov.niem.tools.api.core.config.Config.AppMediaType;
import gov.niem.tools.api.core.exceptions.BadRequestException;

import org.mitre.niem.cmf.HasProperty;
import org.mitre.niem.cmf.Model;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.nio.file.Path;
import lombok.extern.log4j.Log4j2;
import org.json.JSONObject;
import org.mitre.niem.xsd.ModelXMLReader;
import org.mitre.niem.xsd.ModelXMLWriter;
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
  public static Model loadCmf(MultipartFile multipartFile) throws IOException, BadRequestException {

    // Throw exception if the given file is the supported version of CMF
    CmfUtils.checkVersion(multipartFile);

    // Load CMF model
    ModelXMLReader modelXmlReader = new ModelXMLReader();
    Model cmf = modelXmlReader.readXML(multipartFile.getInputStream());

    // Throw exception with error messages if CMF did not load
    if (cmf == null) {
      log.debug("Load failed: Could not parse CMF");
      modelXmlReader.getMessages().forEach(message -> log.debug(message));
      String errorMessages = String.join(", ", modelXmlReader.getMessages());
      throw new BadRequestException(errorMessages);
    }

    return cmf;

  }

  /**
   * Checks that the given file contains the URI for the currently-supported
   * version of CMF.
   */
  public static void checkVersion(MultipartFile multipartFile)
      throws IOException, BadRequestException {
    String cmfString = FileUtils.getFileText(multipartFile);
    CmfUtils.checkVersion(cmfString);
  }

  /**
   * Checks that the given file contains the URI for the currently-supported
   * version of CMF.
   */
  public static void checkVersion(File file) throws IOException {
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
   * Generate the given CMF model as a CMF XML or JSON string.
   */
  public static String generateString(Model model, AppMediaType mediaType) throws Exception {
    ModelXMLWriter modelXmlWriter = new ModelXMLWriter();
    ByteArrayOutputStream outputStream = new ByteArrayOutputStream();

    modelXmlWriter.writeXML(model, outputStream);
    String xml = outputStream.toString();

    // TODO: Resolve CMF XML error at source in CMF tool
    xml = xml.replace("cmf/0.8/\">", "cmf/0.8/\"");

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
   * @param cmf CMF model
   * @param path Directory to save the file.
   * @param filenameBase Filename without the extension.
   */
  public static File saveCmfModel(org.mitre.niem.cmf.Model cmf, Path path, String filenameBase)
      throws Exception {

    // Set up the new file in the given directory
    String filepathString = String.format("%s/%s.cmf.xml", path.toString(), filenameBase);
    File file = FileUtils.file(filepathString);
    // file.createNewFile();

    // Write the CMF model to a string
    ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
    ModelXMLWriter cmfWriter = new ModelXMLWriter();
    cmfWriter.writeXML(cmf, byteArrayOutputStream);
    String cmfString = byteArrayOutputStream.toString();

    // TODO: Resolve CMF XML error at source in CMF tool
    cmfString = cmfString.replace("cmf/0.8/\">", "cmf/0.8/\"");

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
  public static String subpropertyMin(HasProperty hasProperty) {
    return String.valueOf(hasProperty.minOccurs());
  }

  /**
   * Get subproperty max as a string with either a numeric value or "unbounded".
   */
  public static String subpropertyMax(HasProperty hasProperty) {
    if (hasProperty.maxUnbounded()) {
      return "unbounded";
    }
    return String.valueOf(hasProperty.maxOccurs());
  }

}
