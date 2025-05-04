package gov.niem.tools.api.db.namespace;

import gov.niem.tools.api.core.config.Config;
import gov.niem.tools.api.db.base.BaseCmfEntity;
import gov.niem.tools.api.db.base.BaseVersionEntity;
import gov.niem.tools.api.db.property.Property;
import gov.niem.tools.api.db.type.Type;
import gov.niem.tools.api.db.version.Version;

import org.mitre.niem.cmf.CMFException;
import org.mitre.niem.cmf.SchemaDocument;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlProperty;
import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlRootElement;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.ForeignKey;
import jakarta.persistence.Index;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OneToMany;
import jakarta.persistence.OrderBy;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.ToString;
import lombok.experimental.SuperBuilder;
import org.hibernate.Hibernate;
import org.hibernate.envers.Audited;
import org.hibernate.proxy.HibernateProxy;
import org.hibernate.search.engine.backend.types.Sortable;
import org.hibernate.search.mapper.pojo.automaticindexing.ReindexOnUpdate;
import org.hibernate.search.mapper.pojo.mapping.definition.annotation.Indexed;
import org.hibernate.search.mapper.pojo.mapping.definition.annotation.IndexedEmbedded;
import org.hibernate.search.mapper.pojo.mapping.definition.annotation.IndexingDependency;
import org.hibernate.search.mapper.pojo.mapping.definition.annotation.KeywordField;

/**
 * A collection of properties and types managed by an authoritative source.
 */
