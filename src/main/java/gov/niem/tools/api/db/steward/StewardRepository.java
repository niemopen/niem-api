package gov.niem.tools.api.db.steward;

import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

/**
 * Repository interface for finding and managing stewards in the database.
 */
public interface StewardRepository extends JpaRepository<Steward, Long> {

  Optional<Steward> findOneById(Long id);

  Optional<Steward> findOneByStewardKey(String key);

  Optional<Steward> findOneByShortName(String shortName);

  List<Steward> findByCategoryOrderByShortName(String category);

  List<Steward> findByCountryOrderByShortName(String category);

  List<Steward> findByFullNameContainingOrderByFullName(String keyword);

  @Query(nativeQuery = true, value = "select slugify(:value);")
  String slugify(@Param("value") String value);

}
