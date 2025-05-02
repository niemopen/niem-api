package gov.niem.tools.api.db.component;

import java.util.Optional;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.repository.NoRepositoryBean;

/**
 * Repository interface for finding and managing components in the database.
 */
@NoRepositoryBean
public interface ComponentRepository<T extends Component<T>> extends JpaRepository<T, Long> {

  Optional<T> findOneByNamespace_IdAndName(Long namespaceId, String name);

  Page<T> findAllByNamespace_Version_Id(Long versionId, Pageable pageable);

  Page<T> findAllByNamespace_Id(Long namespaceId, Pageable pageable);

  long countByNamespace_Id(long namespaceId);

  long countByNamespace_Version_Id(long versionId);

}
