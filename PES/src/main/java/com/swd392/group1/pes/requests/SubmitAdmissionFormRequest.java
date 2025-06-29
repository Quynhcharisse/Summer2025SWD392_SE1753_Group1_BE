package com.swd392.group1.pes.requests;

import lombok.*;
import lombok.experimental.FieldDefaults;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@FieldDefaults(level = AccessLevel.PRIVATE)
public class SubmitAdmissionFormRequest {
    int studentId;
    int termItemId;
    String householdRegistrationAddress;
    String childCharacteristicsFormImg;
    String commitmentImg;
    String note;
}
