package com.pichincha.software.engineer.back.service.dto;

import com.pichincha.software.engineer.back.model.enums.AccountType;
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
public class AccountDto {
    Long id;
    String number;
    AccountType type;
    BigDecimal initialBalance;
    Boolean active;
}
