package gov.niem.tools.api.db.subproperty;

import gov.niem.tools.api.core.config.Config;
import gov.niem.tools.api.db.base.BaseCmfEntity;
import gov.niem.tools.api.db.base.BaseNamespaceEntity;
import gov.niem.tools.api.db.namespace.Namespace;
import gov.niem.tools.api.db.property.Property;
import gov.niem.tools.api.db.type.Type;

import org.mitre.niem.cmf.CMFException;
import org.mitre.niem.cmf.HasProperty;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlRootElement;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.annotation.Nullable;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.ForeignKey;
import jakarta.persistence.Index;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.Pattern;
import java.util.Map;
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
 * A subproperty is a property that is contained by a type, along with cardinality constraints.
 * Also called a child property association.
 */
@Entity
@Audited
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(callSuper = false)
@JacksonXmlRootElement(localName = "Subproperty")
@Schema(name = "Subproperty")
@Table(
    uniqueConstraints = {@UniqueConstraint(
        name = "subproperty_type_property_key", columnNames = {"type_id", "property_id"})},
    indexes = {
      @Index(name = "subproperty_type_key", columnList = "type_id"),
      @Index(name = "subproperty_property_key", columnList = "property_id"),
      @Index(name = "subproperty_min_key", columnList = "min"),
      @Index(name = "subproperty_max_key", columnList = "max"),
      @Index(name = "subproperty_definition_key", columnList = "definition")
    }
)
public class Subproperty extends BaseNamespaceEntity<Subproperty>
    implements BaseCmfEntity<org.mitre.niem.cmf.HasProperty>, Comparable<Subproperty> {

  @JsonIgnore
  @ManyToOne(fetch = FetchType.LAZY)
  @JoinColumn(foreignKey = @ForeignKey(name = "type_fkey"))
  private Type type;

  @ManyToOne(fetch = FetchType.LAZY)
  @JoinColumn(foreignKey = @ForeignKey(name = "property_fkey"))
  private Property property;

  @JsonIgnore
  @NotAudited
  @Formula("(SELECT namespace.version_id FROM namespace INNER JOIN type on namespace.id = type.namespace_id WHERE type.id = type_id)")
  private Long versionId;

  /**
   * Min cardinality 0 or 1-9 with zero or more additional digits.
   */
  @Pattern(regexp = "^0|([1-9]\\d+)$")
  private String min;

  /**
   * Max cardinality 'unbounded' or 1-9 with zero or more additional digits.
   */
  @Pattern(regexp = "^unbounded|([1-9]\\d+)$")
  private String max;

  @Min(0)
  private int sequence;

  @Nullable
  private String definition;


  @JsonIgnore
  @NotAudited
  @Formula("(SELECT namespace.prefix||':'||type.name FROM type INNER JOIN namespace on type.namespace_id = namespace.id WHERE type_id = type.id)")
  public String typeQname;

  @JsonIgnore
  @NotAudited
  @Formula("(SELECT namespace.prefix||':'||property.name FROM property INNER JOIN namespace ON property.namespace_id = namespace.id WHERE property_id=property.id)")
  public String propertyQname;

  /**
   * Creates a subproperty with the given type and property.
   * Sets default cardinality with min of 0 (optional) and a max of 'unbounded' (for elements)
   * or '1' (for attributes).
   */
  Subproperty(Type type, Property property) {
    this.type = type;
    this.property = property;
    this.min = "0";
    this.max = property.isElement() ? "unbounded" : "1";
  }

  /**
   * Creates a subproperty with the given type, property, and cardinality constraints.
   */
  Subproperty(Type type, Property property, String min, String max) {
    this.type = type;
    this.property = property;
    this.min = min;
    this.max = max;
  }


  /**
   * Gets the type of this subproperty.
   * Makes sure a potential Hibernate proxy is initialized.
   */
  public Type getType() {
    Type type = this.type;
    if (type instanceof HibernateProxy) {
      type = Hibernate.unproxy(type, Type.class);
    }
    return type;
  }

  /**
   * Gets the property of this subproperty.
   * Makes sure a potential Hibernate proxy is initialized.
   */
  public Property getProperty() {
    Property property = this.property;
    if (property instanceof HibernateProxy) {
      property = Hibernate.unproxy(property, Property.class);
    }
    return property;
  }

  /**
   * Gets the namespace where this subproperty is defined (the type namespace).
   */
  @JsonIgnore
  public Namespace getNamespace() {
    return this.type == null ? null : this.type.getNamespace();
  }

  /**
   * Gets the qualified name of this subproperty's type.
   */
  @JsonIgnore
  public String getTypeQname() {
    return this.type == null ? null : this.type.getQname();
  }

  /**
   * Gets the namespace prefix of this subproperty's type.
   */
  @JsonIgnore
  public String getTypePrefix() {
    return this.type == null ? null : this.type.getPrefix();
  }

  /**
   * Gets the qualified name of this subproperty's property.
   */
  @JsonIgnore
  public String getPropertyQname() {
    return this.property == null ? null : this.property.getQname();
  }

  /**
   * Gets the namespace prefix of this subproperty's property.
   */
  @JsonIgnore
  public String getPropertyPrefix() {
    return this.property == null ? null : this.property.getPrefix();
  }

  /**
   * Gets summary fields about this subproperty's type.
   */
  @JsonProperty("type")
  public Map<String, String> getTypeSummary() {
    return this.type == null ? null : this.type.toSummary();
  }

  /*
   * Gets summary fields about this subproperty's property.
   */
  @JsonProperty("property")
  public Map<String, String> getPropertySummary() {
    return this.property == null ? null : this.property.toSummary();
  }

  @Override
  public Type getParentEntity() {
    return this.getType();
  }

  @Override
  @Schema(
      example = Config.BASE_URL + "/stewards/niem/models/model/versions/5.2/types/nc:PersonType/subproperties/nc:PersonName",
      description = "An endpoint to get information about a subproperty."
  )
  public String getRoute() {
    String typeRoute = this.type.getRoute();
    return String.format("%s/subproperties/%s", typeRoute, this.propertyQname);
  }

  @Override
  @Schema(
        example = "Subproperty",
        description = "A kind of NIEM entity, such as a Namespace or a Property.")
  public String getClassName() {
    return super.getClassName();
  }

  @Override
  @Schema(
      example = "niem/model/5.2/nc:PersonType/nc:PersonName",
      description = "A unique identifier.  For a subproperty, this is combines the stewardKey, modelKey, versionNumber, qualified container type, and qualified property fields.")
  public String getIdLabel() {
    return this.getType().getIdLabel() + "/" + this.getPropertyQname();
  }

  @Override
  @Schema(
      example = "nc:PersonType/nc:PersonName",
      description = "An identifier, unique within its immediate scope.  For a subproperty, this is the qname of type container type followed by the the qname of the contained property (unique within its version).")
  public String getIdLocalLabel() {
    return this.typeQname + "/" + this.propertyQname;
  }

  @Override
  @Schema(
      example = "NIEM Model 5.2: nc:PersonType contains nc:PersonName",
      description = "A steward short name, model short name, version number, qualified type name, and qualified property name.")
  public String getTitle() {
    return this.getType().getTitle() + " contains " + this.getPropertyQname();
  }

  @Override
  public void addToCmfModel(org.mitre.niem.cmf.Model cmfModel) throws CMFException {
    if (this.type == null) {
      return;
    }
    // Add the class to the model if it is not already there
    org.mitre.niem.cmf.ClassType classType = cmfModel.getClassType(this.type.getQname());
    if (classType == null) {
      classType = this.type.toCmfClassType();
      cmfModel.addComponent(classType);
    }

    // Flag the CMF type as augmentable if applicable
    if (this.typeQname.endsWith("AugmentationType")) {
      classType.setIsAugmentable(true);
    }

    // Add the subproperty to the class
    classType.addHasProperty(this.toCmf());
  }

  @Override
  public HasProperty toCmf() throws CMFException {

    HasProperty hasProperty = new HasProperty();
    hasProperty.setProperty(this.property.toCmf());
    hasProperty.setMinOccurs(Integer.parseInt(this.min));

    // Set max as integer or "unbounded"
    if (this.max.equals("unbounded")) {
      hasProperty.setMaxUnbounded(true);
    }
    else {
      hasProperty.setMaxOccurs(Integer.parseInt(this.max));
    }

    // Set augmentation info
    if (this.getTypeQname().endsWith("AugmentationType")) {
      org.mitre.niem.cmf.Namespace cmfNamespace = this.getType().getNamespace().toCmf();
      hasProperty.augmentingNS().add(cmfNamespace);
    }

    return hasProperty;

  }

  /**
   * Custom sort function for subproperties, sorting by type qname, then sequence.
   */
  @Override
  public int compareTo(Subproperty other) {
    if (!this.getTypeQname().equals(other.getTypeQname())) {
      return this.getType().compareTo(other.getType());
    }
    return Integer.compare(this.sequence, other.sequence);
  }

}
