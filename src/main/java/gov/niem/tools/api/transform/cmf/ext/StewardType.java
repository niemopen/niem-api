package gov.niem.tools.api.transform.cmf.ext;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonRootName;
import com.fasterxml.jackson.databind.annotation.JsonAppend;

import gov.niem.tools.api.db.steward.Steward;
import gov.niem.tools.api.transform.cmf.nc.OrganizationType;
import lombok.AllArgsConstructor;
import lombok.NoArgsConstructor;
import lombok.ToString;
import lombok.experimental.SuperBuilder;

@SuperBuilder @NoArgsConstructor @AllArgsConstructor @ToString
@JsonAppend.Attr(propName = "xmlns:nc", value = "http://release.niem.gov")
@JsonRootName("ext:Steward")
public class StewardType extends OrganizationType {

  // @JacksonXmlProperty(isAttribute = true)
  // @JsonProperty("xmlns:ext")
  // private final String ext = "http://tools.niem.gov/api/v2/cmf/extension/1.0/";

  // @JacksonXmlProperty(isAttribute = true)
  // @JsonProperty("xmlns:nc")
  // private final String nc = "http://release.niem.gov/niem/niem-core/5.0/";

  // @JacksonXmlProperty(isAttribute = true)
  // @JsonProperty("xmlns:genc")
  // private final String genc = "http://release.niem.gov/niem/codes/genc/5.0/";

  @JsonProperty("ext:OrganizationShortName")
  public String shortName;

  @JsonProperty("ext:StewardKey")
  public String stewardKey;

  @JsonProperty("ext:StewardCategoryCode")
  public Steward.Category category;

}
