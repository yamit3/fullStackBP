package com.pichincha.software.engineer.back.service.impl;

import com.pichincha.software.engineer.back.exception.ApplicationException;
import com.pichincha.software.engineer.back.model.Account;
import com.pichincha.software.engineer.back.model.Client;
import com.pichincha.software.engineer.back.model.enums.AccountType;
import com.pichincha.software.engineer.back.repository.AccountRepository;
import com.pichincha.software.engineer.back.repository.ClientRepository;
import com.pichincha.software.engineer.back.service.dto.AccountDto;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;

import java.math.BigDecimal;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class AccountServiceImplTest {

    @Mock
    private AccountRepository accountRepository;

    @Mock
    private ClientRepository clientRepository;

    @InjectMocks
    private AccountServiceImpl accountService;

    @Test
    void createShouldAssignNextNumberAndCurrentBalance() {
        AccountDto input = AccountDto.builder()
                .type(AccountType.CHECKING)
                .initialBalance(new BigDecimal("100.00"))
                .active(true)
                .clientId(10L)
                .build();

        Client client = new Client();
        client.setId(10L);
        client.setActive(true);

        when(clientRepository.findByIdAndActiveTrue(10L)).thenReturn(Optional.of(client));
        when(accountRepository.findMaxNumber()).thenReturn(100050L);
        when(accountRepository.saveAndFlush(any(Account.class))).thenAnswer(invocation -> {
            Account acc = invocation.getArgument(0);
            acc.setId(1L);
            return acc;
        });

        AccountDto result = accountService.create(input);

        assertEquals("100051", result.getNumber());
        assertEquals(new BigDecimal("100.00"), result.getCurrentBalance());
        assertEquals(1L, result.getId());

        ArgumentCaptor<Account> captor = ArgumentCaptor.forClass(Account.class);
        verify(accountRepository).saveAndFlush(captor.capture());
        assertEquals(new BigDecimal("100.00"), captor.getValue().getCurrentBalance());
    }

    @Test
    void createShouldThrowBadRequestWhenPayloadIsNull() {
        ApplicationException ex = assertThrows(ApplicationException.class, () -> accountService.create(null));

        assertEquals(HttpStatus.BAD_REQUEST, ex.getStatus());
        assertEquals("Account payload is required", ex.getMessage());
    }

    @Test
    void createShouldThrowNotFoundWhenClientDoesNotExist() {
        AccountDto input = AccountDto.builder()
                .type(AccountType.SAVINGS)
                .initialBalance(new BigDecimal("50.00"))
                .active(true)
                .clientId(99L)
                .build();

        when(clientRepository.findByIdAndActiveTrue(99L)).thenReturn(Optional.empty());

        ApplicationException ex = assertThrows(ApplicationException.class, () -> accountService.create(input));

        assertEquals(HttpStatus.NOT_FOUND, ex.getStatus());
        assertTrue(ex.getMessage().contains("Client not found with id: 99"));
    }

    @Test
    void findByIdShouldThrowBadRequestWhenNumberIsBlank() {
        ApplicationException ex = assertThrows(ApplicationException.class, () -> accountService.findById("   "));

        assertEquals(HttpStatus.BAD_REQUEST, ex.getStatus());
        assertEquals("Account number is required", ex.getMessage());
    }

    @Test
    void updateShouldMergeDataAndPersist() {
        Account existing = new Account();
        existing.setId(5L);
        existing.setNumber("100099");
        existing.setType(AccountType.CHECKING);
        existing.setInitialBalance(new BigDecimal("80.00"));
        existing.setCurrentBalance(new BigDecimal("80.00"));
        existing.setActive(true);

        Client oldClient = new Client();
        oldClient.setId(1L);
        existing.setClient(oldClient);

        Client newClient = new Client();
        newClient.setId(2L);

        AccountDto input = AccountDto.builder()
                .type(AccountType.SAVINGS)
                .initialBalance(new BigDecimal("120.00"))
                .active(false)
                .clientId(2L)
                .build();

        when(accountRepository.findByIdAndActiveTrue(5L)).thenReturn(Optional.of(existing));
        when(clientRepository.findByIdAndActiveTrue(2L)).thenReturn(Optional.of(newClient));
        when(accountRepository.save(any(Account.class))).thenAnswer(invocation -> invocation.getArgument(0));

        AccountDto result = accountService.update(5L, input);

        assertEquals(AccountType.SAVINGS, result.getType());
        assertEquals(new BigDecimal("120.00"), result.getInitialBalance());
        assertEquals(false, result.getActive());
        assertEquals(2L, result.getClientId());
        assertEquals("100099", result.getNumber());
    }

    @Test
    void deleteShouldSoftDeleteAccount() {
        Account existing = new Account();
        existing.setId(7L);
        existing.setActive(true);

        when(accountRepository.findByIdAndActiveTrue(7L)).thenReturn(Optional.of(existing));
        when(accountRepository.save(any(Account.class))).thenAnswer(invocation -> invocation.getArgument(0));

        accountService.delete(7L);

        ArgumentCaptor<Account> captor = ArgumentCaptor.forClass(Account.class);
        verify(accountRepository).save(captor.capture());
        assertEquals(false, captor.getValue().getActive());
    }
}

