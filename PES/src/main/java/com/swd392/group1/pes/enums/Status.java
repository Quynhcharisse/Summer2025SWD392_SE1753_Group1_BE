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


    ACTIVE_TERM("active"), // trong khoảng ngày cho phép
    INACTIVE_TERM("inactive"), // chưa đến ngày
    CLOSED("closed"), // hết hạn đăng ký
    LOCKED_TERM("locked"),
    ARCHIVED("archived");

    private final String value;
}
