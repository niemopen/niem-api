package gov.niem.tools.api.core.utils;

import java.io.File;
import java.nio.file.Path;
import java.nio.file.Paths;

import org.json.JSONObject;
import org.json.XML;

import com.fasterxml.jackson.core.util.DefaultPrettyPrinter;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.ObjectWriter;

public class JsonUtils {

  public static JSONObject xmlToJson(String xml) {
    JSONObject json = XML.toJSONObject(xml);
    return json;
  }

  /**
   * Write object to JSON file.
   *
   * @param object
   * @param path
   * @param filenameBase Filename without the .json extension
   */
  public static File saveObjectAsJSON(Object object, Path path, String filenameBase) throws Exception {
    ObjectMapper mapper = new ObjectMapper();
    ObjectWriter writer = mapper.writer(new DefaultPrettyPrinter());
    File file = Paths.get(String.format("%s/%s.json", path, filenameBase)).toFile();
    writer.writeValue(file, object);
    return file;
  }

  public static String toString(Object data) throws Exception {
    ObjectMapper objectMapper = new ObjectMapper();
    return objectMapper.writeValueAsString(data);
  }

}
