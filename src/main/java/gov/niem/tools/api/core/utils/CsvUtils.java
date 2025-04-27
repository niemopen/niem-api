package gov.niem.tools.api.core.utils;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.dataformat.csv.CsvMapper;
import com.fasterxml.jackson.dataformat.csv.CsvSchema;
import com.fasterxml.jackson.dataformat.csv.CsvSchema.Builder;
import java.io.File;

/**
 * CSV-related utility functions.
 */
public class CsvUtils {

  /**
   * Save the given object array formatted as a CSV with column headers to the given file.
   */
  public static void save(File file, Object[] data) throws Exception {
    JsonNode jsonTree = CsvUtils.getJsonNode(data);
    CsvSchema csvSchema = CsvUtils.schemaWithData(data, jsonTree);
    CsvMapper csvMapper = new CsvMapper();
    csvMapper
        .writerFor(JsonNode.class)
        .with(csvSchema)
        .writeValue(file, jsonTree);
  }

  /**
   * Return the given object array formatted as a CSV with column headers as a string.
   */
  public static String toString(Object[] data, String[] columns) throws Exception {
    JsonNode jsonTree = CsvUtils.getJsonNode(data);
    CsvSchema csvSchema = data.length > 0
        ? CsvUtils.schemaWithData(data, jsonTree)
        : CsvUtils.schemaWithHeaderOnly(columns);
    CsvMapper csvMapper = new CsvMapper();
    return csvMapper
    .writer(csvSchema)
    .writeValueAsString(jsonTree);
  }

  private static CsvSchema schemaWithData(Object[] data, JsonNode jsonTree) throws Exception {

    Builder csvSchemaBuilder = CsvSchema.builder();

    // Set up column names from the JSON keys
    JsonNode firstObject = jsonTree.elements().next();
    firstObject.fieldNames().forEachRemaining(fieldName -> {
      csvSchemaBuilder.addColumn(fieldName);
    });

    return csvSchemaBuilder.build().withHeader();
  }

  private static CsvSchema schemaWithHeaderOnly(String[] columns) {
    Builder builder = CsvSchema.builder();

    for (String column : columns) {
      builder.addColumn(column);
    }

    return builder.setUseHeader(true).build();
  }

  private static JsonNode getJsonNode(Object[] data) throws Exception {
    String jsonString = JsonUtils.toString(data);
    return new ObjectMapper().readTree(jsonString);
  }

}
