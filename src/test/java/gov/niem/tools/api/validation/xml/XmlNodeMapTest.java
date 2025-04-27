package gov.niem.tools.api.validation.xml;

import gov.niem.tools.api.TestUtils;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.io.File;
import lombok.extern.log4j.Log4j2;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;

/**
 * Tests mapping of XML line numbers to element names.
 */
@Log4j2
@SpringBootTest(classes = {XmlNodeMap.class})
public class XmlNodeMapTest {

  /**
   * Test finding the applicable XML tag and component name given a line number.
   */
  @Test
  public void checkLineNumbers() throws Exception {
    File xmlFile = TestUtils.getResourcesFile("validation/xml/single/person.xml");
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

    String message = "Should walk up to city and not return the closing address tag";

    // Check closing tags aren't counted
    node = nodeMap.get(7);
    assertEquals("city", node.name, message);

    node = nodeMap.get(15);
    assertEquals("city", node.name, message);

  }

}
