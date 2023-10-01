package gov.niem.tools.api.transform.cmf.nc;

import com.fasterxml.jackson.annotation.JsonProperty;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.NoArgsConstructor;
import lombok.ToString;
import lombok.experimental.SuperBuilder;

@SuperBuilder @NoArgsConstructor @AllArgsConstructor @ToString
public class OrganizationType {

  @JsonProperty("nc:OrganizationName")
  public String name;

  @JsonProperty("nc:OrganizationDescription")
  public String description;

  @JsonProperty("nc:OrganizationLocation")
  @Builder.Default
  public LocationType location = new LocationType();

  @JsonProperty("nc:OrganizationPrimaryContactInformation")
  @Builder.Default
  public ContactInformationType contact = new ContactInformationType();

  @JsonProperty("nc:OrganizationUnitName")
  public String unit;

  @JsonProperty("nc:OrganizationSubUnitName")
  public String subunit;

}
