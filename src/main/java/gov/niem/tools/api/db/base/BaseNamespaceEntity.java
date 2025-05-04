package gov.niem.tools.api.db.base;

import gov.niem.tools.api.db.namespace.Namespace;
import gov.niem.tools.api.db.version.Version;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.persistence.MappedSuperclass;
import java.util.Map;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;
import org.hibernate.envers.Audited;

/**
 * Adds reusable methods for entities that belong to a namespace:
 * Property, Type, Facet, etc.
 *
 * @param <T> A class for a kind of entity that belongs to a namespace, such as a
 *     Property, Type, or Facet.
 */
@MappedSuperclass
@Audited
@Data
@SuperBuilder
@NoArgsConstructor
@EqualsAndHashCode(callSuper = false)
public abstract class BaseNamespaceEntity<T extends BaseVersionEntity<T>>
    extends BaseVersionEntity<T> {

  @JsonIgnore
  public abstract Namespace getNamespace();

  /**
   * Gets the namespace database id.
   */
  @JsonIgnore
  public Long getNamespaceId() {
    if (this.getNamespace() == null) {
      return null;
    }
    return this.getNamespace().getId();
  }

  /**
   * Gets key fields about a namespace.
   */
  @JsonProperty("namespace")
  public Map<String, String> getNamespaceSummary() {
    return this.getNamespace().toSummary();
  }

  // /**
  //  * A short, non-normative identifier for a namespace.
  //  */
  // @JacksonXmlProperty(localName = "NamespacePrefixID")
  // @Schema(example = "nc")
  // public String getPrefix() {
  //   if (this.getNamespace() == null) {
  //     return null;
  //   }
  //   return this.getNamespace().getPrefix();
  // }

  // /**
  //  * A normative identifier for a namespace.
  //  */
  // @JacksonXmlProperty(localName = "NamespaceURI")
  // @Schema(example = "http://release.niem.gov/niem/niem-core/5.0")
  // public String getNamespaceURI() {
  //   if (this.getNamespace() == null) {
  //     return null;
  //   }
  //   return this.getNamespace().getUri();
  // }

  // /**
  //  * A kind of namespace.
  //  */
  // @JacksonXmlProperty(localName = "NamespaceCategoryCode")
  // @Schema(example = "core")
  // public Category getNamespaceCategory() {
  //   if (this.getNamespace() == null) {
  //     return null;
  //   }
  //   return this.getNamespace().getCategory();
  // }

  // /**
  //  * A name of a namespace.
  //  */
  // @JacksonXmlProperty(localName = "NamespaceName")
  // @Schema(example = "NIEM Core")
  // public String getNamespaceName() {
  //   if (this.getNamespace() == null) {
  //     return null;
  //   }
  //   return this.getNamespace().getName();
  // }

  /**
   * Gets the version in which this namespace belongs.
   */
  @JsonIgnore
  public Version getVersion() {
    if (this.getNamespace() == null) {
      return null;
    }
    return this.getNamespace().getVersion();
  }

}
