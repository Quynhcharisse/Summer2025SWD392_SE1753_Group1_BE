package com.swd392.group1.pes.dto.requests;

import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.FieldDefaults;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@FieldDefaults(level = AccessLevel.PRIVATE)
public class RegisterRequest {
    String email;
    String code;
    String password;
    String confirmPassword;
    String name;
    String phone;
    String gender;
    String identityNumber;
    String address;
    String job;
    String relationshipToChild;
}
