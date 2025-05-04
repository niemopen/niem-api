package gov.niem.tools.api.db.steward;

import gov.niem.tools.api.db.base.BaseEntityService;
import gov.niem.tools.api.db.exceptions.EntityNotFoundException;
import gov.niem.tools.api.db.exceptions.EntityNotUniqueException;
import gov.niem.tools.api.db.exceptions.FieldNotFoundException;

import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import jakarta.transaction.Transactional;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

/**
 * Operations for managing a steward.
 */
@Service
public class StewardService extends BaseEntityService<Steward> {

  @PersistenceContext
  private EntityManager em;

  @Autowired
  public StewardRepository repo;

  public StewardRepository repository() {
    return this.repo;
  }

  /**
   * Add a steward to the database.
   */
  @Transactional
  public Steward add(String shortName) throws Exception {
    return this.add(shortName, null, null, null);
  }

  /**
   * Add a steward to the database.
   */
  @Transactional
  public Steward add(String shortName, String longName) throws Exception {
    return this.add(shortName, longName, null, null);
  }

  /**
   * Add a steward to the database.
   */
  @Transactional
  public Steward add(String shortName, String longName, String category) throws Exception {
    return this.add(shortName, longName, Steward.Category.valueOf(category));
  }

  /**
   * Add a steward to the database.
   */
  @Transactional
  public Steward add(String shortName, String longName, Steward.Category category)
      throws Exception {
    return this.add(shortName, longName, category, null);
  }

  /**
   * Add a steward to the database.
   */
  @Transactional
  public Steward add(String shortName, String longName, Steward.Category category,
      String description) throws Exception {
    Steward steward = new Steward(shortName, longName);
    steward.setCategory(category);
    steward.setDescription(description);
    return this.add(steward);
  }

  /**
   * Modify a steward in the database identified by the given key.  Replace all existing
   * fields with the given fields.
   */
  @Transactional
  public Steward edit(String oldStewardKey, Steward updatedSteward) throws Exception {
    Steward oldSteward = this.findOne(oldStewardKey);

    // TODO: Edit collision check
    // Ensure new values that should be unique do not collide with existing ones
    if (updatedSteward.getShortName() != null) {
      if (! oldSteward.getShortName().equalsIgnoreCase(updatedSteward.getShortName())) {
        this.assertUnique(updatedSteward);
      }
    }

    return this.edit(oldSteward.getId(), updatedSteward);
  }

  /**
   * Delete the steward with the given key.
   */
  @Transactional
  public void delete(String stewardKey) throws Exception {
    Steward steward = this.findOne(stewardKey);
    this.delete(steward);
  }

  // TODO: Add cascading deletes

  /**
   * Optionally find one steward by the given key.
   */
  public Optional<Steward> findOneOptional(String stewardKey) {
    return repo.findOneByStewardKey(stewardKey);
  }

  /**
   * Find one steward by the key in the given steward object.
   */
  public Steward findOne(Steward steward) throws EntityNotFoundException {
    return this.findOne(steward.getStewardKey());
  }

  /**
   * Find one steward by the given key.
   */
  public Steward findOne(String stewardKey) throws EntityNotFoundException {
    return repo
    .findOneByStewardKey(stewardKey)
    .orElseThrow(() -> new EntityNotFoundException("Steward", stewardKey));
  }

  /**
   * Find one steward by the short name in the given steward object.
   */
  public Steward findOneByShortName(String shortName) throws EntityNotFoundException {
    return repo
    .findOneByShortName(shortName)
    .orElseThrow(() -> new EntityNotFoundException("Steward", shortName));
  }

  /**
   * Find the NIEM steward.
   */
  public Steward findOneNiem() throws EntityNotFoundException {
    return this.findOne(Steward.niemStewardKey);
  }

  /**
   * Gets a sorted list of all stewards in the database.
   */
  public List<Steward> findAll() {
    List<Steward> stewards = repo.findAll();
    Collections.sort(stewards);
    return stewards;
  }

  /**
   * Check that the given steward has all required fields.
   */
  public void assertRequiredLocalFields(Steward steward) throws FieldNotFoundException {
    assertFieldNotNullAndNotEmpty("shortName", steward.getShortName());
  }

  /**
   * Check that the given steward is unique from other stewards in the database.
   */
  public void assertUnique(Steward steward) throws EntityNotUniqueException {
    this.assertShortNameDoesNotExist(steward.getShortName());
    this.assertKeyDoesNotExist(steward.getShortName());
  }

  /**
   * Check that existing stewards in the database do not already have the given short name.
   */
  private void assertShortNameDoesNotExist(String shortName) throws EntityNotUniqueException {
    repo.findOneByShortName(shortName).ifPresent(
        steward -> this.throwNotUnique(steward, shortName)
    );
  }

  /**
   * Check that existing stewards in the database do not already have a key based on
   * the given short name.
   */
  private void assertKeyDoesNotExist(String shortName) throws EntityNotUniqueException {
    String key = repo.slugify(shortName);
    repo.findOneByStewardKey(key).ifPresent(
        steward -> this.throwNotUnique(steward, key)
    );
  }

}
