package service.impl;

import entity.Account;
import entity.AccountStatus;
import entity.Card;
import entity.CardStatus;
import entity.Payment;
import entity.PaymentType;
import entity.Transaction;
import entity.TransactionStatus;
import entity.TransactionType;
import kafka.TransactionEvent;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import repository.AccountRepository;
import repository.CardRepository;
import repository.PaymentRepository;
import repository.TransactionRepository;

import java.math.BigDecimal;
import java.math.MathContext;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class TransactionServiceImpl {
    private final TransactionRepository transactionRepository;
    private final CardRepository cardRepository;
    private final AccountRepository accountRepository;
    private final PaymentRepository paymentRepository;

    @Value("${transaction.threshold}")
    private int fraudThreshold;

    @Value("${transaction.windowMinutes}")
    private int fraudWindowMinutes;

    @Value("${credit.defaultAnnualRate}")
    private BigDecimal defaultAnnualRate;

    // не понял как получить monthCount из ProductRegistry и использовал заглушку
    @Value("${credit.defaultMonths:12}")
    private int defaultMonths;

    public Transaction processTransaction(TransactionEvent event, UUID messageKey) {
        UUID uuid = event.getEventUuid() != null ? event.getEventUuid() : messageKey;
        if (uuid == null) {
            log.warn("No event UUID supplied; skipping processing");
            throw new RuntimeException("No event UUID supplied");
        }
        if (transactionRepository.existsByEventUuid(uuid)) {
            log.info("Duplicate event {} — skipping", uuid);
            throw new RuntimeException("Duplicate event");
        }

        Card card = cardRepository.findByCardId(event.getCardId())
                .orElseThrow(() -> new RuntimeException("Card not found"));

        if (card.getStatus().equals(CardStatus.BLOCKED) || card.getStatus().equals(CardStatus.ARRESTED)) {
            throw new RuntimeException("You can't make transaction on this card.");
        }

        Transaction transaction = Transaction.builder()
                .cardId(event.getCardId())
                .type(event.getType())
                .amount(event.getAmount())
                .accountId(event.getAccountId())
                .status(event.getStatus())
                .eventUuid(uuid)
                .build();


        boolean shouldBlock = isCardSuspicious(card.getCardId(), fraudThreshold, fraudWindowMinutes);
        Optional<Account> accountOpt = accountRepository.findById(event.getAccountId());
        if (shouldBlock) {
            transaction.setStatus(TransactionStatus.FREEZE);
            if (accountOpt.isPresent()) {
                Account account = accountOpt.get();
                account.setStatus(AccountStatus.BLOCKED);
            }
        } else {
            transaction.setStatus(TransactionStatus.ALLOWED);
        }

        if (accountOpt.isPresent()) {
            Account account = accountOpt.get();
            switch (transaction.getType()) {
                case TransactionType.CREDIT:
                    transaction.setStatus(TransactionStatus.PROCESSING);
                    BigDecimal newBalance = account.getBalance().add(transaction.getAmount());
                    account.setBalance(newBalance);

                    log.info("New balance {} applied for account {}", account.getBalance(), account.getId());

                    if (account.getIsRecalc()) {
                        BigDecimal annualRate = account.getInterestRate() == null
                                ? defaultAnnualRate
                                : BigDecimal.valueOf(account.getInterestRate());

                        int months = defaultMonths;

                        List<Payment> schedule = generateAnnuityPayments(
                                account.getId(),
                                transaction.getAmount(),
                                annualRate,
                                months,
                                LocalDateTime.now().withHour(0).withMinute(0).withSecond(0).withNano(0),
                                card.getId()
                        );

                        if (!schedule.isEmpty()) {
                            paymentRepository.saveAll(schedule);
                            log.info("Saved {} scheduled payments for credit account {}", schedule.size(), account.getId());
                        } else {
                            log.warn("Schedule generation returned empty for account {}", account.getId());
                        }
                    }

                    LocalDate txDate = event.getOccurredAt() != null
                            ? event.getOccurredAt().toLocalDate()
                            : LocalDate.now();

                    List<Payment> due = paymentRepository.findAll().stream()
                            .filter(p -> p.getAccountId().equals(account.getId()))
                            .filter(p -> p.getPaymentDate().toLocalDate().isEqual(txDate))
                            .filter(p -> !Boolean.TRUE.equals(p.getExpired()))
                            .toList();

                    for (Payment p : due) {
                        BigDecimal balance = account.getBalance() == null ? BigDecimal.ZERO : account.getBalance();
                        if (balance.compareTo(p.getAmount()) >= 0) {
                            account.setBalance(balance.subtract(p.getAmount()));
                            p.setPayedAt(LocalDateTime.now());
                            p.setExpired(false);
                            paymentRepository.save(p);
                            accountRepository.save(account);
                            log.info("Auto debited payment {} amount {} for account {}", p.getId(), p.getAmount(), account.getId());
                        } else {
                            p.setExpired(true);
                            paymentRepository.save(p);
                            log.info("Insufficient funds for payment {}, marked expired", p.getId());
                        }
                    }

                    accountRepository.save(account);
                    break;

                case TransactionType.DEBIT, TransactionType.PAYMENT:
                    transaction.setStatus(TransactionStatus.PROCESSING);
                    if (account.getBalance().compareTo(transaction.getAmount()) < 0) {

                        log.warn("Insufficient funds for debit on account {}, transaction {} denied", account.getId(), uuid);

                        throw new RuntimeException("Insufficient funds");
                    }
                    BigDecimal remainingBalance = account.getBalance().subtract(transaction.getAmount());
                    account.setBalance(remainingBalance);

                    log.info("New balance {} applied for account {}", account.getBalance(), account.getId());

                    accountRepository.save(account);
                    break;

                default:
                    log.warn("Unsupported transaction {}", uuid);
                    transaction.setStatus(TransactionStatus.CANCELLED);
                    throw new RuntimeException("Unsupported transaction");
            }
        }

        Transaction saved = transactionRepository.save(transaction);
        transaction.setStatus(TransactionStatus.COMPLETE);
        log.info("Processed transaction {} for card {}", saved.getId(), card.getCardId());

        return saved;
    }

    public boolean isCardSuspicious(Long cardId, int limit, int minutes) {
        LocalDateTime now = LocalDateTime.now();
        LocalDateTime from = now.minusMinutes(minutes);

        long count = transactionRepository.countTransactionsByCardIdInPeriod(cardId, from, now);

        return count > limit;
    }

    private List<Payment> generateAnnuityPayments(Long accountId,
                                                  BigDecimal principal,
                                                  BigDecimal annualRate,
                                                  int months,
                                                  LocalDateTime startDateTime,
                                                  Long cardId) {
        List<Payment> schedule = new ArrayList<>();
        if (principal == null || principal.compareTo(BigDecimal.ZERO) <= 0 || months <= 0) {
            return schedule;
        }

        MathContext mc = new MathContext(20, RoundingMode.HALF_EVEN);

        // мес ставка i = annualRate / 12
        BigDecimal monthlyRate = annualRate.divide(BigDecimal.valueOf(12), mc);

        // (1+i)^n
        BigDecimal onePlusPower = BigDecimal.ONE.add(monthlyRate, mc).pow(months, mc);

        // кэф аннуитета: i*(1+i)^n / ((1+i)^n - 1)
        BigDecimal numerator = monthlyRate.multiply(onePlusPower, mc);
        BigDecimal denominator = onePlusPower.subtract(BigDecimal.ONE, mc);
        BigDecimal factor = numerator.divide(denominator, 20, RoundingMode.HALF_UP);

        BigDecimal monthlyPayment = principal.multiply(factor, mc).setScale(2, RoundingMode.HALF_UP);

        BigDecimal remainingPrincipal = principal.setScale(2, RoundingMode.HALF_EVEN);

        for (int m = 1; m <= months; m++) {
            BigDecimal interestPart = remainingPrincipal.multiply(monthlyRate, mc).setScale(2, RoundingMode.HALF_UP);
            BigDecimal principalPart = monthlyPayment.subtract(interestPart).setScale(2, RoundingMode.HALF_UP);

            if (m == months) {
                principalPart = remainingPrincipal;
                monthlyPayment = principalPart.add(interestPart).setScale(2, RoundingMode.HALF_UP);
            }

            LocalDateTime dueDate = startDateTime.plusMonths(m);

            Payment p = Payment.builder()
                    .accountId(accountId)
                    .paymentDate(dueDate)
                    .amount(monthlyPayment)
                    .isCredit(false)
                    .cardId(cardId)
                    .payedAt(null)
                    .type(PaymentType.TRANSFER)
                    .build();

            schedule.add(p);

            remainingPrincipal = remainingPrincipal.subtract(principalPart).setScale(2, RoundingMode.HALF_EVEN);
            if (remainingPrincipal.compareTo(BigDecimal.ZERO) < 0) remainingPrincipal = BigDecimal.ZERO;
        }

        return schedule;
    }

}
