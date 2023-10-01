package gov.niem.tools.api.transform.cmf.nc;

import com.fasterxml.jackson.annotation.JsonProperty;

import lombok.AllArgsConstructor;
import lombok.NoArgsConstructor;
import lombok.ToString;
import lombok.experimental.SuperBuilder;

@SuperBuilder @NoArgsConstructor @AllArgsConstructor @ToString
public class FullTelephoneNumberType {

  @JsonProperty("nc:TelephoneNumberFullID")
  public String number;

}
