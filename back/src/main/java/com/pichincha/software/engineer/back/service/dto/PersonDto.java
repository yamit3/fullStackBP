package com.pichincha.software.engineer.back.service.dto;

import com.pichincha.software.engineer.back.model.enums.Gender;
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
  String name;
  Gender gender;
  Integer age;
  String identification;
  String address;
  String phone;
}
