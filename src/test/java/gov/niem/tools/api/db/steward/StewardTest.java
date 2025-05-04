package gov.niem.tools.api.db.steward;

import gov.niem.tools.api.db.EntityTest;
import gov.niem.tools.api.db.TestData;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

/**
 * Test database operations for stewards.
 */
@ActiveProfiles("test")
@SpringBootTest
public class StewardTest extends EntityTest<Steward> {

  @Autowired
  StewardController controller;

  // Test data
  Steward nmo;
  Steward acme;

  @Override
  protected StewardService service() {
    return this.hub.stewards;
  }

  @BeforeEach
  @Override
  protected void init() throws Exception {
    nmo = TestData.Stewards.nmo();
    acme = TestData.Stewards.acme();
  }

  @Override
  protected void loadObjects() throws Exception {
    hub.stewards.add(nmo);
    hub.stewards.add(acme);
  }

  @Override
  @Test
  public void databaseAddTest() {
    this.add(acme);
    this.add(nmo);
  }

  @Override
  @Test
  public void databaseAddDuplicateTest() {
    this.addDuplicate(acme);
  }

  @Override
  @Test
  public void databaseEditTest() throws Exception {
    this.edit(acme, "email", "tweety@acme.org");
  }

  @Override
  @Test
  public void databaseDeleteTest() throws Exception {
    // Add stewards
    hub.stewards.add(acme);
    hub.stewards.add(nmo);

    // Delete one steward
    hub.stewards.delete("nmo");

    // Check that deleted steward is no longer in the database
    Optional<Steward> niemResult = hub.stewards.findOneOptional("nmo");
    assertTrue(niemResult.isEmpty());

    // Check that the other added steward still exists in the database
    Optional<Steward> acmeResult = hub.stewards.findOneOptional("acme-co");
    assertTrue(acmeResult.isPresent());
  }

  // TODO: Steward Delete cascading check
  // public void deleteCascadingTest() throws Exception{
  //   Model crash_acme = hub.models.add(TestData.Models.crash(acme));
  //   Model crash_nmo = hub.models.add(TestData.Models.crash(nmo));
  //   this.deleteCascading(hub.stewards, nmo, acme, hub.models);
  // }

  @Test
  public void checkSlugifyFormulaTest() throws Exception {
    hub.stewards.add(acme);
    assertEquals("acme-co", acme.getStewardKey());
  }

  @Override
  @Test
  public void databaseFindOneTest() throws Exception {
    this.loadObjects();
    Steward result = hub.stewards.findOne("acme-co");
    assertEquals("acme-co", result.getStewardKey());
  }

  @Test
  public void findOneByShortNameTest() throws Exception {
    this.loadObjects();
    Steward result = service().findOneByShortName("ACME Co");
    assertEquals("acme-co", result.getStewardKey());
    assertEquals("ACME Corporation", result.getFullName());
  }

  @Override
  @Test
  public void objectLabelTest() throws Exception {
    this.loadObjects();
    Steward result = service().findOne(acme);
    assertEquals("acme-co", result.getIdLabel());
  }

  @Override
  @Test
  public void objectSerializationTest() throws Exception {
    this.loadObjects();
    serializeObject(nmo);
    serializeObject(acme);
  }

  @Test
  public void controllerGetListTest() throws Exception {
    this.loadObjects();
    List<Steward> results = controller.getStewards();
    assertEquals(2, results.size());
  }

}
