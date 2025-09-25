package kafka;

import entity.AccountStatus;
import entity.PaymentSystem;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Service;
import repository.AccountRepository;
import service.AccountService;

@Service
@RequiredArgsConstructor
@Slf4j
public class AccountEventListener {
    private final AccountRepository accountRepository;
    private final AccountService accountService;

    @KafkaListener(topics = "client_products")
    public void consume(AccountEvent event) {
        log.info("Received event: {}", event);

        try {
            if (accountRepository.existsByClientId(event.getClientId())) {
                log.info("Account for clientId={} already exists", event.getClientId());
                return;
            }

            accountService.createAccount(event.getClientId());
            log.info("Account created for clientId={}", event.getClientId());

        } catch (Exception e) {
            log.error("Failed to process AccountEvent for clientId={}, error={}", event.getClientId(), e.getMessage(), e);
        }
    }
}
