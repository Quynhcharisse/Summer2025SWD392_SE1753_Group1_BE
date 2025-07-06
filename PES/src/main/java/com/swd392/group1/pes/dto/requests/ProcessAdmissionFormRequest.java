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
public class ProcessAdmissionFormRequest {
    int id;
    boolean isApproved; // true = approve, false = reject
    String reason; // Lý do từ chối nếu rejected
}
