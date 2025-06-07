package com.swd392.group1.pes.requests;

import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.FieldDefaults;

import java.time.LocalDate;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@FieldDefaults(level = AccessLevel.PRIVATE)
public class SubmitAdmissionFormRequest {
    int studentId;
    String householdRegistrationAddress;
    String profileImage;
    String birthCertificateImg;
    String householdRegistrationImg;
    String commitmentImg;
    String note;
}
