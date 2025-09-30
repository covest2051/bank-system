package repository;

import entity.Payment;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDateTime;
import java.util.List;

public interface PaymentRepository extends JpaRepository<Payment, Long> {
    List<Payment> findByAccountIdAndPaymentDateBetween(Long accountId, LocalDateTime from, LocalDateTime to);

}

