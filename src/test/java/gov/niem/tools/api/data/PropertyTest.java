package gov.niem.tools.api.data;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import gov.niem.tools.api.Application;
import gov.niem.tools.api.core.config.Config;
import gov.niem.tools.api.db.property.Property;
import gov.niem.tools.api.db.property.PropertyService;
import gov.niem.tools.api.db.type.TypeService;
import jakarta.transaction.Transactional;

@SpringBootTest(classes={Application.class, Config.class, PropertyService.class, Property.class})
public class PropertyTest {

  @Autowired
  PropertyService propertyService;

  @Autowired
  TypeService typeService;

  @Test
  @Transactional
  public void testPropertyObject() {
    Property property = propertyService.findOne("niem", "model", "6.0", "nc:Person");
    assertEquals("Person", property.getName());
    assertEquals("nc", property.getPrefix());
    assertEquals("nc:PersonType", property.getType().qname);
    assertTrue(property.isElement());
    assertFalse(property.isAttribute());
    assertFalse(property.isAbstract());
    assertFalse(property.isDeprecated());
    assertNull(property.getGroup());
  }

  @Test
  @Transactional
  public void testPropertySubstitutable() {
    Property property = propertyService.findOne("niem", "model", "6.0", "nc:AddressCategoryCode");
    assertEquals("AddressCategoryCode", property.getName());
    assertEquals("nc:AddressCategoryAbstract", property.getGroup().qname);
    assertEquals("nc:AddressCategoryCodeType", property.getType().qname);
  }

  @Test
  @Transactional
  public void testPropertyAbstract() {
    Property property = propertyService.findOne("niem", "model", "6.0", "nc:AddressCategoryAbstract");
    assertEquals("AddressCategoryAbstract", property.getName());
    assertNull(property.getGroup());
    assertNull(property.getType());
    assertTrue(property.isAbstract());
  }

  @Test
  @Transactional
  public void testPropertyAttribute() {
    Property property = propertyService.findOne("niem", "model", "6.0", "nc:AddressCategoryCode");
    assertEquals("AddressCategoryCode", property.getName());
    assertEquals("nc:AddressCategoryAbstract", property.getGroup().qname);
    assertEquals("nc:AddressCategoryCodeType", property.getType().qname);
  }

}
