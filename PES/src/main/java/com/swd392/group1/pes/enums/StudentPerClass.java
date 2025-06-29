package com.swd392.group1.pes.enums;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum StudentPerClass {

    MAX(20);

    private final int value;
}
