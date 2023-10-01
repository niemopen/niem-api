package gov.niem.tools.api.core.utils;

import java.io.File;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.dataformat.csv.CsvMapper;
import com.fasterxml.jackson.dataformat.csv.CsvSchema;
import com.fasterxml.jackson.dataformat.csv.CsvSchema.Builder;

public class CsvUtils {

  /**
   * Save the given object array formatted as a CSV with column headers to the given file.
   */
  public static void save(File file, Object[] data) throws Exception {
    JsonNode jsonTree = CsvUtils.getJsonNode(data);
    CsvSchema csvSchema = CsvUtils.build(data, jsonTree);
    CsvMapper csvMapper = new CsvMapper();
    csvMapper
    .writerFor(JsonNode.class)
    .with(csvSchema)
    .writeValue(file, jsonTree);
  }

  /**
   * Return the given object array formatted as a CSV with column headers as a string.
   */
  public static String toString(Object[] data) throws Exception {
    JsonNode jsonTree = CsvUtils.getJsonNode(data);
    CsvSchema csvSchema = CsvUtils.build(data, jsonTree);
    CsvMapper csvMapper = new CsvMapper();
    return csvMapper
    .writer(csvSchema)
    .writeValueAsString(jsonTree);
  }

  private static CsvSchema build(Object[] data, JsonNode jsonTree) throws Exception {

    Builder csvSchemaBuilder = CsvSchema.builder();

    // Set up column names from the JSON keys
    JsonNode firstObject = jsonTree.elements().next();
    firstObject.fieldNames().forEachRemaining(fieldName -> {
      csvSchemaBuilder.addColumn(fieldName);
    });

    return csvSchemaBuilder.build().withHeader();
  }

  private static JsonNode getJsonNode(Object[] data) throws Exception {
    String jsonString = JsonUtils.toString(data);
    return new ObjectMapper().readTree(jsonString);
  }

}
