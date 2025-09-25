package repository;

import entity.BlacklistRegistry;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface BlacklistRegistryRepository extends JpaRepository<BlacklistRegistry, Long> {
    @Query("select case when count(b) > 0 then true else false end from BlacklistRegistry b " +
            "where b.documentType = :docType and b.documentId = :docId " +
            "and (b.blacklistExpirationDate is null or b.blacklistExpirationDate > current_timestamp)")
    boolean existsActiveByDocumentTypeAndDocumentId(@Param("docType") String documentType,
                                                    @Param("docId") String documentId);

    Optional<BlacklistRegistry> findTopByDocumentTypeAndDocumentIdOrderByBlacklistedAtDesc(String documentType, String documentId);
}
