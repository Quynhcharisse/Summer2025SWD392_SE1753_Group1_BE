package com.swd392.group1.pes.enums;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum Fees {

    SEED(100000, 800000, 80000, 100000, 100000),
    BUD(110000, 1000000, 100000, 110000, 110000),
    LEAF(120000, 1200000, 120000, 120000, 120000);

    private final double learningMaterial;
    private final double reservation;
    private final double service;
    private final double uniform;
    private final double facility;
}
