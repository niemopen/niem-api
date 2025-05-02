package gov.niem.tools.api.db.property;

import gov.niem.tools.api.db.EntityTest;
import gov.niem.tools.api.db.TestData;
import gov.niem.tools.api.db.model.Model;
import gov.niem.tools.api.db.namespace.Namespace;
import gov.niem.tools.api.db.steward.Steward;
import gov.niem.tools.api.db.version.Version;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.test.context.ActiveProfiles;

/**
 * Test database operations for properties.
 *
 * @todo Add type to Property testing
 * @todo Add substitution group to Property testing
 * @todo Fix skipped tests
 */
@ActiveProfiles("test")
@SpringBootTest
public class PropertyTest extends EntityTest<Property> {

  Steward nmo;
  Model niem;
  Version niemV1;
  Namespace nc;
  Namespace em;

  Property ncPerson;
  Property ncPersonFullName;
  Property emPerson;

  @Override
  protected PropertyService service() {
    return this.hub.properties;
  }

  @BeforeEach
  @Override
  public void init() throws Exception {
    // Add dependencies to the database
    nmo = hub.stewards.add(TestData.Stewards.nmo());
    niem = hub.models.add(TestData.Models.niem(nmo));

    niemV1 = hub.versions.add(TestData.Versions.major(niem, "1.0"));
    niemV1.setNiemVersion(niemV1);

    nc = hub.namespaces.add(TestData.Namespaces.core(niemV1));
    em = hub.namespaces.add(TestData.Namespaces.domain(niemV1, "em"));

    // Initialize data for properties
    ncPerson = TestData.Properties.person(nc);
    emPerson = TestData.Properties.person(em);
    ncPersonFullName = TestData.Properties.personFullName(nc);
  }

  @Override
  protected void loadObjects() throws Exception {
    service().add(ncPerson);
    service().add(emPerson);
    service().add(ncPersonFullName);
  }

  @Override
  @Test
  public void databaseAddTest() {
    this.add(ncPerson);
    this.add(ncPersonFullName);
    this.add(emPerson);
  }

  @Override
  @Test
  public void databaseAddDuplicateTest() {
    this.addDuplicate(ncPerson);
  }

  @Override
  @Test
  @Disabled
  public void databaseEditTest() throws Exception {
    this.edit(ncPerson, "definition", "A real or imaginary human being");
  }

  @Override
  @Test
  @Disabled
  public void databaseDeleteTest() throws Exception {
    this.deleteNonCascading(hub.properties, ncPerson, ncPersonFullName, hub.namespaces);
  }

  @Override
  @Test
  @Disabled
  public void databaseFindOneTest() throws Exception {
    this.loadObjects();
    Property result = service().findOne(ncPerson);
    assertEquals(ncPerson.getQname(), result.getQname());
  }

  @Test
  public void databaseFindAllTest() throws Exception {
    this.loadObjects();
    Pageable pageable = PageRequest.ofSize(20);
    Page<Property> results = service().findByVersion(ncPerson.getStewardKey(),
        ncPerson.getModelKey(), ncPerson.getVersionNumber(), pageable);
    List<Property> properties = results.getContent();
    assertEquals(3, properties.size());
  }

  @Override
  @Test
  @Disabled
  public void objectLabelTest() throws Exception {
    this.loadObjects();
    Property result = service().findOne(ncPerson);
    assertEquals("nmo/niem/1.0/nc:Person", result.getFullIdentifier());
  }

  @Override
  @Test
  public void objectSerializationTest() throws Exception {
    serializeObject(ncPerson);
    serializeObject(ncPersonFullName);
    serializeObject(emPerson);
  }

}
