package gov.niem.tools.api.migrate;

import gov.niem.tools.api.core.exceptions.BadRequestException;
import gov.niem.tools.api.core.utils.CmfUtils;
import gov.niem.tools.api.core.utils.CsvUtils;
import gov.niem.tools.api.core.utils.FileUtils;
import gov.niem.tools.api.core.utils.JsonUtils;
import gov.niem.tools.api.core.utils.ZipUtils;
import gov.niem.tools.api.db.ServiceHub;
import gov.niem.tools.api.db.base.AddModelReason;
import gov.niem.tools.api.db.component.Component;
import gov.niem.tools.api.db.exceptions.EntityNotFoundException;
import gov.niem.tools.api.db.facet.Facet;
import gov.niem.tools.api.db.namespace.Namespace;
import gov.niem.tools.api.db.property.Property;
import gov.niem.tools.api.db.subproperty.Subproperty;
import gov.niem.tools.api.db.type.Type;
import gov.niem.tools.api.db.version.Version;
import gov.niem.tools.api.validation.Test;
import gov.niem.tools.api.validation.Test.Severity;
import gov.niem.tools.api.validation.TestReport;
import gov.niem.tools.api.validation.TestResult;
import gov.niem.tools.api.validation.TestResult.Status;

import org.mitre.niem.cmf.AugmentRecord;
import org.mitre.niem.cmf.ClassType;
import org.mitre.niem.cmf.DataProperty;
import org.mitre.niem.cmf.Datatype;
import org.mitre.niem.cmf.PropertyAssociation;
import org.mitre.niem.cmf.Restriction;

import jakarta.persistence.EntityManager;
import java.io.File;
import java.nio.file.Path;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;
import lombok.extern.log4j.Log4j2;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.multipart.MultipartFile;

/**
 * Migrate a CMF model from one version to any subsequent version.
 */
@org.springframework.stereotype.Component
@Log4j2
public class MigrationService {

  @Autowired
  EntityManager em;

  @Autowired
  ServiceHub hub;

  // Migration status comments for the report
  private static final String NO_MIGRATION = "No migration available";
  private static final String NOT_FOUND = "Not found in this version";
  private static final String PASSED = "Migration succeeded";
  private static final String DEPENDENCY = "Dependency added";

  /**
   * Migrate a CMF model from the given version to the target version.
   *
   * @param stewardKey Identifies the owner of the model
   * @param modelKey With the stewardKey, identifies the model
   * @param from Current version of the model provided by the user
   * @param to Version to which the model should be migrated
   */
  public byte[] migrateCmf(String stewardKey, String modelKey,
      String from, String to, MultipartFile multipartFile) throws Exception {

    log.info("Migrate %s/%s CMF from version [%s] to [%s]", stewardKey, modelKey, from, to);

    // Check for valid params
    checkParams(stewardKey, modelKey, from, to);

    // Initialize the migration report
    TestReport results = new TestReport();

    // Load the original version
    Version oldVersion = hub.versions.findOne(stewardKey, modelKey, from);

    // Read a given CMF file and load into a new CMF model.
    org.mitre.niem.cmf.Model oldCmf = CmfUtils.loadCmf(multipartFile);

    org.mitre.niem.cmf.Model newCmf = new org.mitre.niem.cmf.Model();

    // Loop through each version in the migration chain
    while (!oldVersion.getVersionNumber().equals(to)) {
      Version newVersion = oldVersion.getNext();
      String label = String.format("%s to %s migration",
          oldVersion.getVersionNumber(), newVersion.getVersionNumber());
      log.debug(String.format(label + " started"));

      // Run the current migration
      newCmf = this.migrateCmf(oldVersion, oldCmf, results);
      log.debug(String.format(label + " completed"));

      // Prep the next migration iteration
      oldVersion = newVersion;
      oldCmf = newCmf;
    }

    // Log a summary of the migration results
    int oldTotalComponentCount = this.getCmfModelComponentCount(oldCmf);
    int newComponentCount = this.getCmfModelComponentCount(newCmf);
    results.comment = this.getMigrationComment(oldTotalComponentCount,
        newComponentCount);

    byte[] bytes = this.saveOutput(newCmf, results, multipartFile, from, to);
    return bytes;

  }

