package gov.niem.tools.api.db.namespace;

import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

/**
 * Repository interface for finding and managing namespaces in the database.
 */
public interface NamespaceRepository extends JpaRepository<Namespace, Long> {

  Optional<Namespace> findOneByVersion_IdAndPrefix(Long versionId, String prefix);

}
