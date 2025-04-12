package gov.niem.tools.api.core.utils;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.nio.file.Path;

import org.json.JSONObject;
import org.mitre.niem.cmf.HasProperty;
import org.mitre.niem.cmf.Model;
import org.mitre.niem.xsd.ModelXMLWriter;

import gov.niem.tools.api.core.config.Config.AppMediaType;

public class CmfUtils {

  /**
   * Generate CMF model as an CMF XML or JSON string
   */
  public static String generateString(Model model, AppMediaType mediaType) throws Exception {
    ModelXMLWriter modelXMLWriter = new ModelXMLWriter();
    ByteArrayOutputStream outputStream = new ByteArrayOutputStream();

    modelXMLWriter.writeXML(model, outputStream);
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
   * Generate CMF model as an CMF XML string
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
  public static File saveCmfModel(org.mitre.niem.cmf.Model cmf, Path path, String filenameBase) throws Exception {

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
   * Get subproperty min as a string
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
