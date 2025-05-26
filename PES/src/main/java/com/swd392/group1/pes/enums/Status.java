package com.swd392.group1.pes.enums;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum Status {

    ACCOUNT_ACTIVE("active"),
    ACCOUNT_BAN("ban"),
    ACCOUNT_UNBAN("unban");

    private final String value;
}
