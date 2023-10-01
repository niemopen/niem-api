package gov.niem.tools.api.transform.cmf.nc;

import com.fasterxml.jackson.annotation.JsonProperty;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.NoArgsConstructor;
import lombok.ToString;
import lombok.experimental.SuperBuilder;

@SuperBuilder @NoArgsConstructor @AllArgsConstructor @ToString
public class ContactInformationType {

  @JsonProperty("nc:ContactWebsiteURI")
  public String website;

  @JsonProperty("nc:ContactEmailID")
  public String email;

  @JsonProperty("nc:ContactMailingAddress")
  @Builder.Default
  public AddressType address = new AddressType();

  @JsonProperty("nc:ContactTelephoneNumber")
  @Builder.Default
  public TelephoneNumberType phone = new TelephoneNumberType();

}
