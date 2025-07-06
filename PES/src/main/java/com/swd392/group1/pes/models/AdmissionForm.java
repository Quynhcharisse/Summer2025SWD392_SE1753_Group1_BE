package com.swd392.group1.pes.models;

import com.swd392.group1.pes.enums.Status;
import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Convert;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OneToOne;
import jakarta.persistence.Table;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.ToString;
import lombok.experimental.FieldDefaults;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Builder
@Table(name = "`admission_form`")
@FieldDefaults(level = AccessLevel.PRIVATE)
public class AdmissionForm {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    Integer id;

    @Column(name = "`cancel_reason`")
    String cancelReason;

    @Column(name = "`submitted_date`")
    LocalDateTime submittedDate;

    @Column(name = "`approved_date`")
    LocalDateTime approvedDate; // Thời gian form được Admission Manager duyệt

    @Column(name = "`payment_expiry_date`")
    LocalDateTime paymentExpiryDate; // Thời gian hết hạn để phụ huynh thanh toán

    @Column(name = "`household_registration_address`")
    String householdRegistrationAddress;

    @Column(name = "`child_characteristics_form_img`")
    String childCharacteristicsFormImg;

    @Column(name = "`commitment_img`")
    String commitmentImg ;

    @Column(length = 50)
    String note;

    @Enumerated(EnumType.STRING)
    @Column(length = 50)
    Status status;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "`student_id`")
    Student student;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "`parent_id`")
    Parent parent;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "`term_item_id`")
    TermItem termItem;

    @OneToOne(mappedBy = "admissionForm", fetch = FetchType.EAGER, cascade = CascadeType.ALL)
    @EqualsAndHashCode.Exclude
    @ToString.Exclude
    Transaction transaction;
}
