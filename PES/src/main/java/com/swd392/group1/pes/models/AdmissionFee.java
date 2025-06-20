package com.swd392.group1.pes.models;

import com.swd392.group1.pes.enums.Grade;
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


@Data
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Builder
@Table(name = "`admission_fee`")
@FieldDefaults(level = AccessLevel.PRIVATE)
public class AdmissionFee {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    Integer id;

    @Column(name = "`reservation_fee`")
    double reservationFee;

    @Column(name = "`service_fee`")
    double serviceFee;

    @Column(name = "`uniform_fee`")
    double uniformFee;

    @Column(name = "`learning_material_fee`")
    double learningMaterialFee;

    @Column(name = "`facility_fee`")
    double facilityFee;

    @Enumerated(EnumType.STRING)
    Grade grade;

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "`term_id`")
    AdmissionTerm admissionTerm;

}
