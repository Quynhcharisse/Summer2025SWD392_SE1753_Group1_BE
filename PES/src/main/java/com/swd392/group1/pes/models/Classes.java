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
@Table(name = "`classes`")
@FieldDefaults(level = AccessLevel.PRIVATE)
public class Classes {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    Integer id;

    String name;

    @Column(name = "`number_student`")
    String numberStudent;

    @Column(name = "`room_number`")
    String roomNumber;

    @Column(name = "`start_date`")
    LocalDate startDate;

    @Column(name = "`end_date`")
    LocalDate endDate;

    @Column(name = "`academic_year`")
    int academicYear;

    @Enumerated(EnumType.STRING)
    Grade grade;

    String status;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "`syllabus_id`")
    Syllabus syllabus;

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "`teacher_id`")
    Account teacher;

    @OneToMany(mappedBy = "classes", fetch = FetchType.LAZY, cascade = CascadeType.ALL)
    @EqualsAndHashCode.Exclude
    @ToString.Exclude
    List<StudentClass> studentClassList;

    @OneToMany(mappedBy = "classes", fetch = FetchType.LAZY, cascade = CascadeType.ALL)
    @EqualsAndHashCode.Exclude
    @ToString.Exclude
    List<Schedule> scheduleList;
}
