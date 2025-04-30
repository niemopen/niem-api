package gov.niem.tools.api.db.version;

import gov.niem.tools.api.db.base.BaseEntityService;
import gov.niem.tools.api.db.exceptions.EntityNotFoundException;
import gov.niem.tools.api.db.exceptions.EntityNotUniqueException;
import gov.niem.tools.api.db.exceptions.FieldNotFoundException;
import gov.niem.tools.api.db.model.Model;
import gov.niem.tools.api.db.model.ModelService;
import gov.niem.tools.api.db.steward.Steward;

import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import jakarta.transaction.Transactional;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

/**
 * Operations supporting versions.
 */
@Service
public class VersionService extends BaseEntityService<Version> {

  @PersistenceContext
  private EntityManager em;

  @Autowired
  VersionRepository repo;

  @Autowired
  ModelService modelService;

  public VersionRepository repository() {
    return this.repo;
  }

  @Transactional
  public Version add(String stewardKey, String modelKey, String versionNumber) throws Exception {
    Version version = Version.builder().versionNumber(versionNumber).build();
    return this.add(stewardKey, modelKey, version);
  }

  // @Transactional
  // public Version add(String stewardKey, String modelKey, String versionNumber,
  //     String previousVersionNumber) throws Exception {
  //   // Set the previous version field on the given version
  //   Version previousVersion = this.findOne(stewardKey, modelKey, previousVersionNumber);

  //   Version version = Version.builder()
  //   .versionNumber(versionNumber)
  //   .previousVersion(previousVersion)
  //   .build();

  //   version = this.add(stewardKey, modelKey, version);

  //   // Set the corresponding next version field on the previous field to the given version
  //   previousVersion.setNextVersion(version);
  //   repo.save(previousVersion);

  //   return version;
  // }

  /**
   * Creates a new version in the database with the fields in the given version object
   * and adds it to the model with the given model fields.
   */
  @Transactional
  public Version add(String stewardKey, String modelKey, Version version) throws Exception {
    Model model = modelService.findOne(stewardKey, modelKey);
    version.setModel(model);
    return super.add(version);
  }

  /**
   * Creates a new version in the database with the given version number and adds it
   * to the given model.
   */
  @Transactional
  public Version add(Model model, String versionNumber) throws Exception {
    Version version = Version.builder()
        .model(model)
        .versionNumber(versionNumber)
        .build();
    return super.add(version);
  }

  /**
   * Updates a version in the database with the given fields to the fields in the
   * given version object.
   */
  @Transactional
  public Version edit(String oldStewardKey, String oldModelKey, String oldVersionNumber,
      Version updatedVersion) throws Exception {
    Version oldVersion = this.findOne(oldStewardKey, oldModelKey, oldVersionNumber);

    // TODO: Collision check

    return this.edit(oldVersion.getId(), updatedVersion);
  }

  /**
   * Deletes a version in the database with the given fields.
   */
  @Transactional
  public void delete(String stewardKey, String modelKey, String versionNumber) throws Exception {
    Version version = this.findOne(stewardKey, modelKey, versionNumber);
    this.delete(version);
  }

  // public void setNiemVersion(Version version, String niemVersionNumber) throws Exception {
  //   this.loadId(version);
  //   Version niemVersion = this.findOneNiem(niemVersionNumber);
  //   version.setNiemVersion(niemVersion);
  //   this.saveExisting(version);
  // }

  /**
   * Finds all versions in the database.
   */
  public List<Version> findAll() {
    return repo.findAll();
  }

  /**
   * Finds the version in the database with the identifying fields from the given
   * version object.
   */
  public Version findOne(Version version) throws EntityNotFoundException {
    return this.findOne(version.getStewardKey(), version.getModelKey(), version.getVersionNumber());
  }

  /**
   * Finds the version in the database with the given fields.
   */
  public Version findOne(String stewardKey, String modelKey, String versionNumber)
      throws EntityNotFoundException {
    return this.findOneOptional(stewardKey, modelKey, versionNumber)
    .orElseThrow(() -> this.getNotFoundException(versionNumber));
  }

  /**
   * Optionally finds the version in the database with the given fields.
   */
  public Optional<Version> findOneOptional(String stewardKey, String modelKey,
      String versionNumber) {
    return repo.findOneByModel_Steward_StewardKeyAndModel_ModelKeyAndVersionNumber(
      stewardKey, modelKey, versionNumber);
  }

  /**
   * Finds the version from the NIEM reference model with the given version number.
   */
  public Version findOneNiem(String versionNumber) throws EntityNotFoundException {
    return this.findOne(Steward.niemStewardKey, Model.niemModelKey, versionNumber);
  }

  /**
   * Finds all versions in the model with the given fields.
   */
  public Set<Version> findByKeys(String stewardKey, String modelKey) throws Exception {
    Model model = modelService.findOne(stewardKey, modelKey);
    return model.getVersions();
  }

  /**
   * Finds the version with the given fields.
   */
  public Long findId(String stewardKey, String modelKey, String versionNumber)
      throws EntityNotFoundException {
    Version version = this.findOne(stewardKey, modelKey, versionNumber);
    return version.getId();
  }

  /**
   * Count the number of versions for the model with the given fields.
   */
  public long count(String stewardKey, String modelKey) throws EntityNotFoundException {
    Model model = modelService.findOne(stewardKey, modelKey);
    return repo.countByModelId(model.getId());
  }

  public void assertRequiredLocalFields(Version version) throws FieldNotFoundException {
    assertFieldNotNullAndNotEmpty("versionNumber", version.getVersionNumber());
  }

  /**
   * Checks the the database does not contain another version with the same
   * identifying fields as the given version object.
   */
  public void assertUnique(Version version) throws EntityNotUniqueException {
    repo
        .findOneByModel_Steward_StewardKeyAndModel_ModelKeyAndVersionNumber(
          version.getStewardKey(), version.getModelKey(), version.getVersionNumber())
        .ifPresent(v -> this.throwNotUnique(v));
  }

}
