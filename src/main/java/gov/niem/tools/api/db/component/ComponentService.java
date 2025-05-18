package gov.niem.tools.api.db.component;

import gov.niem.tools.api.db.base.BaseVersionEntityService;
import gov.niem.tools.api.db.exceptions.EntityNotFoundException;
import gov.niem.tools.api.db.exceptions.EntityNotUniqueException;
import gov.niem.tools.api.db.exceptions.FieldNotFoundException;
import gov.niem.tools.api.db.namespace.Namespace;
import gov.niem.tools.api.db.namespace.NamespaceService;
import gov.niem.tools.api.db.version.Version;
import gov.niem.tools.api.db.version.VersionService;

import jakarta.transaction.Transactional;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import org.hibernate.Hibernate;
import org.hibernate.proxy.HibernateProxy;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

/**
 * Operations for managing a component.
 */
public abstract class ComponentService<T extends Component<T>, U extends ComponentRepository<T>>
    extends BaseVersionEntityService<T> {

  @Autowired
  protected U repo;

  @Autowired
  protected VersionService versionService;

  @Autowired
  protected NamespaceService namespaceService;

  public U repository() {
    return this.repo;
  }

  /**
   * Add a component to the database.
   */
  @Transactional
  public T add(String stewardKey, String modelKey, String versionKey, T component)
      throws Exception {
    if (component.getNamespace() == null) {
      Namespace namespace = namespaceService.findOne(
          stewardKey, modelKey, versionKey, component.getPrefix());
      component.setNamespace(namespace);
    }
    Version version = versionService.findOne(stewardKey, modelKey, versionKey);
    return this.add(version, component);
  }

  /**
   * Add a component to the database.
   */
  @Transactional
  public T add(Version version, String prefix, String name, T component) throws Exception {
    Namespace namespace = namespaceService.findOne(version, prefix);
    return this.add(namespace, name, component);
  }

  /**
   * Add a component to the database.
   */
  @Transactional
  public T add(Version version, T component) throws Exception {
    component.getNamespace().setVersion(version);
    return this.add(component);
  }

  /**
   * Add a component to the database.
   */
  @Transactional
  public T add(Namespace namespace, String name, T component) throws Exception {
    component.setName(name);
    component.setPrefix(namespace.getPrefix());
    return this.add(namespace, component);
  }

  /**
   * Add a component to the database.
   */
  @Transactional
  public T add(Namespace namespace, T component) throws Exception {
    component.setNamespace(namespace);
    component.setPrefix(namespace.getPrefix());
    return this.add(component);
  }

  /**
   * Add a component to the database.
   */
  @Transactional
  public T add(T component) throws Exception {
    component = super.add(component);
    return component;
  }

  /**
   * Find a component in the database matching identifying fields in the given component.
   */
  public T findOne(T component) throws EntityNotFoundException {
    return this.findOne(
        component.getStewardKey(), component.getModelKey(), component.getVersionNumber(),
        component.getPrefix(), component.getName());
  }

  /**
   * Find a component in the database matching the given version and qualified name.
   */
  public T findOne(Version version, String qname) throws EntityNotFoundException {
    return this.findOne(
        version.getStewardKey(), version.getModelKey(), version.getVersionNumber(), qname);
  }

  /**
   * Find a component in the database matching the given fields.
   */
  public T findOne(String stewardKey, String modelKey, String versionNumber, String qname)
      throws EntityNotFoundException {
    return this
        .findOneOptional(stewardKey, modelKey, versionNumber, qname)
        .orElseThrow(() -> this.getNotFoundException(qname));
  }

  /**
   * Find a component in the database matching the given fields.
   */
  public T findOne(String stewardKey, String modelKey, String versionNumber, String prefix,
      String name) throws EntityNotFoundException {
    return this
        .findOneOptional(stewardKey, modelKey, versionNumber, prefix, name)
        .orElseThrow(() -> this.getNotFoundException(prefix + ":" + name));
  }

  /**
   * Optionally find a component in the database matching the given fields.
   */
  public Optional<T> findOneOptional(Version version, String qname) throws EntityNotFoundException {
    // return repo.findOneByNamespace_Version_IdAndQname(version.getId(), qname);
    return this.findOneOptional(version.getStewardKey(), version.getModelKey(),
        version.getVersionNumber(), getQualifiedPrefix(qname), getQualifiedName(qname));
  }

  /**
   * Optionally find a component in the database matching the given fields.
   */
  public Optional<T> findOneOptional(String stewardKey, String modelKey,
      String versionNumber, String qname) throws EntityNotFoundException {
    // Version version = versionService.findOne(stewardKey, modelKey, versionNumber);
    // return repo.findOneByNamespace_Version_IdAndQname(version.getId(), qname);
    return this.findOneOptional(stewardKey, modelKey, versionNumber,
        getQualifiedPrefix(qname), getQualifiedName(qname));
  }

  /**
   * Optionally find a component in the database matching the given fields.
   */
  @SuppressWarnings("unchecked")
  public Optional<T> findOneOptional(String stewardKey, String modelKey,
      String versionNumber, String prefix, String name) throws EntityNotFoundException {

    Namespace namespace = namespaceService.findOne(stewardKey, modelKey, versionNumber, prefix);
    Optional<T> result = repo.findOneByNamespace_IdAndName(namespace.getId(), name);

    // Initialize if the result is a Hibernate proxy due to lazy loading
    if (result.isPresent()) {
      T component = result.get();
      if (component instanceof HibernateProxy) {
        component = (T) Hibernate.unproxy(component);
      }
      return Optional.of(component);
    }

    return result;
  }

  /**
   * Find a page of all components in the version with the given fields.
   */
  public Page<T> findByVersion(String stewardKey, String modelKey, String versionKey,
      Pageable pageable) throws Exception {
    Version version = versionService.findOne(stewardKey, modelKey, versionKey);
    return repo.findAllByNamespace_Version_Id(version.getId(), pageable);
  }

  /**
   * Find a page of all components in the namespace with the given fields.
   */
  public Page<T> findByNamespace(String stewardKey, String modelKey, String versionKey,
      String prefix, Pageable pageable) throws Exception {
    Namespace namespace = namespaceService.findOne(stewardKey, modelKey, versionKey, prefix);
    return repo.findAllByNamespace_Id(namespace.getId(), pageable);
  }

  /**
   * Find a list of the top components matching the keyword in the version with the given fields.
   *
   * <p>Note that the keyword may be qualified with a namespace prefix.
   *
   * <p>Additional, a qualified keyword or a keyword with a leading ':' (but no prefix)
   * will be treated as name-starts-wth vs leading and trailing wildcards.
   */
  public List<T> findByKeyword(String stewardKey, String modelKey, String versionKey,
      String keyword) throws Exception {

    if (keyword.contains(" ")) {
      // Spaces in a keyword are not supported
      return new ArrayList<>();
    }

    Version version = versionService.findOne(stewardKey, modelKey, versionKey);

    String prefix = "";
    String nameKeyword = "";

    if (keyword.contains(":")) {
      // Split the keyword into prefix and name
      prefix = keyword.split(":")[0];
      nameKeyword = keyword.split(":")[1] + "%";
    }
    else {
      nameKeyword = "%" + keyword + "%";
    }

    if (prefix.length() > 0) {
      // Component keyword search within the given namespace
      Namespace namespace = namespaceService.findOne(version, prefix);
      if (namespace == null) {
        throw new EntityNotFoundException("namespace", prefix);
      }
      return repo.findTop10ByNamespace_IdAndNameLikeIgnoreCase(namespace.getId(), nameKeyword);
    }
    else {
      // Component keyword search within the given version
      return repo.findTop10ByNamespace_Version_IdAndNameLikeIgnoreCase(version.getId(),
        nameKeyword);
    }

  }

  /**
   * Count all components in a version with the given fields.
   */
  public long countByVersion(String stewardKey, String modelKey, String versionNumber)
      throws EntityNotFoundException {
    Version version = versionService.findOne(stewardKey, modelKey, versionNumber);
    return repo.countByNamespace_Version_Id(version.getId());
  }

  /**
   * Count all components in a namespace with the given fields.
   */
  public long countByNamespace(String stewardKey, String modelKey, String versionNumber,
      String prefix) throws EntityNotFoundException {
    Namespace namespace = namespaceService.findOne(stewardKey, modelKey, versionNumber, prefix);
    return repo.countByNamespace_Id(namespace.getId());
  }

  /**
   * Check that the given component has its required fields.
   */
  public void assertRequiredLocalFields(T component) throws FieldNotFoundException {
    assertFieldNotNullAndNotEmpty("prefix", component.getPrefix());
    assertFieldNotNullAndNotEmpty("name", component.getName());
  }

  /**
   * Check that the required fields in the given component do not already exist in the database.
   */
  public void assertUnique(T component) throws EntityNotUniqueException {
    this.repository()
        .findOneByNamespace_IdAndName(component.getNamespaceId(), component.getName())
        .ifPresent(c -> this.throwNotUnique(component));
  }

  /**
   * Return the prefix from the given qualified name.
   */
  public static String getQualifiedPrefix(String qname) {
    if (qname.contains(":")) {
      return qname.split(":")[0];
    }
    return null;
  }

  /**
   * Return the name from the given qualified name.
   */
  public static String getQualifiedName(String qname) {
    if (qname.contains(":")) {
      return qname.split(":")[1];
    }
    return null;
  }

}
