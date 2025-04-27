package gov.niem.tools.api.db.subproperty;

import gov.niem.tools.api.db.base.BaseEntityService;
import gov.niem.tools.api.db.component.Component;
import gov.niem.tools.api.db.exceptions.EntityNotFoundException;
import gov.niem.tools.api.db.exceptions.EntityNotUniqueException;
import gov.niem.tools.api.db.exceptions.FieldNotFoundException;
import gov.niem.tools.api.db.namespace.Namespace;
import gov.niem.tools.api.db.namespace.NamespaceService;
import gov.niem.tools.api.db.property.Property;
import gov.niem.tools.api.db.property.PropertyService;
import gov.niem.tools.api.db.type.Type;
import gov.niem.tools.api.db.type.TypeService;
import gov.niem.tools.api.db.version.Version;
import gov.niem.tools.api.db.version.VersionService;

import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import jakarta.transaction.Transactional;
import java.util.Optional;
import java.util.Set;
import org.hibernate.Hibernate;
import org.hibernate.proxy.HibernateProxy;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * Operations for subproperties.
 *
 * @todo Return Subproperty lists instead of sets
 */
@org.springframework.stereotype.Component
public class SubpropertyService extends BaseEntityService<Subproperty> {

  @PersistenceContext
  private EntityManager em;

  @Autowired
  SubpropertyRepository repo;

  @Autowired
  VersionService versionService;

  @Autowired
  NamespaceService namespaceService;

  @Autowired
  TypeService typeService;

  @Autowired
  PropertyService propertyService;

  public SubpropertyRepository repository() {
    return this.repo;
  }

  /**
   * Creates a new subproperty in the database and adds it to the type with the given
   * type fields.
   */
  @Transactional
  public Subproperty add(String stewardKey, String modelKey, String versionNumber,
      String typeQname, String propertyQname, String min, String max) throws Exception {
    Type type = typeService.findOne(stewardKey, modelKey, versionNumber, typeQname);
    Property property = propertyService.findOne(stewardKey, modelKey, versionNumber, propertyQname);
    return this.add(type, property, min, max);
  }

  /**
   * Creates a new subproperty in the database and adds it to the type with the given
   * type fields.
   */
  @Transactional
  public Subproperty add(Version version, String typeQname, String propertyQname,
      String min, String max) throws Exception {
    Type type = typeService.findOne(version, typeQname);
    Property property = propertyService.findOne(version, propertyQname);
    return this.add(type, property, min, max);
  }

  /**
   * Creates a new subproperty in the database and adds it to the type with the given type.
   */
  @Transactional
  public Subproperty add(Type type, Property property, String min, String max) throws Exception {
    Subproperty subproperty = Subproperty.builder()
        .type(type)
        .property(property)
        .min(min)
        .max(max)
        .build();

    type.getSubproperties().add(subproperty);
    property.getSubproperties().add(subproperty);

    return super.add(subproperty);
  }

  /**
   * Finds a subproperty in the database with the identifying fields from the given
   * subproperty object.
   */
  public Subproperty findOne(Subproperty subproperty) throws EntityNotFoundException {
    if (subproperty.getVersion() == null) {
      throw new EntityNotFoundException(subproperty);
    }
    return this.findOne(subproperty.getVersion(), subproperty.getTypeQname(),
        subproperty.getPropertyQname());
  }

  /**
   * Finds a subproperty in the database with the type with the given type fields and
   * the property with the given qualified property name.
   */
  public Subproperty findOne(String stewardKey, String modelKey, String versionNumber,
      String typeQname, String propertyQname) throws EntityNotFoundException {
    Version version = versionService.findOne(stewardKey, modelKey, versionNumber);
    return this
        .findOneOptional(version, typeQname, propertyQname)
        .orElseThrow(() -> this.getNotFoundException(typeQname + "/" + propertyQname));
  }

  /**
   * Finds a subproperty in the database with the type with the given version and
   * qualified type name and the property with the given qualified property name.
   */
  public Subproperty findOne(Version version, String typeQname, String propertyQname)
      throws EntityNotFoundException {
    return this
    .findOneOptional(version, typeQname, propertyQname)
    .orElseThrow(() -> this.getNotFoundException(typeQname + "/" + propertyQname));
  }

  /**
   * Finds a subproperty in the database with the given type and property.
   */
  public Subproperty findOne(Type type, Property property) throws EntityNotFoundException {
    return this
        .findOneOptional(type.getVersion(), type.getQname(), property.getQname())
        .orElseThrow(() -> this.getNotFoundException(type.getQname() + "/" + property.getQname()));
  }

