package gov.niem.tools.api.validation.xml;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.util.HashMap;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.commons.io.IOUtils;
import org.apache.commons.io.LineIterator;

import lombok.extern.log4j.Log4j2;

@Log4j2
public class XmlNodeMap {

  public HashMap<Integer, XmlNode> map = new HashMap<>();

  // Group 1: Qualified or unqualified tag name
  String regex = "<([^ >\\/]*)";
  Pattern pattern = Pattern.compile(regex);

  public void load(File file) throws FileNotFoundException {

    BufferedReader bufferedReader = new BufferedReader(new FileReader(file));
    LineIterator lineIterator = IOUtils.lineIterator(bufferedReader);

    int currentLineNumber;

    for (int fileLineNumber = 0; lineIterator.hasNext(); fileLineNumber++) {
      // Adjust for line numbering starting at 0.
      currentLineNumber = fileLineNumber + 1;

      String lineText = (String) lineIterator.next();
      Matcher matcher = pattern.matcher(lineText);

      if (matcher.find()) {
        XmlNode node = new XmlNode("element", matcher.group(1));
        if (!node.name.equals("")) {
          // Put tag in hashmap if not a closing tag
          log.debug("{} {} {}", file.getName(), Integer.toString(currentLineNumber), node);
          this.map.put(currentLineNumber, node);
        }
      }

    }

  }

  /**
   * Get the XML node defined at or before the given line number.
   */
  public XmlNode get(Integer lineNumber) {
    int currentLineNumber = lineNumber;
    XmlNode node = null;

    while (currentLineNumber > 0) {
      node = this.map.get(currentLineNumber);

      if (node != null) {
        return node;
      }
      // Try the previous line
      currentLineNumber--;
    }

    // Not found
    return null;
  }

  public XmlNodeMap create() {
    return new XmlNodeMap();
  }

}
