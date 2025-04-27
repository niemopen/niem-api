package gov.niem.tools.api.db.version;

import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

/**
 * Repository interface for finding and managing versions in the database.
 */
public interface VersionRepository extends JpaRepository<Version, Long> {

  Optional<Version> findOneByModel_Steward_StewardKeyAndModel_ModelKeyAndVersionNumber(
      String stewardKey, String modelKey, String versionNumber);

  Long findIdByModel_Steward_StewardKeyAndModel_ModelKeyAndVersionNumber(
      String stewardKey, String modelKey, String versionNumberString);

}
