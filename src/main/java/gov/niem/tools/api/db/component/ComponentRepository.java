package gov.niem.tools.api.db.component;

import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.repository.NoRepositoryBean;

/**
 * Repository interface for finding and managing components in the database.
 */
@NoRepositoryBean
public interface ComponentRepository<T extends Component<T>> extends JpaRepository<T, Long> {

  Optional<T> findOneByNamespace_IdAndName(Long namespaceId, String name);

  List<T> findByNamespace_Version_IdOrderByNamespace_PrefixAscNameAsc(Long versionId);

  List<T> findByNamespace_IdOrderByNamespace_PrefixAscNameAsc(Long namespaceId);

  List<T> findByNamespace_PrefixOrderByNameAsc(String prefix);

  long countByNamespace_Id(long namespaceId);

  long countByNamespace_Version_Id(long versionId);

}
