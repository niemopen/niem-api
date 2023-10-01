package gov.niem.tools.api.transform.cmf.nc;

import com.fasterxml.jackson.annotation.JsonProperty;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.NoArgsConstructor;
import lombok.ToString;
import lombok.experimental.SuperBuilder;

@SuperBuilder @NoArgsConstructor @AllArgsConstructor @ToString
public class AddressType {

  @JsonProperty("nc:AddressFullText")
  public String fullText;

  @JsonProperty("nc:AddressCountry")
  @Builder.Default
  public CountryType country = new CountryType();

}
