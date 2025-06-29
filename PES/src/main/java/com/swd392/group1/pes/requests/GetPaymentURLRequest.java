package com.swd392.group1.pes.requests;

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
public class GetPaymentURLRequest {
    long amount;
    String paymentInfo; //nội dung chuyển khoản
    int formId; // ID của AdmissionForm liên quan
    String txnRef;  // Mã giao dịch đã được tạo và lưu vào DB
}
