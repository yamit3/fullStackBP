package com.pichincha.software.engineer.back.service.dto.report;

import lombok.*;
import lombok.experimental.FieldDefaults;
import lombok.experimental.SuperBuilder;

import java.math.BigDecimal;
import java.util.List;

import static lombok.AccessLevel.PUBLIC;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@SuperBuilder
@FieldDefaults(level = AccessLevel.PRIVATE)
public class AccountReportResponseDto {
    String pdf;
    List<Account> accounts;

    @Data
    @Builder
    @FieldDefaults(level = PUBLIC)
    public static class Account {
        String number;
        BigDecimal balance;
        BigDecimal credits;
        BigDecimal withdraws;
    }

}
