package gov.niem.tools.api.db.subproperty;

import java.util.Optional;
import java.util.Set;
import org.springframework.data.jpa.repository.JpaRepository;

/**
 * Repository interface for finding and managing subproperties in the database.
 */
public interface SubpropertyRepository extends JpaRepository<Subproperty, Long> {

  Optional<Subproperty> findOneByVersionIdAndTypeIdAndPropertyId(Long versionId,
      Long typeId, Long propertyId);

  Set<Subproperty> findByVersionIdAndType_Namespace_PrefixAndType_NameOrderBySequenceAsc(
      Long versionId, String prefix, String name);

  Set<Subproperty> findByVersionIdAndProperty_Namespace_PrefixAndProperty_NameOrderByType_NameAsc(
      Long versionId, String prefix, String name);

  Set<Subproperty> findByVersionIdAndType_Namespace_PrefixOrderByType_NameAscSequenceAsc(
      Long versionId, String prefix);

  Set<Subproperty> findByVersionIdAndProperty_Namespace_PrefixOrderByType_NameAscSequence(
      Long versionId, String prefix);

  Set<Subproperty> findByVersionId(Long versionId);

  Subproperty findOneByTypeIdAndPropertyNameEndingWith(Long typeId, String nameSuffix);

  long countByType_Namespace_Version_Id(Long versionId);

  long countByType_Namespace_Id(Long namespaceId);

  long countByType_Id(Long typeId);

  long countByProperty_Namespace_Id(Long namespaceId);

  long countByProperty_Id(Long propertyId);

}
