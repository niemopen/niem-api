package gov.niem.tools.api.db.subproperty;

import gov.niem.tools.api.db.EntityTest;
import gov.niem.tools.api.db.TestData;
import gov.niem.tools.api.db.model.Model;
import gov.niem.tools.api.db.namespace.Namespace;
import gov.niem.tools.api.db.property.Property;
import gov.niem.tools.api.db.steward.Steward;
import gov.niem.tools.api.db.type.Type;
import gov.niem.tools.api.db.version.Version;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.util.Set;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

/**
 * Test database operations for subproperties.
 *
 * @todo Fix skipped tests
 */
@ActiveProfiles("test")
@SpringBootTest
public class SubpropertyTest extends EntityTest<Subproperty> {

  Steward nmo;
  Model niem;
  Version niemV1;
  Namespace nc;
  Namespace em;
  Namespace xs;

  Type ncPersonType;
  Type ncObjectType;
  Type ncTextType;

  Property ncPersonFullName;
  Property ncSourceTextAttribute;

  Subproperty ncPersonTypePersonFullname;
  Subproperty ncPersonTypeSourceTextAttribute;
  Subproperty ncObjectTypeSourceTextAttribute;

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

    ncPersonType = hub.types.add(TestData.Types.personType(nc));
    ncObjectType = hub.types.add(TestData.Types.objectType(nc));
    ncTextType = hub.types.add(TestData.Types.textType(nc));

    ncPersonFullName = hub.properties.add(TestData.Properties.personFullName(nc));
    ncSourceTextAttribute = hub.properties.add(TestData.Properties.sourceText(nc));

    // Initialize data for subproperties
    ncPersonTypePersonFullname = new Subproperty(ncPersonType, ncPersonFullName, "0", "unbounded");
    ncPersonTypePersonFullname.setTypeQname(ncPersonType.getQname());
    ncPersonTypePersonFullname.setPropertyQname(ncPersonFullName.getQname());

    ncPersonTypeSourceTextAttribute = new Subproperty(ncPersonType, ncSourceTextAttribute,
        "0", "1");
    ncPersonTypeSourceTextAttribute.setTypeQname(ncPersonType.getQname());
    ncPersonTypeSourceTextAttribute.setPropertyQname(ncSourceTextAttribute.getQname());

    ncObjectTypeSourceTextAttribute = new Subproperty(ncObjectType, ncSourceTextAttribute,
        "1", "1");
    ncObjectTypeSourceTextAttribute.setTypeQname(ncObjectType.getQname());
    ncObjectTypeSourceTextAttribute.setPropertyQname(ncSourceTextAttribute.getQname());

  }

  @Override
  protected void loadObjects() throws Exception {
    service().add(ncPersonTypePersonFullname);
    service().add(ncPersonTypeSourceTextAttribute);
    service().add(ncObjectTypeSourceTextAttribute);
  }

  @Override
  protected SubpropertyService service() {
    return this.hub.subproperties;
  }

  @Override
  @Test
  @Disabled
  public void databaseAddTest() {
    this.add(ncPersonTypePersonFullname);
    this.add(ncPersonTypeSourceTextAttribute);
    this.add(ncObjectTypeSourceTextAttribute);
  }

  @Override
  @Test
  @Disabled
  public void databaseAddDuplicateTest() {
    this.addDuplicate(ncPersonTypePersonFullname);
  }

  @Override
  @Test
  @Disabled
  public void databaseEditTest() throws Exception {
    this.edit(ncPersonTypePersonFullname, "min", "1");
  }

  @Override
  @Test
  @Disabled
  public void databaseDeleteTest() throws Exception {
    this.deleteNonCascading(hub.subproperties, ncPersonTypePersonFullname,
        ncPersonTypeSourceTextAttribute, hub.types);
  }

  @Override
  @Test
  @Disabled
  public void databaseFindOneTest() throws Exception {
    this.loadObjects();
    Subproperty result = service().findOne(ncPersonType, ncPersonFullName);
    assertEquals(ncPersonTypePersonFullname.getMin(), result.getMin());
  }

  @Test
  @Disabled
  public void databaseFindAllTest() throws Exception {
    this.loadObjects();
    Set<Subproperty> results = service().findByVersion(niemV1.getStewardKey(),
        niemV1.getModelKey(), niemV1.getVersionNumber());
    assertEquals(3, results.size());
  }

  @Override
  @Test
  @Disabled
  public void objectLabelTest() throws Exception {
    this.loadObjects();
    Subproperty result = service().findOne(ncPersonType, ncPersonFullName);
    assertEquals("nmo/niem/1.0/nc:PersonType/nc:PersonFullName", result.getIdLabel());
  }

  @Override
  @Test
  public void objectSerializationTest() throws Exception {
    serializeObject(ncPersonTypePersonFullname);
    serializeObject(ncPersonTypeSourceTextAttribute);
    serializeObject(ncObjectTypeSourceTextAttribute);
  }

}
