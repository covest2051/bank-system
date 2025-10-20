package integration;

import com.fasterxml.jackson.databind.ObjectMapper;
import entity.Card;
import entity.CardStatus;
import entity.PaymentSystem;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import repository.CardRepository;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
class CardControllerIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private CardRepository cardRepository;

    private Card testCard;

    @BeforeEach
    void setUp() {
        cardRepository.deleteAll();

        testCard = Card.builder()
                .accountId(1L)
                .cardId(999L)
                .paymentSystem(PaymentSystem.VISA)
                .status(CardStatus.ACTIVE)
                .build();

        testCard = cardRepository.save(testCard);
    }

    @Test
    void getCardById_shouldReturnCard() throws Exception {
        mockMvc.perform(get("/api/cards/{id}", testCard.getId())
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(testCard.getId()))
                .andExpect(jsonPath("$.cardId").value(testCard.getCardId()))
                .andExpect(jsonPath("$.status").value("ACTIVE"));
    }

    @Test
    void getCardById_shouldReturnNotFound_whenCardMissing() throws Exception {
        mockMvc.perform(get("/api/cards/{id}", 999L)
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isNotFound());
    }

    @Test
    void createCard_shouldSaveNewCard() throws Exception {
        Card newCard = Card.builder()
                .accountId(1L)
                .cardId(999L)
                .paymentSystem(PaymentSystem.MASTERCARD)
                .status(CardStatus.ACTIVE)
                .build();

        mockMvc.perform(post("/api/cards")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(newCard)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").exists())
                .andExpect(jsonPath("$.cardId").value(999L));

        List<Card> cards = cardRepository.findAll();
        assertThat(cards).hasSize(2);
    }

    @Test
    void updateCard_shouldUpdateExistingCard() throws Exception {
        testCard.setStatus(CardStatus.BLOCKED);

        mockMvc.perform(put("/api/cards/{id}", testCard.getId())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(testCard)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("BLOCKED"));

        Card updated = cardRepository.findById(testCard.getId()).orElseThrow();
        assertThat(updated.getStatus()).isEqualTo(CardStatus.BLOCKED);
    }

    @Test
    void deleteCard_shouldRemoveCard() throws Exception {
        mockMvc.perform(delete("/api/cards/{id}", testCard.getId()))
                .andExpect(status().isNoContent());

        assertThat(cardRepository.findById(testCard.getId())).isEmpty();
    }
}
