package service;

import entity.Card;
import entity.PaymentSystem;

public interface CardService {
    public Card createCard(Long accountId, PaymentSystem paymentSystem);
}
