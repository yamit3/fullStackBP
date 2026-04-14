package com.pichincha.software.engineer.back.service.dto;

import com.pichincha.software.engineer.back.model.enums.AccountType;
import lombok.*;
import lombok.experimental.FieldDefaults;
import lombok.experimental.SuperBuilder;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotNull;
import java.math.BigDecimal;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@SuperBuilder(toBuilder = true)
@FieldDefaults(level = AccessLevel.PRIVATE)
public class AccountDto {
    Long id;

    String number;

    AccountType type;

    @NotNull(message = "Initial balance is required")
    @DecimalMin(value = "0.00", inclusive = false, message = "Initial balance must be greater than zero")
    BigDecimal initialBalance;

    BigDecimal currentBalance;

    Boolean active;

    Long clientId;
}
