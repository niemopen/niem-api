package gov.niem.tools.api.db.property;

import gov.niem.tools.api.db.component.ComponentRepository;

import java.util.List;

/**
 * Repository interface for finding and managing properties in the database.
 */
public interface PropertyRepository extends ComponentRepository<Property> {

  long countByNamespace_IdAndCategory(long namespaceId, Property.Category category);

  long countByNamespace_Version_IdAndCategory(long versionId, Property.Category category);

  List<Property> findAllByGroup_Id(long groupId);

}
