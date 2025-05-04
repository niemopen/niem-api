package gov.niem.tools.api.db.base;

import gov.niem.tools.api.core.utils.AppUtils;
import gov.niem.tools.api.db.exceptions.EntityNotFoundException;
import gov.niem.tools.api.db.exceptions.EntityNotUniqueException;
import gov.niem.tools.api.db.exceptions.FieldNotFoundException;

import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import jakarta.transaction.Transactional;
import java.util.Optional;
import org.apache.commons.lang3.StringUtils;
import org.springframework.data.jpa.repository.JpaRepository;

/**
 * Abstract class that provides basic database operations for data model items.
 */
public abstract class BaseEntityService<T extends BaseEntity> {

  @PersistenceContext
  private EntityManager em;

  public abstract JpaRepository<T, Long> repository();

  protected String getEntityClassName() {
    return this.getClass().getSimpleName().replace("Service", "");
  }

  /**
   * Gets an object from the database matching the given object criteria.
   */
  public void loadId(T object) throws EntityNotFoundException {
    if (object.getId() == null) {
      // Find the ID from the database and set it on the object
      T result = this.findOne(object);
      object.setId(result.getId());
      object = this.merge(object);
    }
    else {
      // Ensure getting the ID from the object doesn't throw an exception
      this.repository().getReferenceById(object.getId());
    }
  }

  /**
   * Logs the operation, entity class, and entity identifier.
   */
  private static String logOperation(String operation, BaseEntity entity) {
    String message = String.format("%s %s", operation, entity.getClassName());
    return AppUtils.log(message, entity.getIdLabel());
  }

  /**
   * Adds the given object to the database.
   */
  @Transactional
  public T add(T object) throws Exception {
    this.assertRequiredLocalFields(object);
    this.assertUnique(object);
    T result = this.saveNew(object);
    logOperation("Added", result);
    return result;
  }

  /**
   * Modifies the object in the database with the given id with the fields from the given object.
   */
  @Transactional
  public T edit(Long id, T updatedObject) throws Exception {
    updatedObject.setId(id);
    T result = this.saveExisting(updatedObject);
    logOperation("Updated", result);
    return result;
  }

  /**
   * Deletes the object in the database with the id from the given object.
   */
  @Transactional
  public void delete(T object) throws Exception {
    this.repository().delete(object);
    em.flush();
    logOperation("Deleted", object);
  }

  /**
   * If new, adds the given object to the database.  Otherwise, saves the changes.
   */
  @Transactional
  private T save(T object, Boolean isNew) throws Exception {
    if (!isNew) {
      this.loadId(object);
    }
    object = this.repository().saveAndFlush(object);
    em.refresh(object);
    // TODO: Update version last revised date
    return object;
  }

  /**
   * Adds the object to the database.
   */
  @Transactional
  public T saveNew(T object) throws Exception {
    return this.save(object, true);
  }

  /**
   * Saves changes in the database to the existing object based on the object id.
   */
  @Transactional
  public T saveExisting(T object) throws Exception {
    return this.save(object, false);
  }

  /**
   * Syncs the given plain object with object information from the database.
   */
  @Transactional
  public T merge(T object) {
    object = em.merge(object);
    em.refresh(object);
    return object;
  }

  /**
   * Finds an object based on the given object fields.
   */
  public abstract T findOne(T object) throws EntityNotFoundException;

  /**
   * Optionally finds an object based on the given object fields.
   */
  public Optional<T> findOneOptional(T object) {
    try {
      return Optional.of(this.findOne(object));
    }
    catch (Exception e) {
      return Optional.empty();
    }
  }

  /**
   * Counts the total number of entries.
   */
  public long count() {
    return this.repository().count();
  }

  /**
   * Checks that the object has the required fields.
   */
  public abstract void assertRequiredLocalFields(T entity) throws FieldNotFoundException;

  /**
   * Checks that the object will be unique and will not clash with another object in the database.
   */
  public abstract void assertUnique(T entity) throws EntityNotUniqueException;

  /**
   * Checks that the given field value is not null and not empty.
   */
  protected static void assertFieldNotNullAndNotEmpty(String fieldName, String fieldValue)
      throws FieldNotFoundException {
    if (StringUtils.isEmpty(fieldValue)) {
      throw new FieldNotFoundException(fieldName);
    }
  }

  /**
   * Throws an exception for when an entity was not found in the database.
   */
  public EntityNotFoundException getNotFoundException(String label) {
    return new EntityNotFoundException(this.getEntityClassName(), label);
  }

  /**
   * Throws an exception for when an entity was not found in the database.
   */
  public void throwNotFound(BaseEntity entity) throws EntityNotFoundException {
    throwNotFound(entity, entity.getIdLabel());
  }

  /**
   * Throws an exception for when an entity was not found in the database.
   */
  public void throwNotFound(String entityClassName, String label) throws EntityNotFoundException {
    throw new EntityNotFoundException(entityClassName, label);
  }

  /**
   * Throws an exception for when an entity was not found in the database.
   */
  public void throwNotFound(BaseEntity entity, String label) throws EntityNotFoundException {
    throw new EntityNotFoundException(entity.getClassName(), label);
  }

  /**
   * Throws an exception for when an entity with the same required fields already exists
   * in the database.
   */
  public void throwNotUnique(BaseEntity entity) throws EntityNotUniqueException {
    throwNotUnique(entity, entity.getIdLabel());
  }

  /**
   * Throws an exception for when an entity with the same required fields already exists
   * in the database.
   */
  public void throwNotUnique(String entityClassName, String label) throws EntityNotUniqueException {
    throw new EntityNotUniqueException(entityClassName, label);
  }

  /**
   * Throws an exception for when an entity with the same required fields already exists
   * in the database.
   */
  public void throwNotUnique(BaseEntity entity, String label) throws EntityNotUniqueException {
    throw new EntityNotUniqueException(entity.getClassName(), label);
  }

}
