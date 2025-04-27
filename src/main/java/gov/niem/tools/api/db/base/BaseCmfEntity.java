package gov.niem.tools.api.db.base;

import org.mitre.niem.cmf.CMFException;
import org.mitre.niem.cmf.Model;

/**
 * Superclass for a CMF object.
 */
public interface BaseCmfEntity<T> {

  public T toCmf() throws CMFException;

  public void addToCmfModel(Model cmfModel) throws CMFException;

}
