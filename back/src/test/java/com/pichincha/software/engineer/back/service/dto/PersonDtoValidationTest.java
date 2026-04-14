package com.pichincha.software.engineer.back.service.dto;

import com.pichincha.software.engineer.back.model.enums.Gender;
import jakarta.validation.ConstraintViolation;
import jakarta.validation.Validation;
import jakarta.validation.Validator;
import jakarta.validation.ValidatorFactory;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class PersonDtoValidationTest {

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
    void shouldRejectAgeWhenNotPositiveOrTooHigh() {
        PersonDto dto = validDto();
        dto.setAge(120);

        Set<ConstraintViolation<PersonDto>> violations = validator.validate(dto);

        assertTrue(violations.stream().anyMatch(v -> "age".equals(v.getPropertyPath().toString())));
    }

    @Test
    void shouldRejectBlankOrTooLongAddress() {
        PersonDto blankAddressDto = validDto();
        blankAddressDto.setAddress(" ");

        Set<ConstraintViolation<PersonDto>> blankViolations = validator.validate(blankAddressDto);

        assertTrue(blankViolations.stream().anyMatch(v -> "address".equals(v.getPropertyPath().toString())));

        PersonDto longAddressDto = validDto();
        longAddressDto.setAddress("A".repeat(256));

        Set<ConstraintViolation<PersonDto>> longViolations = validator.validate(longAddressDto);

        assertTrue(longViolations.stream().anyMatch(v -> "address".equals(v.getPropertyPath().toString())));
    }

    @Test
    void shouldAcceptValidAgeAndAddress() {
        PersonDto dto = validDto();

        Set<ConstraintViolation<PersonDto>> violations = validator.validate(dto);

        assertFalse(violations.stream().anyMatch(v -> "age".equals(v.getPropertyPath().toString())));
        assertFalse(violations.stream().anyMatch(v -> "address".equals(v.getPropertyPath().toString())));
    }

    private PersonDto validDto() {
        return PersonDto.builder()
                .name("Juan Perez")
                .gender(Gender.MALE)
                .age(30)
                .identification("1234567890")
                .address("Av. Principal 123")
                .phone("0999999999")
                .build();
    }
}
