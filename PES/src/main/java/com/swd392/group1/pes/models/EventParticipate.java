package com.swd392.group1.pes.models;

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
@Entity
@Builder
@Table(name = "`event_participate`")
@FieldDefaults(level = AccessLevel.PRIVATE)
public class EventParticipate {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    Integer id;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "`student_id`")
    Student student;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "`even_id`")
    Event event;

    LocalDateTime registeredAt;

}
