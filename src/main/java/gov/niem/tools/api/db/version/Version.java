
package gov.niem.tools.api.db.version;

import gov.niem.tools.api.core.config.Config;
import gov.niem.tools.api.db.base.BaseCmfEntity;
import gov.niem.tools.api.db.base.BaseVersionedEntity;
import gov.niem.tools.api.db.model.Model;
import gov.niem.tools.api.db.namespace.Namespace;
import gov.niem.tools.api.db.property.Property;
import gov.niem.tools.api.db.steward.Steward;
import gov.niem.tools.api.db.type.Type;

import org.mitre.niem.cmf.CMFException;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlProperty;
import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlRootElement;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.persistence.CascadeType;
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
import lombok.NonNull;
import lombok.ToString;
import lombok.experimental.SuperBuilder;
import org.hibernate.Hibernate;
import org.hibernate.envers.Audited;
import org.hibernate.proxy.HibernateProxy;
import org.hibernate.search.engine.backend.types.Projectable;
import org.hibernate.search.engine.backend.types.Sortable;
import org.hibernate.search.mapper.pojo.automaticindexing.ReindexOnUpdate;
import org.hibernate.search.mapper.pojo.mapping.definition.annotation.AssociationInverseSide;
import org.hibernate.search.mapper.pojo.mapping.definition.annotation.GenericField;
import org.hibernate.search.mapper.pojo.mapping.definition.annotation.IndexedEmbedded;
import org.hibernate.search.mapper.pojo.mapping.definition.annotation.IndexingDependency;
import org.hibernate.search.mapper.pojo.mapping.definition.annotation.ObjectPath;
import org.hibernate.search.mapper.pojo.mapping.definition.annotation.PropertyValue;

/**
 * A specific version or release of a model.
 */
