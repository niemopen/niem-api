package gov.niem.tools.api.db.model;

import gov.niem.tools.api.core.config.Config;
import gov.niem.tools.api.db.base.BaseStewardEntity;
import gov.niem.tools.api.db.steward.Steward;
import gov.niem.tools.api.db.version.Version;

import com.fasterxml.jackson.annotation.JsonIgnore;
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
import jakarta.persistence.Transient;
import jakarta.persistence.UniqueConstraint;
import jakarta.validation.constraints.NotBlank;
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
import org.hibernate.annotations.Formula;
import org.hibernate.envers.Audited;
import org.hibernate.envers.NotAudited;
import org.hibernate.proxy.HibernateProxy;
import org.hibernate.search.engine.backend.types.Projectable;
import org.hibernate.search.engine.backend.types.Sortable;
import org.hibernate.search.mapper.pojo.automaticindexing.ReindexOnUpdate;
import org.hibernate.search.mapper.pojo.mapping.definition.annotation.AssociationInverseSide;
import org.hibernate.search.mapper.pojo.mapping.definition.annotation.Indexed;
import org.hibernate.search.mapper.pojo.mapping.definition.annotation.IndexedEmbedded;
import org.hibernate.search.mapper.pojo.mapping.definition.annotation.IndexingDependency;
import org.hibernate.search.mapper.pojo.mapping.definition.annotation.KeywordField;
import org.hibernate.search.mapper.pojo.mapping.definition.annotation.ObjectPath;
import org.hibernate.search.mapper.pojo.mapping.definition.annotation.PropertyValue;

/**
 * A reference or message data model.
 */
@Entity
@Audited
@Data
@SuperBuilder
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(callSuper = false)
@JacksonXmlRootElement(localName = "api:Model")
@Schema(name = "Model")
@Table(
    uniqueConstraints = @UniqueConstraint(
        name = "model_steward_short_name_key", columnNames = {"steward_id", "shortName"}),
    indexes = {
        @Index(name = "model_category_idx", columnList = "category"),
        @Index(name = "model_objective_idx", columnList = "objective"),
        @Index(name = "model_short_name_idx", columnList = "shortName"),
        @Index(name = "model_subjects_idx", columnList = "subjects")
    }
)
@Indexed
public class Model extends BaseStewardEntity {

  // TODO: Reserved base URIs

  @Transient @JsonIgnore
  public static final String niemModelKey = "model";

  /**
   * A steward responsible for the management of the model.
   */
  @JsonIgnore
  @ManyToOne(cascade = CascadeType.PERSIST, optional = false, fetch = FetchType.LAZY)
  @JoinColumn(foreignKey = @ForeignKey(name = "steward_fkey"))
  @IndexedEmbedded(includeDepth = 0, includePaths = {"shortName"})
  @IndexingDependency(reindexOnUpdate = ReindexOnUpdate.SHALLOW)
  private Steward steward;

  /**
   * A human-readable and url-friendly model identifier generated from the shortName
   * field, unique within the set of models owned by its steward.
   */
  @NotAudited
  @Formula("slugify(short_name)")
  @JacksonXmlProperty(localName = "api:ModelKeyID")
  @Schema(example = "crash-driver")
  private String modelKey;

  /**
   * A short name or acronym used to identify a model.
   */
  @NotBlank
  @Column(nullable = false)
  @JacksonXmlProperty(localName = "api:ModelShortName")
  @Schema(example = "Crash Driver")
  @KeywordField(sortable = Sortable.YES, projectable = Projectable.YES)
  private String shortName;

  /**
   * A full name used to identify a model.
   */
  @JacksonXmlProperty(localName = "api:ModelFullName")
  @Schema(example = "NIEM Training Crash Driver IEPD")
  private String fullName;

  /**
   * A description of a model.
   */
  @JacksonXmlProperty(localName = "api:ModelDescriptionText")
  @Schema(example = "A Crash Driver IEPD used in training to demonstrate NIEM concepts.")
  private String description;

  /**
   * A website address by which a person or organization may be contacted.
   */
  @JacksonXmlProperty(localName = "nc:ContactWebsiteURI")
  @Schema(example = "https://niem.github.io/training/")
  private String website;

  /**
   * A code repository which hosts full IEPDs or message specifications representing the model.
   */
  @JacksonXmlProperty(localName = "api:ModelRepositoryID")
  @Schema(example = "https://github.com/niem/niem-training")
  private String repo;

  /**
   * A common alias, term, or phrase that would help to facilitate search and
   * discovery of this IEPD.
   */
  @JacksonXmlProperty(localName = "c:KeywordText")
  @Schema(example = "crash, accident, vehicle, injury, charge, driver")
  private String keywords;

  /**
   * A description of the environment or NIEM Domain in which this IEPD is
   * applicable or used.
   */
  @JacksonXmlProperty(localName = "c:DomainText")
  @Schema(example = "Justice, Transportation, Highway Safety")
  private String subjects;

  /**
   * A description of the intended usage and reason for which an IEPD exists.
   */
  @JacksonXmlProperty(localName = "api:ModelPurposeText")
  @Schema(
      example = "This IEPD was developed to demonstrate NIEM concepts such as associations, roles, augmentations, metadata, adapters, and external standards.")
  private String purpose;

