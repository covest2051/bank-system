package service;

import entity.Payment;
import kafka.PaymentEvent;

public interface PaymentService {
    public Payment processPayment(PaymentEvent event);
}
