package com.pichincha.software.engineer.back.service.dto;

import com.pichincha.software.engineer.back.model.enums.MovementType;
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

class MovementDtoValidationTest {

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
    void shouldRejectWhenDateIsNull() {
        MovementDto dto = validDto();
        dto.setDate(null);

        Set<ConstraintViolation<MovementDto>> violations = validator.validate(dto);

        assertTrue(violations.stream().anyMatch(v -> "date".equals(v.getPropertyPath().toString())));
    }

    @Test
    void shouldRejectWhenValueIsNull() {
        MovementDto dto = validDto();
        dto.setValue(null);

        Set<ConstraintViolation<MovementDto>> violations = validator.validate(dto);

        assertTrue(violations.stream().anyMatch(v -> "value".equals(v.getPropertyPath().toString())));
    }

    @Test
    void shouldRejectWhenValueIsZero() {
        MovementDto dto = validDto();
        dto.setValue(BigDecimal.ZERO);

        Set<ConstraintViolation<MovementDto>> violations = validator.validate(dto);

        assertTrue(violations.stream().anyMatch(v -> "value".equals(v.getPropertyPath().toString())));
    }

    @Test
    void shouldAcceptValidMovementData() {
        MovementDto dto = validDto();

        Set<ConstraintViolation<MovementDto>> violations = validator.validate(dto);

        assertFalse(violations.stream().anyMatch(v -> "date".equals(v.getPropertyPath().toString())));
        assertFalse(violations.stream().anyMatch(v -> "value".equals(v.getPropertyPath().toString())));
    }

    private MovementDto validDto() {
        return MovementDto.builder()
                .date(System.currentTimeMillis())
                .type(MovementType.DEPOSIT)
                .value(new BigDecimal("150.00"))
                .balance(new BigDecimal("150.00"))
                .build();
    }
}

