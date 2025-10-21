package service;

import entity.Decision;
import entity.PaymentRegistry;
import entity.ProductRegistry;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import repository.PaymentRegistryRepository;
import repository.ProductRegistryRepository;

import java.math.BigDecimal;
import java.util.List;

@Service
@RequiredArgsConstructor
public class CreditDecisionService {
    @Value("${credit.limit}")
    private BigDecimal creditLimit;

    private final PaymentRegistryRepository paymentRegistryRepository;
    private final ProductRegistryRepository productRegistryRepository;

    @Transactional
    public Decision evaluate(Long clientId, BigDecimal requestedAmount) {
        List<ProductRegistry> existingProductRegistry = productRegistryRepository.findByClientId(clientId);

        BigDecimal sumOutstandingAmount = existingProductRegistry.stream()
                .flatMap(productRegistry ->
                        paymentRegistryRepository.findByProductRegistryIdOrderByPaymentExpirationDate(productRegistry.getId())
                                .stream().filter(payment -> !payment.getExpired())
                                .map(PaymentRegistry::getAmount)
                ).reduce(BigDecimal.ZERO, BigDecimal::add);

        BigDecimal totalAfterNew = sumOutstandingAmount.add(requestedAmount);
        if (totalAfterNew.compareTo(creditLimit) > 0) { //
            return Decision.reject("Total outstanding amount would exceed configured limit");
        }

        boolean hasLate = paymentRegistryRepository.hasLatePaymentsByClientId(clientId);
        if (hasLate) {
            return Decision.reject("Client has overdue payment");
        }

        return Decision.approve();
    }
}

