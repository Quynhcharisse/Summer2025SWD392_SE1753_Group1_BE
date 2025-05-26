package com.swd392.group1.pes.enums;

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
}
