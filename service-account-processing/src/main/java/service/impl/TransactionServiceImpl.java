package service.impl;

import entity.Card;
import entity.CardStatus;
import entity.Transaction;
import kafka.TransactionEvent;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import repository.CardRepository;
import repository.TransactionRepository;

@Service
@RequiredArgsConstructor
@Slf4j
public class TransactionServiceImpl {
    private final TransactionRepository transactionRepository;
    private final CardRepository cardRepository;

    public Transaction processTransaction(TransactionEvent event) {
        Card card = cardRepository.findByCardId(event.getCardId())
                .orElseThrow(() -> new RuntimeException("Card not found"));

        if (card.getStatus().equals(CardStatus.BLOCKED)) {
            throw new RuntimeException("Card is blocked, transaction denied");
        }

        Transaction transaction = Transaction.builder()
                .cardId(event.getCardId())
                .type(event.getType())
                .amount(event.getAmount())
                .accountId(event.getAccountId())
                .status(event.getStatus())
                .build();

        Transaction saved = transactionRepository.save(transaction);

        log.info("Processed transaction {} for card {}", saved.getId(), card.getCardId());

        return saved;
    }
}
