import entity.Account;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import repository.AccountRepository;
import service.impl.AccountServiceImpl;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;
import static org.assertj.core.api.AssertionsForClassTypes.assertThatThrownBy;
import static org.mockito.Mockito.any;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class AccountServiceImplTest {

    @Mock
    private AccountRepository accountRepository;

    @InjectMocks
    private AccountServiceImpl accountService;

    private Account account;

    @BeforeEach
    void setUp() {
        account = Account.builder()
                .id(1L)
                .clientId(100L)
                .build();
    }

    @Test
    void createAccount_shouldSaveAccountWithClientId() {
        Long clientId = 100L;
        when(accountRepository.save(any(Account.class))).thenReturn(account);

        Account result = accountService.createAccount(clientId);

        ArgumentCaptor<Account> captor = ArgumentCaptor.forClass(Account.class);
        verify(accountRepository, times(1)).save(captor.capture());

        Account saved = captor.getValue();
        assertThat(saved.getClientId()).isEqualTo(clientId);
        assertThat(result).isNotNull();
        assertThat(result.getClientId()).isEqualTo(clientId);
    }

    @Test
    void createAccount_shouldThrowExceptionWhenClientIdIsNull() {
        assertThatThrownBy(() -> accountService.createAccount(null))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("clientId");
        verifyNoInteractions(accountRepository);
    }

    @Test
    void createAccount_shouldCallSaveOnlyOnce() {
        when(accountRepository.save(any(Account.class))).thenReturn(account);

        accountService.createAccount(1L);

        verify(accountRepository, times(1)).save(any(Account.class));
    }
}
