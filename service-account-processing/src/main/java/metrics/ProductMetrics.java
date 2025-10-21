package metrics;

import io.micrometer.core.instrument.MeterRegistry;
import lombok.RequiredArgsConstructor;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import repository.AccountRepository;

@Component
@RequiredArgsConstructor
public class ProductMetrics {

    private final AccountRepository accountRepository;
    private final MeterRegistry meterRegistry;

    @Scheduled(fixedRate = 60000)
    public void updateProductMetrics() {
        long totalAccounts = accountRepository.count();
        meterRegistry.gauge("bank_products_total", totalAccounts);
    }
}
