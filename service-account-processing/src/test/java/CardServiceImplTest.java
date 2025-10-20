import entity.Card;
import entity.CardStatus;
import entity.PaymentSystem;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import repository.CardRepository;
import service.impl.CardServiceImpl;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;
import static org.assertj.core.api.AssertionsForClassTypes.assertThatThrownBy;
import static org.mockito.Mockito.any;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class CardServiceImplTest {

    @Mock
    private CardRepository cardRepository;

    @InjectMocks
    private CardServiceImpl cardService;

    private Card savedCard;

    @BeforeEach
    void setUp() {
        savedCard = Card.builder()
                .id(1L)
                .accountId(10L)
                .status(CardStatus.ACTIVE)
                .build();
    }

    @Test
    void createCard_shouldSaveCardWithCorrectAccountId() {
        Long accountId = 10L;
        when(cardRepository.save(any(Card.class))).thenReturn(savedCard);

        Card result = cardService.createCard(accountId, PaymentSystem.VISA);

        ArgumentCaptor<Card> captor = ArgumentCaptor.forClass(Card.class);
        verify(cardRepository).save(captor.capture());

        Card saved = captor.getValue();
        assertThat(saved.getAccountId()).isEqualTo(accountId);
        assertThat(saved.getStatus().equals(CardStatus.ACTIVE));
        assertThat(result).isEqualTo(savedCard);
    }

    @Test
    void createCard_shouldThrowExceptionWhenRepositoryFails() {
        when(cardRepository.save(any(Card.class)))
                .thenThrow(new RuntimeException("DB error"));

        assertThatThrownBy(() -> cardService.createCard(10L, PaymentSystem.VISA))
                .isInstanceOf(RuntimeException.class)
                .hasMessageContaining("DB error");

        verify(cardRepository, times(1)).save(any(Card.class));
    }

    @Test
    void createCard_shouldGenerateCardNumber() {
        when(cardRepository.save(any(Card.class))).thenReturn(savedCard);

        Card result = cardService.createCard(10L, PaymentSystem.VISA);

        ArgumentCaptor<Card> captor = ArgumentCaptor.forClass(Card.class);
        verify(cardRepository).save(captor.capture());
        Card cardToSave = captor.getValue();

        assertThat(result.getAccountId()).isEqualTo(10L);
    }
}
