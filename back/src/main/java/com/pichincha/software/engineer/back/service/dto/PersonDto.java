package com.pichincha.software.engineer.back.service.dto;

import com.pichincha.software.engineer.back.model.enums.Gender;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Size;
import lombok.*;
import lombok.experimental.FieldDefaults;
import lombok.experimental.SuperBuilder;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@SuperBuilder(toBuilder = true)
@FieldDefaults(level = AccessLevel.PRIVATE)
public class PersonDto {
    Long id;

    @NotBlank(message = "Name can not be empty")
    @Size(min = 2, max = 100, message = "Name must have 2 characters minimum up to 100")
    @Pattern(regexp = "^[a-zA-ZáéíóúÁÉÍÓÚñÑ ]+$",
            message = "Name must only contain letters and spaces")
    String name;
    Gender gender;

    @Positive(message = "Age must be positive")
    @Max(value = 119, message = "Age must be lower than 120")
    Integer age;

    @Size(max = 10, min = 10, message = "Identification must be 10 characters long.")
    @Pattern(regexp = "^[0-9]+$",
            message = "Only number for identification")
    String identification;


    @NotBlank(message = "Address can not be empty")
    @Size(max = 255, message = "Address must not exceed 255 characters")
    String address;

    @Size(max = 15, message = "Phone must not exceed 15 characters")
    String phone;
}
