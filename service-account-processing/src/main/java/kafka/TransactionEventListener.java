package kafka;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Service;
import service.TransactionService;

@Service
@RequiredArgsConstructor
@Slf4j
public class TransactionEventListener {
    private final TransactionService transactionService;

    @KafkaListener(topics = "client_transactions")
    public void consume(TransactionEvent event) {
        log.info("Received transaction event: {}", event);
        try {
            transactionService.processTransaction(event);
        } catch (Exception e) {
            log.error("Failed to process transaction: {}", e.getMessage());
        }
    }
}

