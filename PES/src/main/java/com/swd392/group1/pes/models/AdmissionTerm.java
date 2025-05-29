package com.swd392.group1.pes.models;

import com.swd392.group1.pes.enums.Grade;
import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
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
@Table(name = "`admission_term`")
@FieldDefaults(level = AccessLevel.PRIVATE)
public class AdmissionTerm {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    Integer id;

    String name;

    @Column(name = "`start_date`")
    LocalDate startDate;

    @Column(name = "`end_date`")
    LocalDate endDate;

    int year;

    @Column(name = "`max_number_registration`")
    LocalDate maxNumberRegistration;

    @Enumerated(EnumType.STRING)
    Grade grade;

    @OneToMany(mappedBy = "admissionTerm", fetch = FetchType.LAZY)
    @EqualsAndHashCode.Exclude
    @ToString.Exclude
    List<AdmissionForm> admissionFormList;

    @OneToOne(mappedBy = "admissionTerm", fetch = FetchType.LAZY)
    @EqualsAndHashCode.Exclude
    @ToString.Exclude
    AdmissionFee admissionFee;
}
