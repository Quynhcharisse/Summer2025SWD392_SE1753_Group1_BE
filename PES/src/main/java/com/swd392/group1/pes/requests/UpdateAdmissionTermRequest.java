package com.swd392.group1.pes.requests;

import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.FieldDefaults;

import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@FieldDefaults(level = AccessLevel.PRIVATE)
public class UpdateAdmissionTermRequest {
    int id;
    LocalDateTime startDate;
    LocalDateTime endDate;
    int maxNumberRegistration;
    String grade;
    double reservationFee;
    double serviceFee;
    double uniformFee;
    double learningMaterialFee;
    double facilityFee;
}
