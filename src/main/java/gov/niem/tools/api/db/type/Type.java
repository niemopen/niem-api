package gov.niem.tools.api.db.type;

import gov.niem.tools.api.core.config.Config;
import gov.niem.tools.api.db.base.AddModelReason;
import gov.niem.tools.api.db.base.BaseCmfEntity;
import gov.niem.tools.api.db.component.Component;
import gov.niem.tools.api.db.facet.Facet;
import gov.niem.tools.api.db.property.Property;
import gov.niem.tools.api.db.subproperty.Subproperty;
import gov.niem.tools.api.validation.Test;

import org.mitre.niem.cmf.CMFException;
import org.mitre.niem.cmf.ClassType;
import org.mitre.niem.cmf.Datatype;
import org.mitre.niem.cmf.ListType;
import org.mitre.niem.cmf.Restriction;
import org.mitre.niem.cmf.Union;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlRootElement;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.persistence.CascadeType;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.Index;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OneToMany;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
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
import org.hibernate.search.mapper.pojo.mapping.definition.annotation.GenericField;

/**
 * A type defines a structure - an allowable set of values.
 * A type might describe a simple value (e.g., a string, a number)
 * or a complex object (e.g., PersonType).
 */
@Entity
@Audited
@Data
@SuperBuilder
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(callSuper = false)
@JacksonXmlRootElement(localName = "Type")
@JsonIgnoreProperties({"hibernateLazyInitializer", "handler"})
@Schema(name = "Type")
@Table(
    uniqueConstraints = {@UniqueConstraint(
        name = "type_namespace_name_key", columnNames = { "namespace_id", "name" })},
    indexes = {
      @Index(name = "type_category_idx", columnList = "category"),
      @Index(name = "type_base_id_idx", columnList = "base_id"),
      @Index(name = "type_namespace_prefix_idx", columnList = "namespace_prefix"),
      @Index(name = "type_namespace_id_idx", columnList = "namespace_id"),
      @Index(name = "type_name_idx", columnList = "name")
    }
)
public class Type extends Component<Type> implements BaseCmfEntity<org.mitre.niem.cmf.Component> {

