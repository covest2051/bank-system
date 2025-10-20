package service.impl;

import entity.Card;
import entity.CardStatus;
import entity.PaymentSystem;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import repository.CardRepository;
import service.CardService;

@Service
@RequiredArgsConstructor
@Slf4j
public class CardServiceImpl implements CardService {
    private final CardRepository cardRepository;

    @Override
    public Card createCard(Long accountId, PaymentSystem paymentSystem) {
        Card card = Card.builder()
                .accountId(accountId)
                .cardId(generateCardNumber())
                .paymentSystem(paymentSystem)
                .status(CardStatus.ACTIVE)
                .build();

        Card saved = cardRepository.save(card);

        log.info("Created card {} for accountId={}", saved.getCardId(), accountId);
        return saved;
    }

    private Long generateCardNumber() {
        return System.currentTimeMillis();
    }
}

