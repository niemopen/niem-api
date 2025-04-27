package gov.niem.tools.api.db.type;

import gov.niem.tools.api.db.component.ComponentRepository;

/**
 * Repository interface for finding and managing types in the database.
 */
public interface TypeRepository extends ComponentRepository<Type> {

  // TODO: Clean up type repository

  // Optional<Type> findOneByReleaseIdAndQname(Long releaseId, String qname);

  // Set<Type> findByReleaseIdOrderByQname(Long releaseId);
  // Set<Type> findByNamespaceIdOrderByQname(Long namespaceId);
  // Set<Type> findByPrefixOrderByNameAsc(String prefix);

}
