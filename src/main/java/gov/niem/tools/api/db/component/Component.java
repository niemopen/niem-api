package gov.niem.tools.api.db.component;

import gov.niem.tools.api.db.base.BaseNamespaceEntity;
import gov.niem.tools.api.db.namespace.Namespace;

import org.mitre.niem.cmf.CMFException;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlProperty;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.persistence.Column;
import jakarta.persistence.Embedded;
import jakarta.persistence.FetchType;
import jakarta.persistence.ForeignKey;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.MappedSuperclass;
import java.util.HashMap;
import java.util.Map;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;
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

/**
 * A parent class for Property and Type.
 *
 * @param <T> - A subclass of Component, i.e., Property or Type.
 */
@MappedSuperclass
@Audited
@Data
@SuperBuilder
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(callSuper = false)
@JsonIgnoreProperties({"hibernateLazyInitializer", "handler"})
@Indexed
public abstract class Component<T extends BaseNamespaceEntity<T>>
    extends BaseNamespaceEntity<T> implements Comparable<Component<T>> {

  /**
   * A namespace prefix for the property or type.
   */
  @NotAudited
  @JacksonXmlProperty(localName = "ComponentNamespacePrefixID")
  @Schema(example = "nc")
  @Formula("(SELECT namespace.prefix FROM namespace WHERE namespace.id = namespace_id)")
  protected String prefix;

  @NotAudited
  @Formula("(SELECT namespace.prefix||':'||name FROM namespace WHERE namespace.id = namespace_id)")
  public String qname;

  /**
   * A ranking used to support sorting component namespaces by category, with
   * Core and Core Supplements sorting first, followed by domains, etc.
   */
  @NotAudited
  @Formula("(SELECT CASE "
      + "WHEN namespace.category = 'core' THEN 1 "
      + "WHEN namespace.category = 'core_supplement' THEN 1 "
      + "WHEN namespace.category = 'domain' THEN 2 "
      + "WHEN namespace.category = 'domain_supplement' THEN 2 "
      + "WHEN namespace.category = 'code' THEN 3 "
      + "WHEN namespace.category = 'adapter' THEN 4 "
      + "WHEN namespace.category = 'extension' THEN 5 "
      + "WHEN namespace.category = 'exchange' THEN 5 "
      + "ELSE 99 END "
      + "FROM namespace WHERE namespace.id = namespace_id)")
  public int namespaceRank;

  protected abstract Object getCategory();

  /**
   * A name of the property or type.
   */
  @JacksonXmlProperty(localName = "ComponentName")
  @Schema(example = "PersonGivenName")
  @FullTextField(name = "name_tokens", analyzer = "camel")
  @FullTextField(name = "name_substring",  analyzer = "nGram")
  // @KeywordField(name = "name_substring", normalizer = "case")
  @KeywordField(name = "name_sort", normalizer = "case",
      sortable = Sortable.YES, projectable = Projectable.YES)
  protected String name;

  /**
   * A definition describing a property or type.
   */
  @JacksonXmlProperty(localName = "ComponentDefinitionText")
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
  @JacksonXmlProperty(localName = "ComponentNameTermText")
  @Schema(example = "['Person', 'Given', 'Name']")
  public String[] getTerms() {
    return StringUtils.splitByCharacterTypeCamelCase(this.name);
  }

  /**
   * Returns key fields about a component.
   */
  @JsonIgnore
  public Map<String, String> toSummary() {
    Map<String, String> map = new HashMap<>();
    map.put("prefix", this.getPrefix());
    map.put("name", this.getName());
    map.put("qname", this.getQname());
    map.put("definition", this.getDefinition());
    map.put("category", this.getCategory().toString());
    map.put("@id", this.getIdLabel());
    map.put("route", this.getRoute());
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

  /**
   * Custom sorting function for components.
   * Returns components sorted by namespace rank, prefix, and name.
   */
  @Override
  public int compareTo(Component<T> other) {
    if (this.namespaceRank < other.namespaceRank) {
      return -1;
    }
    if (this.namespaceRank > other.namespaceRank) {
      return 1;
    }
    return this.qname.compareTo(other.qname);
  }

}
