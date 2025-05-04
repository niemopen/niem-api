package gov.niem.tools.api.db.facet;

import gov.niem.tools.api.core.config.Config;
import gov.niem.tools.api.db.base.BaseCmfEntity;
import gov.niem.tools.api.db.base.BaseNamespaceEntity;
import gov.niem.tools.api.db.namespace.Namespace;
import gov.niem.tools.api.db.type.Type;

import org.mitre.niem.cmf.CMFException;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlRootElement;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.ForeignKey;
import jakarta.persistence.Index;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import org.hibernate.Hibernate;
import org.hibernate.annotations.Formula;
import org.hibernate.envers.Audited;
import org.hibernate.envers.NotAudited;
import org.hibernate.proxy.HibernateProxy;

/**
 * A constraint on a datatype, such as an enumeration or a pattern.
 */
@Entity
@Audited
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(callSuper = false)
@JacksonXmlRootElement(localName = "api:Facet")
@Schema(name = "Facet")
@Table(
    // uniqueConstraints = {@UniqueConstraint(
    //   name = "facet_type_category_value", columnNames = {"type_id", "category", "value"}
    // )},
    indexes = {
      @Index(name = "facet_type_key", columnList = "type_id"),
      @Index(name = "facet_category_key", columnList = "category"),
      @Index(name = "facet_value_key", columnList = "value")
    }
)
public class Facet extends BaseNamespaceEntity<Facet>
    implements BaseCmfEntity<org.mitre.niem.cmf.Facet> {

  /**
   * Type that contains the facet.
   */
  @JsonIgnore
  @ManyToOne(fetch = FetchType.LAZY)
  @JoinColumn(foreignKey = @ForeignKey(name = "type_fkey"))
  private Type type;

  @JsonIgnore
  @NotAudited
  @Formula("(SELECT namespace.prefix FROM namespace INNER JOIN type on namespace.id=type.namespace_id WHERE type.id = type_id)")
  private String prefix;

  @JsonIgnore
  @NotAudited
  @Formula("(SELECT namespace.prefix||':'||type.name FROM namespace INNER JOIN type on namespace.id=type.namespace_id WHERE type.id = type_id)")
  private String qname;

  @JsonIgnore
  @NotAudited
  @Formula("(SELECT namespace.version_id FROM namespace INNER JOIN type on namespace.id = type.namespace_id WHERE type.id = type_id)")
  private Long versionId;

  /**
   * Kinds of facet categories, such as enumeration, pattern, and length.
   */
  public enum Category {
    enumeration,
    pattern,
    length,
    minLength,
    maxLength,
    minExclusive,
    maxExclusive,
    minInclusive,
    maxInclusive,
    fractionDigits,
    totalDigits,
    whiteSpace
  }

  /**
   * A kind of facet, e.g., "enumeration"
   */
  @Builder.Default
  @Enumerated(EnumType.STRING)
  private Category category = Category.enumeration;

  private String value;

  private String definition;

  /**
   * Creates a new facet with the given fields on the given type.
   */
  public Facet(Type type, Category category, String value, String definition) {
    this.setType(type);
    this.setCategory(category);
    this.setValue(value);
    this.setDefinition(definition);
    this.setPrefix(type.getPrefix());
    this.setVersionId(type.getVersionId());
    this.setQname(type.getPrefix() + ":" + type.getName());
  }

  /**
   * Gets the Namespace that contains the type of this facet.
   */
  @JsonIgnore
  public Namespace getNamespace() {
    return this.type == null ? null : this.type.getNamespace();
  }

  /**
   * Gets the Type that contains this facet.
   */
  @JsonIgnore
  public Type getType() {
    Type type = this.type;
    if (type instanceof HibernateProxy) {
      type = Hibernate.unproxy(type, Type.class);
    }
    return type;
  }

  @Override
  public Type getParentEntity() {
    return this.getType();
  }

  @Override
  @Schema(
      example = Config.BASE_URL + "/stewards/niem/models/model/versions/5.2/types/nc:AddressCategoryCodeType/enumeration/residential",
      description = "An endpoint to get information about a facet.")
  public String getRoute() {
    if (this.type == null) {
      return null;
    }
    String typeRoute = this.type.getRoute();
    return String.format("%s/%s/%s", typeRoute, this.category, this.value);
  }

  @Override
  @Schema(
      example = "Facet",
      description = "A kind of NIEM entity, such as a Namespace or a Property.")
  public String getClassName() {
    return super.getClassName();
  }

  @Override
  @Schema(
      example = "niem/model/5.2/nc:AddressCategoryCodeSimpleType/enumeration/residential",
      description = "A unique identifier.  For a facet, this is combines the stewardKey, modelKey, versionNumber, qualified type, facet category, and facet value fields.")
  public String getFullIdentifier() {
    if (this.type == null) {
      return null;
    }
    return String.format("%s/%s/%s", this.type.getFullIdentifier(), this.category, this.value);
  }

  @Override
  @Schema(
      example = "nc:AddressCategoryCodeSimpleType/enumeration/residential",
      description = "An identifier, unique within its immediate scope.  For a facet, this is the qname of its type followed by facet category and value (unique within its version starting with NIEM 6.0).")
  public String getLocalIdentifier() {
    return String.format("%s/%s/%s", this.qname, this.category, this.value);
  }

  @Override
  @Schema(
      example = "NIEM Model 5.2: nc:PersonType contains nc:PersonName",
      description = "A steward short name, model short name, version number, qualified type name, and qualified property name.")
  public String getTitle() {
    if (this.type == null) {
      return null;
    }
    return String.format("%s %s %s", this.getType().getTitle(), this.category, this.value);
  }

  /**
   * Adds this facet to the given CMF model.
   */
  public void addToCmfModel(org.mitre.niem.cmf.Model cmfModel) throws CMFException {
    if (this.type == null) {
      return;
    }
    org.mitre.niem.cmf.Datatype datatype = cmfModel.getDatatype(this.qname);
    if (datatype == null) {
      datatype = this.type.toCmfDatatype();
      cmfModel.addComponent(datatype);
    }
    datatype.getRestrictionOf().addFacet(this.toCmf());
  }

  /**
   * Converts this facet to a CMF facet object.
   */
  public org.mitre.niem.cmf.Facet toCmf() throws CMFException {
    org.mitre.niem.cmf.Facet cmfFacet = new org.mitre.niem.cmf.Facet();
    cmfFacet.setDefinition(this.definition);
    cmfFacet.setFacetKind(this.getCmfFacetKind(this.category));
    cmfFacet.setStringVal(this.value);
    return cmfFacet;
  }

  /**
   * CMF Facet.facetKind values must be upper camel case.
   */
  public String getCmfFacetKind(Category category) {
    switch (category) {
      case enumeration:
        return "Enumeration";
      case fractionDigits:
        return "FractionDigits";
      case length:
        return "Length";
      case maxExclusive:
        return "MaxExclusive";
      case maxInclusive:
        return "MinInclusive";
      case maxLength:
        return "MaxLength";
      case minExclusive:
        return "MinExclusive";
      case minInclusive:
        return "MinInclusive";
      case minLength:
        return "MinLength";
      case pattern:
        return "Pattern";
      case totalDigits:
        return "TotalDigits";
      case whiteSpace:
        return "WhiteSpace";
      default:
        return null;
    }
  }

}
