package gov.niem.tools.api.db;

import gov.niem.tools.api.db.facet.FacetService;
import gov.niem.tools.api.db.model.ModelService;
import gov.niem.tools.api.db.namespace.NamespaceService;
import gov.niem.tools.api.db.property.PropertyService;
import gov.niem.tools.api.db.steward.StewardService;
import gov.niem.tools.api.db.subproperty.SubpropertyService;
import gov.niem.tools.api.db.type.TypeService;
import gov.niem.tools.api.db.version.VersionService;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

/**
 * Provides all database services as a convenience.
 */
@Component
public class ServiceHub {

  @Autowired
  public StewardService stewards;

  @Autowired
  public ModelService models;

  // @Autowired
  // public ModelStewardRepository modelStewardRepo;

  @Autowired
  public VersionService versions;

  @Autowired
  public NamespaceService namespaces;

  @Autowired
  public TypeService types;

  @Autowired
  public PropertyService properties;

  @Autowired
  public SubpropertyService subproperties;

  @Autowired
  public FacetService facets;

}