  /**
   * Run a single migration iteration.
   *
   * @param oldVersion Current version to be migrated.
   * @param oldCmf Current CMF model to be migrated.
   * @param results Migration report to be updated with results of this migration.
   */
  private org.mitre.niem.cmf.Model migrateCmf(Version oldVersion,
      org.mitre.niem.cmf.Model oldCmf, TestReport results) throws Exception {

    // Create new test to capture issues from migrating current version to the next version and
    // start the migration timer
    Test test = this.initTest(results, oldVersion);

    // Set up a new CMF model for the results of the migration
    org.mitre.niem.cmf.Model newCmf = new org.mitre.niem.cmf.Model();

    // Migrate each object property in the CMF model
    for (org.mitre.niem.cmf.Property cmfProperty : oldCmf.propertyL()) {
      log.debug(String.format("Migrating %s %s",
          oldVersion.getVersionNumber(), cmfProperty.qname()));
      this.migrateProperty(oldVersion, cmfProperty.qname(), newCmf, test);
    }

    // Migrate each data property in the CMF model
    for (DataProperty cmfDataProperty : oldCmf.dataPropertyL()) {
      log.debug(String.format("Migrating %s %s",
          oldVersion.getVersionNumber(), cmfDataProperty.qname()));
      this.migrateProperty(oldVersion, cmfDataProperty.qname(), newCmf, test);
    }

    // Migrate each class type in the CMF model
    for (ClassType cmfClassType : oldCmf.classTypeL()) {
      log.debug(String.format("Migrating %s %s",
          oldVersion.getVersionNumber(), cmfClassType.qname()));
      this.migrateCmfType(oldVersion, cmfClassType.qname(), newCmf, test, cmfClassType);
    }

    // Migrate each datatype in the CMF model
    for (Datatype cmfDatatype : oldCmf.datatypeL()) {
      log.debug(String.format("Migrating %s %s",
          oldVersion.getVersionNumber(), cmfDatatype.qname()));
      this.migrateCmfType(oldVersion, cmfDatatype.qname(), newCmf, test, null);
    }

    // Migrate each namespace's augmentation record list in the CMF model
    for (org.mitre.niem.cmf.Namespace cmfNamespace : oldCmf.namespaceList()) {
      log.debug(String.format("Migrating %s %s augmentation records",
          oldVersion.getVersionNumber(), cmfNamespace.prefix()));
      this.migrateCmfNamespaceAugmentationRecordList(oldVersion, cmfNamespace.augL(), newCmf, test,
          cmfNamespace.prefix());
    }

    // Log summary of results and stop the migration timer
    int oldComponentCount = this.getCmfModelComponentCount(oldCmf);
    int newComponentCount = this.getCmfModelComponentCount(newCmf);
    test.comments = this.getMigrationComment(oldComponentCount, newComponentCount);
    test.endTest();

    return newCmf;

  }

  /**
   * Migrate a property.
   *
   * @param oldVersion Current version to be migrated.
   * @param qname Qualified name of the property to be migrated.
   * @param newCmf Current components that have already been migrated.
   * @param test Test from the migration report to be updated with results of this migration.
   */
  private boolean migrateProperty(Version oldVersion, String qname,
      org.mitre.niem.cmf.Model newCmf, Test test) throws Exception {

    Property oldProperty = null;

    // Find old property
    try {
      oldProperty = hub.properties.findOne(oldVersion, qname);
    }
    catch (EntityNotFoundException exception) {
      // Exception handled in the hasMigration method
    }

    // Make sure property exists and has a migration
    if (!this.hasMigration(oldProperty, test, qname, "Property")) {
      return false;
    }

    // Find the migrated property
    @SuppressWarnings("null")
    Property newProperty = oldProperty.getNext();

    // Add the migrated property to the new CMF model
    this.addComponentToCmf(newProperty, newCmf, test, oldProperty, true);

    return true;

  }

  /**
   * Add a property's substitution group and type and their dependencies.
   */
  private void addPropertyDependencies(Property property, org.mitre.niem.cmf.Model cmf,
      Test test) throws Exception {

    // Add substitution group and its dependencies
    Property group = property.getGroup();
    if (group != null) {
      this.addComponentToCmf(group, cmf, test, property, false);
      this.addPropertyDependencies(group, cmf, test);
    }

    // Add type and its dependencies
    Type type = property.getType();
    if (type != null) {
      this.addComponentToCmf(type, cmf, test, property, false);
      this.addTypeDependencies(type, cmf, test);
    }

  }

