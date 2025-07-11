package com.swd392.group1.pes.dto.requests;

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
public class TransactionQueryRequest {
    private String status;
    private String bankCode;
    private String keyword;
    private LocalDate fromDate;
    private LocalDate toDate;
}
