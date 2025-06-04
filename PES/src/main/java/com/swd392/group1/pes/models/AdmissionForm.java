package com.swd392.group1.pes.models;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OneToMany;
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

    @Column(name = "`child_name`")
    String childName;

    @Column(name = "`child_gender`")
    String childGender;

    @Column(name = "`date_of_birth`")
    LocalDate dateOfBirth;

    @Column(name = "`place_of_birth`")
    String placeOfBirth;

    @Column(name = "`profile_image`")
    String profileImage;

    @Column(name = "`household_registration_address`")
    String householdRegistrationAddress;

    @Column(name = "`birth_certificate_img`")
    String birthCertificateImg;

    @Column(name = "`household_registration_img`")
    String householdRegistrationImg ;

    @Column(name = "`commitment_img`")
    String commitmentImg ;

    @Column(name = "`cancel_reason`")
    String cancelReason;

    @Column(name = "`submitted_date`")
    LocalDate submittedDate;

    String note;

    String status;

    @OneToOne(mappedBy = "admissionForm", fetch = FetchType.EAGER)
    @EqualsAndHashCode.Exclude
    @ToString.Exclude
    Student student;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "`parent_id`")
    Parent parent;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "`term_id`")
    AdmissionTerm admissionTerm;

    @OneToMany(mappedBy = "admissionForm", fetch = FetchType.LAZY) // ko dùng cascade
    @EqualsAndHashCode.Exclude
    @ToString.Exclude
    List<Transaction> transactionList;
}
