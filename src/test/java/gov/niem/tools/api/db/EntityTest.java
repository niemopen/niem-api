package gov.niem.tools.api.db;

import gov.niem.tools.api.db.base.BaseEntity;
import gov.niem.tools.api.db.base.BaseEntityService;
import gov.niem.tools.api.db.exceptions.EntityNotUniqueException;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.transaction.Transactional;
import java.lang.reflect.Method;
import java.util.Optional;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

/**
 * Common methods for testing database operations.
 */
@ActiveProfiles("test")
@Transactional
@SpringBootTest
public abstract class EntityTest<T extends BaseEntity> {

  @Autowired
  protected ServiceHub hub;

  /**
   * The Service for the given object.
   */
  protected abstract BaseEntityService<T> service();

  /**
   * Add related objects to the database.  Load test data for the current
   * kind of object but do not add to the database.
   */
  protected abstract void init() throws Exception;

  /**
   * Load the objects into the database.
   */
  protected abstract void loadObjects() throws Exception;

  /**
   * Test that adding objects correctly to the database does not throw an exception.
   * Objects must be unique and have the required fields.
   */
  public abstract void databaseAddTest();

  /**
   * Test that trying to add an object twice will throw EntityNotUniqueException.
   */
  public abstract void databaseAddDuplicateTest();

  /**
   * Test that updates are persisted to the database after adding an object and then editing
   * a field.
   */
  public abstract void databaseEditTest() throws Exception;

  /**
   * Test that deleting an object removes it from the database without removing another object.
   */
  public abstract void databaseDeleteTest() throws Exception;

  // TODO: Test cascading deletes
  // public abstract void deleteCascadingTest();

  /**
   * Test that a single entity can be returned from the database.
   */
  public abstract void databaseFindOneTest() throws Exception;

  /**
   * Test the an entity has the expected label.
   */
  public abstract void objectLabelTest() throws Exception;

  /**
   * Test that the object can be successfully serialized and does not
   * run into infinite recursion.
   */
  public abstract void objectSerializationTest() throws Exception;

  /**
   * Add an object without throwing an exception.
   */
  protected void add(T object) {
    assertDoesNotThrow(() -> this.service().add(object));
  }

  /**
   * Try to add the given object twice.
   * The first add should succeed; the second add should throw an EntityNotUniqueException.
   */
  protected void addDuplicate(T object) {
    assertDoesNotThrow(() -> this.service().add(object));
    assertThrows(EntityNotUniqueException.class, () -> this.service().add(object));
  }

  /**
   * Add the given object and then update the given field name with the given value.
   * Pull the object from the database to make sure it has the updated field.
   */
  protected void edit(T object, String fieldName, String fieldValue) throws Exception {
    // Add the object
    this.service().add(object);

    // Update the given field on the object and call the edit function to save the changes
    Method setter = object.getClass().getMethod("set" + StringUtils.capitalize(fieldName),
        String.class);
    setter.invoke(object, fieldValue);
    this.service().edit(object.getId(), object);

    // Pull a fresh copy of the object from the database and ensure changes were persisted
    T result = this.service().findOne(object);
    Method getter = object.getClass().getMethod("get" + StringUtils.capitalize(fieldName));
    String resultValue = (String) getter.invoke(result);
    assertEquals(fieldValue, resultValue);
  }

  /**
   * Check that an entity can be deleted and that a related entity in a relationship
   * is removed via cascading delete.
   */
  protected <U extends BaseEntity> void deleteCascading(
      BaseEntityService<T> primaryService, T entity1, T entity2,
      BaseEntityService<U> relatedService) throws Exception {
    this.delete(primaryService, entity1, entity2, relatedService, true);
  }

  /**
   * Check that an entity can be deleted and that a related entity in a relationship
   * is not removed via cascading delete.
   */
  protected <U extends BaseEntity> void deleteNonCascading(
      BaseEntityService<T> primaryService, T entity1, T entity2,
      BaseEntityService<U> relatedService) throws Exception {
    this.delete(primaryService, entity1, entity2, relatedService, false);
  }

  /**
   * Check that an entity can be deleted and that a related entity in a relationship
   * is either present or removed via cascading delete.
   */
  private <U extends BaseEntity> void delete(
      BaseEntityService<T> primaryService, T entity1, T entity2,
      BaseEntityService<U> relatedService, Boolean cascadeDelete) throws Exception {

    // Add two entities to the primary service
    primaryService.add(entity1);
    primaryService.add(entity2);

    // Delete one entity
    primaryService.delete(entity1);

    // Check that the deleted entity cannot be found and the other entity is still available
    Optional<T> result1 = primaryService.findOneOptional(entity1);
    Optional<T> result2 = primaryService.findOneOptional(entity2);

    assertTrue(result1.isEmpty());
    assertTrue(result2.isPresent());

    // Count the number of entities in the primary and related services
    Long initialPrimaryCount = primaryService.repository().count();
    Long initialRelatedCount = relatedService.repository().count();

    // Get the new counts
    Long updatedPrimaryCount = primaryService.repository().count();
    Long updatedRelatedCount = relatedService.repository().count();

    // Make sure the number of primary entities decreased by one
    assertEquals(initialPrimaryCount - 1, updatedPrimaryCount);

    if (cascadeDelete) {
      // Make sure the delete cascaded and the number of related entities decreased
      assertTrue(updatedRelatedCount < initialRelatedCount);
    }
    else {
      // Make sure the delete did not cascade and did not affect the number of related entities
      assertEquals(initialRelatedCount, updatedRelatedCount);
    }
  }

  /**
   * Test that the object can be successfully serialized and does not
   * run into infinite recursion.
   */
  public String serializeObject(T object) throws Exception {
    ObjectMapper objectMapper = new ObjectMapper();
    return objectMapper.writeValueAsString(object);
  }

}
