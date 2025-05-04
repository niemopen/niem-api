package gov.niem.tools.api.db.base;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;
import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlProperty;
import jakarta.persistence.EntityListeners;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.MappedSuperclass;
import java.io.Serializable;
import java.time.LocalDateTime;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;
import org.hibernate.envers.Audited;
import org.springframework.data.annotation.CreatedBy;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedBy;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

/**
 * A parent class for all kinds of entities used to represent the structure of a
 * NIEM model.
 */
@MappedSuperclass
@Data
@Audited
@SuperBuilder
@NoArgsConstructor
@AllArgsConstructor
@EntityListeners(AuditingEntityListener.class)
@JsonPropertyOrder({ "idLabel", "className" })
public abstract class BaseEntity implements Serializable {

  // TODO: enum Format
  // public enum Format {
  //   api,
  //   cmf
  // }

  /**
   * A unique, auto-generated identifier for an entity.
   */
  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  @JsonIgnore
  private Long id;

  /**
   * A user which created an entity.
   */
  @CreatedBy
  @JsonIgnore
  private String createdBy;

  /**
   * A date on which an entity was created.
   */
  @CreatedDate
  @JsonIgnore
  private LocalDateTime createdDate;

  /**
   * A user which last updated an entity.
   */
  @LastModifiedBy
  @JsonIgnore
  private String lastModifiedBy;

  /**
   * A date on which an entity was last updated.
   */
  @LastModifiedDate
  @JsonIgnore
  private LocalDateTime lastModifiedDate;

  /**
   * A human-readable and url-friendly unique identifier for an entity.
   */
  @JsonProperty("@id")
  @JacksonXmlProperty(localName = "EntityID")
  public abstract String getIdLabel();

  /**
   * An identifier, unique within its immediate scope.
   * Examples include a prefix (uniquely identifying a namespace) or a qualified
   * name (uniquely identifying a property or a type) within a version of a model.
   */
  @JsonProperty("localID")
  @JacksonXmlProperty(localName = "EntityLocalID")
  public abstract String getIdLocalLabel();

  /**
   * A kind of NIEM entity, such as a Namespace or a Property.
   */
  @JsonProperty("@type")
  @JacksonXmlProperty(localName = "EntityCategoryCode")
  public String getClassName() {
    return this.getClass().getSimpleName();
  }

  /**
   * An endpoint to get information about an entity.
   */
  @JacksonXmlProperty(localName = "EntityRouteURI")
  public abstract String getRoute();

  /**
   * A descriptive label or title used to identify an entity.
   */
  @JacksonXmlProperty(localName = "EntityTitleText")
  public abstract String getTitle();

  /**
   * An immediate entity or scope to which this entity belongs.
   * Examples include a steward (parent) for a model or a namespace (parent) for
   * a property or type.
   */
  @JsonIgnore
  public abstract BaseEntity getParentEntity();

  // TODO: getClassLabel()
  // private String getClassLabel(Boolean includeClassLabel) {
  //   if (includeClassLabel) {
  //     return this.getClassName() + " ";
  //   }
  //   return "";
  // }

  // TODO: getLabelWithKeys()
  // public String getLabelWithKeys(Boolean includeClassLabel) {
  //   return getClassLabel(includeClassLabel) + this.identifier();
  // }

  // TODO: getLabelRelative()
  // public String getLabelRelative(Boolean includeClassLabel) {
  //   return getClassLabel(includeClassLabel) + this.getRelativeId();
  // }

  // TODO: getLabelWithNames()
  // public String getLabelWithNames(Boolean includeClassLabel) {
  //   return getClassLabel(includeClassLabel) + this.identifier();
  // }

  // TODO: optional prev
  // @JsonIgnore
  // public Optional<Version> getPreviousVersion() {
  // return Optional.of(this.previousVersion);
  // }

  // TODO: optional next
  // @JsonIgnore
  // public Optional<Version> getNextVersion() {
  // return Optional.of(this.nextVersion);
  // }

  // TODO: compare(a, b) - returns list of fieldName, oldValue, newValue for
  // modified fields

  // TODO: toCmf()
  // public abstract Object toCmf() throws CMFException;

  // TODO: toCmfList()
  // public static List<Object> toCmfList(List<? extends BaseEntity> entities) throws CMFException {
  //   List<Object> cmfObjects = new ArrayList<Object>();
  //   for (BaseEntity entity : entities) {
  //     cmfObjects.add(entity.toCmf());
  //   }
  //   return cmfObjects;
  // }

}
