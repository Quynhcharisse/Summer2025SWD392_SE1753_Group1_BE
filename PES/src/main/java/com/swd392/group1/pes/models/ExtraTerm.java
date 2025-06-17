package com.swd392.group1.pes.models;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.FieldDefaults;

import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@FieldDefaults(level = AccessLevel.PRIVATE)
@Entity
@Table(name = "`extra_term`")
public class ExtraTerm {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    Integer id;

    String name;

    @Column(name = "`start_date`")
    LocalDateTime startDate; //theo ngày + h

    @Column(name = "`end_date`")
    LocalDateTime endDate; //theo ngày + h

    @Column(name = "`max_number_registration`")
    int maxNumberRegistration;

    String reason;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "`admission_term_id`")
    AdmissionTerm admissionTerm;
}
