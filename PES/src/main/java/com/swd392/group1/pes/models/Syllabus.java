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
@Builder
@Entity
@Table(name = "`syllabus`")
@FieldDefaults(level = AccessLevel.PRIVATE)
public class Syllabus {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    Integer id;

    @Column(length = 50)
    String subject;

    @Column(length = 2000)
    String description;

    @Column(name = "`max_number_of_week`")
    int numberOfWeek;

    int hoursOfSyllabus;

    @Enumerated(EnumType.STRING)
    @Column(length = 50)
    Grade grade;

    @OneToMany(mappedBy = "syllabus", fetch = FetchType.LAZY) //ko dùng cascade
    @EqualsAndHashCode.Exclude
    @ToString.Exclude
    List<Classes> classesList;

    @OneToMany(mappedBy = "syllabus", fetch = FetchType.LAZY, cascade = CascadeType.ALL, orphanRemoval = true)
    @EqualsAndHashCode.Exclude
    @ToString.Exclude
    List<SyllabusLesson> syllabusLessonList;

    LocalDateTime createdAt;

    @Column
    boolean assignedToClasses;

}
