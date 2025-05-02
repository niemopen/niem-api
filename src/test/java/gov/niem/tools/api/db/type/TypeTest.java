package gov.niem.tools.api.db.type;

import gov.niem.tools.api.db.EntityTest;
import gov.niem.tools.api.db.TestData;
import gov.niem.tools.api.db.model.Model;
import gov.niem.tools.api.db.namespace.Namespace;
import gov.niem.tools.api.db.steward.Steward;
import gov.niem.tools.api.db.version.Version;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.test.context.ActiveProfiles;

/**
 * Test database operations for types.
 *
 * @todo Add base type to Type testing
 */
@ActiveProfiles("test")
@SpringBootTest
public class TypeTest extends EntityTest<Type> {

  Steward nmo;
  Model niem;
  Version niemV1;
  Namespace nc;
  Namespace em;
  Namespace xs;

  Type ncPersonType;
  Type ncObjectType;
  Type ncTextType;
  Type emCodeSimpleType;
  Type xsStringType;

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

    niemV1 = hub.versions.add(TestData.Versions.major(niem, "1.0"));
    niemV1.setNiemVersion(niemV1);

    nc = hub.namespaces.add(TestData.Namespaces.core(niemV1));
    em = hub.namespaces.add(TestData.Namespaces.domain(niemV1, "em"));
    xs = hub.namespaces.add(TestData.Namespaces.xs(niemV1));

    // Initialize data for properties
    ncPersonType = TestData.Types.personType(nc);
    ncObjectType = TestData.Types.objectType(nc);
    ncTextType = TestData.Types.textType(nc);
    emCodeSimpleType = TestData.Types.codeSimpleType(em);
    xsStringType = TestData.Types.xsStringType(xs);
  }

  @Override
  protected void loadObjects() throws Exception {
    ncPersonType = service().add(ncPersonType);
    ncObjectType = service().add(ncObjectType);
    ncTextType = service().add(ncTextType);
    emCodeSimpleType = service().add(emCodeSimpleType);
    xsStringType = service().add(xsStringType);

    // Set parent or base types
    ncPersonType.setBase(ncObjectType);
    ncTextType.setBase(xsStringType);
    emCodeSimpleType.setBase(xsStringType);
  }

  @Override
  @Test
  public void databaseAddTest() {
    this.add(ncPersonType);
    this.add(ncTextType);
    this.add(emCodeSimpleType);
  }

  @Override
  @Test
  public void databaseAddDuplicateTest() {
    this.addDuplicate(ncPersonType);
  }

  @Override
  @Test
  @Disabled
  public void databaseEditTest() throws Exception {
    this.edit(ncPersonType, "definition", "A data type for a real or imaginary human being");
  }

  @Override
  @Test
  @Disabled
  public void databaseDeleteTest() throws Exception {
    this.deleteNonCascading(hub.types, ncPersonType, ncTextType, hub.namespaces);
  }

  @Override
  @Test
  @Disabled
  public void databaseFindOneTest() throws Exception {
    this.loadObjects();
    Type result = service().findOne(ncPersonType);
    assertEquals(ncPersonType.getQname(), result.getQname());
  }

  @Test
  public void databaseFindAllTest() throws Exception {
    this.loadObjects();
    Pageable pageable = PageRequest.ofSize(20);
    Page<Type> results = service().findByVersion(ncPersonType.getStewardKey(),
        ncPersonType.getModelKey(), ncPersonType.getVersionNumber(), pageable);
    List<Type> types = results.getContent();
    assertEquals(5, types.size());
  }

  @Override
  @Test
  @Disabled
  public void objectLabelTest() throws Exception {
    this.loadObjects();
    Type result = service().findOne(ncPersonType);
    assertEquals("nmo/niem/1.0/nc:PersonType", result.getFullIdentifier());
  }

  @Override
  @Test
  public void objectSerializationTest() throws Exception {
    serializeObject(ncPersonType);
    serializeObject(ncTextType);
    serializeObject(emCodeSimpleType);
  }

}
