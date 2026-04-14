package com.pichincha.software.engineer.back.service.dto;

import com.pichincha.software.engineer.back.model.enums.MovementType;
import lombok.*;
import lombok.experimental.FieldDefaults;
import lombok.experimental.SuperBuilder;

import java.math.BigDecimal;
import java.sql.Timestamp;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@SuperBuilder(toBuilder = true)
@FieldDefaults(level = AccessLevel.PRIVATE)
public class MovementDto {
    Long id;
    Timestamp date;
    MovementType type;
    BigDecimal value;
    BigDecimal balance;
}
