package gov.niem.tools.api.db.property;

import gov.niem.tools.api.core.config.Config;
import gov.niem.tools.api.db.base.AddModelReason;
import gov.niem.tools.api.db.base.BaseCmfEntity;
import gov.niem.tools.api.db.component.Component;
import gov.niem.tools.api.db.exceptions.EntityNotUniqueException;
import gov.niem.tools.api.db.subproperty.Subproperty;
import gov.niem.tools.api.db.type.Type;
import gov.niem.tools.api.validation.Test;

import org.mitre.niem.cmf.CMFException;
import org.mitre.niem.cmf.ClassType;
import org.mitre.niem.cmf.DataProperty;
import org.mitre.niem.cmf.Datatype;
import org.mitre.niem.cmf.ObjectProperty;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlProperty;
import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlRootElement;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.annotation.Nullable;
import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
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
import lombok.extern.log4j.Log4j2;
import org.hibernate.Hibernate;
import org.hibernate.envers.Audited;
import org.hibernate.proxy.HibernateProxy;
import org.hibernate.search.mapper.pojo.mapping.definition.annotation.AssociationInverseSide;
import org.hibernate.search.mapper.pojo.mapping.definition.annotation.FullTextField;
import org.hibernate.search.mapper.pojo.mapping.definition.annotation.GenericField;
import org.hibernate.search.mapper.pojo.mapping.definition.annotation.Indexed;
import org.hibernate.search.mapper.pojo.mapping.definition.annotation.IndexedEmbedded;
import org.hibernate.search.mapper.pojo.mapping.definition.annotation.ObjectPath;
import org.hibernate.search.mapper.pojo.mapping.definition.annotation.PropertyValue;

/**
 * A property represents a concept, idea, or thing.
 *
 * @see <a href = "https://niem.github.io/reference/concepts/property/">NIEM Property info</a>
 */
