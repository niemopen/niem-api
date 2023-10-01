package gov.niem.tools.api.transform.cmf.nc;

import com.fasterxml.jackson.annotation.JsonProperty;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.NoArgsConstructor;
import lombok.ToString;
import lombok.experimental.SuperBuilder;

@SuperBuilder @NoArgsConstructor @AllArgsConstructor @ToString
public class TelephoneNumberType {

  @JsonProperty("nc:FullTelephoneNumber")
  @Builder.Default
  public FullTelephoneNumberType fullNumber = new FullTelephoneNumberType();

}
