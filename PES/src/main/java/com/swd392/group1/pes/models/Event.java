package com.swd392.group1.pes.models;

import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.OneToMany;
import jakarta.persistence.Table;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.ToString;
import lombok.experimental.FieldDefaults;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "`event`")
@FieldDefaults(level = AccessLevel.PRIVATE)
public class Event {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    Integer id;

    String name;

    LocalDate date;

    @Column(name = "`start_time`")
    LocalDateTime startTime;

    @Column(name = "`end_time`")
    LocalDateTime endTime;

    String location;

    String description;

    @Column(name = "`created_by`")
    String createdBy;

    @Column(name = "`created_at`")
    LocalDate createdAt;

    String status;

    @Column(name = "`registration_deadline`")
    String registrationDeadline;

    @Column(name = "`attachment_img`")
    String attachmentImg;

    @Column(name = "`host_name`")
    String hostName;

    @OneToMany(mappedBy = "event", fetch = FetchType.LAZY) //ko dùng cascade
    @EqualsAndHashCode.Exclude
    @ToString.Exclude
    List<TeacherEvent> teacherEventList;

    @OneToMany(mappedBy = "event", fetch = FetchType.EAGER, cascade = CascadeType.ALL)
    @EqualsAndHashCode.Exclude
    @ToString.Exclude
    List<EventParticipate> eventParticipateList;
}
