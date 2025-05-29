package com.swd392.group1.pes.models;

import jakarta.persistence.Entity;
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

import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Builder
@Table(name = "`lesson`")
@FieldDefaults(level = AccessLevel.PRIVATE)
public class Lesson {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    Integer id;

    String topic;

    String description;

    @OneToMany(mappedBy = "lesson", fetch = FetchType.LAZY )
    @EqualsAndHashCode.Exclude
    @ToString.Exclude
    List<SyllabusLesson> syllabusLessonList;
}
