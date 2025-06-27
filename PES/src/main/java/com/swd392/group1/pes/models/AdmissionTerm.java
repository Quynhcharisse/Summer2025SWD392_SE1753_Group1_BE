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
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OneToMany;
import jakarta.persistence.Table;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.ToString;
import lombok.experimental.FieldDefaults;

import java.time.LocalDateTime;
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
    LocalDateTime startDate;

    @Column(name = "`end_date`")
    LocalDateTime endDate;

    Integer year;

    @Column(name = "`students_per_class`")
    Integer studentsPerClass; // số học sinh tự quy định = 20 người

    @Column(name = "`expected_classes`")
    Integer expectedClasses; // Số lớp dự kiến

    @Column(name = "`max_number_registration`")
    int maxNumberRegistration; // tự gán tự động

    @Enumerated(EnumType.STRING)
    Grade grade;

    String status;

    @OneToMany(mappedBy = "admissionTerm", fetch = FetchType.LAZY)
    @EqualsAndHashCode.Exclude
    @ToString.Exclude
    List<AdmissionForm> admissionFormList;

    //join chính nó
    @ManyToOne
    @JoinColumn(name = "parent_term_id")
    @EqualsAndHashCode.Exclude
    @ToString.Exclude
    AdmissionTerm parentTerm;
}
