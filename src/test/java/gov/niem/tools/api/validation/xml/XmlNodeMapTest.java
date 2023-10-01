package gov.niem.tools.api.validation.xml;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.io.File;
import java.io.FileNotFoundException;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;

import gov.niem.tools.api.core.utils.FileUtils;
import lombok.extern.log4j.Log4j2;

@Log4j2
@SpringBootTest(classes = {XmlNodeMap.class})
public class XmlNodeMapTest {

  String curDir = XmlValidationServiceTest.curDir;

  /**
   * Test finding the applicable XML tag and component name given a line number.
   */
  @Test
  public void checkLineNumbers() throws FileNotFoundException {
    File xmlFile = FileUtils.file(curDir + "input/single/person.xml");
    XmlNodeMap nodeMap = new XmlNodeMap();
    nodeMap.load(xmlFile);

    nodeMap.map.forEach((key, node) -> {
      log.info("{} {} {}", key, node.style, node.name);
    });

    XmlNode node;

    // Check tags
    node = nodeMap.get(1);
    assertEquals("?xml", node.name);

    node = nodeMap.get(2);
    assertEquals("individual", node.name);

    node = nodeMap.get(4);
    assertEquals("address", node.name);

    node = nodeMap.get(6);
    assertEquals("city", node.name);

    // Check closing tags aren't counted
    node = nodeMap.get(7);
    assertEquals("city", node.name, "Should walk up to city and not return the closing address tag");

    node = nodeMap.get(15);
    assertEquals("city", node.name, "Should wak up to city and not return the closing individual tag");

  }

}
