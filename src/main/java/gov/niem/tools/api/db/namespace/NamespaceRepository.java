package gov.niem.tools.api.db.namespace;

import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

/**
 * Repository interface for finding and managing namespaces in the database.
 */
public interface NamespaceRepository extends JpaRepository<Namespace, Long> {

  Optional<Namespace> findOneByVersion_IdAndPrefix(Long versionId, String prefix);

  long countByVersion_Id(Long versionId);

  long countByVersion_IdAndCategory(Long versionId, Namespace.Category category);

}