@Entity
@Audited
@Data
@SuperBuilder
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(callSuper = false)
@JacksonXmlRootElement(localName = "Namespace")
@Schema(name = "Namespace")
@Table(
    uniqueConstraints = {@UniqueConstraint(
        name = "namespace_version_prefix_key", columnNames = { "version_id", "prefix" })
    },
    indexes = {
      @Index(name = "namespace_category_idx", columnList = "category"),
      @Index(name = "namespace_draft_idx", columnList = "draft"),
      @Index(name = "namespace_prefix_idx", columnList = "prefix"),
      @Index(name = "namespace_uri_idx", columnList = "uri")
    }
)
@Indexed
public class Namespace extends BaseVersionEntity<Namespace>
    implements BaseCmfEntity<org.mitre.niem.cmf.Namespace>, Comparable<Namespace> {

  /**
   * Model version in which this entity is defined.
   */
  @JsonIgnore
  @ManyToOne(optional = false, fetch = FetchType.LAZY)
  @JoinColumn(foreignKey = @ForeignKey(name = "version_fkey"))
  @IndexedEmbedded(
      includeDepth = 0,
      includePaths = {"versionNumber", "niemVersion.versionNumber", "model.shortName"})
  @IndexingDependency(reindexOnUpdate = ReindexOnUpdate.SHALLOW)
  private Version version;

  /**
   * A short, non-normative identifier for a namespace.
   */
  @Column(nullable = false)
  @JacksonXmlProperty(localName = "NamespacePrefixID")
  @Schema(example = "nc")
  @KeywordField(sortable = Sortable.YES)
  private String prefix;

  /**
   * A name of a namespace.
   */
  @JacksonXmlProperty(localName = "NamespaceName")
  @Schema(example = "NIEM Core")
  private String name;

  /**
   * A normative identifier for a namespace.
   */
  @JacksonXmlProperty(localName = "NamespaceURI")
  @Schema(example = "http://release.niem.gov/niem/niem-core/5.0")
  private String uri;

  /**
   * A definition that describes a namespace.
   */
  @Column(columnDefinition = "text")
  @JacksonXmlProperty(localName = "NamespaceDefinitionText")
  @Schema(example = "NIEM Core.")
  private String definition;

  /**
   * Kinds of namespaces, such as core, domain, code, adapter, and extension.
   */
  public enum Category {
    core,
    domain,
    code,
    adapter,
    auxiliary,
    external,
    utility,
    core_supplement,
    domain_update,
    extension,
    exchange,
    built_in,
    other
  }

  /**
   * A kind of namespace.
   */
  @JacksonXmlProperty(localName = "NamespaceCategoryCode")
  @Schema(example = "core")
  @Enumerated(EnumType.STRING)
  @KeywordField
  private Category category;

  /**
   * A draft version of a namespace. NIEM conventions are to use the value "1"
   * for a namespace that has reached release candidate status or has been
   * published.
   */
  @JacksonXmlProperty(localName = "NamespaceDraftID")
  @Schema(example = "alpha2")
  private String draft;

  /**
   * A ranking used to support sorting namespaces by category, with
   * Core and Core Supplements sorting first, followed by domains, etc.
   */
  public int getRank() {
    switch (this.category) {
      case core:
      case core_supplement:
        return 1;
      case domain:
      case domain_update:
        return 2;
      case code:
        return 3;
      case adapter:
        return 4;
      case extension:
      case exchange:
        return 5;
      default:
        return 99;
    }
  }

  /**
   * Kinds of NDR targets, such as REF and EXT.
   *
   * @todo Support NDR 6.0 MSG and SUB targets.
   */
  public enum NdrTarget {
    REF,
    EXT,
    MSG,
    SUB
  }

  /**
   * A code representing an NDR conformance target, or null if the namespace
   * is not meant to be conformant.
   */
  @JacksonXmlProperty(localName = "NamespaceNDRTargetCode")
  @Schema(example = "EXT")
  @Enumerated(EnumType.STRING)
  private NdrTarget target;

  /**
   * Kinds of schema generation options, representing schemas that have to be
   * built, schemas that are to be included as static files, and schemas that
   * do not have to be included at all (e.g., the schema for XML Schema).
   */
  public enum Generation {
    build,
    static_file,
    none
  }

  /**
   * A means by which a namespace should be generated.
   * <ul>
   *   <li>build: Generate by assembling its properties and types</li>
   *   <li>static_file: Include it's pre-built file (e.g., externals and utilities)</li>
   *   <li>none: Namespace may be referenced but does not need to be included
   *     (e.g., XML Schema)</li>
   * </ul>
   * Defaults to "build".
   */
  @Builder.Default
  @JacksonXmlProperty(localName = "NamespaceGenerationCode")
  @Schema(example = "REF")
  @Enumerated(EnumType.STRING)
  private Generation generation = Generation.build;

  /**
   * The filename to use for representations of this namespace such as XML schemas,
   * without a file extension.
   */
  @JacksonXmlProperty(localName = "NamespaceFileName")
  @Schema(example = "niem-core")
  private String filename;

  /**
   * The filepath to use for nested representations of this namespace such as
   * for XML schemas, without the filename or extension.
   */
  @JacksonXmlProperty(localName = "NamespaceFilePathID")
  @Schema(example = "xsd/")
  private String filepath;

  @JsonIgnore
  @Builder.Default
  @ToString.Exclude
  @EqualsAndHashCode.Exclude
  @OneToMany(mappedBy = "namespace")
  @OrderBy("qname")
  private Set<Type> types = new HashSet<Type>();

  @JsonIgnore
  @Builder.Default
  @ToString.Exclude
  @EqualsAndHashCode.Exclude
  @OneToMany(mappedBy = "namespace")
  @OrderBy("qname")
  private Set<Property> properties = new HashSet<Property>();

  /**
   * Gets the version to which this namespace belongs.
   */
  public Version getVersion() {
    Version version = this.version;
    if (version instanceof HibernateProxy) {
      version = Hibernate.unproxy(version, Version.class);
    }
    return version;
  }

  /**
   * True if the namespace has a NDR conformance target and is meant to be
   * conforming; false if the namespace is a utility, external, or is otherwise
   * not meant to be conforming.
   */
  @JacksonXmlProperty(localName = "NamespaceTargetIndicator")
  @Schema(example = "true")
  @JsonProperty("hasTarget")
  public boolean hasTarget() {
    return this.target == null ? false : true;
  }

  @Override
  public Version getParentEntity() {
    return this.version;
  }

  @Override
  @Schema(
      example = Config.BASE_URL + "/stewards/niem/models/crash-driver/version/1.1/namespaces/nc",
      description = "An endpoint to get information about a namespace.")
  public String getRoute() {
    String versionRoute = this.version.getRoute();
    return String.format("%s/namespaces/%s", versionRoute, this.prefix);
  }

  @Override
  @Schema(
      example = "Namespace",
      description = "A kind of NIEM entity, such as a Namespace or a Property.")
  public String getClassName() {
    return super.getClassName();
  }

  @Override
  @Schema(
      example = "niem/crash-driver/1.1/nc",
      description = "A unique identifier.  For a namespace, this is combines the stewardKey, modelKey, versionNumber, and prefix fields.")
  public String getIdLabel() {
    return String.format("%s/%s", this.getVersion().getIdLabel(), this.getPrefix());
  }

  @Override
  @Schema(
      example = "nc",
      description = "An identifier, unique within its immediate scope.  For a namespace, this is the same as the prefix field (unique within its version).")
  public String getIdLocalLabel() {
    return this.prefix;
  }

  @Override
  @Schema(
      example = "NIEM Crash Driver 1.1: NIEM Core",
      description = "A steward short name, model short name, version number, and namespace name (if provided) or prefix.")
  public String getTitle() {
    String namespaceLabel = this.name == null ? this.prefix : this.name;
    return String.format("%s: %s", this.getVersion().getTitle(), namespaceLabel);
  }

  /**
   * Returns the name of the conformance target.  For example, for a REF target,
   * returns "Reference".
   */
  @JsonIgnore
  public String getConformanceTargetName() {
    switch (this.target) {
      case REF:
        return "Reference";
      case EXT:
        return "Extension";
      case MSG:
        return "Message";
      case SUB:
        return "Subset";
      default:
        return null;
    }
  }

  /**
   * Returns the first digit of the NIEM version compatible with this namespace.
   */
  @JsonIgnore
  public Integer getNiemVersionMajorDigit() {
    if (this.getNdrVersion() == null) {
      return null;
    }
    return Integer.parseInt(this.getNdrVersion().substring(0, 1));
  }

  /**
   * Gets the NDR version for this namespace, based on the version of NIEM
   * that is compatible with this namespace.
   */
  @JsonIgnore
  public String getNdrVersion() {
    return this.getNiemVersionNumber().replaceAll(".\\d$", ".0");
  }

  /**
   * Gets the conformance target URI based on the target and NIEM version number
   * of this namespace.
   */
  @JsonIgnore
  public String getNdrConformanceTarget() {
    Set<String> unsupported = Set.of("1.0", "2.0", "2.1");
    if (this.target == null || unsupported.contains(this.getNiemVersionNumber())) {
      return null;
    }

    Integer niemVersionMajorDigit = this.getNiemVersionMajorDigit();
    String ndrVersion = this.getNdrVersion();
    String targetName = this.getConformanceTargetName();

    String conformanceTarget;

    if (niemVersionMajorDigit == null) {
      return null;
    }
    else if (niemVersionMajorDigit < 6) {
      // NIEM
      conformanceTarget = String.format("http://reference.niem.gov/niem/specification/naming-and-design-rules/%s/#%sSchemaDocument", ndrVersion, targetName);
    }
    else {
      // NIEMOpen
      conformanceTarget = String.format("https://docs.oasis-open.org/niemopen/ns/specification/NDR/%s/#%sSchemaDocument", ndrVersion, targetName);
    }

    return conformanceTarget;

  }

  /**
   * Gets key fields about a namespace.
   */
  @JsonIgnore
  public Map<String, String> toSummary() {
    Map<String, String> map = new HashMap<>();
    map.put("prefix", this.prefix);
    map.put("name", this.name);
    map.put("uri", this.uri);
    map.put("category", this.category.toString());
    map.put("@id", this.getIdLabel());
    map.put("route", this.getRoute());
    return map;
  }

  /**
   * Adds this namespace to the given CMF model.
   */
  public void addToCmfModel(org.mitre.niem.cmf.Model cmfModel) throws CMFException {
    // Skip namespaces already supported by CMF
    if (this.prefix.equals("xml")) {
      return;
    }

    // Add this namespace to the given CMF model
    cmfModel.addNamespace(this.toCmf());

    // Add SchemaDocument information
    SchemaDocument schemaDocument = new SchemaDocument();
    schemaDocument.setConfTargets(this.getNdrConformanceTarget());
    schemaDocument.setFilePath(this.filepath + this.filename + ".xsd");
    // TODO: Support namespace language
    // schemaDocument.setLanguage(null);
    schemaDocument.setNIEMversion(this.getNiemVersionNumber());
    schemaDocument.setSchemaVersion(this.draft);
    // TODO: Support sequenceID?
    // schemaDocument.setSequenceID(null);
    schemaDocument.setTargetNS(this.uri);
    cmfModel.addSchemaDoc(this.uri, schemaDocument);
  }

  /**
   * Converts this namespace to a CMF namespace object.
   */
  public org.mitre.niem.cmf.Namespace toCmf() throws CMFException {
    org.mitre.niem.cmf.Namespace cmfNamespace = new org.mitre.niem.cmf.Namespace();
    cmfNamespace.setDefinition(this.definition);
    cmfNamespace.setNamespacePrefix(this.prefix);
    cmfNamespace.setNamespaceURI(this.uri);
    cmfNamespace.setKind(this.categoryToCmf(this.category));
    // TODO: Namespace CMF properties, classes, datatypes
    return cmfNamespace;
  }

  /**
   * Converts CMF namespace categories to API namespace categories.
   */
  public Category categoryFromCmf(int kind) {
    switch (kind) {
      case org.mitre.niem.cmf.NamespaceKind.NSK_CORE:
        return Category.core;
      case org.mitre.niem.cmf.NamespaceKind.NSK_DOMAIN:
        return Category.domain;
      case org.mitre.niem.cmf.NamespaceKind.NSK_EXTENSION:
        return Category.extension;
      case org.mitre.niem.cmf.NamespaceKind.NSK_EXTERNAL:
        return Category.external;
      case org.mitre.niem.cmf.NamespaceKind.NSK_OTHERNIEM:
        return Category.other;
      case org.mitre.niem.cmf.NamespaceKind.NSK_UNKNOWN:
        return Category.other;
      case org.mitre.niem.cmf.NamespaceKind.NSK_XML:
        return Category.built_in;
      case org.mitre.niem.cmf.NamespaceKind.NSK_XSD:
        return Category.built_in;
      default:
        return Category.other;
    }
  }

  /**
   * Converts API namespace categories to CMF namespace categories.
   *
   * <p>From CMF: cmf/NamespaceKind.java
   * NSK_EXTENSION = 0; has conformance assertion, not in NIEM model
   * NSK_DOMAIN    = 1; domain schema
   * NSK_CORE      = 2; niem core schema
   * NSK_OTHERNIEM = 3; other niem model; starts with release or publication prefix
   * NSK_BUILTIN   = 4; appinfo, code-lists, conformance, proxy, structures
   * NSK_XSD       = 5; namespace for XSD datatypes
   * NSK_XML       = 6; namespace for xml: attributes
   * NSK_EXTERNAL  = 7; imported with appinfo:externalImportIndicator
   * NSK_UNKNOWN   = 8; no conformance assertion; not any of the above
   * NSK_NUMKINDS  = 9; this many kinds of namespaces
   */
  public int categoryToCmf(Category category) {
    if (prefix.equals("xs")) {
      return org.mitre.niem.cmf.NamespaceKind.NSK_XSD;
    }

    if (prefix.equals("xml")) {
      return org.mitre.niem.cmf.NamespaceKind.NSK_XML;
    }

    if (prefix.equals("structures")) {
      return org.mitre.niem.cmf.NamespaceKind.NIEM_STRUCTURES;
    }

    if (prefix.equals("appinfo")) {
      return org.mitre.niem.cmf.NamespaceKind.NIEM_APPINFO;
    }

    if (prefix.equals("cli")) {
      return org.mitre.niem.cmf.NamespaceKind.NIEM_CLI;
    }

    if (prefix.equals("clsa")) {
      return org.mitre.niem.cmf.NamespaceKind.NIEM_CLSA;
    }

    switch (category) {
      case core:
        return org.mitre.niem.cmf.NamespaceKind.NSK_CORE;
      case domain:
        return org.mitre.niem.cmf.NamespaceKind.NSK_DOMAIN;
      case exchange:
      case extension:
        return org.mitre.niem.cmf.NamespaceKind.NSK_EXTENSION;
      case external:
        return org.mitre.niem.cmf.NamespaceKind.NSK_EXTERNAL;
      case other:
        return org.mitre.niem.cmf.NamespaceKind.NSK_UNKNOWN;
      case utility:
      case adapter:
      case auxiliary:
      case code:
      case core_supplement:
      case domain_update:
        return org.mitre.niem.cmf.NamespaceKind.NSK_OTHERNIEM;
      case built_in:
        return org.mitre.niem.cmf.NamespaceKind.NSK_UNKNOWN;
      default:
        return org.mitre.niem.cmf.NamespaceKind.NSK_UNKNOWN;
    }
  }

  /**
   * Custom sorting function for namespaces.  Sorts by namespace rank, then prefix.
   */
  @Override
  public int compareTo(Namespace other) {
    if (this.getRank() != other.getRank()) {
      return Integer.compare(this.getRank(), other.getRank());
    }
    return this.prefix.compareTo(other.prefix);
  }

}
