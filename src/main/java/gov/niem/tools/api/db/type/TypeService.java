package gov.niem.tools.api.db.type;

import gov.niem.tools.api.db.component.ComponentService;
import gov.niem.tools.api.db.exceptions.EntityNotFoundException;
import gov.niem.tools.api.db.namespace.Namespace;
import gov.niem.tools.api.db.property.Property;
import gov.niem.tools.api.db.property.PropertyRepository;
import gov.niem.tools.api.db.subproperty.Subproperty;
import gov.niem.tools.api.db.subproperty.SubpropertyRepository;
import gov.niem.tools.api.db.version.Version;

import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import jakarta.transaction.Transactional;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

/**
 * Operations supporting types.
 */
@Component
public class TypeService extends ComponentService<Type, TypeRepository> {

  @PersistenceContext
  private EntityManager em;

  @Autowired
  PropertyRepository propertyRepository;

  @Autowired
  SubpropertyRepository subpropertyRepo;

  /**
   * Create a new type in the database with the given prefix and name and add to
   * the version with the given version fields.
   */
  @Transactional
  public Type add(String stewardKey, String modelKey, String versionNumber, String prefix,
      String name) throws Exception {
    Type type = new Type();
    type.setPrefix(prefix);
    type.setName(name);
    return this.add(stewardKey, modelKey, versionNumber, type);
  }

  /**
   * Create a new type in the database with the given prefix and name and add to
   * the given version.
   */
  @Transactional
  public Type add(Version version, String prefix, String name) throws Exception {
    Type type = new Type();
    return this.add(version, prefix, name, type);
  }

  /**
   * Create a new type in the database with the given name and add to the given namespace.
   */
  @Transactional
  public Type add(Namespace namespace, String name) throws Exception {
    Type type = new Type();
    return this.add(namespace, name, type);
  }

  /**
   * Count all types in a version with the given fields.
   */
  public long countByVersion(String stewardKey, String modelKey, String versionNumber,
      Type.Category category) throws EntityNotFoundException {
    Version version = versionService.findOne(stewardKey, modelKey, versionNumber);
    if (category == null) {
      return repo.countByNamespace_Version_Id(version.getId());
    }
    return repo.countByNamespace_Version_IdAndCategory(version.getId(), category);
  }

  /**
   * Count all types in a namespace with the given fields.
   */
  public long countByNamespace(String stewardKey, String modelKey, String versionNumber,
      String prefix, Type.Category category) throws EntityNotFoundException {
    Namespace namespace = namespaceService.findOne(stewardKey, modelKey, versionNumber, prefix);
    if (category == null) {
      return repo.countByNamespace_Id(namespace.getId());
    }
    return repo.countByNamespace_IdAndCategory(namespace.getId(), category);
  }

  /**
   * Get the type inheritance or restriction chain.
   */
  public List<Type> getBases(String stewardKey, String modelKey, String versionNumber,
      String qname) throws EntityNotFoundException {
    Type type = findOne(stewardKey, modelKey, versionNumber, qname);
    List<Type> bases = new LinkedList<Type>();

    Type current = type;
    do {
      Type base = current.getBase();
      if (base == null) {
        break;
      }
      bases.add(0, base);
      current = base;
    } while (true);

    return bases;
  }

  /**
   * Get the list of types that extend or restrict the type with the given fields.
   */
  public List<Type> getChildren(String stewardKey, String modelKey, String versionNumber,
      String qname) throws EntityNotFoundException {
    Type type = findOne(stewardKey, modelKey, versionNumber, qname);
    List<Type> children = repo.findAllByBase_Id(type.getId());
    Collections.sort(children);
    return children;
  }

  /**
   * Find the augmentation point element for the type with the given fields.
   */
  public Property findAugmentationPoint(String stewardKey, String modelKey,
      String versionNumber, String typeQname) throws EntityNotFoundException {
    Version version = versionService.findOne(stewardKey, modelKey, versionNumber);
    Type type = this.findOne(version, typeQname);
    Subproperty subproperty = subpropertyRepo.findOneByTypeIdAndPropertyNameEndingWith(
          type.getId(), "AugmentationPoint");
    if (subproperty == null) {
      throw new EntityNotFoundException("Augmentation point", "for " + typeQname);
    }
    return subproperty.getProperty();
  }

  /**
   * Find augmentations for the type with the given fields.
   */
  public List<Property> findAugmentations(String stewardKey, String modelKey,
      String versionNumber, String typeQname) throws EntityNotFoundException {
    Property augmentationPoint = this.findAugmentationPoint(stewardKey, modelKey,
        versionNumber, typeQname);
    if (augmentationPoint == null) {
      throw new EntityNotFoundException("Augmentation point", "for " + typeQname);
    }
    List<Property> augmentations = propertyRepository.findAllByGroup_Id(augmentationPoint.getId());
    Collections.sort(augmentations);
    return augmentations;
  }

}
