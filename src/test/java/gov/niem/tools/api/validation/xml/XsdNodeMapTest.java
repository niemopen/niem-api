package gov.niem.tools.api.validation.xml;

import gov.niem.tools.api.TestUtils;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;

import java.io.File;
import lombok.extern.log4j.Log4j2;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;

/**
 * Tests mapping of XSD line numbers to component names and styles.
 */
@Log4j2
@SpringBootTest(classes = {XsdNodeMap.class})
public class XsdNodeMapTest {

  /**
   * Test finding the applicable XML tag and component name given a line number.
   */
  @Test
  public void checkLineNumbers() throws Exception {
    File xsdFile = TestUtils.getResourcesFile("validation/xml/single/person.xsd");
    XsdNodeMap nodeMap = new XsdNodeMap();
    nodeMap.load(xsdFile);

    nodeMap.map.forEach((key, node) -> {
      log.info("{} {} {}", key, node.style, node.name);
    });

    XmlNode node;

    // Check no matches near beginning of file
    node = nodeMap.get(2);
    assertNull(node, "No components defined at or before line 2");

    // Check XSD component
    node = nodeMap.get(3);
    assertEquals("xs:element", node.style, "Line 3 should have tag name xs:element");

    // Check component name
    node = nodeMap.get(3);
    assertEquals("individual", node.name, "Line 3 should have tag name individual");

    node = nodeMap.get(4);
    assertEquals("individual", node.name, "Line 4 should walk up to tag name individual");

    node = nodeMap.get(11);
    assertEquals("city", node.name, "Line 11 should have tag name city");

    node = nodeMap.get(15);
    assertEquals("city", node.name, "Line 15 should walk up to tag name city");

    node = nodeMap.get(50);
    assertEquals("city", node.name, "Line 50 should walk up to tag name city");

  }

}