  /**
   * Optionally finds a subproperty in the database with a type with the given version
   * and qualified type name and a property with the given qualified property name.
   */
  public Optional<Subproperty> findOneOptional(Version version, String typeQname,
      String propertyQname) {
    Optional<Subproperty> result = repo.findOneByVersionIdAndType_Namespace_PrefixAndType_NameAndProperty_Namespace_PrefixAndProperty_Name(
        version.getId(), Component.getPrefix(typeQname), Component.getName(typeQname),
        Component.getPrefix(propertyQname), Component.getName(propertyQname));

    // Initialize the result if Hibernate returns a proxy due to lazy loading
    if (result.isPresent()) {
      Subproperty subproperty = result.get();
      if (subproperty instanceof HibernateProxy) {
        subproperty = Hibernate.unproxy(subproperty, Subproperty.class);
      }
      return Optional.of(subproperty);
    }

    return result;
  }

  /**
   * Finds the subproperties from the database in the type with the given fields.
   */
  public Set<Subproperty> findByType(String stewardKey, String modelKey,
      String versionNumber, String qname) throws Exception {
    Version version = versionService.findOne(stewardKey, modelKey, versionNumber);
    return repo.findByVersionIdAndType_Namespace_PrefixAndType_NameOrderBySequenceAsc(
      version.getId(), Component.getPrefix(qname), Component.getName(qname));
  }

  /**
   * Finds the subproperties from the database in the given type.
   */
  public Set<Subproperty> findByType(Type type) throws Exception {
    String qname = type.getQname();
    return repo.findByVersionIdAndType_Namespace_PrefixAndType_NameOrderBySequenceAsc(
      type.getVersion().getId(), Component.getPrefix(qname), Component.getName(qname));
  }

  /**
   * Finds all subproperties in the database in the version with the given version fields
   * and with the property with the given qualified property name.
   */
  public Set<Subproperty> findByProperty(String stewardKey, String modelKey,
      String versionNumber, String qname) throws Exception {
    Version version = versionService.findOne(stewardKey, modelKey, versionNumber);
    return repo.findByVersionIdAndProperty_Namespace_PrefixAndProperty_NameOrderByType_NameAsc(
      version.getId(), Component.getPrefix(qname), Component.getName(qname));
  }

  /**
   * Finds all subproperties in the database with the given property.
   */
  public Set<Subproperty> findByProperty(Property property) throws Exception {
    String qname = property.getQname();
    return repo.findByVersionIdAndProperty_Namespace_PrefixAndProperty_NameOrderByType_NameAsc(
      property.getVersion().getId(), Component.getPrefix(qname), Component.getName(qname));
  }

  /**
   * Finds all subproperties in the database in the version with the given fields.
   */
  public Set<Subproperty> findByVersion(String stewardKey, String modelKey, String versionNumber)
      throws Exception {
    Version version = versionService.findOne(stewardKey, modelKey, versionNumber);
    return repo.findByVersionId(version.getId());
  }

  /**
   * Finds all subproperties in the database in the version with the given version fields
   * and with types with the given namespace prefix.
   */
  public Set<Subproperty> findByTypePrefix(String stewardKey, String modelKey,
      String versionNumber, String prefix) throws Exception {
    Namespace namespace = namespaceService.findOne(stewardKey, modelKey, versionNumber, prefix);
    return repo.findByVersionIdAndType_Namespace_PrefixOrderByType_NameAscSequenceAsc(
      namespace.getVersion().getId(), prefix);
  }

  /**
   * Finds all subproperties in the database in the version with the given version fields
   * and with properties with the given namespace prefix.
   */
  public Set<Subproperty> findByPropertyPrefix(String stewardKey, String modelKey,
      String versionNumber, String prefix) throws Exception {
    Namespace namespace = namespaceService.findOne(stewardKey, modelKey, versionNumber, prefix);
    return repo.findByVersionIdAndProperty_Namespace_PrefixOrderByType_NameAscSequence(
      namespace.getVersion().getId(), prefix);
  }

  public void assertRequiredLocalFields(Subproperty subproperty) throws FieldNotFoundException {
    assertFieldNotNullAndNotEmpty("typeQname", subproperty.typeQname);
    assertFieldNotNullAndNotEmpty("propertyQname", subproperty.propertyQname);
  }

  /**
   * Checks that the database does not contain a subproperty with the same identifying
   * fields in the given subproperty object.
   */
  public void assertUnique(Subproperty subproperty) throws EntityNotUniqueException {
    this
        .findOneOptional(subproperty.getVersion(), subproperty.getTypeQname(),
            subproperty.getPropertyQname())
        .ifPresent(result -> this.throwNotUnique(subproperty));
  }

}
