package gov.niem.tools.api.db.base;

import gov.niem.tools.api.db.model.Model;
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
 * Adds reusable methods for entities that belong to a model: Version, Namespace, Property, etc.
 */
@MappedSuperclass
@Data
@SuperBuilder
@NoArgsConstructor
@EqualsAndHashCode(callSuper = false)
public abstract class BaseModelEntity extends BaseStewardEntity {

  @JsonIgnore
  public abstract Model getModel();

  /**
   * Gets the key of the model in which this entity belongs.
   */
  // @JacksonXmlProperty(localName = "ModelKeyID")
  // @Schema(
  //     example = "niem",
  //    description = "A human-readable and url-friendly steward identifier generated from the steward shortName field."
  // )
  @JsonIgnore
  public String getModelKey() {
    if (this.getModel() == null) {
      return null;
    }
    return this.getModel().getModelKey();
  }

  @JsonProperty("model")
  public Map<String, String> getModelSummary() {
    return this.getModel().toSummary();
  }

  // @JacksonXmlProperty(localName = "ModelRouteID")
  // @Schema(
  //   example = "https://tools.niem.gov/api/v2/stewards/niem",
  //   description = "An endpoint to get information about a steward."
  // )
  // public String getModelRoute() {
  //   if (this.getModel() == null) {
  //     return null;
  //   }
  //   return this.getModel().getRoute();
  // }

  // @JacksonXmlProperty(localName = "ModelShortName")
  // @Schema(
  //     example = "NIEM",
  //     description = "A short name or acronym used to identify a steward. This could be the name of an organization or unit, a program name, or other kind of authoritative source."
  // )
  // public String getModelShortName() {
  //   if (this.getModel() == null) {
  //     return null;
  //   }
  //   return this.getModel().getShortName();
  // }

  /**
   * Gets the Steward which is responsible for managing this entity.
   */
  @JsonIgnore
  public Steward getSteward() {
    if (this.getModel() == null) {
      return null;
    }
    return this.getModel().getSteward();
  }

}