@Entity
@Audited
@Data
@SuperBuilder
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(callSuper = false)
@JacksonXmlRootElement(localName = "api:Version")
@Schema(name = "Version")
@Table(
    uniqueConstraints = { @UniqueConstraint(
        name = "version_model_number_key",
        columnNames = {"model_id", "versionNumber"})
    },
    indexes = {
      @Index(name = "version_is_published_idx", columnList = "isPublished"),
      @Index(name = "version_version_number_idx", columnList = "versionNumber"),
      @Index(name = "version_category_idx", columnList = "category")
    }
)
public class Version extends BaseVersionedEntity<Version>
    implements BaseCmfEntity<org.mitre.niem.cmf.Model> {

  @JsonIgnore
  @ManyToOne(optional = false, fetch = FetchType.LAZY)
  @JoinColumn(foreignKey = @ForeignKey(name = "model_fkey"))
  @IndexedEmbedded(includeDepth = 0, includePaths = {"shortName"})
  @IndexingDependency(reindexOnUpdate = ReindexOnUpdate.SHALLOW)
  private Model model;

  /**
   * A number which identifies a version of a model, such as "5.2" or "1.0.1".
   */
  @NonNull
  @Column(nullable = false)
  @JacksonXmlProperty(localName = "api:VersionNumberID")
  @Schema(example = "1.1")
  @GenericField(sortable = Sortable.YES, projectable = Projectable.YES)
  private String versionNumber;

  /**
   * A draft of a version, such as "alpha1" or "rc2".
   */
  @JacksonXmlProperty(localName = "api:VersionDraftID")
  @Schema(example = "alpha1")
  private String draft;

  /**
   * True if a version has been published and the content is to be considered
   * finalized; false otherwise.
   */
  @JsonProperty("isPublished")
  @JacksonXmlProperty(localName = "api:VersionPublishedIndicator")
  @Schema(example = "false")
  private boolean isPublished;

  /**
   * True if this version of the model is considered the current version; false otherwise.
   */
  @JsonProperty("isCurrent")
  @JacksonXmlProperty(localName = "api:VersionCurrentIndicator")
  @Schema(example = "false")
  private boolean isCurrent;

  /**
   * An URI identifier for a version of a model.
   */
  @JacksonXmlProperty(localName = "c:iepdURI", isAttribute = true)
  @Schema(example = "http://example.com/CrashDriver/1.1/")
  private String uri;

  /**
   * A list of one or more URIs that each represents an IEPD class to which the
   * IEPD claims conformance.
   */
  @JacksonXmlProperty(localName = "c:iepdConformanceTargetIdentifierURIList", isAttribute = true)
  @Schema(example = "http://reference.niem.gov/niem/specification/model-package-description/5.0/#IEPD")
  private String conformanceTargets;

  /**
   * A description of a transactional or design pattern used for this IEPD.
   */
  @JacksonXmlProperty(localName = "c:ExchangePatternText")
  @Schema(example = "query/response")
  private String exchangePattern;

  /**
   * A name of an entity or organization that uses this IEPD.
   */
  @JacksonXmlProperty(localName = "c:ExchangePartnerName")
  @Schema(example = "Department of Transportation, State of Virginia")
  private String exchangePartners;

  // TODO: IEPD spec LastRevisionDate doesn't make sense
  /**
   * A date the latest changes to an IEPD were published (i.e., CreationDate of
   * previous version).
   */
  @JacksonXmlProperty(localName = "c:LastRevisionDate")
  @Schema(example = "2020-08-01")
  private String revised;

  /**
   * A description of the current state of this IEPD in development; may also
   * project future plans for the IEPD.
   */
  @JacksonXmlProperty(localName = "c:StatusText")
  @Schema(
      example = "Finalized for NIEM 5.0-series training; due to be updated after the publication of NIEM 6.0.")
  private String status;

  /**
   * Kinds of versions, such as major, minor, patch, core supplement, and domain update.
   */
  public enum Category {
    major,
    minor,
    patch,
    core_supplement,
    domain_update,
    other
  }

  /**
   * A kind of version, such as major or minor.
   */
  @JacksonXmlProperty(localName = "api:VersionCategoryCode")
  @Schema(example = "major")
  @Enumerated(EnumType.STRING)
  private Category category;

  /**
   * A set of namespaces defined by a version of a model.
   */
  @JsonIgnore
  @Builder.Default
  @ToString.Exclude
  @EqualsAndHashCode.Exclude
  @OneToMany(mappedBy = "version")
  @OrderBy("prefix")
  @AssociationInverseSide(inversePath = @ObjectPath(@PropertyValue(propertyName = "version")))
  private Set<Namespace> namespaces = new HashSet<Namespace>();

  // /**
  //  * A set of properties defined by a version of a model.
  //  */
  // @JsonIgnore
  // @Builder.Default
  // @ToString.Exclude
  // @EqualsAndHashCode.Exclude
  // @OneToMany(mappedBy = "version")
  // @OrderBy("qname")
  // private Set<Property> properties = new HashSet<Property>();

  // /**
  //  * A set of types defined by a version of a model.
  //  */
  // @JsonIgnore
  // @Builder.Default
  // @ToString.Exclude
  // @EqualsAndHashCode.Exclude
  // @OneToMany(mappedBy = "version")
  // @OrderBy("qname")
  // private Set<Type> types = new HashSet<Type>();

  /**
   * A set of properties that serve as the root or starting point of message instances.
   */
  @JsonIgnore
  @Builder.Default
  @ToString.Exclude
  @EqualsAndHashCode.Exclude
  @OneToMany
  @OrderBy("qname")
  private Set<Property> messageRoots = new HashSet<Property>();

  /**
   * A NIEM version on which this version is based, or itself if this is a NIEM version.
   */
  @JsonIgnore
  @ToString.Exclude
  @EqualsAndHashCode.Exclude
  @ManyToOne(cascade = CascadeType.PERSIST, fetch = FetchType.LAZY)
  @JoinColumn(
      name = "niem_version_id",
      referencedColumnName = "id", foreignKey = @ForeignKey(name = "niem_version_fkey"))
  @IndexedEmbedded(includeDepth = 0, includePaths = {"versionNumber"})
  @IndexingDependency(reindexOnUpdate = ReindexOnUpdate.SHALLOW)
  private Version niemVersion;

  /**
   * Gets the model to which the version belongs.
   * Makes sure a potential Hibernate proxy is initialized.
   */
  public Model getModel() {
    Model model = this.model;
    if (model instanceof HibernateProxy) {
      model = Hibernate.unproxy(model, Model.class);
    }
    return model;
  }

  /**
   * Gets the reference model from the NIEM steward.
   * Makes sure a potential Hibernate proxy is initialized.
   */
  @JsonIgnore
  public Version getNiemVersion() {
    Version version = this.niemVersion;
    if (version instanceof HibernateProxy) {
      version = Hibernate.unproxy(version, Version.class);
    }
    return version;
  }

  /**
   * Gets the NIEM version number compatible with this version of the model.
   */
  public String getNiemVersionNumber() {
    if (this.getNiemVersion() != null) {
      return this.getNiemVersion().getVersionNumber();
    }
    return null;
  }

  @Override
  public Model getParentEntity() {
    return this.model;
  }

  @Override
  @Schema(
      example = Config.BASE_URL + "/stewards/niem/models/crash-driver/version/1.1",
      description = "An endpoint to get information about a version.")
  public String getRoute() {
    String modelRoute = this.model.getRoute();
    return String.format("%s/versions/%s", modelRoute, this.versionNumber);
  }

  @Override
  @Schema(
      example = "Version",
      description = "A kind of NIEM entity, such as a Namespace or a Property.")
  public String getClassName() {
    return super.getClassName();
  }

  @Override
  @Schema(
      example = "niem/crash-driver/1.1",
      description = "A unique identifier.  For a version, this is combines the stewardKey, modelKey, and versionNumber fields.")
  public String getIdLabel() {
    return this.getModel().getIdLabel() + "/" + this.versionNumber;
  }

  @Override
  @Schema(
      example = "1.1",
      description = "An identifier, unique within its immediate scope.  For a version, this is the same as the versionNumber field (unique within its model).")
  public String getIdLocalLabel() {
    return this.versionNumber;
  }

  @Override
  @Schema(
      example = "NIEM Crash Driver 1.1",
      description = "A steward short name, model short name, and version number.")
  public String getTitle() {
    return String.format("%s %s", this.getModel().getTitle(), this.getVersionNumber());
  }

  /**
   * Gets the steward from the model that this version belongs to.
   */
  @JsonIgnore
  public Steward getSteward() {
    return this.getModel().getSteward();
  }

  /**
   * Gets key fields about a version.
   */
  @JsonIgnore
  public Map<String, String> toSummary() {
    Map<String, String> map = new HashMap<>();
    map.put("versionNumber", this.getVersionNumber());
    map.put("niemVersionNumber", this.getNiemVersion().getVersionNumber());
    map.put("@id", this.getIdLabel());
    map.put("route", this.getRoute());
    return map;
  }

  // TODO: optional getNIEMVersion()
  // @JsonIgnore
  // public Optional<Version> getNIEMVersion() {
  // return Optional.of(this.niemVersion);
  // }

  // TODO: getNIEMVersionBasics()
  // public Map<String, String> getNIEMVersionBasics() {
  // return this.niemVersion == null ? null : this.niemVersion.toBasics();
  // }

  @Override
  public void addToCmfModel(org.mitre.niem.cmf.Model cmfModel) throws CMFException {
    for (Namespace namespace : this.namespaces) {
      // Add properties
      for (Property property : namespace.getProperties()) {
        property.addToCmfModel(cmfModel);
      }

      // Add types
      for (Type type : namespace.getTypes()) {
        type.addToCmfModel(cmfModel);
      }
    }
  }

  /**
   * Adds the namespaces, properties, and types in this model to the given CMF model.
   */
  public void addToCmfModel(org.mitre.niem.cmf.Model cmfModel, Boolean includeContent)
      throws CMFException {
    if (includeContent == true) {
      addToCmfModel(cmfModel);
    }
    else {
      // TODO: Load version properties once supported by CMF
      return;
    }
  }

  @Override
  public org.mitre.niem.cmf.Model toCmf() {
    org.mitre.niem.cmf.Model cmfModel = new org.mitre.niem.cmf.Model();
    return cmfModel;
  }

  // TODO: toCmf()
  // public VersionType toCmf() {
  //   VersionType v = new VersionType();
  //   if (this.category != null) {
  //     v.category = this.category.toString();
  //   }
  //   v.conformanceTargets = this.conformanceTargets;
  //   v.draft = this.draft;
  //   v.exchangePartners = this.exchangePartners;
  //   v.exchangePattern = this.exchangePattern;
  //   v.isPublished = this.isPublished;
  //   v.revised = this.revised;
  //   v.status = this.status;
  //   v.uri = this.uri;
  //   v.versionNumber = this.versionNumber;
  //   return v;
  // }

  // TODO: getCatalog()
  // public Catalog getCatalog() {
  //   Model model = this.getModel();

  //   Catalog catalog = Catalog.builder()
  //   .name(model.getShortName())
  //   .version(this.getDraft())
  //   .uri(this.getUri())
  //   .conformanceTargets(this.getConformanceTargets())
  //   .description(model.getDescription())
  //   .build();

  //   catalog.getIepdInformation().source = this.getSteward().toAuthoritativeSource();

  //   catalog.getIepdInformation().creationDate = this.getCreated();
  //   catalog.getIepdInformation().revisionDate = this.getRevised();
  //   catalog.getIepdInformation().status = this.getStatus();
  //   catalog.getIepdInformation().keywords = model.getKeywords();
  //   catalog.getIepdInformation().subjects = model.getSubjects();
  //   catalog.getIepdInformation().exchangePattern = this.getExchangePattern();
  //   catalog.getIepdInformation().exchangePartner = this.getExchangePartners();

  //   return catalog;
  // }

}
