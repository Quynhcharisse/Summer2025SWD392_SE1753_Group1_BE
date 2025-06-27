package com.swd392.group1.pes.enums;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum Status {

    ACCOUNT_ACTIVE("active"),
    ACCOUNT_BAN("ban"),
    ACCOUNT_UNBAN("unban"),

    DRAFT("draft"),
    PENDING_APPROVAL ("pending approval"),
    CANCELLED("cancelled"),
    APPROVED("approved"),
    REJECTED("rejected"),

    EVENT_REGISTRATION_ACTIVE("active"),
    EVENT_REGISTRATION_CLOSED("closed"),
    EVENT_CANCELLED("cancelled"),

    CLASS_ACTIVE("active"),
    CLASS_IN_PROGRESS("in_progress"),
    CLASS_CLOSED("closed"),

    ACTIVE_TERM("active"), // trong khoảng ngày cho phép
    INACTIVE_TERM("inactive"), // chưa đến ngày
    LOCKED_TERM("locked"),

    TRANSACTION_SUCCESSFUL("success");

    private final String value;
}
