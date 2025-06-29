package com.swd392.group1.pes.models;

import com.swd392.group1.pes.enums.Status;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.ToString;
import lombok.experimental.FieldDefaults;

import java.time.LocalDate;
import java.util.List;

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
    LocalDate submittedDate;

    @Column(name = "`household_registration_address`")
    String householdRegistrationAddress;

    @Column(name = "`child_characteristics_form_img`")
    String childCharacteristicsFormImg;

    @Column(name = "`commitment_img`")
    String commitmentImg ;

    String note;

    @Enumerated(EnumType.STRING)
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

    @OneToOne(mappedBy = "admissionForm", fetch = FetchType.EAGER, cascade = CascadeType.ALL) // ko dùng cascade
    @EqualsAndHashCode.Exclude
    @ToString.Exclude
    Transaction transaction;

}
