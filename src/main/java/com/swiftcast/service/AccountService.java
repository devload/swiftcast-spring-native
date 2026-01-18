package com.swiftcast.service;

import com.swiftcast.model.Account;
import com.swiftcast.repository.AccountRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
@Slf4j
public class AccountService {

    private final AccountRepository accountRepository;

    @Transactional
    public Account createAccount(String name, String baseUrl, String apiKey) {
        Account account = new Account(name, baseUrl, apiKey);

        // 첫 번째 계정이면 자동 활성화
        long count = accountRepository.count();
        if (count == 0) {
            account.setIsActive(true);
        }

        Account saved = accountRepository.save(account);
        log.info("Account created: {}", saved.getName());
        return saved;
    }

    public List<Account> getAllAccounts() {
        return accountRepository.findAll();
    }

    public Optional<Account> getActiveAccount() {
        return accountRepository.findByIsActive(true);
    }

    @Transactional
    public void switchAccount(String accountId) {
        // 모든 계정 비활성화
        List<Account> allAccounts = accountRepository.findAll();
        allAccounts.forEach(acc -> acc.setIsActive(false));
        accountRepository.saveAll(allAccounts);

        // 선택한 계정만 활성화
        accountRepository.findById(accountId).ifPresent(account -> {
            account.setIsActive(true);
            accountRepository.save(account);
            log.info("Switched to account: {}", account.getName());
        });
    }

    @Transactional
    public void deleteAccount(String accountId) {
        accountRepository.deleteById(accountId);
        log.info("Account deleted: {}", accountId);
    }
}
