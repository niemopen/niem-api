package gov.niem.tools.api.db.type;

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
 * @todo Add base type to Type testing
 */
@ActiveProfiles("test")
@SpringBootTest
public class TypeTest extends EntityTest<Type> {

  Steward nmo;
  Model niem;
  Version niem_v1;
  Namespace nc;
  Namespace em;
  Namespace xs;

  Type nc_PersonType;
  Type nc_ObjectType;
  Type nc_TextType;
  Type em_CodeSimpleType;
  Type xs_stringType;

  @Override
  protected TypeService service() {
    return this.hub.types;
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

    // Initialize data for properties
    nc_PersonType = TestData.Types.personType(nc);
    nc_ObjectType = TestData.Types.objectType(nc);
    nc_TextType = TestData.Types.textType(nc);
    em_CodeSimpleType = TestData.Types.codeSimpleType(em);
    xs_stringType = TestData.Types.xsStringType(xs);
  }

  @Override
  protected void loadObjects() throws Exception {
    nc_PersonType = service().add(nc_PersonType);
    nc_ObjectType = service().add(nc_ObjectType);
    nc_TextType = service().add(nc_TextType);
    em_CodeSimpleType = service().add(em_CodeSimpleType);
    xs_stringType = service().add(xs_stringType);

    // Set parent or base types
    nc_PersonType.setBase(nc_ObjectType);
    nc_TextType.setBase(xs_stringType);
    em_CodeSimpleType.setBase(xs_stringType);
  }

  @Override
  @Test
  public void databaseAddTest() {
    this.add(nc_PersonType);
    this.add(nc_TextType);
    this.add(em_CodeSimpleType);
  }

  @Override
  @Test
  public void databaseAddDuplicateTest() {
    this.addDuplicate(nc_PersonType);
  }

  @Override
  @Test
  @Disabled
  public void databaseEditTest() throws Exception {
    this.edit(nc_PersonType, "definition", "A data type for a real or imaginary human being");
  }

  @Override
  @Test
  @Disabled
  public void databaseDeleteTest() throws Exception {
    this.deleteNonCascading(hub.types, nc_PersonType, nc_TextType, hub.namespaces);
  }

  @Override
  @Test
  @Disabled
  public void databaseFindOneTest() throws Exception {
    this.loadObjects();
    Type result = service().findOne(nc_PersonType);
    assertEquals(nc_PersonType.getQname(), result.getQname());
  }

  @Test
  public void databaseFindAllTest() throws Exception {
    this.loadObjects();
    List<Type> results = service().findByVersion(nc_PersonType.getStewardKey(), nc_PersonType.getModelKey(), nc_PersonType.getVersionNumber());
    assertEquals(5, results.size());
  }

  @Override
  @Test
  @Disabled
  public void objectLabelTest() throws Exception {
    this.loadObjects();
    Type result = service().findOne(nc_PersonType);
    assertEquals("nmo/niem/1.0/nc:PersonType", result.getFullIdentifier());
  }

  @Override
  @Test
  public void objectSerializationTest() throws Exception {
    serializeObject(nc_PersonType);
    serializeObject(nc_TextType);
    serializeObject(em_CodeSimpleType);
  }

}
