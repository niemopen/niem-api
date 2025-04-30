package gov.niem.tools.api.db.model;

import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

/**
 * Repository interface for finding and managing models in the database.
 */
public interface ModelRepository extends JpaRepository<Model, Long> {

  Optional<Model> findOneBySteward_StewardKeyAndModelKey(String stewardKey, String modelKey);

  Optional<Model> findOneBySteward_StewardKeyAndShortName(String stewardKey, String shortName);

  List<Model> findByCategory(String category);

  List<Model> findByShortName(String shortName);

  List<Model> findStewardsByStewardId(Long id);

  List<Model> findStewardsByCategory(String category);

  long countBySteward_Id(Long stewardId);

}
