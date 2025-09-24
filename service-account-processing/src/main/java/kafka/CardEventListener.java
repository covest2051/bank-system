package kafka;

import entity.AccountStatus;
import entity.PaymentSystem;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Service;
import repository.AccountRepository;
import service.CardService;

@Service
@RequiredArgsConstructor
@Slf4j
public class CardEventListener {
    private final AccountRepository accountRepository;
    private final CardService cardService;

    @KafkaListener(topics = "client_cards", groupId = "card-service-group")
    public void consume(CardEvent event) {
        log.info("Received event: {}", event);

        var account = accountRepository.findById(event.getAccountId())
                .orElseThrow(() -> new RuntimeException("Account not found"));

        if (account.getStatus().equals(AccountStatus.BLOCKED)) {
            log.warn("Account {} is blocked, card not created", account.getId());
            return;
        }

        cardService.createCard(account.getId(), PaymentSystem.VISA); // #need убрать заглушку
    }
}


