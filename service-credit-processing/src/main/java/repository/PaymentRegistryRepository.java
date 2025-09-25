package repository;

import entity.PaymentRegistry;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface PaymentRegistryRepository extends JpaRepository<PaymentRegistry, Long> {
    List<PaymentRegistry> findByProductRegistryIdOrderByPaymentExpirationDate(Long productRegistryId);

    boolean hasLatePaymentsByClientId(Long clientId);
}
