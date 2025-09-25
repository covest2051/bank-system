package service;

import entity.Transaction;
import kafka.TransactionEvent;

public interface TransactionService {
    public Transaction processTransaction(TransactionEvent event);
}
