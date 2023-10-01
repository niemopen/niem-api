package gov.niem.tools.api.validation.xml;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NonNull;

@Data
@AllArgsConstructor
public class XmlNode {

  @NonNull
  public String style = "";

  @NonNull
  public String name = "";

}
