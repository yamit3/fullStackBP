package com.pichincha.software.engineer.back.service.impl;

import com.pichincha.software.engineer.back.configuration.MovementProperties;
import com.pichincha.software.engineer.back.exception.ApplicationException;
import com.pichincha.software.engineer.back.model.Account;
import com.pichincha.software.engineer.back.model.Client;
import com.pichincha.software.engineer.back.model.Movement;
import com.pichincha.software.engineer.back.model.enums.AccountType;
import com.pichincha.software.engineer.back.model.enums.MovementType;
import com.pichincha.software.engineer.back.repository.AccountRepository;
import com.pichincha.software.engineer.back.repository.MovementRepository;
import com.pichincha.software.engineer.back.service.dto.MovementDto;
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
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class MovementServiceImplTest {

    @Mock
    private MovementRepository movementRepository;

    @Mock
    private AccountRepository accountRepository;

    @Mock
    private MovementProperties movementProperties;

    @InjectMocks
    private MovementServiceImpl movementService;

    @Test
    void createShouldConvertWithdrawToNegativeAndUpdateAccountBalance() {
        Account account = accountWithBalance(1L, new BigDecimal("500.00"));
        MovementDto request = movementDto(MovementType.WITHDRAW, new BigDecimal("200.00"), 1L);

        when(accountRepository.findByIdAndActiveTrue(1L)).thenReturn(Optional.of(account));
        when(movementProperties.getDailyWithdrawLimit()).thenReturn(new BigDecimal("1000.00"));
        when(movementRepository.sumDailyWithdraws(any(Long.class), any())).thenReturn(Optional.of(new BigDecimal("100.00")));
        when(accountRepository.save(any(Account.class))).thenAnswer(invocation -> invocation.getArgument(0));
        when(movementRepository.saveAndFlush(any(Movement.class))).thenAnswer(invocation -> {
            Movement m = invocation.getArgument(0);
            m.setId(10L);
            return m;
        });

        MovementDto result = movementService.create(request);

        assertEquals(new BigDecimal("-200.00"), result.getValue());
        assertEquals(new BigDecimal("300.00"), result.getBalance());

        ArgumentCaptor<Account> accountCaptor = ArgumentCaptor.forClass(Account.class);
        verify(accountRepository).save(accountCaptor.capture());
        assertEquals(new BigDecimal("300.00"), accountCaptor.getValue().getCurrentBalance());
    }

    @Test
    void createShouldThrowWhenWithdrawAndCurrentBalanceIsZero() {
        Account account = accountWithBalance(1L, BigDecimal.ZERO);
        MovementDto request = movementDto(MovementType.WITHDRAW, new BigDecimal("10.00"), 1L);

        when(accountRepository.findByIdAndActiveTrue(1L)).thenReturn(Optional.of(account));

        ApplicationException ex = assertThrows(ApplicationException.class, () -> movementService.create(request));

        assertEquals(HttpStatus.BAD_REQUEST, ex.getStatus());
        assertEquals("No available balance", ex.getMessage());
    }

    @Test
    void createShouldThrowWhenDailyWithdrawLimitExceeded() {
        Account account = accountWithBalance(1L, new BigDecimal("500.00"));
        MovementDto request = movementDto(MovementType.WITHDRAW, new BigDecimal("200.00"), 1L);

        when(accountRepository.findByIdAndActiveTrue(1L)).thenReturn(Optional.of(account));
        when(movementProperties.getDailyWithdrawLimit()).thenReturn(new BigDecimal("1000.00"));
        when(movementRepository.sumDailyWithdraws(any(Long.class), any())).thenReturn(Optional.of(new BigDecimal("900.00")));

        ApplicationException ex = assertThrows(ApplicationException.class, () -> movementService.create(request));

        assertEquals(HttpStatus.BAD_REQUEST, ex.getStatus());
        assertEquals("Daily withdraw limit exceeded. Limit: 1000.00", ex.getMessage());
    }

    @Test
    void createShouldKeepDepositPositiveAndIncreaseBalance() {
        Account account = accountWithBalance(1L, new BigDecimal("500.00"));
        MovementDto request = movementDto(MovementType.DEPOSIT, new BigDecimal("200.00"), 1L);

        when(accountRepository.findByIdAndActiveTrue(1L)).thenReturn(Optional.of(account));
        when(accountRepository.save(any(Account.class))).thenAnswer(invocation -> invocation.getArgument(0));
        when(movementRepository.saveAndFlush(any(Movement.class))).thenAnswer(invocation -> {
            Movement m = invocation.getArgument(0);
            m.setId(20L);
            return m;
        });

        MovementDto result = movementService.create(request);

        assertEquals(new BigDecimal("200.00"), result.getValue());
        assertEquals(new BigDecimal("700.00"), result.getBalance());
    }

    @Test
    void findByIdShouldThrowBadRequestWhenIdIsNull() {
        ApplicationException ex = assertThrows(ApplicationException.class, () -> movementService.findById(null));

        assertEquals(HttpStatus.BAD_REQUEST, ex.getStatus());
        assertEquals("Movement id is required", ex.getMessage());
    }

    private Account accountWithBalance(Long id, BigDecimal currentBalance) {
        Account account = new Account();
        account.setId(id);
        account.setCurrentBalance(currentBalance);
        account.setActive(true);
        account.setType(AccountType.SAVINGS);
        Client client = new Client();
        client.setId(99L);
        account.setClient(client);
        return account;
    }

    private MovementDto movementDto(MovementType type, BigDecimal value, Long accountId) {
        return MovementDto.builder()
                .date(System.currentTimeMillis())
                .type(type)
                .value(value)
                .balance(new BigDecimal("0.00"))
                .accountId(accountId)
                .build();
    }
}

