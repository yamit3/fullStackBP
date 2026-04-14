package com.pichincha.software.engineer.back.service.dto.report;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.*;
import lombok.experimental.FieldDefaults;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder(toBuilder = true)
@FieldDefaults(level = AccessLevel.PRIVATE)
public class AccountReportRequestDto {
    @Size(max = 10, min = 10, message = "Identification must be 10 characters long.")
    @Pattern(regexp = "^[0-9]+$",
            message = "Only number for identification")
    String identification;

    @NotNull
    Long startDate;
    @NotNull
    Long endDate;

    Boolean pdf = Boolean.FALSE;
}