@Entity
@Audited
@Data
@SuperBuilder
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(callSuper = false)
@JacksonXmlRootElement(localName = "Property")
@JsonIgnoreProperties({"hibernateLazyInitializer", "handler"})
@Log4j2
@Schema(name = "Property")
@Table(
    uniqueConstraints = {
      @UniqueConstraint(
        name = "property_namespace_name_key", columnNames = { "namespace_id", "name" })
    },
    indexes = {
      @Index(name = "property_category_idx", columnList = "category"),
      @Index(name = "property_group_id_idx", columnList = "group_id"),
      @Index(name = "property_type_id_idx", columnList = "type_id"),
      @Index(name = "property_namespace_prefix_idx", columnList = "namespace_prefix"),
      @Index(name = "property_namespace_id_idx", columnList = "namespace_id"),
      @Index(name = "property_name_idx", columnList = "name")
    }
)
@Indexed
public class Property extends Component<Property>
    implements BaseCmfEntity<org.mitre.niem.cmf.Property> {

  /**
   * A type that describes the structure and value set of the property.
   */
  @JsonIgnore
  @Nullable
  @ManyToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "type_id", referencedColumnName = "id")
  @IndexedEmbedded(includeDepth = 1)
  @AssociationInverseSide(
      inversePath = @ObjectPath(@PropertyValue(propertyName = "dataProperties")))
  private Type type;

  /**
   * A substitution group head, which may be replaced in message instances by this property.
   */
  @JsonIgnore
  @Nullable
  @ManyToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "group_id", referencedColumnName = "id")
  // @IndexedEmbedded(includeDepth = 1)
  // @IndexingDependency(reindexOnUpdate = ReindexOnUpdate.SHALLOW)
  private Property group;

  /**
   * A kind of property indicating whether is is a concrete element, abstract element,
   * or attribute.
   */
  public enum Category {
    /**
     * A typical element, which may be used in instances.
     */
    element,

    /**
     * An abstract element, which must be replaced by a regular element in instances.
     */
    abstract_element,

    /**
     * An attribute, which in XML does not exist independently and must be carried by an element.
     */
    attribute
  }

  /**
   * A kind of property.
   */
  @Builder.Default
  @Enumerated(EnumType.STRING)
  @JacksonXmlProperty(localName = "PropertyCategoryCode")
  @GenericField
  private Category category = Category.element;

  /**
   * An alternate way to categorize a property based on its type.  Identifies
   * whether it carries properties (an object)
   */
  public enum ContentStyle {
    /**
     * A property with a class type that can carry elements.
     */
    object,

    /**
     * A property with a datatype class or a datatype that carries a value
     * and may or may not carry attributes.
     */
    value,

    /**
     * A property that does not carry content (for abstract properties).
     */
    none
  }

  /**
   * An alternate way to categorize a property based on its type.  Identifies
   * whether it carries properties (an object)
   */
  public ContentStyle getContentStyle() {
    if (this.isAbstract() || this.type == null) {
      return ContentStyle.none;
    }
    if (this.isAttribute()) {
      return ContentStyle.value;
    }
    if (this.getType().isSimpleContent()) {
      return ContentStyle.value;
    }
    return ContentStyle.object;
  }

  private String alias;

  @FullTextField
  private String keywords;

  @Column(columnDefinition = "text")
  private String exampleContent;

  @Column(columnDefinition = "text")
  private String usageInfo;

  @JsonIgnore
  @ToString.Exclude
  @EqualsAndHashCode.Exclude
  @Builder.Default
  @OneToMany(mappedBy = "group")
  private Set<Property> substitutions = new HashSet<>();

  @JsonIgnore
  @ToString.Exclude
  @EqualsAndHashCode.Exclude
  @Builder.Default
  @OneToMany(mappedBy = "property", cascade = CascadeType.ALL)
  private Set<Subproperty> subproperties = new HashSet<>();

  /**
   * Makes sure a potential Hibernate proxy is initialized.
   */
  public Property getGroup() {
    if (this.group instanceof HibernateProxy) {
      this.group = Hibernate.unproxy(this.group, Property.class);
    }
    return this.group;
  }

  /**
   * Makes sure a potential Hibernate proxy is initialized.
   */
  public Type getType() {
    Type type = this.type;
    if (type instanceof HibernateProxy) {
      type = Hibernate.unproxy(type, Type.class);
    }
    return type;
  }

  // TODO: referenceTarget
  // /**
  //  * Legacy NIEM support (pre NIEM 3.0) for reference target appinfo on
  //  * reference properties (properties with type 's:ReferenceType').
  //  */
  // @OneToOne @JoinColumn(name = "reference_target_id", referencedColumnName = "id")
  // private Type referenceTarget;

  /**
   * True if the property is a regular or abstract element; false if the property is an attribute.
   */
  @JsonProperty("isElement")
  public boolean isElement() {
    return this.category != Category.attribute;
  }

  /**
   * True if the property is an attribute; false if the property is a regular or abstract element.
   */
  @JsonProperty("isAttribute")
  public boolean isAttribute() {
    return this.category == Category.attribute;
  }

  /**
   * True if the property is an element and is abstract; false otherwise.
   */
  @JsonProperty("isAbstract")
  public boolean isAbstract() {
    return this.category == Category.abstract_element;
  }

  // /**
  //  * A namespace prefix for a property's type.
  //  */
  // @JacksonXmlProperty(localName = "PropertyTypeNamespacePrefixID")
  // @Schema(example = "j")
  // public String getTypePrefix() {
  //   return this.type == null ? null : this.type.getPrefix();
  // }

  // /**
  //  * A name for a property's type.
  //  */
  // @JacksonXmlProperty(localName = "PropertyTypeName")
  // @Schema(example = "PersonEyeColorCodeType")
  // public String getTypeName() {
  //   return this.type == null ? null : this.type.getName();
  // }

  // /**
  //  * A qualified name, starting with a namespace prefix, for a property's type.
  //  */
  // @JacksonXmlProperty(localName = "PropertyTypeQualifiedName")
  // @Schema(example = "j:PersonEyeColorCodeType")
  // public String getTypeQname() {
  //   return this.type == null ? null : this.type.getQname();
  // }

  // /**
  //  * A kind of a property's type.
  //  */
  // @JacksonXmlProperty(localName = "PropertyTypeName")
  // @Schema(example = "complex_value")
  // public Type.Category getTypeCategory() {
  //   return this.type == null ? null : this.type.getCategory();
  // }

  // /**
  //  * A definition for a property's type.
  //  */
  // @JacksonXmlProperty(localName = "PropertyTypeDefinitionText")
  // @Schema(example = "A data type for a code set identifying an eye color of a person.")
  // public String getTypeDefinition() {
  //   return this.type == null ? null : this.type.getName();
  // }

  @JsonProperty("type")
  public Map<String, String> getTypeSummary() {
    return this.type == null ? null : this.type.toSummary();
  }

  @JsonProperty("group")
  public Map<String, String> getGroupSummary() {
    return this.group == null ? null : this.group.toSummary();
  }

  // TODO: Remove group basics 2
  // public Map<String, String> getGroupBasics() {
  //   Map<String, String> map = new HashMap<>();
  //   if (this.group != null) {
  //     map.put("prefix", this.group.getPrefix());
  //     map.put("name", this.group.getName());
  //     map.put("qname", this.group.getQname());
  //     map.put("definition", this.group.getDefinition());
  //     map.put("route", this.group.getRoute());
  //     map.put("category", this.group.category.toString());
  //   }
  //   return map;
  // }

  // /**
  //  * A namespace prefix for a property's substitution group head property.
  //  */
  // @JacksonXmlProperty(localName = "PropertyGroupNamespacePrefixID")
  // @Schema(example = "nc")
  // public String getGroupPrefix() {
  //   return this.group == null ? null : this.group.getPrefix();
  // }

  // /**
  //  * A name for a property's group head property.
  //  */
  // @JacksonXmlProperty(localName = "PropertyGroupName")
  // @Schema(example = "PersonEyeColorAbstract")
  // public String getGroupName() {
  //   return this.group == null ? null : this.group.getName();
  // }

  // /**
  //  * A qualified name, starting with a namespace prefix, for a property's
  //  * substitution group property.
  //  */
  // @JacksonXmlProperty(localName = "PropertyGroupQualifiedName")
  // @Schema(example = "nc:PersonEyeColorAbstract")
  // public String getGroupQname() {
  //   return this.group == null ? null : this.group.getQname();
  // }

  // /**
  //  * A kind of a property's substitution group property.
  //  */
  // @JacksonXmlProperty(localName = "PropertyGroupName")
  // @Schema(example = "complex_value")
  // public Category getGroupCategory() {
  //   return this.type == null ? null : this.group.getCategory();
  // }

  // /**
  //  * A definition for a property's substitution group property.
  //  */
  // @JacksonXmlProperty(localName = "PropertyGroupDefinitionText")
  // @Schema(example = "A data concept for a color of the eyes of a person.")
  // public String getGroupDefinition() {
  //   return this.type == null ? null : this.type.getName();
  // }

  @Override
  @Schema(
      example = Config.BASE_URL + "/stewards/niem/models/crash-driver/version/1.1/properties/nc:PersonGivenName",
      description = "An endpoint to get information about a property.")
  public String getRoute() {
    String versionRoute = this.getVersion().getRoute();
    return String.format("%s/properties/%s", versionRoute, this.getQname());
  }

  @Override
  @Schema(
      example = "Property",
      description = "A kind of NIEM entity, such as a Namespace or a Property.")
  public String getClassName() {
    return super.getClassName();
  }

  @Override
  @Schema(
      example = "niem/crash-driver/1.1/nc:PersonGivenName",
      description = "A unique identifier.  For a property, this is combines the stewardKey, modelKey, versionNumber, prefix, and name fields.")
  public String getIdLabel() {
    return this.getVersion().getIdLabel() + "/" + this.getQname();
  }

  @Override
  @Schema(
      example = "nc:PersonGivenName",
      description = "An identifier, unique within its immediate scope.  For a property, this is the same as the qname field (unique within its version).")
  public String getIdLocalLabel() {
    return this.getQname();
  }

  @Override
  @Schema(
      example = "NIEM Crash Driver 1.1: nc:PersonGivenName",
      description = "A steward short name, model short name, version number, and qualified property name.")
  public String getTitle() {
    return super.getTitle();
  }

  /**
   * True if this property can be considered a CMF data property; false if it
   * is a CMF object property.
   */
  @JsonIgnore
  public boolean isCmfDataProperty() {
    if (this.isAbstract() || this.type == null) {
      return false;
    }
    if (this.isAttribute()) {
      return true;
    }

    Type type = this.getType();
    if (type.isSimple()) {
      return true;
    }

    return false;
  }

  /**
   * Adds this property as a data property to the given CMF model if not already present.
   *
   * @param cmfModel The CMF model
   * @param addDependencies True to also add property dependencies if not already in the CMF model
   *     (namespace, type, group); false to just add this property.
   */
  @Override
  public org.mitre.niem.cmf.Component addToCmfModel(org.mitre.niem.cmf.Model cmfModel,
      boolean addDependencies, AddModelReason addModelReason, Test test)
      throws CMFException, EntityNotUniqueException {

    if (this.getNamespace() != null) {
      // Add namespace dependency to the CMF model
      this.getNamespace().addToCmfModel(cmfModel, addDependencies, AddModelReason.DEPENDENCY, test);
    }

    if (this.isCmfDataProperty()) {
      this.addToCmfModelAsDataProperty(cmfModel);
    }
    else {
      this.addToCmfModelAsObjectProperty(cmfModel);
    }

    // Log result to test object if given
    if (test != null) {
      if (addModelReason == AddModelReason.MIGRATION) {
        // TODO: Log migration result to test object
      }
      else if (addModelReason == AddModelReason.DEPENDENCY) {
        // TODO: Log dependency result to test object
      }
    }

    // Return if not adding dependencies
    if (!addDependencies) {
      return this.toCmf();
    }

    // Add type dependency to CMF model
    if (this.type != null) {
      this.type.addToCmfModel(cmfModel, addDependencies, addModelReason, test);
    }

    // Add substitution group dependency to the CMF model
    if (this.group != null) {
      this.group.addToCmfModel(cmfModel, addDependencies, AddModelReason.DEPENDENCY, test);
    }

    return this.toCmf();

  }

  private void addToCmfModelAsDataProperty(org.mitre.niem.cmf.Model cmfModel) throws
      CMFException, EntityNotUniqueException {

    if (!this.isCmfDataProperty()) {
      return;
    }

    // Skip if property is already in the model
    DataProperty dataProperty = cmfModel.qnToDataProperty(this.qname);
    if (dataProperty != null) {
      throw new EntityNotUniqueException("property", this.qname);
    }

    // Add property to the model
    log.debug(String.format("--Adding data property %s to CMF", this.qname));
    dataProperty = this.toCmfDataProperty();
    cmfModel.addDataProperty(dataProperty);

  }

  private void addToCmfModelAsObjectProperty(org.mitre.niem.cmf.Model cmfModel) throws
      CMFException, EntityNotUniqueException {

    if (this.isCmfDataProperty()) {
      return;
    }

    // Skip if property is already in the model
    ObjectProperty objectProperty = cmfModel.qnToObjectProperty(this.qname);
    if (objectProperty != null) {
      throw new EntityNotUniqueException("property", this.qname);
    }

    // Add property to the model
    log.debug(String.format("--Adding object property %s to CMF", this.qname));
    objectProperty = this.toCmfObject();
    cmfModel.addObjectProperty(objectProperty);

  }

  @Override
  public org.mitre.niem.cmf.Property toCmf() throws CMFException {
    if (this.isCmfDataProperty()) {
      return this.toCmfDataProperty();
    }
    return this.toCmfObject();
  }

  /**
   * Converts a property to a CMF object property.
   */
  public ObjectProperty toCmfObject() throws CMFException {
    ObjectProperty cmfProperty = new ObjectProperty();

    cmfProperty.setNamespace(this.getNamespace().toCmf());
    cmfProperty.setName(this.name);
    // TODO: Support languages on property documentation
    cmfProperty.addDocumentation(this.definition, "en-US");
    cmfProperty.setIsAbstract(this.isAbstract());

    // Set type info
    if (this.type != null) {
      org.mitre.niem.cmf.Namespace typeNamespace = this.type.getNamespace().toCmf();
      ClassType classType = new ClassType(typeNamespace, this.type.getName());
      cmfProperty.setClassType(classType);
    }

    // Set substitution group info
    if (this.getGroup() != null) {
      org.mitre.niem.cmf.Property cmfGroup = new org.mitre.niem.cmf.Property();
      cmfGroup.setNamespace(this.getGroup().getNamespace().toCmf());
      cmfGroup.setName(this.getGroup().name);
      cmfProperty.setSubproperty(cmfGroup);
    }

    return cmfProperty;
  }

  /**
   * Converts a property to a CMF data property.
   */
  public DataProperty toCmfDataProperty() throws CMFException {
    DataProperty cmfProperty = new DataProperty();

    cmfProperty.setNamespace(this.getNamespace().toCmf());
    cmfProperty.setName(this.name);
    // TODO: Support languages on property documentation
    cmfProperty.addDocumentation(this.definition, "en-US");
    cmfProperty.setIsAbstract(this.isAbstract());
    cmfProperty.setIsAttribute(this.isAttribute());

    // Set type info
    if (this.type != null) {
      org.mitre.niem.cmf.Namespace typeNamespace = this.type.getNamespace().toCmf();
      if (this.type.isSimple()) {
        Datatype datatype = new Datatype(typeNamespace, this.type.getName());
        cmfProperty.setDatatype(datatype);
      }
    }

    return cmfProperty;
  }

}
