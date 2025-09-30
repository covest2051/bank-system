package kafka;

import entity.Account;
import entity.Payment;
import entity.PaymentType;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Service;
import repository.AccountRepository;
import repository.PaymentRepository;
import service.TransactionService;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Slf4j
public class PaymentEventListener {
    private final PaymentRepository paymentRepository;
    private final AccountRepository accountRepository;

    @KafkaListener(topics = "client_payments")
    public void consume(ConsumerRecord<String, PaymentEvent> record) {
        UUID eventUuid = UUID.fromString(record.key());
        PaymentEvent event = record.value();

        log.info("Received payment event: {}", event);

        if (paymentRepository.existsByUuid(eventUuid)) {
            log.warn("Duplicated payment {} — skipped", eventUuid);
            return;
        }

        Account account = accountRepository.findById(event.getAccountId())
                .orElseThrow(() -> new RuntimeException("Account not found"));

        List<Payment> unpaidPayments = paymentRepository.findAllByAccountIdAndPayedAtIsNull(account.getId());
        BigDecimal totalDebt = unpaidPayments.stream()
                .map(Payment::getAmount)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        if (event.getAmount().compareTo(totalDebt) == 0) {
            account.setBalance(account.getBalance().subtract(event.getAmount()));
            accountRepository.save(account);

            unpaidPayments.forEach(p -> p.setPayedAt(LocalDateTime.now()));
            paymentRepository.saveAll(unpaidPayments);

            Payment payment = Payment.builder()
                    .accountId(account.getId())
                    .cardId(event.getCardId())
                    .paymentDate(event.getPaymentDate())
                    .amount(event.getAmount())
                    .isCredit(true)
                    .type(PaymentType.CREDIT)
                    .uuid(eventUuid)
                    .payedAt(LocalDateTime.now())
                    .build();

            paymentRepository.save(payment);

            log.info("Payment {} managed. Account balance: {}", eventUuid, account.getBalance());
        } else {
            log.warn("Payment amount {} not equal to amount owed {}. Ignored.", event.getAmount(), totalDebt);
        }
    }
}

