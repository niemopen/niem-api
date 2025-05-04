package gov.niem.tools.api.db.model;

import gov.niem.tools.api.db.EntityTest;
import gov.niem.tools.api.db.TestData;
import gov.niem.tools.api.db.steward.Steward;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

/**
 * Test database operations for models.
 */
@ActiveProfiles("test")
@SpringBootTest
public class ModelTest extends EntityTest<Model> {

  // Test data
  static Steward nmo;
  static Steward acme;

  Model niem;
  Model crashNmo;
  Model crashAcme;

  @Override
  protected ModelService service() {
    return this.hub.models;
  }

  @BeforeEach
  @Override
  public void init() throws Exception {
    // Add stewards to the database
    nmo = hub.stewards.add(TestData.Stewards.nmo());
    acme = hub.stewards.add(TestData.Stewards.acme());

    // Load data into model objects, but do not add to the database
    niem = TestData.Models.niem(nmo);
    crashNmo = TestData.Models.crash(nmo);
    crashAcme = TestData.Models.crash(acme);
  }

  /**
   * Add models to the database.
   */
  @Override
  protected void loadObjects() throws Exception {
    niem = hub.models.add(niem);
    crashNmo = hub.models.add(crashNmo);
    crashAcme = hub.models.add(crashAcme);
  }

  @Override
  @Test
  public void databaseAddTest() {
    this.add(niem);
    this.add(crashNmo);
    this.add(crashAcme);
  }

  @Override
  @Test
  public void databaseAddDuplicateTest() {
    this.addDuplicate(crashAcme);
  }

  @Override
  @Test
  public void databaseEditTest() throws Exception {
    this.edit(crashAcme, "fullName", "ACME Crash Driver IEPD");
  }

  @Override
  @Test
  @Disabled
  public void databaseDeleteTest() throws Exception {
    // Make sure a model can be deleted and the number of stewards does not change
    this.deleteNonCascading(hub.models, niem, crashNmo, hub.stewards);
  }

  @Override
  @Test
  public void databaseFindOneTest() throws Exception {
    this.loadObjects();
    Model result  = hub.models.findOne("acme-co", "crash-driver");
    assertNotNull(result);
  }

  @Test
  public void databaseFindOneByShortName() throws Exception {
    this.loadObjects();
    Model result = service().findOneByShortName("acme-co", "Crash Driver");
    assertEquals("acme-co", result.getStewardKey());
    assertEquals("Crash Driver Report IEPD", result.getFullName());
  }

  @Override
  @Test
  public void objectLabelTest() throws Exception {
    this.loadObjects();
    Model result = hub.models.findOne(crashAcme);
    assertEquals("acme-co/crash-driver", result.getIdLabel());
  }

  @Override
  @Test
  public void objectSerializationTest() throws Exception {
    serializeObject(niem);
    serializeObject(crashAcme);
  }

}
