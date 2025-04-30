package gov.niem.tools.api.db.facet;

import gov.niem.tools.api.db.facet.Facet.Category;

import java.util.Optional;
import java.util.Set;
import org.springframework.data.jpa.repository.JpaRepository;

/**
 * Repository interface for finding and managing facets in the database.
 */
public interface FacetRepository extends JpaRepository<Facet, Long> {

  Optional<Facet> findOneByVersionIdAndPrefixAndType_NameAndCategoryAndValue(
      Long versionId, String prefix, String name, Category category, String valueString);

  Set<Facet> findByVersionIdAndPrefix(Long versionId, String prefix);

  Set<Facet> findByVersionIdAndPrefixAndType_Name(Long versionId, String prefix, String name);

  long countByType_Namespace_Version_Id(Long versionId);

  long countByType_Namespace_Id(Long namespaceId);

  long countByType_Id(Long typeId);

}
