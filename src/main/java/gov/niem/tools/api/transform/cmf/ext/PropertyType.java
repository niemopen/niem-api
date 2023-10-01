package gov.niem.tools.api.transform.cmf.ext;

import org.mitre.niem.cmf.Property;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonRootName;

import lombok.AllArgsConstructor;
import lombok.NoArgsConstructor;
import lombok.ToString;

@JsonRootName("cmf:Property")
@NoArgsConstructor @AllArgsConstructor @ToString
public class PropertyType extends Property {

  @JsonProperty("ext:PropertyFileName")
  public String filename;

}
