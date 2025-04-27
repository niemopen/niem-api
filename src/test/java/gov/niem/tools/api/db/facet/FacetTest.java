package gov.niem.tools.api.db.facet;

import gov.niem.tools.api.db.EntityTest;
import gov.niem.tools.api.db.TestData;
import gov.niem.tools.api.db.model.Model;
import gov.niem.tools.api.db.namespace.Namespace;
import gov.niem.tools.api.db.steward.Steward;
import gov.niem.tools.api.db.type.Type;
import gov.niem.tools.api.db.version.Version;

import static org.junit.jupiter.api.Assertions.assertEquals;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

/**
 * Test database facet operations.
 *
 * @todo Fix skipped tests
 */
@ActiveProfiles("test")
@SpringBootTest
public class FacetTest extends EntityTest<Facet> {

  Steward nmo;
  Model niem;
  Version niemV1;
  Namespace nc;
  Namespace em;
  Namespace xs;

  Type ncCodeSimpleType;
  Type emCodeSimpleType;
  Type xsStringType;

  Facet ncCode1;
  Facet ncCode2;
  Facet emCode1;
  Facet emCode3;
  Facet emCode4;

  @Override
  protected FacetService service() {
    return this.hub.facets;
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
    xs = hub.namespaces.add(TestData.Namespaces.xs(niemV1));

    ncCodeSimpleType = hub.types.add(TestData.Types.codeSimpleType(nc));
    emCodeSimpleType = hub.types.add(TestData.Types.codeSimpleType(em));
    xsStringType = hub.types.add(TestData.Types.xsStringType(xs));

    // Initialize data for facets
    ncCode1 = new Facet(ncCodeSimpleType, Facet.Category.enumeration, "1", "1.");
    ncCode2 = new Facet(ncCodeSimpleType, Facet.Category.enumeration, "2", "2.");

    emCode1 = new Facet(emCodeSimpleType, Facet.Category.enumeration, "1", "1.");
    emCode3 = new Facet(emCodeSimpleType, Facet.Category.enumeration, "3", "3.");
    emCode4 = new Facet(emCodeSimpleType, Facet.Category.enumeration, "4", "4.");
  }

  @Override
  protected void loadObjects() throws Exception {
    ncCode1 = service().add(ncCode1);
    ncCode2 = service().add(ncCode2);
    emCode1 = service().add(emCode1);
    emCode3 = service().add(emCode3);
    emCode4 = service().add(emCode4);
  }

  @Override
  @Test
  public void databaseAddTest() {
    this.add(ncCode1);
    this.add(ncCode2);
    this.add(emCode1);
    this.add(emCode3);
    this.add(emCode4);
  }

  @Override
  @Test
  @Disabled
  public void databaseAddDuplicateTest() {
    this.addDuplicate(ncCode1);
  }

  @Override
  @Test
  @Disabled
  public void databaseEditTest() throws Exception {
    this.edit(ncCode1, "definition", "A 1 value.");
  }

  @Override
  @Test
  @Disabled
  public void databaseDeleteTest() throws Exception {
    this.deleteNonCascading(hub.facets, ncCode1, ncCode2, hub.types);
  }

  @Override
  @Test
  @Disabled
  public void databaseFindOneTest() throws Exception {
    this.loadObjects();
    Facet result = service().findOne(ncCode1);
    assertEquals(ncCode1.getValue(), result.getValue());
  }

  // /**
  //  * Finds all facets in a version.
  //  *
  //  * @todo Implement FacetService.findByVersion and update empty test
  //  */
  // @Test
  // public void databaseFindAllTest() throws Exception {
  //   this.loadObjects();
  //   List<Facet> results = service().findByVersion(ncCode1.getStewardKey(),
  //       ncCode1.getModelKey(), ncCode1.getVersionNumber());
  //   // assertEquals(2, results.size());
  // }

  @Override
  @Test
  @Disabled
  public void objectLabelTest() throws Exception {
    this.loadObjects();
    Facet result = service().findOne(ncCode1);
    assertEquals("nmo/niem/1.0/nc:CodeSimpleType/enumeration=1", result.getFullIdentifier());
  }

  @Override
  @Test
  public void objectSerializationTest() throws Exception {
    serializeObject(ncCode1);
    serializeObject(ncCode2);
    serializeObject(emCode1);
    serializeObject(emCode3);
    serializeObject(emCode4);
  }

}
