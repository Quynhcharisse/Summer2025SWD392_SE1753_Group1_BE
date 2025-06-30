package com.swd392.group1.pes.models;

import com.swd392.group1.pes.enums.Status;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.OneToOne;
import jakarta.persistence.Table;
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
@Entity
@Builder
@Table(name = "`transaction`")
@FieldDefaults(level = AccessLevel.PRIVATE)
public class Transaction {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    Integer id;

    long amount;

    String description;

    @Column(name = "`payment_date`")
    LocalDate paymentDate;

    @Column(name = "`receipt_number`")
    String receiptNumber; // Số biên lai

    @Column(name = "`txn_ref`")
    String txnRef; //Thêm trường txnRef để lưu mã giao dịch VNPay

    @Enumerated(EnumType.STRING)
    Status status;

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "`form_id`")
    AdmissionForm admissionForm;

}
