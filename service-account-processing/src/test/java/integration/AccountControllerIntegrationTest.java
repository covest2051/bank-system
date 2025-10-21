import com.fasterxml.jackson.databind.ObjectMapper;
import entity.Account;
import entity.AccountStatus;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import repository.AccountRepository;

import java.math.BigDecimal;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
class AccountControllerIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private AccountRepository accountRepository;

    private Account testAccount;

    @BeforeEach
    void setUp() {
        accountRepository.deleteAll();

        testAccount = Account.builder()
                .balance(BigDecimal.valueOf(1000))
                .status(AccountStatus.ACTIVE)
                .isRecalc(false)
                .build();

        testAccount = accountRepository.save(testAccount);
    }

    @Test
    void getAccountById_shouldReturnAccount() throws Exception {
        mockMvc.perform(get("/api/accounts/{id}", testAccount.getId())
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(testAccount.getId()))
                .andExpect(jsonPath("$.balance").value(1000))
                .andExpect(jsonPath("$.status").value("ACTIVE"));
    }

    @Test
    void getAccountById_shouldReturnNotFound_whenAccountMissing() throws Exception {
        mockMvc.perform(get("/api/accounts/{id}", 999L)
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isNotFound());
    }

    @Test
    void createAccount_shouldSaveNewAccount() throws Exception {
        Account newAccount = Account.builder()
                .balance(BigDecimal.valueOf(500))
                .status(AccountStatus.ACTIVE)
                .isRecalc(false)
                .build();

        mockMvc.perform(post("/api/accounts")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(newAccount)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").exists())
                .andExpect(jsonPath("$.balance").value(500));

        assertThat(accountRepository.findAll()).hasSize(2);
    }

    @Test
    void updateAccount_shouldUpdateExistingAccount() throws Exception {
        testAccount.setBalance(BigDecimal.valueOf(1500));

        mockMvc.perform(put("/api/accounts/{id}", testAccount.getId())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(testAccount)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.balance").value(1500));

        Account updated = accountRepository.findById(testAccount.getId()).orElseThrow();
        assertThat(updated.getBalance()).isEqualByComparingTo(BigDecimal.valueOf(1500));
    }

    @Test
    void deleteAccount_shouldRemoveAccount() throws Exception {
        mockMvc.perform(delete("/api/accounts/{id}", testAccount.getId()))
                .andExpect(status().isNoContent());

        assertThat(accountRepository.findById(testAccount.getId())).isEmpty();
    }
}