  private void addTypeDependencies(Type type, org.mitre.niem.cmf.Model cmf, Test test)
      throws Exception {

    // Add base type dependency or parent type inheritance chain
    Type base = type.getBase();
    if (base != null) {
      this.addComponentToCmf(base, cmf, test, type, false);
      this.addTypeDependencies(base, cmf, test);
    }

  }

  /**
   * Migrate a type.
   *
   * @param oldVersion Current version to be migrated.
   * @param qname Qualified name of the type to be migrated.
   * @param newCmf Current components that have already been migrated.
   * @param test Test from the migration report to be updated with results of this migration.
   * @param oldCmfClassType Contains type subproperties that will also need to be migrated.
   */
  private boolean migrateCmfType(Version oldVersion, String qname,
      org.mitre.niem.cmf.Model newCmf, Test test, ClassType oldCmfClassType) throws Exception {

    // Find old type to be migrated
    Type oldType = null;

    try {
      oldType = hub.types.findOne(oldVersion, qname);
    }
    catch (EntityNotFoundException exception) {
      // Exception handled in hasMigration method
    }

    // Make sure type exists and has a migration
    if (!hasMigration(oldType, test, qname, "Type")) {
      return false;
    }

    // Find the migrated type
    @SuppressWarnings("null")
    Type newType = oldType.getNext();

    this.addComponentToCmf(newType, newCmf, test, oldType, true);

    // Add any facets that exist on the type
    if (newType.isSimple()) {
      Datatype cmfDatatype = newCmf.qnToDatatype(newType.getQname());
      Restriction restriction = cmfDatatype.asRestriction();

      for (Facet facet : newType.getFacets()) {
        org.mitre.niem.cmf.Facet cmfFacet = facet.toCmf();
        restriction.addFacet(cmfFacet);
      }
    }

    // Migrate subproperties
    if (oldCmfClassType != null) {
      ClassType newCmfClassType = newCmf.qnToClassType(newType.getQname());
      for (PropertyAssociation oldPropertyAssociation : oldCmfClassType.propL()) {
        this.migrateSubproperty(oldVersion, oldType, oldPropertyAssociation, newCmfClassType, test);
      }
    }

    return true;

  }

  /**
   * Migrate a type-contains-property relationship.
   *
   * @param oldVersion Current version to be migrated.
   * @param oldType The old type being migrated.
   * @param oldPropertyAssociation CMF property association being migrated.
   * @param newCmfClassType CMF type that has been migrated.  Migrated subproperty
   *     will be attached here.
   * @param oldMin Old min cardinality of the property in the type.
   * @param oldMax Old max cardinality of the property in the type.
   * @param test Test from the migration report to be updated with results of this migration.
   */
  private boolean migrateSubproperty(Version oldVersion, Type oldType,
      PropertyAssociation oldPropertyAssociation,
      ClassType newCmfClassType, Test test) throws Exception {

    Subproperty oldSubproperty = null;

    // Set fields for old subproperty components
    String oldPrefix = oldType.getPrefix();
    String oldTypeQname = oldType.getQname();
    String oldPropertyQname = oldPropertyAssociation.property().qname();

    // Prepare fields for migration report
    String oldLabel = String.format("%s/%s", oldTypeQname, oldPropertyQname);

    // Find the old subproperty
    try {
      oldSubproperty = hub.subproperties.findOne(oldVersion, oldTypeQname, oldPropertyQname);
    }
    catch (EntityNotFoundException exception) {
      // Exception handled in the next block of code along with subproperty found but no migration
    }

    // Make sure subproperty exists and has a migration
    if (oldSubproperty == null || oldSubproperty.getNext() == null) {
      String message = oldSubproperty == null ? NOT_FOUND : NO_MIGRATION;
      this.logResult(test, message, oldPrefix, oldLabel, "Subproperty", false, null);
      return false;
    }

    // Find the migrated subproperty
    Subproperty newSubproperty = oldSubproperty.getNext();

    // Dependencies: Make sure the type and property from the migrated subproperty exist in the CMF
    org.mitre.niem.cmf.Model newCmf = newCmfClassType.model();
    this.addComponentToCmf(newSubproperty.getType(), newCmf, test, null, false);
    this.addComponentToCmf(newSubproperty.getProperty(), newCmf, test, null, false);

    // Set fields for old subproperty cardinality
    String oldMin = CmfUtils.subpropertyMin(oldPropertyAssociation);
    String oldMax = CmfUtils.subpropertyMax(oldPropertyAssociation);

    // Adjust min/max if the old cardinality is no longer valid in the new model
    newSubproperty.setMin(this.getMigratedSubpropertyMin(oldMin, newSubproperty.getMin()));
    newSubproperty.setMax(this.getMigratedSubpropertyMax(oldMax, newSubproperty.getMax()));

    // Add the migrated subproperty to migrated type
    newCmfClassType.addPropertyAssociation(newSubproperty.toCmf());
    this.logResult(test, PASSED, newSubproperty, true, oldSubproperty);
    log.debug(String.format("--Adding subproperty %s to type", newSubproperty.getPropertyQname()));

    return true;

  }

