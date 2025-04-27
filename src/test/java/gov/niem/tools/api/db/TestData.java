package gov.niem.tools.api.db;

import gov.niem.tools.api.db.model.Model;
import gov.niem.tools.api.db.namespace.Namespace;
import gov.niem.tools.api.db.niem.genc.CountryAlpha3CodeType;
import gov.niem.tools.api.db.property.Property;
import gov.niem.tools.api.db.steward.Steward;
import gov.niem.tools.api.db.type.Type;
import gov.niem.tools.api.db.version.Version;

/**
 * Reusable data for model and database testing.
 */
public class TestData {

  protected static Steward nmo = Stewards.nmo();
  protected static Steward acme = Stewards.acme();

  /**
   * Reusable steward test data.
   */
  public class Stewards {

    public static String nmoKey = "nmo";
    public static String acmeKey = "acme-co";

    /**
     * Test steward ACME.
     */
    public static Steward acme() {
      return Steward.builder()
          .shortName("ACME Co")
          .fullName("ACME Corporation")
          .category(Steward.Category.Industry)
          .address("124 Main St, Old Town, CA")
          .contactName("Marvin Acme")
          .country(CountryAlpha3CodeType.USA)
          .description("A Company that Makes Everything.")
          .email("tweety@acme.org")
          .unit("ink")
          .subunit("disappearing")
          .phone("867-5309")
          .website("https://acme.com")
          .build();
    }

    /**
     * Test steward NMO.
     */
    public static Steward nmo() {
      return Steward.builder()
          .shortName("NMO")
          .fullName("NIEM Management Office")
          .category(Steward.Category.Nonprofit)
          .country(CountryAlpha3CodeType.USA)
          .email("info@niemopen.org")
          .website("https://niemopen.org")
          .build();
    }

  }

  /**
   * Reusable test data for models.
   */
  public class Models {

    public static String niemKey = "niem";
    public static String crashKey = "crash-driver";

    /**
     * A test NIEM reference model.
     */
    public static Model niem(Steward steward) {
      return Model.builder()
          .category(Model.Category.reference)
          .objective(Model.Objective.implementation)
          .description("")
          .fullName("National Information Exchange Model")
          .repo("https://github.com/niem/niem-releases.git")
          .shortName("NIEM")
          .steward(steward)
          .build();
    }

    /**
     * A test crash message model.
     */
    public static Model crash(Steward steward) {
      String description = "This example IEPD is designed for the training program. It exercises most of the features in the NDR.";

      return Model.builder()
          .category(Model.Category.message)
          .objective(Model.Objective.example)
          .description(description)
          .fullName("Crash Driver Report IEPD")
          .repo("https://github.com/niem/niem-training.git")
          .shortName("Crash Driver")
          .steward(steward)
          .build();
    }

  }

  /**
   * Reusable test data for versions.
   */
  public class Versions {

    /**
     * Create a test major version.
     */
    public static Version major(Model model, String versionNumber, Version niemVersion) {
      Version version = Versions.major(model, versionNumber);
      version.setNiemVersion(niemVersion);
      return version;
    }

    /**
     * Create a test major version.
     */
    public static Version major(Model model, String versionNumber) {
      return Version.builder()
          .category(Version.Category.major)
          .model(model)
          .versionNumber(versionNumber)
          .build();
    }

    /**
     * Create a test minor version.
     */
    public static Version minor(Model model, String versionNumber, Version niemVersion) {
      Version version = Versions.minor(model, versionNumber);
      version.setNiemVersion(niemVersion);
      return version;
    }

    /**
     * Create a test minor version.
     */
    public static Version minor(Model model, String versionNumber) {
      return Version.builder()
          .category(Version.Category.minor)
          .model(model)
          .versionNumber(versionNumber)
          .build();
    }

  }

  /**
   * Reusable test data for namespaces.
   */
  public class Namespaces {

    /**
     * Creates a test Core namespace.
     */
    public static Namespace core(Version version) {
      return Namespace.builder()
          .version(version)
          .prefix("nc")
          .category(Namespace.Category.core)
          .build();
    }

    /**
     * Creates a test domain namespace.
     */
    public static Namespace domain(Version version, String prefix) {
      return Namespace.builder()
          .version(version)
          .prefix(prefix)
          .category(Namespace.Category.domain)
          .build();
    }

    /**
     * Creates a test XML Schema namespace.
     */
    public static Namespace xs(Version version) {
      return Namespace.builder()
          .version(version)
          .prefix("xs")
          .category(Namespace.Category.built_in)
          .build();
    }

  }

  /**
   * Reusable test data for properties.
   */
  public class Properties {

    /**
     * Creates a test Person element property.
     */
    public static Property person(Namespace namespace) {
      return Property.builder()
          .namespace(namespace)
          .prefix(namespace.getPrefix())
          .name("Person")
          .definition("A human being.")
          .build();
    }

    /**
     * Creates a test PersonFullName element property.
     */
    public static Property personFullName(Namespace namespace) {
      return Property.builder()
          .namespace(namespace)
          .prefix(namespace.getPrefix())
          .name("PersonFullName")
          .definition("A full name of a person.")
          .build();
    }

    /**
     * Creates a test sourceText attribute property.
     */
    public static Property sourceText(Namespace namespace) {
      return Property.builder()
          .namespace(namespace)
          .prefix(namespace.getPrefix())
          .name("sourceText")
          .definition("A source which provided the information.")
          .category(Property.Category.attribute)
          .build();
    }

  }

  /**
   * Test data for types.
   */
  public class Types {

    /**
     * Creates a test Person type (CCC).
     */
    public static Type personType(Namespace namespace) {
      return Type.builder()
          .namespace(namespace)
          .prefix(namespace.getPrefix())
          .name("PersonType")
          .definition("A data type for a human being.")
          .category(Type.Category.complex_object)
          .build();
    }

    /**
     * Creates a test Object type (CCC).
     */
    public static Type objectType(Namespace namespace) {
      return Type.builder()
          .namespace(namespace)
          .prefix(namespace.getPrefix())
          .name("ObjectType")
          .definition("A data type for an object.")
          .category(Type.Category.complex_object)
          .build();
    }

    /**
     * Creates a test Text type (CSC).
     */
    public static Type textType(Namespace namespace) {
      return Type.builder()
          .namespace(namespace)
          .prefix(namespace.getPrefix())
          .name("TextType")
          .definition("A data type for a string.")
          .category(Type.Category.complex_value)
          .build();
    }

    /**
     * Creates a test Code simple type (simple).
     */
    public static Type codeSimpleType(Namespace namespace) {
      return Type.builder()
          .namespace(namespace)
          .prefix(namespace.getPrefix())
          .name("CodeSimpleType")
          .definition("A data type for a code set.")
          .category(Type.Category.simple_value)
          .build();
    }

    /**
     * Creates a test string type (simple).
     */
    public static Type xsStringType(Namespace namespace) {
      return Type.builder()
          .namespace(namespace)
          .prefix(namespace.getPrefix())
          .name("string")
          .definition("A data type for a string.")
          .category(Type.Category.simple_value)
          .build();
    }

  }

}
