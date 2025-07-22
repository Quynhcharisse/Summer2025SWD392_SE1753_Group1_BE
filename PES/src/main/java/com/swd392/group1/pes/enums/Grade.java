package com.swd392.group1.pes.enums;

import com.fasterxml.jackson.annotation.JsonCreator;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum Grade {

    SEED("seed", 3),
    BUD("bud", 4),
    LEAF("leaf", 5);

    private final String name;
    private final int age;

    @JsonCreator
    public static Grade fromString(String value) {
        for (Grade grade : Grade.values()) {
            if (grade.name().equalsIgnoreCase(value)) {
                return grade;
            }
        }
        throw new IllegalArgumentException("Invalid grade: " + value);
    }
}
