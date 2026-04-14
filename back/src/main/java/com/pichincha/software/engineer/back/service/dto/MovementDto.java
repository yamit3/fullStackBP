package com.pichincha.software.engineer.back.service.dto;

import com.pichincha.software.engineer.back.model.enums.MovementType;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotNull;
import lombok.*;
import lombok.experimental.FieldDefaults;
import lombok.experimental.SuperBuilder;

import java.math.BigDecimal;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@SuperBuilder(toBuilder = true)
@FieldDefaults(level = AccessLevel.PRIVATE)
public class MovementDto {
    Long id;

    @NotNull(message = "Date must not be null")
    Long date;

    MovementType type;

    @NotNull(message = "Value must not be null")
    @DecimalMin(value = "0.00", inclusive = false, message = "Value must be different than zero")
    BigDecimal value;

    BigDecimal balance;

    Long accountId;
}