  @JsonIgnore
  @ManyToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "base_id", referencedColumnName = "id")
  private Type base;

  /**
   * A kind of type, representing CCC types (classes), CSC types (datatype classes)
   * and simple types (datatypes).
   */
  public enum Category {
    complex_object,
    complex_value,
    simple_value
  }

  @GenericField
  @Enumerated(EnumType.STRING)
  private Category category;

  /**
   * A derivation method for a type, i.e., extension or restriction.
   */
  public enum Derivation {
    extension,
    restriction
  }

  @Enumerated(EnumType.STRING)
  private Derivation derivation;

  /**
   * A kind of type pattern with values representing concepts like object, adapter,
   * association, list, union, etc.
   */
  public enum Pattern {
    object,
    adapter,
    association,
    augmentation,
    metadata,
    complex_value,
    simple_value,
    simple_list,
    simple_union
  }

  @Enumerated(EnumType.STRING)
  private Pattern pattern;

  @JsonIgnore
  @ToString.Exclude
  @EqualsAndHashCode.Exclude
  @Builder.Default
  @OneToMany(mappedBy = "type")
  private Set<Property> dataProperties = new HashSet<>();

  @JsonIgnore
  @ToString.Exclude
  @EqualsAndHashCode.Exclude
  @Builder.Default
  @OneToMany(mappedBy = "type", cascade = CascadeType.ALL)
  private Set<Subproperty> subproperties = new HashSet<>();

  @JsonIgnore
  @ToString.Exclude
  @EqualsAndHashCode.Exclude
  @Builder.Default
  @OneToMany(mappedBy = "type")
  private Set<Facet> facets = new HashSet<>();

  /**
   * Gets the extension (e.g., parent) or restriction base type.
   */
  public Type getBase() {
    Type base = this.base;
    if (base instanceof HibernateProxy) {
      base = Hibernate.unproxy(base, Type.class);
    }
    return base;
  }

  /**
   * True if the type can contain attributes (type is complex).
   */
  @JsonProperty("isComplex")
  public boolean isComplex() {
    if (this.getCategory().toString().startsWith("complex")) {
      return true;
    }
    return false;
  }

  /**
   * True if the type can contain elements (type is a CCC).
   */
  @JsonProperty("isComplexContent")
  public boolean isComplexContent() {
    if (this.getCategory().equals(Category.complex_object)) {
      return true;
    }
    return false;
  }

  /**
   * True if a type carries a value and cannot contain attributes (type is a datatype).
   */
  @JsonProperty("isSimple")
  public boolean isSimple() {
    if (this.getCategory().toString().startsWith("simple")) {
      return true;
    }
    return false;
  }

  /**
   * True if a type carries a value (type is simple or CSC).  It may or may not contain attributes.
   */
  @JsonProperty("isSimpleContent")
  public boolean isSimpleContent() {
    if (this.getCategory().equals(Category.complex_object)) {
      return false;
    }
    return true;
  }

  /**
   * Get key fields about a type.
   */
  @JsonProperty("base")
  public Map<String, String> getBaseSummary() {
    return this.base == null ? null : this.base.toSummary();
  }

  @Override
  @Schema(
      example = Config.BASE_URL + "/stewards/niem/models/crash-driver/version/1.1/types/nc:PersonType",
      description = "An endpoint to get information about a type.")
  public String getRoute() {
    String versionRoute = this.getVersion().getRoute();
    return String.format("%s/types/%s", versionRoute, this.getQname());
  }

  @Override
  @Schema(
      example = "Type",
      description = "A kind of NIEM entity, such as a Namespace or a Property.")
  public String getClassName() {
    return super.getClassName();
  }

  @Override
  @Schema(
      example = "niem/crash-driver/1.1/nc:PersonType",
      description = "A unique identifier.  For a type, this is combines the stewardKey, modelKey, versionNumber, prefix, and name fields.")
  public String getIdLabel() {
    return this.getVersion().getIdLabel() + "/" + this.getQname();
  }

  @Override
  @Schema(
      example = "nc:PersonType",
      description = "An identifier, unique within its immediate scope.  For a type, this is the same as the qname field (unique within its version).")
  public String getIdLocalLabel() {
    return this.getQname();
  }

  @Override
  @Schema(
      example = "NIEM Crash Driver 1.1: nc:PersonType",
      description = "A steward short name, model short name, version number, and qualified type name.")
  public String getTitle() {
    return super.getTitle();
  }

  /**
   * True if this type should be considered a CMF class type; false
   * if it should be considered a CMF datatype.
   */
  @JsonIgnore
  public boolean isCmfDatatype() {
    return this.isSimple();
  }

  @Override
  public org.mitre.niem.cmf.Component addToCmfModel(org.mitre.niem.cmf.Model cmfModel, boolean
      addDependencies, AddModelReason addModelReason, Test test) throws CMFException {

    // Add CMF namespace to model if not already there
    if (this.getNamespace() != null) {
      this.getNamespace().addToCmfModel(cmfModel, addDependencies, addModelReason, test);
    }

    org.mitre.niem.cmf.Component cmfComponent = cmfModel.qnToComponent(this.qname);

    if (cmfComponent == null) {
      if (this.isCmfDatatype()) {
        cmfModel.addDatatype(this.toCmfDatatype());
      }
      else {
        cmfModel.addClassType(this.toCmfClassType());
      }
    }

    if (addDependencies) {
      // TODO: Add base type to CMF model
    }

    return cmfModel.qnToComponent(this.qname);
  }

  @Override
  public org.mitre.niem.cmf.Component toCmf() throws CMFException {
    if (this.isCmfDatatype()) {
      return this.toCmfDatatype();
    }
    return this.toCmfClassType();
  }

  /**
   * Converts this type to a CMF class object if this is a complex type.
   */
  public ClassType toCmfClassType() throws CMFException {
    if (this.isCmfDatatype()) {
      return null;
    }

    ClassType classType = new ClassType(this.getNamespace().toCmf(), this.name);
    // TODO: Support class type definition languages
    classType.addDocumentation(this.definition, "en-US");

    if (this.base != null) {
      classType.setSubclass(this.base.toCmfClassType());
    }

    // TODO: Set CMF class type is external
    // classType.setIsExternal();

    // TODO: Add CMF class type has property info
    // classType.addHasProperty(null);

    return classType;
  }

  /**
   * Converts this type to a CMF data type if this is a simple type.
   */
  public Datatype toCmfDatatype() throws CMFException {

    if (!this.isCmfDatatype()) {
      return null;
    }

    org.mitre.niem.cmf.Namespace cmfNamespace = this.getNamespace().toCmf();

    switch (this.pattern) {

      case simple_list:
        ListType listType = new ListType(cmfNamespace, this.name);
        // TODO: Support list datatype definition languages
        listType.addDocumentation(this.definition, "en-US");
        listType.setItemType(this.base.toCmfDatatype());
        return listType;

      case simple_union:
        Union union = new Union(cmfNamespace, this.name);
        // TODO: Support union datatype definition languages
        union.addDocumentation(this.definition, "en-US");
        // TODO: Add CMF data type union types
        return union;

      case simple_value:
        Restriction restriction = new Restriction(cmfNamespace, this.name);
        // TODO: Support restriction datatype definition languages
        restriction.addDocumentation(this.definition, "en-US");

        // TODO: Add CMF data type facets

        // XML Schema datatypes (e.g., xs:string) should not have a RestrictionOf property
        if (this.base != null || !this.prefix.equals("xs")) {
          restriction.setBase(this.getBase().toCmfDatatype());
        }

        // Add facets
        Set<Facet> facets = this.getFacets();
        for (Facet facet : facets) {
          restriction.addFacet(facet.toCmf());
        }

        return restriction;

      default:
        break;

    }

    return null;

  }

}
