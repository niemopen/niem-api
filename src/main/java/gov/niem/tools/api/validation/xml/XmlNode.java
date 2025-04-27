package gov.niem.tools.api.validation.xml;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NonNull;

/**
 * A node to capture the name and style (category) of a node in an XML document.
 */
@Data
@AllArgsConstructor
public class XmlNode {

  @NonNull
  public String style = "";

  @NonNull
  public String name = "";

}
