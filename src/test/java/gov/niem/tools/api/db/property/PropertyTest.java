package gov.niem.tools.api.db.property;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.util.List;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

import gov.niem.tools.api.db.EntityTest;
import gov.niem.tools.api.db.TestData;
import gov.niem.tools.api.db.model.Model;
import gov.niem.tools.api.db.namespace.Namespace;
import gov.niem.tools.api.db.steward.Steward;
import gov.niem.tools.api.db.version.Version;

/**
 * @todo Add type to Property testing
 * @todo Add substitution group to Property testing
 * @todo Fix skipped tests
 */
@ActiveProfiles("test")
@SpringBootTest
public class PropertyTest extends EntityTest<Property> {

  Steward nmo;
  Model niem;
  Version niem_v1;
  Namespace nc;
  Namespace em;

  Property nc_Person;
  Property nc_PersonFullName;
  Property em_Person;

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

    niem_v1 = hub.versions.add(TestData.Versions.major(niem, "1.0"));
    niem_v1.setNiemVersion(niem_v1);

    nc = hub.namespaces.add(TestData.Namespaces.core(niem_v1));
    em = hub.namespaces.add(TestData.Namespaces.domain(niem_v1, "em"));

    // Initialize data for properties
    nc_Person = TestData.Properties.person(nc);
    em_Person = TestData.Properties.person(em);
    nc_PersonFullName = TestData.Properties.personFullName(nc);
  }

  @Override
  protected void loadObjects() throws Exception {
    service().add(nc_Person);
    service().add(em_Person);
    service().add(nc_PersonFullName);
  }

  @Override
  @Test
  public void databaseAddTest() {
    this.add(nc_Person);
    this.add(nc_PersonFullName);
    this.add(em_Person);
  }

  @Override
  @Test
  public void databaseAddDuplicateTest() {
    this.addDuplicate(nc_Person);
  }

  @Override
  @Test
  @Disabled
  public void databaseEditTest() throws Exception {
    this.edit(nc_Person, "definition", "A real or imaginary human being");
  }

  @Override
  @Test
  @Disabled
  public void databaseDeleteTest() throws Exception {
    this.deleteNonCascading(hub.properties, nc_Person, nc_PersonFullName, hub.namespaces);
  }

  @Override
  @Test
  @Disabled
  public void databaseFindOneTest() throws Exception {
    this.loadObjects();
    Property result = service().findOne(nc_Person);
    assertEquals(nc_Person.getQname(), result.getQname());
  }

  @Test
  public void databaseFindAllTest() throws Exception {
    this.loadObjects();
    List<Property> results = service().findByVersion(nc_Person.getStewardKey(), nc_Person.getModelKey(), nc_Person.getVersionNumber());
    assertEquals(3, results.size());
  }

  @Override
  @Test
  @Disabled
  public void objectLabelTest() throws Exception {
    this.loadObjects();
    Property result = service().findOne(nc_Person);
    assertEquals("nmo/niem/1.0/nc:Person", result.getFullIdentifier());
  }

  @Override
  @Test
  public void objectSerializationTest() throws Exception {
    serializeObject(nc_Person);
    serializeObject(nc_PersonFullName);
    serializeObject(em_Person);
  }

}
