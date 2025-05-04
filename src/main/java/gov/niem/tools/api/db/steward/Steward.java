package gov.niem.tools.api.db.steward;

import gov.niem.tools.api.core.config.Config;
import gov.niem.tools.api.db.base.BaseEntity;
import gov.niem.tools.api.db.model.Model;
import gov.niem.tools.api.db.niem.genc.CountryAlpha3CodeType;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlProperty;
import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlRootElement;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Index;
import jakarta.persistence.OneToMany;
import jakarta.persistence.OrderBy;
import jakarta.persistence.Table;
import jakarta.persistence.Transient;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.ToString;
import lombok.experimental.SuperBuilder;
import org.hibernate.annotations.Formula;
import org.hibernate.envers.Audited;
import org.hibernate.envers.NotAudited;
import org.hibernate.search.engine.backend.types.Projectable;
import org.hibernate.search.engine.backend.types.Sortable;
import org.hibernate.search.mapper.pojo.mapping.definition.annotation.AssociationInverseSide;
import org.hibernate.search.mapper.pojo.mapping.definition.annotation.Indexed;
import org.hibernate.search.mapper.pojo.mapping.definition.annotation.KeywordField;
import org.hibernate.search.mapper.pojo.mapping.definition.annotation.ObjectPath;
import org.hibernate.search.mapper.pojo.mapping.definition.annotation.PropertyValue;

/**
 * A program, group, or other entity responsible for managing NIEM content.
 */
@Entity
@Audited
@Data
@SuperBuilder
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(callSuper = false)
@JacksonXmlRootElement(localName = "api:Steward")
@Schema(name = "Steward")
@Indexed
@Table(indexes = {
  @Index(name = "steward_category_idx", columnList = "category"),
  @Index(name = "steward_country_idx", columnList = "country"),
  @Index(name = "steward_short_name_idx", columnList = "shortName"),
  @Index(name = "steward_unit_idx", columnList = "unit")
})
public class Steward extends BaseEntity implements Comparable<Steward> {

  @Transient @JsonIgnore
  public static final String niemStewardKey = "niem";

  /**
   * A human-readable and url-friendly steward identifier generated from the shortName field.
   */
  @NotAudited
  @Formula("slugify(short_name)")
  @JacksonXmlProperty(localName = "api:StewardKeyID")
  @Schema(example = "niem")
  private String stewardKey;

  /**
   * A short name or acronym used to identify a steward.  This could be the name of an
   * organization or unit, a program name, or other kind of authoritative source.
   */
  @NotBlank(message = "shortName is required")
  @Column(unique = true, nullable = false)
  @JacksonXmlProperty(localName = "api:StewardShortName")
  @Schema(example = "NIEM")
  @KeywordField(sortable = Sortable.YES, projectable = Projectable.YES)
  private String shortName;

  /**
   * A name of an organization.
   */
  @JacksonXmlProperty(localName = "nc:OrganizationName")
  @Schema(example = "NIEM Management Office")
  // @FullTextField
  private String fullName;

  /**
   * General kinds of stewards, such as Federal, State, Local, Industry, Nonprofit, etc.
   */
  public enum Category {
    Federal,
    State,
    Local,
    Tribal,
    Territorial,
    International,
    Industry,
    Nonprofit,
    SDO,
    Educational,
    Person,
    Other
  }

  /**
   * A kind of steward.
   */
  @JacksonXmlProperty(localName = "api:StewardCategoryCode")
  @Enumerated(EnumType.STRING)
  private Category category;

  /**
   * A description of an organization.
   */
  @Column(columnDefinition = "text")
  @JacksonXmlProperty(localName = "nc:OrganizationDescriptionText")
  @Schema(
      example = "A community-driven, standards-based approach to defining information exchange packages for multiple business domains.")
  private String description;

  /**
   * A name of the person to be contacted.
   */
  @JacksonXmlProperty(localName = "usmtf:ContactName")
  @Schema(example = "George P. Burdell")
  private String contactName;

  /**
   * A website address by which a person or organization may be contacted.
   */
  @JacksonXmlProperty(localName = "nc:ContactWebsiteURI")
  @Schema(example = "https://niem.gov")
  private String website;

  /**
   * An electronic mailing address by which a person or organization may be
   * contacted.
   */
  @Email
  @JacksonXmlProperty(localName = "nc:ContactEmailID")
  @Schema(example = "information@niem.gov")
  private String email;

  /**
   * A complete telephone number.
   */
  @JacksonXmlProperty(localName = "nc:TelephoneNumberFullID")
  @Schema(example = "555-867-5309")
  private String phone;

  /**
   * A complete address.
   */
  @JacksonXmlProperty(localName = "nc:AddressFullText")
  @Schema(example = "Hampton Roads, Virginia")
  private String address;

  /**
   * A country, territory, dependency, or other such geopolitical subdivision of a
   * location.
   */
  @JacksonXmlProperty(localName = "genc:CountryAlpha3Code")
  @Enumerated(EnumType.STRING)
  private CountryAlpha3CodeType country;

  /**
   * A name of a high-level division of an organization.
   */
  @JacksonXmlProperty(localName = "nc:OrganizationUnitName")
  @Schema(example = "Joint Staff J6")
  private String unit;

  /**
   * A name of a subdivision of an organization.
   */
  @JacksonXmlProperty(localName = "nc:OrganizationSubUnitName")
  @Schema(example = "Data and Standards Division")
  private String subunit;

