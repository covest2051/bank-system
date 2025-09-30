package repository;

import entity.Transaction;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.time.OffsetDateTime;
import java.util.UUID;

@Repository
public interface TransactionRepository extends JpaRepository<Transaction, Long> {
    boolean existsByEventUuid(UUID eventUuid);

    @Query("SELECT COUNT(t) FROM Transaction t " +
            "WHERE t.cardId = :cardId " +
            "AND t.timestamp BETWEEN :from AND :to")
    long countTransactionsByCardIdInPeriod(Long cardId, LocalDateTime from, LocalDateTime to);
}

