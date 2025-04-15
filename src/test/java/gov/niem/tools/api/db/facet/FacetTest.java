package gov.niem.tools.api.db.facet;

import static org.junit.jupiter.api.Assertions.assertEquals;

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
import gov.niem.tools.api.db.type.Type;
import gov.niem.tools.api.db.version.Version;

/**
 * @todo Fix skipped tests
 */
@ActiveProfiles("test")
@SpringBootTest
public class FacetTest extends EntityTest<Facet> {

  Steward nmo;
  Model niem;
  Version niem_v1;
  Namespace nc;
  Namespace em;
  Namespace xs;

  Type nc_CodeSimpleType;
  Type em_CodeSimpleType;
  Type xs_stringType;

  Facet nc_Code_1;
  Facet nc_Code_2;
  Facet em_Code_1;
  Facet em_Code_3;
  Facet em_Code_4;

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

    niem_v1 = hub.versions.add(TestData.Versions.major(niem, "1.0"));
    niem_v1.setNiemVersion(niem_v1);

    nc = hub.namespaces.add(TestData.Namespaces.core(niem_v1));
    em = hub.namespaces.add(TestData.Namespaces.domain(niem_v1, "em"));
    xs = hub.namespaces.add(TestData.Namespaces.xs(niem_v1));

    nc_CodeSimpleType = hub.types.add(TestData.Types.codeSimpleType(nc));
    em_CodeSimpleType = hub.types.add(TestData.Types.codeSimpleType(em));
    xs_stringType = hub.types.add(TestData.Types.xsStringType(xs));

    // Initialize data for facets
    nc_Code_1 = new Facet(nc_CodeSimpleType, Facet.Category.enumeration, "1", "1.");
    nc_Code_2 = new Facet(nc_CodeSimpleType, Facet.Category.enumeration, "2", "2.");

    em_Code_1 = new Facet(em_CodeSimpleType, Facet.Category.enumeration, "1", "1.");
    em_Code_3 = new Facet(em_CodeSimpleType, Facet.Category.enumeration, "3", "3.");
    em_Code_4 = new Facet(em_CodeSimpleType, Facet.Category.enumeration, "4", "4.");
  }

  @Override
  protected void loadObjects() throws Exception {
    nc_Code_1 = service().add(nc_Code_1);
    nc_Code_2 = service().add(nc_Code_2);
    em_Code_1 = service().add(em_Code_1);
    em_Code_3 = service().add(em_Code_3);
    em_Code_4 = service().add(em_Code_4);
  }

  @Override
  @Test
  public void databaseAddTest() {
    this.add(nc_Code_1);
    this.add(nc_Code_2);
    this.add(em_Code_1);
    this.add(em_Code_3);
    this.add(em_Code_4);
  }

  @Override
  @Test
  @Disabled
  public void databaseAddDuplicateTest() {
    this.addDuplicate(nc_Code_1);
  }

  @Override
  @Test
  @Disabled
  public void databaseEditTest() throws Exception {
    this.edit(nc_Code_1, "definition", "A 1 value.");
  }

  @Override
  @Test
  @Disabled
  public void databaseDeleteTest() throws Exception {
    this.deleteNonCascading(hub.facets, nc_Code_1, nc_Code_2, hub.types);
  }

  @Override
  @Test
  @Disabled
  public void databaseFindOneTest() throws Exception {
    this.loadObjects();
    Facet result = service().findOne(nc_Code_1);
    assertEquals(nc_Code_1.getValue(), result.getValue());
  }

  /**
   * @todo Implement FacetService.findByVersion and update empty test
   */
  @Test
  public void databaseFindAllTest() throws Exception {
    this.loadObjects();
    // List<Facet> results = service().findByVersion(nc_Code_1.getStewardKey(), nc_Code_1.getModelKey(), nc_Code_1.getVersionNumber());
    // assertEquals(2, results.size());
  }

  @Override
  @Test
  @Disabled
  public void objectLabelTest() throws Exception {
    this.loadObjects();
    Facet result = service().findOne(nc_Code_1);
    assertEquals("nmo/niem/1.0/nc:CodeSimpleType/enumeration=1", result.getFullIdentifier());
  }

  @Override
  @Test
  public void objectSerializationTest() throws Exception {
    serializeObject(nc_Code_1);
    serializeObject(nc_Code_2);
    serializeObject(em_Code_1);
    serializeObject(em_Code_3);
    serializeObject(em_Code_4);
  }

}
