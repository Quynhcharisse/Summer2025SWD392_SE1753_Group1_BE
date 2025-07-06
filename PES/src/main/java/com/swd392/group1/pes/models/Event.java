package com.swd392.group1.pes.models;

import com.swd392.group1.pes.enums.Status;
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
@Table(name = "`event`")
@FieldDefaults(level = AccessLevel.PRIVATE)
public class Event {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    Integer id;

    @Column(length = 50)
    String name;

    LocalDateTime startTime;

    LocalDateTime endTime;

    @Column(length = 100)
    String location;

    @Column(length = 5000, nullable = false)
    String description;

    @Column(name = "`created_at`")
    LocalDateTime createdAt;

    @Enumerated(EnumType.STRING)
    @Column(length = 50)
    Status status;

    @Column(name = "`registration_deadline`")
    LocalDateTime registrationDeadline;

    @Column(name = "`attachment_img`")
    String attachmentImg;

    @Column(name = "`host_name`", length = 50)
    String hostName;

    @OneToMany(mappedBy = "event", fetch = FetchType.LAZY)
    @EqualsAndHashCode.Exclude
    @ToString.Exclude
    List<TeacherEvent> teacherEventList;

    @OneToMany(mappedBy = "event", fetch = FetchType.LAZY)
    @EqualsAndHashCode.Exclude
    @ToString.Exclude
    List<EventParticipate> eventParticipateList;
}
