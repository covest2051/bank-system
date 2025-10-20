import entity.Account;
import entity.AccountStatus;
import entity.Card;
import entity.CardStatus;
import entity.Transaction;
import entity.TransactionStatus;
import entity.TransactionType;
import kafka.TransactionEvent;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import repository.AccountRepository;
import repository.CardRepository;
import repository.PaymentRepository;
import repository.TransactionRepository;
import service.impl.TransactionServiceImpl;

import java.math.BigDecimal;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.any;
import static org.mockito.Mockito.anyLong;
import static org.mockito.Mockito.atLeastOnce;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class TransactionServiceImplTest {

    @Mock
    private TransactionRepository transactionRepository;
    @Mock
    private CardRepository cardRepository;
    @Mock
    private AccountRepository accountRepository;
    @Mock
    private PaymentRepository paymentRepository;

    @InjectMocks
    private TransactionServiceImpl transactionService;

    private TransactionEvent event;
    private Card card;
    private Account account;

    @BeforeEach
    void setUp() {
        card = Card.builder()
                .id(10L)
                .cardId(999L)
                .status(CardStatus.ACTIVE)
                .build();

        account = Account.builder()
                .id(1L)
                .balance(BigDecimal.valueOf(1000))
                .status(AccountStatus.ACTIVE)
                .isRecalc(false)
                .build();

        event = TransactionEvent.builder()
                .eventUuid(UUID.randomUUID())
                .accountId(account.getId())
                .cardId(card.getCardId())
                .type(TransactionType.DEBIT)
                .amount(BigDecimal.valueOf(200))
                .status(TransactionStatus.PROCESSING)
                .build();
    }

    @Test
    void processTransaction_shouldProcessDebitSuccessfully() {
        when(transactionRepository.existsByEventUuid(any())).thenReturn(false);
        when(cardRepository.findByCardId(card.getCardId())).thenReturn(Optional.of(card));
        when(accountRepository.findById(account.getId())).thenReturn(Optional.of(account));
        when(transactionRepository.countTransactionsByCardIdInPeriod(anyLong(), any(), any())).thenReturn(0L);
        when(transactionRepository.save(any(Transaction.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        Transaction result = transactionService.processTransaction(event, event.getEventUuid());

        assertThat(result.getStatus()).isEqualTo(TransactionStatus.COMPLETE);
        assertThat(account.getBalance()).isEqualByComparingTo(BigDecimal.valueOf(800));

        verify(transactionRepository).save(any(Transaction.class));
        verify(accountRepository, atLeastOnce()).save(account);
    }

    @Test
    void processTransaction_shouldThrowIfDuplicateEvent() {
        when(transactionRepository.existsByEventUuid(any())).thenReturn(true);

        assertThatThrownBy(() ->
                transactionService.processTransaction(event, event.getEventUuid()))
                .isInstanceOf(RuntimeException.class)
                .hasMessageContaining("Duplicate");

        verify(transactionRepository, never()).save(any());
    }

    @Test
    void processTransaction_shouldThrowIfCardNotFound() {
        when(transactionRepository.existsByEventUuid(any())).thenReturn(false);
        when(cardRepository.findByCardId(anyLong())).thenReturn(Optional.empty());

        assertThatThrownBy(() ->
                transactionService.processTransaction(event, event.getEventUuid()))
                .isInstanceOf(RuntimeException.class)
                .hasMessageContaining("Card not found");
    }

    @Test
    void processTransaction_shouldThrowIfCardBlocked() {
        card.setStatus(CardStatus.BLOCKED);
        when(transactionRepository.existsByEventUuid(any())).thenReturn(false);
        when(cardRepository.findByCardId(anyLong())).thenReturn(Optional.of(card));

        assertThatThrownBy(() ->
                transactionService.processTransaction(event, event.getEventUuid()))
                .isInstanceOf(RuntimeException.class)
                .hasMessageContaining("can't make transaction");
    }

    @Test
    void processTransaction_shouldThrowIfInsufficientFunds() {
        account.setBalance(BigDecimal.valueOf(100));
        when(transactionRepository.existsByEventUuid(any())).thenReturn(false);
        when(cardRepository.findByCardId(anyLong())).thenReturn(Optional.of(card));
        when(accountRepository.findById(anyLong())).thenReturn(Optional.of(account));
        when(transactionRepository.countTransactionsByCardIdInPeriod(anyLong(), any(), any())).thenReturn(0L);

        assertThatThrownBy(() ->
                transactionService.processTransaction(event, event.getEventUuid()))
                .isInstanceOf(RuntimeException.class)
                .hasMessageContaining("Insufficient funds");
    }

    @Test
    void isCardSuspicious_shouldReturnTrueWhenLimitExceeded() {
        when(transactionRepository.countTransactionsByCardIdInPeriod(anyLong(), any(), any()))
                .thenReturn(10L);

        boolean result = transactionService.isCardSuspicious(1L, 5, 60);
        assertThat(result).isTrue();
    }

    @Test
    void isCardSuspicious_shouldReturnFalseWhenBelowLimit() {
        when(transactionRepository.countTransactionsByCardIdInPeriod(anyLong(), any(), any()))
                .thenReturn(2L);

        boolean result = transactionService.isCardSuspicious(1L, 5, 60);
        assertThat(result).isFalse();
    }
}
