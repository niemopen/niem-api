package gov.niem.tools.api.db.base;

import gov.niem.tools.api.db.steward.Steward;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.persistence.MappedSuperclass;
import java.util.Map;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;

/**
 * Adds reusable methods for entities that belong to a steward: Model, Version, Namespace, etc.
 */
@MappedSuperclass
@Data
@SuperBuilder
@NoArgsConstructor
@EqualsAndHashCode(callSuper = false)
public abstract class BaseStewardEntity extends BaseEntity {

  public abstract Steward getSteward();

  /**
   * A human-readable and url-friendly steward identifier generated from the steward short name.
   */
  @JsonIgnore
  public String getStewardKey() {
    if (this.getSteward() == null) {
      return null;
    }
    return this.getSteward().getStewardKey();
  }

  /**
   * Gets a map of key fields with string values about a steward.
   */
  @JsonProperty("steward")
  public Map<String, String> getStewardSummary() {
    return this.getSteward().toSummary();
  }

}
