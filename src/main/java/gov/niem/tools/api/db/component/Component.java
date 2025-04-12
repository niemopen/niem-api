package gov.niem.tools.api.db.component;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlProperty;

import gov.niem.tools.api.db.base.BaseNamespaceEntity;
import gov.niem.tools.api.db.namespace.Namespace;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;

import java.util.HashMap;
import java.util.Map;

import org.apache.commons.lang3.StringUtils;
import org.hibernate.Hibernate;
import org.hibernate.annotations.Formula;
import org.hibernate.envers.Audited;
import org.hibernate.envers.NotAudited;
import org.hibernate.proxy.HibernateProxy;
import org.hibernate.search.engine.backend.types.Projectable;
import org.hibernate.search.engine.backend.types.Sortable;
import org.hibernate.search.mapper.pojo.mapping.definition.annotation.FullTextField;
import org.hibernate.search.mapper.pojo.mapping.definition.annotation.Indexed;
import org.hibernate.search.mapper.pojo.mapping.definition.annotation.IndexedEmbedded;
import org.hibernate.search.mapper.pojo.mapping.definition.annotation.KeywordField;
import org.mitre.niem.cmf.CMFException;

import jakarta.persistence.Column;
import jakarta.persistence.Embedded;
import jakarta.persistence.FetchType;
import jakarta.persistence.ForeignKey;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.MappedSuperclass;

/**
 * A parent class for Property and Type.
 */
@MappedSuperclass
@Audited
@Data
@SuperBuilder
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(callSuper = false)
@Indexed
public abstract class Component<T extends BaseNamespaceEntity<T>> extends BaseNamespaceEntity<T> {

  /**
   * A namespace prefix for the property or type.
   */
  @NotAudited
  @JacksonXmlProperty(localName = "api:ComponentNamespacePrefixID")
  @Schema(example = "nc")
  @Formula("(SELECT namespace.prefix FROM namespace WHERE namespace.id = namespace_id)")
  protected String prefix;

  @NotAudited
  @Formula("(SELECT namespace.prefix||':'||name FROM namespace WHERE namespace.id = namespace_id)")
  public String qname;

  protected abstract Object getCategory();

  /**
   * A name of the property or type.
   */
  @JacksonXmlProperty(localName = "api:ComponentName")
  @Schema(example = "PersonGivenName")
  // @FullTextField(analyzer = "camel", searchAnalyzer = "freeText")
  @FullTextField(analyzer = "camel")
  @FullTextField(name = "name_substring", analyzer = "substring")
  @KeywordField(name = "name_keyword", sortable = Sortable.YES, projectable = Projectable.YES)
  protected String name;

  /**
   * A definition describing a property or type.
   */
  @JacksonXmlProperty(localName = "api:ComponentDefinitionText")
  @Schema(example = "A first name of a person.")
  @Column(columnDefinition = "text")
  @FullTextField
  protected String definition;

  /**
   * Namespace that defines this component.
   */
  @JsonIgnore
  @ManyToOne(optional = false, fetch = FetchType.LAZY)
  @JoinColumn(foreignKey = @ForeignKey(name = "namespace_fkey"))
  @Embedded
  @IndexedEmbedded
  private Namespace namespace;

  /**
   * Namespace that defines this component.
   */
  @JsonIgnore
  public Namespace getNamespace() {
    Namespace namespace = this.namespace;
    if (namespace instanceof HibernateProxy) {
      namespace = Hibernate.unproxy(namespace, Namespace.class);
    }
    return namespace;
  }

  @JsonIgnore
  @Override
  public Namespace getParentEntity() {
    return this.getNamespace();
  }

  public String getTitle() {
    return String.format("%s %s", this.getVersion().getTitle(), this.getQname());
  }

  /**
   * A set of terms from the name of the property or type, broken apart by camel casing.
   */
  @JacksonXmlProperty(localName = "api:ComponentNameTermText")
  @Schema(example = "['Person', 'Given', 'Name']")
  public String[] getTerms() {
    return StringUtils.splitByCharacterTypeCamelCase(this.name);
  }

  @JsonIgnore
  public Map<String, String> toSummary() {
    Map<String, String> map = new HashMap<>();
    map.put("prefix", this.getPrefix());
    map.put("name", this.getName());
    map.put("qname", this.getQname());
    map.put("definition", this.getDefinition());
    map.put("route", this.getRoute());
    map.put("category", this.getCategory().toString());
    return map;
  }

  /**
   * Return the first part (namespace prefix) of a qualified name string
   * with a ":" delimiter. For example, return "nc" given "nc:PersonBirthDate".
   */
  public static String getPrefix(String qname) {
    if (qname == null || !qname.contains(":")) {
      return null;
    }
    return qname.split(":")[0];
  }

  /**
   * Return the second part (namespace prefix) of a qualified name string
   * with a ":" delimiter. For example, return "PersonBirthDate" given
   * "nc:PersonBirthDate".
   */
  public static String getName(String qname) {
    if (qname == null || !qname.contains(":")) {
      return null;
    }
    return qname.split(":")[1];
  }

  public org.mitre.niem.cmf.Component toCmf() throws CMFException {
    // Override
    return null;
  }

}
