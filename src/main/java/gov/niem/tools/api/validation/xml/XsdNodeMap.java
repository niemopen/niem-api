package gov.niem.tools.api.validation.xml;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.util.Objects;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.commons.io.IOUtils;
import org.apache.commons.io.LineIterator;

import lombok.extern.log4j.Log4j2;

@Log4j2
public class XsdNodeMap extends XmlNodeMap {

  // Group 1: Optional namespace prefix of the schema component
  // Group 2: element or attribute or complexType or simpleType
  // Group 3: Name of the component
  String regex = "<(xs:|xsd:)?(element|attribute|complexType|simpleType).*name=[\"']([A-Za-z0-9-_]*)";
  Pattern pattern = Pattern.compile(regex);

  public void load(File file) throws FileNotFoundException {

    BufferedReader bufferedReader = new BufferedReader(new FileReader(file));
    LineIterator lineIterator = IOUtils.lineIterator(bufferedReader);

    for (int currentLineNumber = 0; lineIterator.hasNext(); currentLineNumber++) {
      String lineText = (String) lineIterator.next();

      Matcher matcher = pattern.matcher(lineText);

      if (matcher.find()) {
        // Replace potential null prefix with empty string.
        String prefix = Objects.toString(matcher.group(1), "");
        XmlNode node = new XmlNode(prefix + matcher.group(2), matcher.group(3));
        log.debug("{} {} {}", file.getName(), Integer.toString(currentLineNumber), node);
        this.map.put(currentLineNumber+1, node);

      }

    }

  }

  public XsdNodeMap create() {
    return new XsdNodeMap();
  }

}