  /**
   * A name of an organization or person that developed a model.
   */
  @JacksonXmlProperty(localName = "api:ModelDeveloperText")
  @Schema(example = "NIEM staff")
  private String developer;

  /**
   * A kind of model, such as reference, message, or other.
   */
  public enum Category {
    reference,
    message,
    other
  }

  /**
   * A kind of data model.
   */
  @Enumerated(EnumType.STRING)
  @JacksonXmlProperty(localName = "api:ModelCategoryCode")
  @Schema(example = "message")
  private Category category;

  /**
   * A kind of message objective, such as implementation, example, or test.
   */
  public enum Objective {
    implementation,
    example,
    test,
    other
  }

  /**
   * An intended use for a model, such as for actual implementation or for
   * testing tool features or IEPD / message specification features.
   */
  @Enumerated(EnumType.STRING)
  @JacksonXmlProperty(localName = "api:ModelObjectiveCode")
  @Schema(example = "example")
  private Objective objective;

  /**
   * A set of versions defined by a model.
   */
  @JsonIgnore
  @Builder.Default
  @ToString.Exclude
  @EqualsAndHashCode.Exclude
  @OneToMany(mappedBy = "model")
  @OrderBy("versionNumber")
  @AssociationInverseSide(inversePath = @ObjectPath(@PropertyValue(propertyName = "model")))
  private Set<Version> versions = new HashSet<Version>();

  /**
   * Gets the Steward responsible for this model.
   * Makes sure a potential Hibernate proxy is initialized.
   */
  public Steward getSteward() {
    Steward steward = this.steward;
    if (steward instanceof HibernateProxy) {
      steward = Hibernate.unproxy(steward, Steward.class);
    }
    return steward;
  }

  // TODO: set stewardships
  // @JsonIgnore @Builder.Default @ToString.Exclude @EqualsAndHashCode.Exclude
  // @OneToMany(mappedBy="model", cascade=CascadeType.ALL, fetch=FetchType.LAZY)
  // private Set<ModelStewardship> stewardships = new HashSet<ModelStewardship>();

  // TODO: set maintainers
  // @JsonIgnore @Builder.Default @ToString.Exclude @EqualsAndHashCode.Exclude
  // @OneToMany(mappedBy="model", cascade=CascadeType.ALL, fetch=FetchType.LAZY)
  // private Set<ModelMaintainer> maintainers = new HashSet<ModelMaintainer>();

  /**
   * Creates a model with the given fields.
   */
  public Model(String shortName) {
    this.shortName = shortName;
  }

  /**
   * Creates a model with the given fields.
   */
  public Model(String shortName, String longName, Category category) {
    this.shortName = shortName;
    this.fullName = longName;
    this.category = category;
  }

  /**
   * Creates a model with the given fields.
   */
  public Model(String shortName, String longName, Steward steward, Category category) {
    this.shortName = shortName;
    this.fullName = longName;
    this.category = category;
    this.steward = steward;
  }

  @Override
  public Steward getParentEntity() {
    return this.steward;
  }

  @Override
  @Schema(
      example = Config.BASE_URL + "/stewards/niem/models/crash-driver",
      description = "An endpoint to get information about a model.")
  public String getRoute() {
    String stewardRoute = this.steward.getRoute();
    return String.format("%s/models/%s", stewardRoute, this.modelKey);
  }

  @Override
  @Schema(
      example = "Model",
      description = "A kind of NIEM entity, such as a Namespace or a Property.")
  public String getClassName() {
    return super.getClassName();
  }

  @Override
  @Schema(
      example = "niem/crash-driver",
      description = "A unique identifier.  For a model, this is combines the stewardKey and modelKey fields.")
  public String getFullIdentifier() {
    return String.format("%s/%s", this.getStewardKey(), this.getModelKey());
  }

  @Override
  @Schema(
      example = "crash-driver",
      description = "An identifier, unique within its immediate scope.  For a model, this is the same as the modelKey field (unique within its steward).")
  public String getLocalIdentifier() {
    return this.getModelKey();
  }

  @Override
  @Schema(
      example = "NIEM Crash Driver",
      description = "A steward short name and model short name.")
  public String getTitle() {
    return String.format("%s %s", this.getSteward().getTitle(), this.getShortName());
  }

  /**
   * Gets the API route to the NIEM model.
   */
  @JsonIgnore
  public String getNiemRoute() {
    String stewardRoute = this.steward.getNiemRoute();
    return String.format("%s/models/%s", stewardRoute, niemModelKey);
  }

  /**
   * Gets keys fields about the model.
   */
  @JsonIgnore
  public Map<String, String> toSummary() {
    Map<String, String> map = new HashMap<>();
    map.put("modelKey", this.getModelKey());
    map.put("shortName", this.shortName);
    map.put("route", this.getRoute());
    return map;
  }

  // TODO: getStewardDescriptor()
  // public Object getStewardDescriptor() {
  //   return this.steward.toDescriptor();
  // }

  // TODO: getHistoricalStewardships()
  // public List<Object> getHistoricalStewardships() {
  //   return this.getStewardships()
  //   .stream()
  //   .map(stewardship -> stewardship.toDescriptorBaseWithSteward())
  //   .collect(Collectors.toList());
  // }

}
