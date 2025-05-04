package gov.niem.tools.api.db.namespace;

import gov.niem.tools.api.db.EntityTest;
import gov.niem.tools.api.db.TestData;
import gov.niem.tools.api.db.model.Model;
import gov.niem.tools.api.db.steward.Steward;
import gov.niem.tools.api.db.version.Version;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

/**
 * Test database operations for namespaces.
 */
@ActiveProfiles("test")
@SpringBootTest
public class NamespaceTest extends EntityTest<Namespace> {

  Steward nmo;
  Model niem;

  Version niemV1;
  Version niemV2;

  Namespace ncV1;
  Namespace ncV2;

  Namespace emV1;

  @Override
  protected NamespaceService service() {
    return this.hub.namespaces;
  }

  @BeforeEach
  @Override
  public void init() throws Exception {
    // Add dependencies to the database
    nmo = hub.stewards.add(TestData.Stewards.nmo());
    niem = hub.models.add(TestData.Models.niem(nmo));
    niemV1 = hub.versions.add(TestData.Versions.major(niem, "1.0"));
    niemV2 = hub.versions.add(TestData.Versions.minor(niem, "1.1"));

    niemV1.setNiemVersion(niemV1);
    niemV2.setNiemVersion(niemV2);

    // Initialize data for namespaces
    ncV1 = TestData.Namespaces.core(niemV1);
    ncV2 = TestData.Namespaces.core(niemV2);
    emV1 = TestData.Namespaces.domain(niemV1, "em");
  }

  @Override
  protected void loadObjects() throws Exception {
    ncV1 = hub.namespaces.add(ncV1);
    ncV2 = hub.namespaces.add(ncV2);
    emV1 = hub.namespaces.add(emV1);
  }

  @Override
  @Test
  public void databaseAddTest() {
    this.add(ncV1);
    this.add(ncV2);
    this.add(emV1);
  }

  @Override
  @Test
  public void databaseAddDuplicateTest() {
    this.addDuplicate(ncV1);
  }

  @Override
  @Test
  public void databaseEditTest() throws Exception {
    this.edit(ncV1, "definition", "Core");
  }

  @Override
  @Test
  @Disabled
  public void databaseDeleteTest() throws Exception {
    this.deleteNonCascading(hub.namespaces, ncV1, ncV2, hub.versions);
  }

  @Override
  @Test
  public void databaseFindOneTest() throws Exception {
    this.loadObjects();
    Namespace result = hub.namespaces.findOne(ncV2.getStewardKey(), ncV2.getModelKey(),
        ncV2.getVersionNumber(), ncV2.getPrefix());
    assertEquals(ncV2.getVersionNumber(), result.getVersionNumber());
    assertEquals(ncV2.getPrefix(), result.getPrefix());
  }

  @Test
  public void databaseFindAllTest() throws Exception {
    this.loadObjects();
    List<Namespace> results = service().findAll();
    assertEquals(3, results.size());
  }

  @Override
  @Test
  public void objectLabelTest() throws Exception {
    this.loadObjects();
    Namespace result = service().findOne(ncV1);
    assertEquals("nmo/niem/1.0/nc", result.getIdLabel());
  }

  @Override
  @Test
  public void objectSerializationTest() throws Exception {
    serializeObject(ncV1);
    serializeObject(ncV2);
    serializeObject(emV1);
  }

}