  /**
   * Migrate all augmentation records for the given namespace.
   *
   * @param oldVersion Current version to be migrated.
   * @param oldAugmentRecordList Old augmentation records for the given namespace being migrated.
   * @param newCmf Current components that have already been migrated.
   * @param test Test from the migration report to be updated with results of this migration.
   * @param oldPrefix Prefix of the namespace with the augmentation records being migrated.
   */
  private boolean migrateCmfNamespaceAugmentationRecordList(Version oldVersion,
      List<AugmentRecord> oldAugmentRecordList, org.mitre.niem.cmf.Model newCmf, Test test,
      String oldPrefix) throws Exception {

    // Unique set of classes from the augmentation records being augmented (e.g., nc:PersonType).
    Set<ClassType> oldAugmentedClasses = oldAugmentRecordList
        .stream()
        .map(AugmentRecord::classType)
        .collect(Collectors.toSet());

    // Migrate the augmentation records by class
    for (ClassType oldAugmentedClass : oldAugmentedClasses) {

      // Get the old augmentation type
      String oldAugmentationTypeQname = CmfUtils.getAugmentationClassQname(oldPrefix,
          oldAugmentedClass.name());
      Type oldAugmentationType = hub.types.findOne(oldVersion, oldAugmentationTypeQname);

      // Confirm the old type exists
      if (oldAugmentationType == null) {
        this.logResult(test, NOT_FOUND, oldPrefix, oldAugmentedClass.qname(),
            "Augmentation record", false, null);
        return false;
      }

      String oldAugmentedClassQname = oldAugmentedClass.qname();

      // Filter namespace's augmentation record list for the current class
      List<AugmentRecord> oldClassAugmentationRecords = oldAugmentRecordList
          .stream()
          .filter(augmentRecord -> augmentRecord.classType().qname().equals(oldAugmentedClassQname))
          .collect(Collectors.toList());

      // Migrate all augmentation records for the given class
      for (AugmentRecord oldAugmentRecord : oldClassAugmentationRecords) {
        this.migrateCmfAugmentationRecord(oldVersion, oldAugmentRecord, newCmf, test,
            oldAugmentationType);
      }

    }

    return true;

  }