  /**
   * A set of models which belong to the steward.
   */
  @JsonIgnore @Builder.Default @ToString.Exclude @EqualsAndHashCode.Exclude
  @OneToMany(mappedBy = "steward", cascade = CascadeType.PERSIST, orphanRemoval = true)
  @OrderBy("shortName")
  @NotAudited
  @AssociationInverseSide(inversePath = @ObjectPath(@PropertyValue(propertyName = "steward")))
  private Set<Model> models = new HashSet<>();

  // TODO: set modelStewardships
  // @JsonIgnore @Builder.Default @ToString.Exclude @EqualsAndHashCode.Exclude
  // @OneToMany(mappedBy="steward", cascade=CascadeType.ALL,   // @NotAudited
  // private Set<ModelStewardship> modelStewardships = new HashSet<ModelStewardship>();

  // TODO: set namespaces
  // @JsonIgnore @Builder.Default @ToString.Exclude @EqualsAndHashCode.Exclude
  // @OneToMany(mappedBy="steward")
  // @NotAudited
  // private Set<Namespace> namespaces = new HashSet<>();

  public Steward(String shortName, String longName) {
    this.shortName = shortName;
    this.fullName = longName;
  }

  @Override
  public BaseEntity getParentEntity() {
    return null;
  }

  @Override
  @Schema(
      example = Config.BASE_URL + "/stewards/niem",
      description = "An endpoint to get information about a steward.")
  public String getRoute() {
    return String.format("%s/stewards/%s", Config.baseUrl, this.stewardKey);
  }

  @Override
  @Schema(
      example = "Steward",
      description = "A kind of NIEM entity, such as a Namespace or a Property.")
  public String getClassName() {
    return super.getClassName();
  }

  @Override
  @Schema(
      example = "niem",
      description = "A unique identifier.  For a steward, this is the stewardKey field.")
  public String getIdLabel() {
    return this.getStewardKey();
  }

  @Override
  @Schema(
      example = "niem",
      description = "A scoped identifier.  For a steward, this is the stewardKey field.")
  public String getIdLocalLabel() {
    return this.getIdLabel();
  }

  @Override
  @Schema(example = "NIEM", description = "A description used to identify an entity.")
  public String getTitle() {
    return this.shortName;
  }

  /**
   * Route to the NIEM steward.
   */
  @JsonIgnore
  public String getNiemRoute() {
    return String.format("%s/stewards/%s", Config.baseUrl, niemStewardKey);
  }

  /**
   * Get a model belonging to the steward with the given model key.
   */
  public Optional<Model> getModelOptional(String modelKey) {
    return this.getModels()
    .stream()
    .filter(m -> m.getModelKey().equals(modelKey))
    .findFirst();
  }

  /**
   * Get key fields about a steward.
   */
  @JsonIgnore
  public Map<String, String> toSummary() {
    Map<String, String> map = new HashMap<>();
    map.put("stewardKey", this.getStewardKey());
    map.put("shortName", this.shortName);
    map.put("@id", this.getIdLabel());
    map.put("route", this.getRoute());
    return map;
  }

  // TODO: static format()
  // public static Object format(List<Steward> stewards, Format format) throws CMFException {
  //   switch (format) {
  //     case cmf:
  //       return new StewardTypeList(stewards);
  //     case api:
  //       return new StewardList(stewards);
  //   }
  //   return null;
  // }

  // TODO: getHistoricalStewardship()
  // @JsonIgnore
  // public Set<Model> getHistoricalStewardship() {
  //   return this.getModelStewardships()
  //   .stream()
  //   .filter( stewardship -> stewardship.isCurrent() == true )
  //   .map( stewardship -> stewardship.getModel() )
  //   .collect(Collectors.toSet());
  // }

  // TODO: getHistoricalModelLabels()
  // @JsonIgnore
  // public Set<String> getHistoricalModelLabels() {
  //   return this.getHistoricalStewardship()
  //   .stream()
  //   .map(model -> model.getModelKey())
  //   .collect(Collectors.toSet());
  // }

  // TODO: getHistoricalModelRoutes()
  // public HashMap<String, String> getHistoricalModelRoutes() {
  //   HashMap<String, String> map = new HashMap<String, String>();
  //   for(Model model : this.getHistoricalStewardship()) {
  //     map.put(model.getShortName(),
  //       AppUtils.getModelRoute(this.getStewardKey(), model.getModelKey())
  //     );
  //   }
  //   return map;
  // }

  // TODO: getHistoricalModelStewardships()
  // public List<Object> getHistoricalModelStewardships() {
  //   return this.getModelStewardships()
  //   .stream()
  //   .map(stewardship -> stewardship.toDescriptorBaseWithModel())
  //   .collect(Collectors.toList());
  // }

  // TODO: toCmf()
  // @Override
  // public StewardType toCmf() {

  //   StewardType steward = new StewardType();

  //   steward.stewardKey = this.stewardKey;
  //   steward.name = this.fullName;
  //   steward.description = this.description;
  //   steward.category = this.category;
  //   steward.shortName = this.shortName;
  //   steward.unit = this.unit;
  //   steward.subunit = this.subunit;

  //   steward.location.address.country.alpha3Code = this.country;

  //   steward.contact.email = this.email;
  //   steward.contact.website = this.website;
  //   steward.contact.phone.fullNumber.number = this.phone;
  //   steward.contact.address.fullText = this.address;

  //   return steward;

  // }

  /**
   * Custom sorting function for stewards.
   * Return the NIEM steward first, followed by stewards sorted by steward key.
   */
  @Override
  public int compareTo(Steward other) {
    // Return the NIEM steward first.
    if (this.stewardKey.equals(Steward.niemStewardKey)) {
      return -1;
    }
    return this.stewardKey.compareTo(other.stewardKey);
  }

}
