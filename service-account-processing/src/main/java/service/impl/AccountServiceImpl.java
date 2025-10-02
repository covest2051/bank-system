package service.impl;

import entity.Account;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import repository.AccountRepository;
import service.AccountService;

@Service
@RequiredArgsConstructor
@Slf4j
public class AccountServiceImpl implements AccountService {
    private final AccountRepository accountRepository;

    @Override
    @Transactional
    public Account createAccount(Long clientId) {
        Account account = Account.builder()
                .clientId(clientId)
                .build();

        Account saved = accountRepository.save(account);

        log.info("Created account {} for clientId={}", account.getId(), saved.getClientId());
        return saved;
    }
}
