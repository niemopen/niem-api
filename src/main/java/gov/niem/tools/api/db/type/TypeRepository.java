package gov.niem.tools.api.db.type;

import gov.niem.tools.api.db.component.ComponentRepository;

import java.util.List;

/**
 * Repository interface for finding and managing types in the database.
 */
public interface TypeRepository extends ComponentRepository<Type> {

  long countByNamespace_IdAndCategory(long namespaceId, Type.Category category);

  long countByNamespace_Version_IdAndCategory(long versionId, Type.Category category);

  List<Type> findAllByBase_Id(long baseId);

}
