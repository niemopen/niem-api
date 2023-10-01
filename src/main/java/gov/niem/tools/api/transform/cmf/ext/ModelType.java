package gov.niem.tools.api.transform.cmf.ext;

import org.mitre.niem.cmf.Model;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonRootName;

import lombok.AllArgsConstructor;
import lombok.NoArgsConstructor;
import lombok.ToString;

@JsonRootName("cmf:Model")
@NoArgsConstructor @AllArgsConstructor @ToString
public class ModelType extends Model {

  @JsonProperty("ext:ModelKey")
  public String modelKey;

  @JsonProperty("ext:ModelShortName")
  public String shortName;

  @JsonProperty("ext:ModelName")
  public String longName;

  @JsonProperty("ext:ModelCategoryCode")
  public String category;

  @JsonProperty("ext:ModelDescriptionText")
  public String description;

  @JsonProperty("ext:ModelWebsiteURI")
  public String website;

  @JsonProperty("ext:ModelRepoURI")
  public String repo;

  @JsonProperty("ext:ModelKeywordText")
  public String keywords;

  @JsonProperty("ext:ModelSubjectText")
  public String subjects;

  @JsonProperty("ext:ModelPurposeText")
  public String purpose;

  @JsonProperty("ext:ModelDeveloperText")
  public String developer;

}
