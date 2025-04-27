package gov.niem.tools.api.db.version;

import gov.niem.tools.api.db.EntityTest;
import gov.niem.tools.api.db.TestData;
import gov.niem.tools.api.db.model.Model;
import gov.niem.tools.api.db.steward.Steward;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

/**
 * Test database operations for versions.
 */
@ActiveProfiles("test")
@SpringBootTest
public class VersionTest extends EntityTest<Version> {

  Steward nmo;
  Steward acme;

  Model niem;
  Model crashNmo;
  Model crashAcme;

  Version niemV1;
  Version niemV2;

  Version crashAcmeV1;
  Version crashAcmeV2;

  @Override
  protected VersionService service() {
    return this.hub.versions;
  }

  @BeforeEach
  @Override
  public void init() throws Exception {
    // Add stewards to the database
    nmo = hub.stewards.add(TestData.Stewards.nmo());
    acme = hub.stewards.add(TestData.Stewards.acme());

    // Add models to the database
    niem = hub.models.add(TestData.Models.niem(nmo));
    crashNmo = hub.models.add(TestData.Models.crash(nmo));
    crashAcme = hub.models.add(TestData.Models.crash(acme));

    // Initialize data for NIEM model versions
    niemV1 = TestData.Versions.major(niem, "1.0");
    niemV2 = TestData.Versions.minor(niem, "1.1");

    niemV1.setNiemVersion(niemV1);
    niemV2.setNiemVersion(niemV2);

    // Initialize data for ACME crash IEPD versions
    crashAcmeV1 = TestData.Versions.major(crashAcme, "1.0", niemV1);
    crashAcmeV2 = TestData.Versions.minor(crashAcme, "1.1", niemV2);
  }

  @Override
  protected void loadObjects() throws Exception {
    niemV1 = hub.versions.add(niemV1);
    niemV2 = hub.versions.add(niemV2);

    crashAcmeV1 = hub.versions.add(crashAcmeV1);
    crashAcmeV2 = hub.versions.add(crashAcmeV2);
  }

  @Override
  @Test
  public void databaseAddTest() {
    this.add(niemV1);
    this.add(niemV2);
    niemV1.setNiemVersion(niemV1);
    this.add(crashAcmeV1);
    this.add(crashAcmeV2);
  }

  @Override
  @Test
  public void databaseAddDuplicateTest() {
    this.addDuplicate(crashAcmeV1);
  }

  @Override
  @Test
  public void databaseEditTest() throws Exception {
    crashAcmeV1.setDraft("1");
    this.edit(crashAcmeV1, "draft", "2");
  }

  @Override
  @Test
  public void databaseDeleteTest() throws Exception {
    this.deleteNonCascading(hub.versions, crashAcmeV1, crashAcmeV2, hub.models);
  }

  @Override
  @Test
  public void databaseFindOneTest() throws Exception {
    this.loadObjects();
    Version result = hub.versions.findOne("acme-co", "crash-driver", "1.1");
    assertEquals("ACME Co", result.getSteward().getShortName());
    assertEquals("1.1", result.getVersionNumber());
    assertEquals(Version.Category.minor, result.getCategory());
  }

  @Test
  public void databaseFindAllTest() throws Exception {
    this.loadObjects();
    List<Version> results = hub.versions.findAll();
    assertEquals(4, results.size());
  }

  @Override
  @Test
  public void objectLabelTest() throws Exception {
    this.loadObjects();
    Version result = hub.versions.findOne(crashAcmeV1);
    assertEquals("acme-co/crash-driver/1.0", result.getFullIdentifier());
  }

  @Override
  @Test
  public void objectSerializationTest() throws Exception {
    serializeObject(niemV1);
    serializeObject(niemV2);
    serializeObject(crashAcmeV1);
    serializeObject(crashAcmeV2);
  }

}
