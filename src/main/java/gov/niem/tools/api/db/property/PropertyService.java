package gov.niem.tools.api.db.property;

import gov.niem.tools.api.db.component.ComponentService;
import gov.niem.tools.api.db.exceptions.EntityNotFoundException;
import gov.niem.tools.api.db.namespace.Namespace;
import gov.niem.tools.api.db.type.Type;
import gov.niem.tools.api.db.type.TypeRepository;
import gov.niem.tools.api.db.version.Version;

import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import jakarta.transaction.Transactional;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Component;

/**
 * Operations supporting properties.
 */
@Component
public class PropertyService extends ComponentService<Property, PropertyRepository> {

  @PersistenceContext
  private EntityManager em;

  @Autowired
  TypeRepository typeRepository;

  /**
   * Adds a property to the database with the given name to the namespace with the
   * given namespace fields.
   */
  @Transactional
  public Property add(String stewardKey, String modelKey, String versionNumber,
      String prefix, String name) throws Exception {
    Property property = new Property();
    property.setPrefix(prefix);
    property.setName(name);
    return this.add(stewardKey, modelKey, versionNumber, property);
  }

  /**
   * Adds a property to the database with the given name to the namespace with the given
   * prefix and version.
   */
  @Transactional
  public Property add(Version version, String prefix, String name) throws Exception {
    Property property = new Property();
    return this.add(version, prefix, name, property);
  }

  /**
   * Adds a property to the database with the given name to the given namespace.
   */
  @Transactional
  public Property add(Namespace namespace, String name) throws Exception {
    Property property = new Property();
    return this.add(namespace, name, property);
  }

  /**
   * Adds a property to the database with the given name and definition to the given namespace.
   */
  @Transactional
  public Property add(Namespace namespace, String name, String definition) throws Exception {
    Property property = new Property();
    property.setName(name);
    property.setDefinition(definition);
    return this.add(namespace, property);
  }

  /**
   * Count all properties in a version with the given fields.
   */
  public long countByVersion(String stewardKey, String modelKey, String versionNumber,
      Property.Category category) throws EntityNotFoundException {
    Version version = versionService.findOne(stewardKey, modelKey, versionNumber);
    if (category == null) {
      return repo.countByNamespace_Version_Id(version.getId());
    }
    return repo.countByNamespace_Version_IdAndCategory(version.getId(), category);
  }

  /**
   * Count all properties in a namespace with the given fields.
   */
  public long countByNamespace(String stewardKey, String modelKey, String versionNumber,
      String prefix, Property.Category category) throws EntityNotFoundException {
    Namespace namespace = namespaceService.findOne(stewardKey, modelKey, versionNumber, prefix);
    if (category == null) {
      return repo.countByNamespace_Id(namespace.getId());
    }
    return repo.countByNamespace_IdAndCategory(namespace.getId(), category);
  }

  /**
   * Get a list of substitutions for the property with the given fields.
   */
  public List<Property> getSubstitutions(String stewardKey, String modelKey,
      String versionNumber, String qname) throws EntityNotFoundException {
    Property property = this.findOne(stewardKey, modelKey, versionNumber, qname);
    List<Property> substitutions = repo.findAllByGroup_Id(property.getId());
    Collections.sort(substitutions);
    return substitutions;
  }

  /**
   * Get a list of substitution group heads for the property with the given fields.
   *
   * <p>Note that there is usually only a single substitution group head for
   * a substitutable property, but occasionally there can be a chain of substitutions.
   */
  public List<Property> getSubstitutionGroups(String stewardKey, String modelKey,
      String versionNumber, String qname) throws EntityNotFoundException {
    Property property = this.findOne(stewardKey, modelKey, versionNumber, qname);
    if (property == null) {
      throw new EntityNotFoundException("property", qname);
    }

    List<Property> groups = new LinkedList<Property>();
    Property current = property;
    do {
      Property group = current.getGroup();
      if (group == null) {
        break;
      }
      else {
        groups.add(group);
        current = group;
      }
    } while (true);

    return groups;
  }

  /**
   * Get a page of properties of the type with the given fields.
   */
  public Page<Property> getPropertiesOfType(Type type, Pageable pageable)
      throws EntityNotFoundException {
    Page<Property> properties = repo.findAllByType_Id(type.getId(), pageable);
    return properties;
  }

}
