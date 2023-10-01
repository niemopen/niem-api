package gov.niem.tools.api.transform.cmf.nc;

import com.fasterxml.jackson.annotation.JsonProperty;

import gov.niem.tools.api.transform.cmf.genc.CountryAlpha3CodeType;
import lombok.AllArgsConstructor;
import lombok.NoArgsConstructor;
import lombok.ToString;
import lombok.experimental.SuperBuilder;

@SuperBuilder @NoArgsConstructor @AllArgsConstructor @ToString
public class CountryType {

  @JsonProperty("genc:CountryAlpha3Code")
  public CountryAlpha3CodeType alpha3Code;

}
