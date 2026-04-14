package com.pichincha.software.engineer.back.service.dto;

import com.pichincha.software.engineer.back.model.enums.AccountType;
import lombok.*;
import lombok.experimental.FieldDefaults;
import lombok.experimental.SuperBuilder;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import java.math.BigDecimal;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@SuperBuilder(toBuilder = true)
@FieldDefaults(level = AccessLevel.PRIVATE)
public class AccountDto {
    Long id;

    @NotBlank(message = "Account number is required")
    @Pattern(regexp = "\\d{6,}", message = "Account number must contain only digits and be at least 6 characters")
    String number;

    AccountType type;

    @NotNull(message = "Initial balance is required")
    @DecimalMin(value = "0.00", inclusive = false, message = "Initial balance must be greater than zero")
    BigDecimal initialBalance;

    Boolean active;

    Long clientId;
}
