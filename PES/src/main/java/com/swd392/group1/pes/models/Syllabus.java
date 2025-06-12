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
@Entity
@Builder
@Table(name = "`syllabus`")
@FieldDefaults(level = AccessLevel.PRIVATE)
public class Syllabus {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    Integer id;

    String subject;

    String description;

    boolean isAssigned;

    @Column(name = "`max_number_of_week`")
    int maxNumberOfWeek;

    int maxHoursOfSyllabus;

    @Enumerated(EnumType.STRING)
    Grade grade;

    @OneToMany(mappedBy = "syllabus", fetch = FetchType.LAZY) //ko dùng cascade
    @EqualsAndHashCode.Exclude
    @ToString.Exclude
    List<Classes> classesList;

    @OneToMany(mappedBy = "syllabus", fetch = FetchType.EAGER, cascade = CascadeType.ALL, orphanRemoval = true)
    @EqualsAndHashCode.Exclude
    @ToString.Exclude
    List<SyllabusLesson> syllabusLessonList;

    LocalDateTime createdAt;

}
