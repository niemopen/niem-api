package gov.niem.tools.api.db.namespace;

import gov.niem.tools.api.db.base.BaseVersionEntityService;
import gov.niem.tools.api.db.exceptions.EntityNotFoundException;
import gov.niem.tools.api.db.exceptions.EntityNotUniqueException;
import gov.niem.tools.api.db.exceptions.FieldNotFoundException;
import gov.niem.tools.api.db.version.Version;
import gov.niem.tools.api.db.version.VersionService;

import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import jakarta.transaction.Transactional;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

/**
 * Operations to support namespaces.
 */
@Service
public class NamespaceService extends BaseVersionEntityService<Namespace> {

  @PersistenceContext
  private EntityManager em;

  @Autowired
  NamespaceRepository repo;

  @Autowired
  VersionService versionService;

  public NamespaceRepository repository() {
    return this.repo;
  }

  /**
   * Creates a new namespace in the database with the given fields and adds it to the given version.
   */
  @Transactional
  public Namespace add(Version version, String prefix, String name) throws Exception {
    Namespace namespace = Namespace.builder()
        .version(version)
        .prefix(prefix)
        .name(name)
        .build();
    return this.add(namespace);
  }

  /**
   * Creates a new namespace in the database with the given namespace fields and adds it
   * to the version with the given version fields.
   */
  @Transactional
  public Namespace add(String stewardKey, String modelKey, String versionNumber,
      String prefix, String name) throws Exception {
    Namespace namespace = Namespace.builder()
        .prefix(prefix)
        .name(name)
        .build();
    return this.add(stewardKey, modelKey, versionNumber, namespace);
  }

  /**
   * Creates a new namespace in the database with the given namespace fields and adds it
   * to the version with the given version fields.
   */
  @Transactional
  public Namespace add(String stewardKey, String modelKey, String versionNumber, String prefix)
      throws Exception {
    Namespace namespace = Namespace.builder().prefix(prefix).build();
    return this.add(stewardKey, modelKey, versionNumber, namespace);
  }

  /**
   * Creates a new namespace in the database with the given namespace object and adds it
   * to the version with the given version fields.
   */
  @Transactional
  public Namespace add(String stewardKey, String modelKey, String versionNumber,
      Namespace namespace) throws Exception {
    Version version = versionService.findOne(stewardKey, modelKey, versionNumber);
    return this.add(version, namespace);
  }

  /**
   * Creates a new namespace in the database with the given namespace object and adds it
   * to the given version object.
   */
  @Transactional
  public Namespace add(Version version, Namespace namespace) throws Exception {
    namespace.setVersion(version);
    Namespace result = super.add(namespace);
    version.getNamespaces().add(result);
    return result;
  }

  /**
   * Finds all namespaces in the database.
   */
  public List<Namespace> findAll() {
    return repo.findAll();
  }

  /**
   * Finds all namespaces in the database in the version with the given fields.
   */
  public List<Namespace> findByVersion(String stewardKey, String modelKey, String versionNumber)
      throws EntityNotFoundException {
    Version version = versionService.findOne(stewardKey, modelKey, versionNumber);
    List<Namespace> namespaces = new ArrayList<Namespace>(version.getNamespaces());
    Collections.sort(namespaces);
    return namespaces;
  }

  /**
   * Optionally finds a namespace in the database with the given prefix in the given version.
   */
  public Optional<Namespace> findOneOptional(Version version, String prefix)
      throws EntityNotFoundException {
    return repo.findOneByVersion_IdAndPrefix(version.getId(), prefix);
  }

  /**
   * Optionally finds a namespace in the database with the given prefix in the version
   * with the given version fields.
   */
  public Optional<Namespace> findOneOptional(String stewardKey, String modelKey,
      String versionNumber, String prefix) throws EntityNotFoundException {
    Version version = versionService.findOne(stewardKey, modelKey, versionNumber);
    return repo.findOneByVersion_IdAndPrefix(version.getId(), prefix);
  }

  /**
   * Finds a namespace in the database with the given prefix in the version with the
   * given version fields.
   */
  public Namespace findOne(String stewardKey, String modelKey, String versionNumber, String prefix)
      throws EntityNotFoundException {
    return this.findOneOptional(stewardKey, modelKey, versionNumber, prefix)
    .orElseThrow(() -> this.getNotFoundException(prefix));
  }

  /**
   * Finds a namespace in the database with the given prefix in the version matching
   * the given version object.
   */
  public Namespace findOne(Version version, String prefix) throws EntityNotFoundException {
    return this.findOneOptional(version, prefix)
    .orElseThrow(() -> this.getNotFoundException(prefix));
  }

  /**
   * Finds a namespace in the database matching identifiers in the given namespace object.
   */
  public Namespace findOne(Namespace namespace) throws EntityNotFoundException {
    return this.findOne(namespace.getVersion(), namespace.getPrefix());
  }

  /**
   * Count the number of namespaces with the given fields.
   */
  public long count(String stewardKey, String modelKey, String versionNumber)
      throws EntityNotFoundException {
    Version version = versionService.findOne(stewardKey, modelKey, versionNumber);
    return repo.countByVersion_Id(version.getId());
  }

  /**
   * Count the number of namespaces with the given fields.
   */
  public long count(String stewardKey, String modelKey, String versionNumber,
      Namespace.Category category) throws EntityNotFoundException {
    Version version = versionService.findOne(stewardKey, modelKey, versionNumber);
    if (category == null) {
      return repo.countByVersion_Id(version.getId());
    }
    return repo.countByVersion_IdAndCategory(version.getId(), category);
  }

  /**
   * Checks that all required fields are present.
   */
  public void assertRequiredLocalFields(Namespace namespace) throws FieldNotFoundException {
    assertFieldNotNullAndNotEmpty("prefix", namespace.getPrefix());
  }

  /**
   * Checks that the database does not already contain a namespace with the same identifying
   * fields as the given namespace object.
   */
  public void assertUnique(Namespace namespace) throws EntityNotUniqueException {
    repo
        .findOneByVersion_IdAndPrefix(namespace.getVersion().getId(), namespace.getPrefix())
        .ifPresent(n -> this.throwNotUnique(n));
  }

}