  /**
   * Migrate all augmentation records for the given namespace.
   *
   * @param oldVersion Current version to be migrated.
   * @param oldAugmentRecordList Old augmentation records for the given namespace being migrated.
   * @param newCmf Current components that have already been migrated.
   * @param test Test from the migration report to be updated with results of this migration.
   * @param oldAugmentationType Augmentation type being migrated.
   */
  private boolean migrateCmfAugmentationRecord(Version oldVersion,
      AugmentRecord oldAugmentRecord, org.mitre.niem.cmf.Model newCmf, Test test,
      Type oldAugmentationType) throws Exception {

    Subproperty oldSubproperty = null;

    // Get the old augmentation subproperty
    try {
      oldSubproperty = hub.subproperties.findOne(oldVersion,
          oldAugmentationType.qname, oldAugmentRecord.property().qname());
    }
    catch (EntityNotFoundException exception) {
      // Exception handled below
    }

    // Prepare fields for migration report
    String oldLabel = String.format("%s/%s", oldAugmentationType.qname, oldAugmentRecord.property().qname());

    // Make sure subproperty exists and has a migration
    if (oldSubproperty == null || oldSubproperty.getNext() == null) {
      String message = oldSubproperty == null ? NOT_FOUND : NO_MIGRATION;
      this.logResult(test, message, oldAugmentationType.getPrefix(), oldLabel,
          "Subproperty", false, null);
      return false;
    }

    Subproperty newSubproperty = oldSubproperty.getNext();

    // Make sure the migrated type and property are in CMF
    this.addComponentToCmf(newSubproperty.getType(), newCmf, test, null, false);
    this.addComponentToCmf(newSubproperty.getProperty(), newCmf, test, null, false);

    // Set old cardinality fields
    String oldMin = CmfUtils.subpropertyMin(oldAugmentRecord);
    String oldMax = CmfUtils.subpropertyMax(oldAugmentRecord);

    // Adjust min/max if the old cardinality is no longer valid in the new model
    newSubproperty.setMin(this.getMigratedSubpropertyMin(oldMin, newSubproperty.getMin()));
    newSubproperty.setMax(this.getMigratedSubpropertyMax(oldMax, newSubproperty.getMax()));

    if (newSubproperty.getTypeQname().endsWith("AugmentationType")) {
      // Add subproperty to CMF as an augmentation record
      ClassType newClassType = newSubproperty.getType().toCmfClassType();
      newClassType.addPropertyAssociation(newSubproperty.toCmf());
      this.logResult(test, PASSED, newSubproperty, true, oldSubproperty);
      log.debug(oldAugmentationType.qname);
    }
    else {
      // Add subproperty to CMF as a property association
      String newPrefix = newSubproperty.getTypePrefix();
      org.mitre.niem.cmf.Namespace cmfNamespace = newCmf.namespaceObj(newPrefix);

      AugmentRecord newAugmentRecord = new AugmentRecord(newSubproperty.toCmf());
      cmfNamespace.addAugmentRecord(newAugmentRecord);
    }

    return true;

  }

  /**
   * Return the old min cardinality if it is still valid in the new model, else
   * return the new model's min cardinality.
   *
   * <p>For example, if the old min = 0, this would be invalid (too low) if the new min = 1.
   *
   * @param oldMin Minimum cardinality value from the subproperty to be migrated.
   * @param newModelMin Lowest valid cardinality of the subproperty permitted in the migrated model.
   */
  private String getMigratedSubpropertyMin(String oldMin, String newModelMin) {
    if (oldMin.compareTo(newModelMin) < 0) {
      return newModelMin;
    }
    return oldMin;
  }

  /**
   * Return the old max cardinality if it is still valid in the new model, else
   * return the new model's max cardinality.
   *
   * <p>For example, if the old max = unbounded, this would be invalid (too high) if the
   * new max = 1.
   *
   * @param oldMin Minimum cardinality value from the subproperty to be migrated.
   * @param newModelMin Lowest valid cardinality of the subproperty permitted in the migrated model.
   */
  private String getMigratedSubpropertyMax(String oldMax, String newModelMax) {
    if (newModelMax.equals("unbounded")) {
      // If the new model permits any max value, return the old max
      return oldMax;
    }
    else if (oldMax.equals("unbounded")) {
      // The new max is not unbounded, so return the new max (this is the largest valid max value)
      return newModelMax;
    }
    else if (oldMax.compareTo(newModelMax) > 0) {
      // Old max is larger than the new max (too high) so return the new max
      return newModelMax;
    }
    return oldMax;
  }

  /**
   * Adds a CMF component to a CMF model if it doesn't already exist.
   * Also adds the component's namespace to the CMF model if not already there.
   *
   * @param newComponent Component to be converted to CMF and added to the CMF model
   * @param cmf New model to which the converted component should be added
   * @param test Test from the migration report to be updated with results of this migration.
   * @param originalComponent The old component that was converted or had the dependency,
   *     or null to skip the log
   * @param isMigration True if result of migration; false if result of dependency
   */
  @SuppressWarnings("rawtypes")
  private void addComponentToCmf(Component newComponent, org.mitre.niem.cmf.Model cmf,
      Test test, Component originalComponent, Boolean isMigration) throws Exception {

    // TODO: Support CSV code lists
    if (newComponent.getName().equals("CountryCodeSimpleType")) {
      // String s = "s";
    }

    // Try to find the component in the current CMF model
    org.mitre.niem.cmf.Component cmfComponent = cmf.qnToComponent(newComponent.getQname());

    // Convert the API component to CMF and add to the CMF model if not already there
    if (cmfComponent == null) {
      log.debug(String.format("--Adding component %s to CMF", newComponent.getQname()));

      // Add namespace dependency
      this.addNamespaceToCmf(newComponent.getNamespace(), cmf, test);
      org.mitre.niem.cmf.Namespace namespace = cmf.namespaceObj(newComponent.getPrefix());

      // Make sure component has namespace linked to the model
      cmfComponent = newComponent.toCmf();
      cmfComponent.setNamespace(namespace);
      // TODO: Test cmfComponent.addToModel
      cmfComponent.addToModel("element name", "location", cmf);

      // Add entry to the migration report
      if (originalComponent != null) {
        if (isMigration) {
          this.logResult(test, PASSED, newComponent, true, originalComponent);
        }
        else {
          this.logDependency(test, true, newComponent, originalComponent);
        }
      }

      if (newComponent.getClassName().equals("Property")) {
        this.addPropertyDependencies((Property) newComponent, cmf, test);
      }
      else if (newComponent.getClassName().equals("Type")) {
        this.addTypeDependencies((Type) newComponent, cmf, test);
      }

    }
    else {
      // Component had already been added.
      log.debug(String.format("--Skipping duplicate component %s", newComponent.getQname()));
    }

  }

