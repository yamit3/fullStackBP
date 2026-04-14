package com.pichincha.software.engineer.back.service.impl;

import com.pichincha.software.engineer.back.exception.ApplicationException;
import com.pichincha.software.engineer.back.model.Account;
import com.pichincha.software.engineer.back.model.Client;
import com.pichincha.software.engineer.back.repository.AccountRepository;
import com.pichincha.software.engineer.back.repository.ClientRepository;
import com.pichincha.software.engineer.back.service.AccountService;
import com.pichincha.software.engineer.back.service.dto.AccountDto;
import lombok.RequiredArgsConstructor;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class AccountServiceImpl implements AccountService {

    private final AccountRepository accountRepository;
    private final ClientRepository clientRepository;

    @Override
    @Transactional
    public AccountDto create(AccountDto accountDto) {
        if (accountDto == null) {
            throw new ApplicationException("Account payload is required", HttpStatus.BAD_REQUEST);
        }

        try {
            Account account = toEntity(accountDto);
            account.setId(null);
            account.setNumber(nextAccountNumber());
            return toDto(accountRepository.save(account));
        } catch (DataIntegrityViolationException ex) {
            throw new ApplicationException("Account data violates constraints", HttpStatus.CONFLICT);
        } catch (ApplicationException ex) {
            throw ex;
        } catch (Exception ex) {
            throw new ApplicationException("Unexpected error while creating account", HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    @Override
    public AccountDto findById(String number) {
        try {
            return toDto(getAccountByNumberOrThrow(number));
        } catch (ApplicationException ex) {
            throw ex;
        } catch (Exception ex) {
            throw new ApplicationException("Unexpected error while searching account", HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    @Override
    public List<AccountDto> findAll() {
        try {
            return accountRepository.findAllByActiveTrue()
                    .stream()
                    .map(this::toDto)
                    .toList();
        } catch (Exception ex) {
            throw new ApplicationException("Unexpected error while listing accounts", HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    @Override
    @Transactional
    public AccountDto update(Long id, AccountDto accountDto) {
        if (accountDto == null) {
            throw new ApplicationException("Account payload is required", HttpStatus.BAD_REQUEST);
        }

        try {
            Account existingAccount = getAccountOrThrow(id);
            mergeAccount(existingAccount, accountDto);
            return toDto(accountRepository.save(existingAccount));
        } catch (DataIntegrityViolationException ex) {
            throw new ApplicationException("Account data violates constraints", HttpStatus.CONFLICT);
        } catch (ApplicationException ex) {
            throw ex;
        } catch (Exception ex) {
            throw new ApplicationException("Unexpected error while updating account", HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    @Override
    @Transactional
    public void delete(Long id) {
        try {
            Account existingAccount = getAccountOrThrow(id);
            existingAccount.setActive(false);
            accountRepository.save(existingAccount);
        } catch (ApplicationException ex) {
            throw ex;
        } catch (Exception ex) {
            throw new ApplicationException("Unexpected error while deleting account", HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    private Account getAccountOrThrow(Long id) {
        if (id == null) {
            throw new ApplicationException("Account id is required", HttpStatus.BAD_REQUEST);
        }

        return accountRepository.findByIdAndActiveTrue(id)
                .orElseThrow(() -> new ApplicationException("Account not found with id: " + id, HttpStatus.NOT_FOUND));
    }

    private Account getAccountByNumberOrThrow(String number) {
        if (number == null || number.isBlank()) {
            throw new ApplicationException("Account number is required", HttpStatus.BAD_REQUEST);
        }

        return accountRepository.findByNumberAndActiveTrue(number)
                .orElseThrow(() -> new ApplicationException("Account not found with number: " + number, HttpStatus.NOT_FOUND));
    }

    private Client getClientOrThrow(Long clientId) {
        if (clientId == null) {
            throw new ApplicationException("Client id is required", HttpStatus.BAD_REQUEST);
        }

        return clientRepository.findByIdAndActiveTrue(clientId)
                .orElseThrow(() -> new ApplicationException("Client not found with id: " + clientId, HttpStatus.NOT_FOUND));
    }

    private Account toEntity(AccountDto accountDto) {
        Account account = new Account();
        // number is assigned by service during create
        account.setType(accountDto.getType());
        account.setInitialBalance(accountDto.getInitialBalance());
        account.setActive(accountDto.getActive());
        account.setClient(getClientOrThrow(accountDto.getClientId()));
        return account;
    }

    private void mergeAccount(Account account, AccountDto accountDto) {
        // account number is immutable after creation
        account.setType(accountDto.getType());
        account.setInitialBalance(accountDto.getInitialBalance());
        account.setActive(accountDto.getActive());
        if (accountDto.getClientId() != null) {
            account.setClient(getClientOrThrow(accountDto.getClientId()));
        }
    }

    private AccountDto toDto(Account account) {
        return AccountDto.builder()
                .id(account.getId())
                .number(account.getNumber())
                .type(account.getType())
                .initialBalance(account.getInitialBalance())
                .active(account.getActive())
                .clientId(account.getClient() != null ? account.getClient().getId() : null)
                .build();
    }

    private String nextAccountNumber() {
        Long currentMax = accountRepository.findMaxNumber();
        long next = (currentMax == null ? 100000L : currentMax) + 1L;
        return String.valueOf(next);
    }
}
