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
    REFILLED("refilled"),
    PENDING_APPROVAL("pending approval"),
    CANCELLED("cancelled"),
    APPROVED("approved"),
    WAITING_PAYMENT("waiting payment"), // Đã duyệt và chờ thanh toán
    REJECTED("rejected"),
    APPROVED_PAID("approved paid"), // Đã duyệt và đã thanh toán

    EVENT_REGISTRATION_ACTIVE("active"),
    EVENT_REGISTRATION_CLOSED("closed"),
    EVENT_CANCELLED("cancelled"),

    ACTIVE_TERM("active"), // trong khoảng ngày cho phép
    INACTIVE_TERM("inactive"), // chưa đến ngày
    LOCKED_TERM("locked"),

    ACTIVE_TERM_ITEM("active term item"), // trong khoảng ngày cho phép
    INACTIVE_TERM_ITEM("inactive term item"), //
    LOCKED_TERM_ITEM("locked term item"), //

    // Giao dịch đang chờ xử lý / chờ thanh toán
    TRANSACTION_PENDING("pending"),
    // Giao dịch thành công
    TRANSACTION_SUCCESSFUL("success"),
    // Giao dịch thất bại (ví dụ: do lỗi từ VNPay, thẻ không đủ tiền, v.v.)
    TRANSACTION_FAILED("failed"),
    // Giao dịch bị hủy (có thể do người dùng hoặc hệ thống)
    TRANSACTION_CANCELLED("cancelled");

    private final String value;
}
