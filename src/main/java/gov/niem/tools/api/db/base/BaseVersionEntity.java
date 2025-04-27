package gov.niem.tools.api.db.base;

import gov.niem.tools.api.db.model.Model;
import gov.niem.tools.api.db.version.Version;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlProperty;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.persistence.FetchType;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.MappedSuperclass;
import java.util.Map;
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

/**
 * Adds reusable methods for entities that belong to a version: Namespace, Property, etc.
 *
 * @param <T> A class for a kind of entity that that supports versioning and migration rules to
 *     create previous and next links between releases, such as Namespace, Property, Type, or Facet.
 */
@MappedSuperclass
@Audited
@Data
@SuperBuilder
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(callSuper = false)
public abstract class BaseVersionEntity<T extends BaseVersionEntity<T>> extends BaseModelEntity {

  @JsonIgnore
  public abstract Version getVersion();


  /**
   * Corresponding entity mapped from the previous version.
   */
  @JsonIgnore
  @ToString.Exclude
  @EqualsAndHashCode.Exclude
  @ManyToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "prev_id", referencedColumnName = "id")
  public T prev;

  /**
   * Corresponding entity mapped from the next version.
   */
  @JsonIgnore
  @ToString.Exclude
  @EqualsAndHashCode.Exclude
  @ManyToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "next_id", referencedColumnName = "id")
  public T next;

  /**
   * Corresponding entity from the previous version of the model.
   * The Hibernate proxy (from lazy loading) is initialized.
   */
  @SuppressWarnings("unchecked")
  public T getPrev() {
    T prev = this.prev;
    if (prev instanceof HibernateProxy) {
      prev = (T) Hibernate.unproxy(prev);
    }
    return prev;
  }

  /**
   * Corresponding entity from the next version of the model.
   * The Hibernate proxy (from lazy loading) is initialized.
   */
  @SuppressWarnings("unchecked")
  public T getNext() {
    T next = this.next;
    if (next instanceof HibernateProxy) {
      next = (T) Hibernate.unproxy(next);
    }
    return next;
  }

  /**
   * For a subset that reuses content from another model, this is the link to the
   * original entity. The value is null if this entity is actually defined by its
   * version rather than reused from another model as part of a subset.
   *
   * <p>Example 1: NIEM model 5.2 is the original source of property "nc:Person".
   * In this case, this property is itself the original and has no other original
   * source to point to.  The value of {@literal original} is null.
   *
   * <p>Example 2: Acme Crash Driver 1.0 reuses property nc:Person from NIEM model 5.2
   * and is not its original source.  The value of {@literal original} is a reference
   * to the NIEM model 5.2 nc:Person property.
   *
   * <p>Although most information about an entity will be duplicated between the original
   * and its usages via subsets, tracking the usages of each entity independently
   * supports subset-approved customizations, like adjusted cardinality, custom
   * namespace prefixes, property aliases, inlined substitutions, and flattened types.
   */
  @JsonIgnore
  @ToString.Exclude
  @EqualsAndHashCode.Exclude
  @ManyToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "original_id", referencedColumnName = "id")
  T original;

  /**
   * For a component in a subset, gets the component from the original source.
   */
  @SuppressWarnings("unchecked")
  public T getOriginal() {
    T original = this.original;
    if (original instanceof HibernateProxy) {
      original = (T) Hibernate.unproxy(original);
    }
    return original;
  }

  // TODO: inverse of original
  // @JsonIgnore
  // @ToString.Exclude
  // @EqualsAndHashCode.Exclude
  // @Builder.Default
  // protected Set<T> reuses = new HashSet<>();

  /**
   * True if an entity is defined by its version and can be modified freely; false
   * if an entity is being referenced and reused from another model and version
   * and is restricted in the changes that can be made (subset options).
   */
  @JsonProperty("isOriginal")
  @JacksonXmlProperty(localName = "api:EntityOriginalIndicator")
  @Schema(example = "false")
  public boolean isOriginal() {
    if (this.original == null) {
      return true;
    }
    return false;
  }

  // // TODO: Original
  // @JacksonXmlProperty(localName = "api:EntityOriginalFullID")
  // @Schema(example = "niem/model/5.2/nc")
  // public String getOriginalFullIdentifier() {
  //   if (this.isOriginal()) {
  //     return null;
  //   }
  //   return this.original.getFullIdentifier();
  // }

  // @JacksonXmlProperty(localName = "api:EntityOriginalVersionFullID")
  // @Schema(example = "niem/model/5.2")
  // public String getOriginalVersionIdentifier() {
  //   if (this.isOriginal()) {
  //     return null;
  //   }
  //   return this.original.getVersion().getFullIdentifier();
  // }

  // @JacksonXmlProperty(localName = "api:EntityOriginalVersionTitle")
  // @Schema(example = "NIEM Model 5.2")
  // public String getOriginalVersionTitle() {
  //   if (this.isOriginal()) {
  //     return null;
  //   }
  //   return this.original.getVersion().getTitle();
  // }

  /**
   * True if an entity is deprecated; false or null otherwise.
   */
  @Builder.Default
  @JacksonXmlProperty(localName = "api:EntityDeprecatedIndicator")
  @JsonProperty("isDeprecated")
  @Schema(example = "false")
  private boolean isDeprecated = false;

  /**
   * Gets the version database id.
   */
  @JsonIgnore
  public Long getVersionId() {
    if (this.getVersion() == null) {
      return null;
    }
    return this.getVersion().getId();
  }

  @JsonProperty("version")
  public Map<String, String> getVersionSummary() {
    return this.getVersion().toSummary();
  }

  /**
   * A number which identifies a version within a model.
   */
  // @JacksonXmlProperty(localName = "api:VersionNumberID")
  // @Schema(example = "1.1")
  @JsonIgnore
  public String getVersionNumber() {
    if (this.getVersion() == null) {
      return null;
    }
    return this.getVersion().getVersionNumber();
  }

  /**
   * Gets the NIEM Version object that is compatible with this object.
   */
  @JsonIgnore
  public Version getNiemVersion() {
    if (this.getVersion() == null) {
      return null;
    }
    return this.getVersion().getNiemVersion();
  }

  /**
   * Gets the NIEM Version number (string) that is compatible with this object.
   */
  // @JacksonXmlProperty(localName = "api:VersionBaseNIEMVersionNumberID")
  // @Schema(example = "5.2")
  @JsonIgnore
  public String getNiemVersionNumber() {
    if (this.getNiemVersion() == null) {
      return null;
    }
    return this.getNiemVersion().getVersionNumber();
  }

  /**
   * Gets the Model that defines this object.
   */
  @JsonIgnore
  public Model getModel() {
    if (this.getVersion() == null) {
      return null;
    }
    return this.getVersion().getModel();
  }

}
