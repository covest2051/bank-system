package repository;

import entity.Payment;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

public interface PaymentRepository extends JpaRepository<Payment, Long> {
    boolean existsByUuid(UUID eventUuid);
    List<Payment> findAllByAccountIdAndPayedAtIsNull(Long accountId);
}

