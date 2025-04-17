package gov.niem.tools.api.db.base;

import java.util.Optional;

import org.apache.commons.lang3.StringUtils;
import org.springframework.data.jpa.repository.JpaRepository;

import gov.niem.tools.api.core.utils.AppUtils;
import gov.niem.tools.api.db.exceptions.EntityNotFoundException;
import gov.niem.tools.api.db.exceptions.EntityNotUniqueException;
import gov.niem.tools.api.db.exceptions.FieldNotFoundException;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import jakarta.transaction.Transactional;

public abstract class BaseEntityService<T extends BaseEntity> {

  @PersistenceContext
  private EntityManager em;

  public abstract JpaRepository<T, Long> repository();

  protected String getEntityClassName() {
    return this.getClass().getSimpleName().replace("Service", "");
  }

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

  private static String logOperation(String operation, BaseEntity entity) {
    String message = String.format("%s %s", operation, entity.getClassName());
    return AppUtils.log(message, entity.getFullIdentifier());
  }

  @Transactional
  public T add(T object) throws Exception {
    this.assertRequiredLocalFields(object);
    this.assertUnique(object);
    T result = this.saveNew(object);
    logOperation("Added", result);
    return result;
  }

  @Transactional
  public T edit(Long id, T updatedObject) throws Exception {
    updatedObject.setId(id);
    T result = this.saveExisting(updatedObject);
    logOperation("Updated", result);
    return result;
  }

  @Transactional
  public void delete(T object) throws Exception {
    this.repository().delete(object);
    em.flush();
    logOperation("Deleted", object);
  }

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

  @Transactional
  public T saveNew(T object) throws Exception {
    return this.save(object, true);
  }

  @Transactional
  public T saveExisting(T object) throws Exception {
    return this.save(object, false);
  }

  @Transactional
  public T merge(T object) {
    object = em.merge(object);
    em.refresh(object);
    return object;
  }

  public abstract T findOne(T object) throws EntityNotFoundException;

  public Optional<T> findOneOptional(T object) {
    try {
      return Optional.of(this.findOne(object));
    }
    catch (Exception e) {
    }
    return Optional.empty();
  }

  public abstract void assertRequiredLocalFields(T entity) throws FieldNotFoundException;

  public abstract void assertUnique(T entity) throws EntityNotUniqueException;

  protected static void assertFieldNotNullAndNotEmpty(String fieldName, String fieldValue)
      throws FieldNotFoundException {
    if (StringUtils.isEmpty(fieldValue)) {
      throw new FieldNotFoundException(fieldName);
    }
  }

  public EntityNotFoundException getNotFoundException(String label) {
    return new EntityNotFoundException(this.getEntityClassName(), label);
  }

  public void throwNotFound(BaseEntity entity) throws EntityNotFoundException {
    throwNotFound(entity, entity.getFullIdentifier());
  }

  public void throwNotFound(String entityClassName, String label) throws EntityNotFoundException {
    throw new EntityNotFoundException(entityClassName, label);
  }

  public void throwNotFound(BaseEntity entity, String label) throws EntityNotFoundException {
    throw new EntityNotFoundException(entity.getClassName(), label);
  }

  public void throwNotUnique(BaseEntity entity) throws EntityNotUniqueException {
    throwNotUnique(entity, entity.getFullIdentifier());
  }

  public void throwNotUnique(String entityClassName, String label) throws EntityNotUniqueException {
    throw new EntityNotUniqueException(entityClassName, label);
  }

  public void throwNotUnique(BaseEntity entity, String label) throws EntityNotUniqueException {
    throw new EntityNotUniqueException(entity.getClassName(), label);
  }

}
