package com.swd392.group1.pes.dto.requests;

import com.swd392.group1.pes.enums.Grade;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.FieldDefaults;

import java.time.LocalDateTime;
import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@FieldDefaults(level = AccessLevel.PRIVATE)
public class CreateAdmissionTermRequest {
    LocalDateTime startDate;
    LocalDateTime endDate;
    List<TermItem> termItemList;

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    @FieldDefaults(level = AccessLevel.PRIVATE)
    public static class TermItem {
        int expectedClasses; // Số lớp dự kiến
        Grade grade;
    }

}
