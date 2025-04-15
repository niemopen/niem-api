package gov.niem.tools.api.db.subproperty;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.util.Set;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

import gov.niem.tools.api.db.EntityTest;
import gov.niem.tools.api.db.TestData;
import gov.niem.tools.api.db.model.Model;
import gov.niem.tools.api.db.namespace.Namespace;
import gov.niem.tools.api.db.property.Property;
import gov.niem.tools.api.db.steward.Steward;
import gov.niem.tools.api.db.type.Type;
import gov.niem.tools.api.db.version.Version;

/**
 * @todo Fix skipped tests
 */
@ActiveProfiles("test")
@SpringBootTest
public class SubpropertyTest extends EntityTest<Subproperty> {

  Steward nmo;
  Model niem;
  Version niem_v1;
  Namespace nc;
  Namespace em;
  Namespace xs;

  Type nc_PersonType;
  Type nc_ObjectType;
  Type nc_TextType;

  Property nc_PersonFullName;
  Property nc_sourceText;

  Subproperty nc_PersonType_PersonFullname;
  Subproperty nc_PersonType_sourceText;
  Subproperty nc_ObjectType_sourceText;

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

    nc_PersonType = hub.types.add(TestData.Types.personType(nc));
    nc_ObjectType = hub.types.add(TestData.Types.objectType(nc));
    nc_TextType = hub.types.add(TestData.Types.textType(nc));

    nc_PersonFullName = hub.properties.add(TestData.Properties.personFullName(nc));
    nc_sourceText = hub.properties.add(TestData.Properties.sourceText(nc));

    // Initialize data for subproperties
    nc_PersonType_PersonFullname = new Subproperty(nc_PersonType, nc_PersonFullName, "0", "unbounded");
    nc_PersonType_PersonFullname.setTypeQname(nc_PersonType.getQname());
    nc_PersonType_PersonFullname.setPropertyQname(nc_PersonFullName.getQname());

    nc_PersonType_sourceText = new Subproperty(nc_PersonType, nc_sourceText, "0", "1");
    nc_PersonType_sourceText.setTypeQname(nc_PersonType.getQname());
    nc_PersonType_sourceText.setPropertyQname(nc_sourceText.getQname());

    nc_ObjectType_sourceText = new Subproperty(nc_ObjectType, nc_sourceText, "1", "1");
    nc_ObjectType_sourceText.setTypeQname(nc_ObjectType.getQname());
    nc_ObjectType_sourceText.setPropertyQname(nc_sourceText.getQname());

  }

  @Override
  protected void loadObjects() throws Exception {
    service().add(nc_PersonType_PersonFullname);
    service().add(nc_PersonType_sourceText);
    service().add(nc_ObjectType_sourceText);
  }

  @Override
  protected SubpropertyService service() {
    return this.hub.subproperties;
  }

  @Override
  @Test
  @Disabled
  public void databaseAddTest() {
    this.add(nc_PersonType_PersonFullname);
    this.add(nc_PersonType_sourceText);
    this.add(nc_ObjectType_sourceText);
  }

  @Override
  @Test
  @Disabled
  public void databaseAddDuplicateTest() {
    this.addDuplicate(nc_PersonType_PersonFullname);
  }

  @Override
  @Test
  @Disabled
  public void databaseEditTest() throws Exception {
    this.edit(nc_PersonType_PersonFullname, "min", "1");
  }

  @Override
  @Test
  @Disabled
  public void databaseDeleteTest() throws Exception {
    this.deleteNonCascading(hub.subproperties, nc_PersonType_PersonFullname, nc_PersonType_sourceText, hub.types);
  }

  @Override
  @Test
  @Disabled
  public void databaseFindOneTest() throws Exception {
    this.loadObjects();
    Subproperty result = service().findOne(nc_PersonType, nc_PersonFullName);
    assertEquals(nc_PersonType_PersonFullname.getMin(), result.getMin());
  }

  @Test
  @Disabled
  public void databaseFindAllTest() throws Exception {
    this.loadObjects();
    Set<Subproperty> results = service().findByVersion(niem_v1.getStewardKey(), niem_v1.getModelKey(), niem_v1.getVersionNumber());
    assertEquals(3, results.size());
  }

  @Override
  @Test
  @Disabled
  public void objectLabelTest() throws Exception {
    this.loadObjects();
    Subproperty result = service().findOne(nc_PersonType, nc_PersonFullName);
    assertEquals("nmo/niem/1.0/nc:PersonType/nc:PersonFullName", result.getFullIdentifier());
  }

  @Override
  @Test
  public void objectSerializationTest() throws Exception {
    serializeObject(nc_PersonType_PersonFullname);
    serializeObject(nc_PersonType_sourceText);
    serializeObject(nc_ObjectType_sourceText);
  }

}
