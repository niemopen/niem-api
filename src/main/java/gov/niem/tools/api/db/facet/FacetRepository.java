package gov.niem.tools.api.db.facet;

import gov.niem.tools.api.db.facet.Facet.Category;

import java.util.Optional;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

/**
 * Repository interface for finding and managing facets in the database.
 */
public interface FacetRepository extends JpaRepository<Facet, Long> {

  Optional<Facet> findOneByVersionIdAndPrefixAndType_NameAndCategoryAndValue(
      Long versionId, String prefix, String name, Category category, String valueString);

  Page<Facet> findAllByType_Namespace_Version_Id(Long versionId, Pageable pageable);

  Page<Facet> findAllByType_Namespace_Id(Long namespaceId, Pageable pageable);

  Page<Facet> findAllByType_Id(Long typeId, Pageable pageable);

  long countByType_Namespace_Version_Id(Long versionId);

  long countByType_Namespace_Id(Long namespaceId);

  long countByType_Id(Long typeId);

}
