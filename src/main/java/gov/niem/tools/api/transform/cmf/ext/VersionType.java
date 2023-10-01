package gov.niem.tools.api.transform.cmf.ext;

import org.mitre.niem.cmf.Model;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonRootName;

import lombok.AllArgsConstructor;
import lombok.NoArgsConstructor;
import lombok.ToString;

@JsonRootName("cmf:Version")
@NoArgsConstructor @AllArgsConstructor @ToString
public class VersionType extends Model {

  @JsonProperty("ext:VersionNumberID")
  public String versionNumber;

  @JsonProperty("ext:VersionDraftID")
  public String draft;

  @JsonProperty("ext:VersionPublishedIndicator")
  public Boolean isPublished;

  @JsonProperty("ext:VersionCategoryCode")
  public String category;

  @JsonProperty("ext:VersionURI")
  public String uri;

  @JsonProperty("ext:VersionConformanceTargetsText")
  public String conformanceTargets;

  @JsonProperty("ext:VersionExchangePatternText")
  public String exchangePattern;

  @JsonProperty("ext:VersionExchangePartnersText")
  public String exchangePartners;

  @JsonProperty("ext:VersionRevisionText")
  public String revised;

  @JsonProperty("ext:VersionStatusText")
  public String status;

}