  /**
   * Adds a namespace to a CMF model if it doesn't already exist.
   *
   * @param namespace Namespace to be converted to CMF and added to the CMF model
   * @param cmf New model to which the converted component should be added
   */
  private void addNamespaceToCmf(Namespace namespace, org.mitre.niem.cmf.Model cmf, Test test)
      throws Exception {

    // Add namespace if it does not already exist
    if (cmf.namespaceObj(namespace.getPrefix()) == null) {
      log.debug(String.format("--Adding namespace %s to CMF", namespace.getPrefix()));
      namespace.addToCmfModel(cmf, false, AddModelReason.MIGRATION, test);
    }
    else {
      log.debug(String.format("--Skipped duplicate namespace %s", namespace.getPrefix()));
    }

  }

  /**
   * Check that the user-provided parameters are valid.
   *
   * @param stewardKey Identifies the owner of the model
   * @param modelKey With the stewardKey, identifies the model
   * @param from Current version of the model provided by the user
   * @param to Version to which the model should be migrated
   */
  public void checkParams(String stewardKey, String modelKey, String from, String to)
      throws Exception {

    // Check that the from (current) version exists
    Version current = hub.versions.findOne(stewardKey, modelKey, from);
    if (current == null) {
      throw new BadRequestException(String.format("Current version [%s] not found", from));
    }

    // Check that the to (target) version exists
    Version target = hub.versions.findOne(stewardKey, modelKey, to);
    if (target == null) {
      throw new BadRequestException(String.format("Target version [%s] not found", to));
    }

    // Check that there is a migration path from the from-version to the to-version
    Version version = current;
    while (version.getId() != target.getId()) {
      version = version.getNext();
      if (version == null) {
        String message = String.format("Current version [%s] must precede the target version [%s]",
            from, to);
        throw new BadRequestException(message);
      }
    }

    log.info("Parameter checks passed for %s/%s/%s to %s", stewardKey, modelKey, from, to);

  }

  /**
   * General comment for the migration report capturing metrics about the migration.
   */
  private String getMigrationComment(int oldCount, int newCount) {
    return String.format("%s components migrated out of %s", newCount, oldCount);
  }

  /**
   * Saves CMF model to a CML XML file.
   * Saves the migration results to a JSON file and a CSV file.
   * Zips the files and returns the results.
   */
  private byte[] saveOutput(org.mitre.niem.cmf.Model cmf, TestReport results,
      MultipartFile file, String from, String to) throws Exception {

    // Set up temp directory for files to be exported
    Path tempDir = FileUtils.createTempDir("migrate-cmf");

    // Get the original name from the input CMF file
    String filenameBase = FileUtils.getFilenameBase(file);

    // Save CMF file as filename-migrated-to-#.cmf.xml
    String cmfFilenameBase = String.format("%s-%s", filenameBase, to);
    File cmfFile = CmfUtils.saveCmfModel(cmf, tempDir, cmfFilenameBase);

    // Save migration report as filename-migration-report-#-to-#.json
    String reportFilenameBase = String.format("%s-migration-report-%s-to-%s",
        filenameBase, from, to);
    File jsonReportFile = JsonUtils.saveObjectAsJson(results, tempDir, reportFilenameBase);

    // Save the list of test results from the full migration report to a CSV file
    String csvReportFilePathString = FileUtils.normalize(String.format("%s/%s.csv",
        tempDir, reportFilenameBase));
    File csvReportFile = FileUtils.createFile(csvReportFilePathString);
    CsvUtils.save(csvReportFile, results.getTestResults().toArray());

    // Zip the files and return the data
    byte[] bytes = ZipUtils.zip(new LinkedList<File>(
      List.of(cmfFile, jsonReportFile, csvReportFile)));

    // Delete the temporary folder
    FileUtils.deleteTempDir(tempDir);

    return bytes;

  }

