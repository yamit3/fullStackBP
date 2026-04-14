package com.pichincha.software.engineer.back.service.dto;

import com.pichincha.software.engineer.back.model.enums.AccountType;
import jakarta.validation.ConstraintViolation;
import jakarta.validation.Validation;
import jakarta.validation.Validator;
import jakarta.validation.ValidatorFactory;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class AccountDtoValidationTest {

    private static ValidatorFactory factory;
    private static Validator validator;

    @BeforeAll
    static void setUpValidator() {
        factory = Validation.buildDefaultValidatorFactory();
        validator = factory.getValidator();
    }

    @AfterAll
    static void closeValidator() {
        factory.close();
    }

    @Test
    void shouldRejectAccountNumberWhenNotOnlyDigitsOrTooShort() {
        AccountDto dto = AccountDto.builder()
                .number("12AB")
                .type(AccountType.SAVINGS)
                .initialBalance(new BigDecimal("100.00"))
                .active(true)
                .build();

        Set<ConstraintViolation<AccountDto>> violations = validator.validate(dto);

        assertTrue(violations.stream().anyMatch(v -> "number".equals(v.getPropertyPath().toString())));
    }

    @Test
    void shouldRejectInitialBalanceWhenZeroOrNegative() {
        AccountDto dto = AccountDto.builder()
                .number("123456")
                .type(AccountType.SAVINGS)
                .initialBalance(BigDecimal.ZERO)
                .active(true)
                .build();

        Set<ConstraintViolation<AccountDto>> violations = validator.validate(dto);

        assertTrue(violations.stream().anyMatch(v -> "initialBalance".equals(v.getPropertyPath().toString())));
    }

    @Test
    void shouldAcceptValidAccountData() {
        AccountDto dto = AccountDto.builder()
                .number("123456")
                .type(AccountType.CHECKING)
                .initialBalance(new BigDecimal("50.00"))
                .active(true)
                .build();

        Set<ConstraintViolation<AccountDto>> violations = validator.validate(dto);

        assertFalse(violations.stream().anyMatch(v -> "number".equals(v.getPropertyPath().toString())));
        assertFalse(violations.stream().anyMatch(v -> "initialBalance".equals(v.getPropertyPath().toString())));
    }
}

