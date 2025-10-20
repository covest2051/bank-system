package service;

import entity.PaymentRegistry;
import entity.ProductRegistry;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import repository.PaymentRegistryRepository;
import repository.ProductRegistryRepository;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Service
@RequiredArgsConstructor
public class ProductRegistryService {
    private final ProductRegistryRepository productRegistryRepository;
    private final PaymentRegistryRepository paymentRegistryRepository;

    public ProductRegistry openProduct(Long clientId,
                                       Double interestRate,
                                       BigDecimal amount,
                                       Integer monthCount) {

        ProductRegistry productRegistry = ProductRegistry.builder()
                .clientId(clientId)
                .interestRate(interestRate)
                .monthCount(monthCount)
                .openDate(LocalDateTime.now())
                .build();

        PaymentRegistry paymentRegistry = PaymentRegistry.builder()
                .amount(amount)
                .productRegistryId(productRegistry.getId())
                .build();

        productRegistryRepository.save(productRegistry);
        paymentRegistryRepository.save(paymentRegistry);

        List<PaymentRegistry> payments = generateSchedule(productRegistry, amount, interestRate, monthCount);

        paymentRegistryRepository.saveAll(payments);

        return productRegistry;
    }

    private List<PaymentRegistry> generateSchedule(ProductRegistry registry,
                                                   BigDecimal principal,
                                                   Double annualRateDouble,
                                                   Integer monthCount) {

        BigDecimal annualRate = BigDecimal.valueOf(annualRateDouble);
        List<PaymentRegistry> schedule = new ArrayList<>();

        BigDecimal monthlyRate = annualRate.divide(BigDecimal.valueOf(12), 10, RoundingMode.HALF_UP); // i
        BigDecimal numerator = monthlyRate.multiply((BigDecimal.ONE.add(monthlyRate)).pow(monthCount));
        BigDecimal denominator = (BigDecimal.ONE.add(monthlyRate)).pow(monthCount).subtract(BigDecimal.ONE);
        BigDecimal annuityPayment = principal.multiply(numerator.divide(denominator, 10, RoundingMode.HALF_UP))
                .setScale(2, RoundingMode.HALF_UP); // A

        BigDecimal remainingDebt = principal;

        for (int month = 1; month <= monthCount; month++) {
            // проценты за текущий месяц
            BigDecimal interestPart = remainingDebt.multiply(monthlyRate)
                    .setScale(2, RoundingMode.HALF_UP);

            // осн долг
            BigDecimal principalPart = annuityPayment.subtract(interestPart)
                    .setScale(2, RoundingMode.HALF_UP);

            // ост долга
            remainingDebt = remainingDebt.subtract(principalPart)
                    .setScale(2, RoundingMode.HALF_UP);

            // дата платежа
            LocalDateTime paymentDate = registry.getOpenDate().plusMonths(month);

            PaymentRegistry payment = PaymentRegistry.builder()
                    .productRegistryId(registry.getId())
                    .paymentDate(LocalDateTime.now())
                    .paymentExpirationDate(paymentDate)
                    .amount(annuityPayment)
                    .interestRateAmount(interestPart)
                    .debtAmount(principalPart)
                    .expired(false)
                    .build();

            schedule.add(payment);
        }

        return schedule;
    }
}