  /**
   * Initialize a new test to capture success and issues for a migration between
   * two consecutive versions.
   */
  private Test initTest(TestReport results, Version oldVersion) {

    // Create a new test and start the run clock
    Test test = new Test();
    test.startTest(Severity.error);

    Version newVersion = oldVersion.getNext();

    // Example: migrate-3.0-to-3.1
    test.id = String.format("migrate-%s-to-%s",
        oldVersion.getVersionNumber(), newVersion.getVersionNumber());

    // Example: Migrate NIEM Model 3.0 to 3.1
    test.description = String.format("Migrate %s %s %s to %s",
        oldVersion.getSteward().getShortName(), oldVersion.getModel().getShortName(),
        oldVersion.getVersionNumber(), newVersion.getVersionNumber());

    results.tests.add(test);
    return test;

  }

  @SuppressWarnings("rawtypes")
  private void logDependency(Test test, Boolean passed, Component dependency, Component source) {
    this.logResult(test, DEPENDENCY, dependency.getPrefix(), dependency.getQname(),
        dependency.getClassName(), passed, "Added as dependency of " + source.getQname());
  }

  /**
   * Log test result to the test passes or test issues list.
   */
  private void logResult(Test test, String message, String prefix, String entity,
      String entityCategory, Boolean passed, String comment) {
    if (comment == null) {
      comment = "";
    }

    TestResult result = TestResult.builder()
        .testId(test.id)
        .message(message)
        .prefix(prefix)
        .entity(entity)
        .entityCategory(entityCategory)
        .comment(comment)
        .status((passed ? Status.passed : Status.valueOf(test.severity.toString())))
        .build();
    test.results.add(result);
  }

  /**
   * Log test result to the test passes or test issues list.
   */
  @SuppressWarnings("rawtypes")
  private void logResult(Test test, String message, Component component, Boolean passed,
      Component oldComponent) {

    // Add a comment if the component was renamed
    String comment = null;
    if (passed && !oldComponent.getQname().equals(component.getQname())) {
      comment = "Renamed from " + oldComponent.getQname();
    }

    // Log test result
    this.logResult(test, message, component.getPrefix(), component.getQname(),
        component.getClassName(), passed, comment);

  }

  private void logResult(Test test, String message, Subproperty subproperty,
      Boolean passed, Subproperty oldSubproperty) {

    // Add a comment if the component was moved or replaced
    String comment = null;

    if (passed) {
      if (!subproperty.typeQname.equals(oldSubproperty.typeQname)
          || !subproperty.propertyQname.equals(oldSubproperty.propertyQname)) {
        comment = String.format("Previously %s/%s",
            oldSubproperty.typeQname, oldSubproperty.propertyQname);
      }
    }

    this.logResult(test, message, subproperty.getTypePrefix(),
        subproperty.getIdLocalLabel(), "Subproperty", passed, comment);

  }

  /**
   * Returns true if the given component or subproperty has a migration.
   * Returns false if the given entity is null or it does not have a migration listed.
   *
   * @param label - Qualified name or other label of the entity if it could not be found
   */
  @SuppressWarnings("rawtypes")
  private boolean hasMigration(Component component, Test test, String label, String className) {

    // Check that the component exists
    if (component == null) {
      this.logResult(test, NOT_FOUND, Component.getPrefix(label), label, className, false, "");
      log.debug("--Not found");
      return false;
    }

    // Check that the current component has a migration
    if (component.getNext() == null) {
      this.logResult(test, NO_MIGRATION, component, false, null);
      log.debug("--No migration available");
      return false;
    }

    return true;

  }

  /**
   * Get the number of components in a CMF model.
   */
  private int getCmfModelComponentCount(org.mitre.niem.cmf.Model cmfModel) {
    return cmfModel.classTypeL().size()
        + cmfModel.datatypeL().size()
        + cmfModel.propertyL().size()
        + cmfModel.dataPropertyL().size();
  }

}
