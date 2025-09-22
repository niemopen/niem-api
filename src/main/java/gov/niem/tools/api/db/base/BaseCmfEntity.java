package gov.niem.tools.api.db.base;

import gov.niem.tools.api.db.exceptions.EntityNotUniqueException;
import gov.niem.tools.api.validation.Test;

import org.mitre.niem.cmf.CMFException;
import org.mitre.niem.cmf.CMFObject;
import org.mitre.niem.cmf.Model;

/**
 * Interface for a model object that can be represented as a CMF object.
 */
public interface BaseCmfEntity<T> {

  public T toCmf() throws CMFException;

  /**
   * Add self to the given CMF model if not already there.
   *
   * @param cmfModel The CMF model.
   *
   * @param addDependencies True to also add dependencies if not already in the CMF model
   *     (namespace, type, group); false (default) to just add this property.
   *
   * @param addModelReason Indicates the reason why a component is being added to the
   *     model, either directly due to a task like migration or transformation, or indirectly
   *     as a required dependency.
   *
   * @param test If present, log info to given test object.
   */
  public CMFObject addToCmfModel(Model cmfModel, boolean addDependencies, AddModelReason
      addModelReason, Test test) throws CMFException, EntityNotUniqueException;

}
