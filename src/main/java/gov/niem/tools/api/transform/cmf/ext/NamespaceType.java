package gov.niem.tools.api.transform.cmf.ext;

import org.mitre.niem.cmf.Namespace;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonRootName;

import lombok.AllArgsConstructor;
import lombok.NoArgsConstructor;
import lombok.ToString;

@JsonRootName("cmf:Namespace")
@NoArgsConstructor @AllArgsConstructor @ToString
public class NamespaceType extends Namespace {

  @JsonProperty("ext:NamespaceFileName")
  public String filename;

  @JsonProperty("ext:NamespaceFilePathID")
  public String filepath;

}
