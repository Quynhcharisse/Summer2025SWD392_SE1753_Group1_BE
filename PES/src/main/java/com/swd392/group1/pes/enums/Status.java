package com.swd392.group1.pes.enums;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum Status {

    ACCOUNT_ACTIVE("active"),
    ACCOUNT_BAN("ban"),

    DRAFT("draft"),
    REFILLED("refilled"),
    PENDING_APPROVAL("pending approval"),
    CANCELLED("cancelled"),
    APPROVED("approved"),
    WAITING_PAYMENT("waiting payment"),
    REJECTED("rejected"),
    APPROVED_PAID("approved paid"),

    EVENT_REGISTRATION_ACTIVE("active"),
    EVENT_REGISTRATION_CLOSED("closed"),
    EVENT_CANCELLED("cancelled"),

    CLASS_ACTIVE("active"),
    CLASS_IN_PROGRESS("in_progress"),
    CLASS_CLOSED("closed"),

    ACTIVE_TERM("active"),
    INACTIVE_TERM("inactive"),
    LOCKED_TERM("locked"),

    ACTIVE_TERM_ITEM("active term item"),
    INACTIVE_TERM_ITEM("inactive term item"),
    LOCKED_TERM_ITEM("locked term item"),

    TRANSACTION_PENDING("pending"),
    TRANSACTION_SUCCESSFUL("success"),
    TRANSACTION_FAILED("failed"),
    TRANSACTION_CANCELLED("cancelled");

    private final String value;
}
