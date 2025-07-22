package com.swd392.group1.pes.models;

import com.swd392.group1.pes.enums.Grade;
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

import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Builder
@Table(name = "`term_item`")
@FieldDefaults(level = AccessLevel.PRIVATE)
public class TermItem {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    Integer id;

    @Column(name = "`max_number_registration`")
    int maxNumberRegistration;

    @Enumerated(EnumType.STRING)
    @Column(length = 50)
    Grade grade;

    @Enumerated(EnumType.STRING)
    @Column(length = 50)
    Status status;

    @Column(name = "`students_per_class`")
    Integer studentsPerClass;

    @Column(name = "`expected_classes`")
    Integer expectedClasses;

    @Column(name = "`current_registered_students`")
    Integer currentRegisteredStudents;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "`term_id`")
    AdmissionTerm admissionTerm;

    @OneToMany(mappedBy = "termItem", fetch = FetchType.LAZY)
    @EqualsAndHashCode.Exclude
    @ToString.Exclude
    List<AdmissionForm> admissionFormList;
}
