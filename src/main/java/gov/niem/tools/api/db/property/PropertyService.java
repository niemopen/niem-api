package gov.niem.tools.api.db.property;

import gov.niem.tools.api.db.component.ComponentService;
import gov.niem.tools.api.db.exceptions.EntityNotFoundException;
import gov.niem.tools.api.db.namespace.Namespace;
import gov.niem.tools.api.db.version.Version;

import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import jakarta.transaction.Transactional;
import org.springframework.stereotype.Component;

/**
 * Operations supporting properties.
 */
@Component
public class PropertyService extends ComponentService<Property, PropertyRepository> {

  @PersistenceContext
  private EntityManager em;

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

}
