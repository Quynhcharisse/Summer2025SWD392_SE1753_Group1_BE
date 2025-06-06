package com.swd392.group1.pes.requests;

import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.FieldDefaults;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@FieldDefaults(level = AccessLevel.PRIVATE)
public class CreateEventRequest {
    String name;
    LocalDate date;
    LocalDateTime startTime;
    LocalDateTime endTime;
    String location;
    String description;
    String createdBy;
    String status;
    String registrationDeadline;
    String attachmentImg;
    String hostName;
}
