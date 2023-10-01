package gov.niem.tools.api.transform.cmf.nc;

import com.fasterxml.jackson.annotation.JsonProperty;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.NoArgsConstructor;
import lombok.ToString;
import lombok.experimental.SuperBuilder;

@SuperBuilder @NoArgsConstructor @AllArgsConstructor @ToString
public class LocationType {

  @JsonProperty("nc:LocationAddress")
  @Builder.Default
  public AddressType address = new AddressType();

}
