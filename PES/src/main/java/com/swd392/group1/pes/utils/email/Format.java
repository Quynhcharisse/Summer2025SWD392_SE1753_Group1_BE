package com.swd392.group1.pes.utils.email;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;

public class Format {
    /**
     * Fragment cho tài khoản giáo viên
     */
    public static String getTeacherBody(String email, String password) {
        return
                "<p>Dear Teacher,</p>\n" +
                        "<p>Your Sunshine Preschool account is ready with the following credentials:</p>\n" +
                        "<ul style=\"padding-left:16px;\">" +
                        "  <li><strong>Email:</strong> " + email + "</li>" +
                        "  <li><strong>Temporary Password:</strong> " + password + "</li>" +
                        "</ul>" +
                        "<p>Please <a href=\"http://localhost:5173/auth/login\">Sign in to your account</a> and change your password on first login.</p>" +
                        "<p>For assistance, contact us at <a href=\"mailto:info@sunshinepreschool.edu\">info@sunshinepreschool.edu</a> or call (555) 123-4567.</p>" +
                        "<p>Best regards,<br/>Sunshine Preschool</p>";
    }

    /**
     * Fragment khi phụ huynh nộp đơn lần đầu
     */
    public static String getAdmissionSubmittedBody(String parentName, String dateTime) {
        return
                "<p>Dear " + parentName + ",</p>\n" +
                        "<p>We have received your admission form on <strong>" + dateTime + "</strong>.</p>" +
                        "<p>Please wait while our Admission Manager reviews your submission.</p>" +
                        "<p>For any questions, feel free to contact us at <a href=\"mailto:info@sunshinepreschool.edu\">info@sunshinepreschool.edu</a> or (555) 123-4567.</p>" +
                        "<p>Best regards,<br/>Sunshine Preschool</p>";
    }

    /**
     * Fragment khi đơn được duyệt
     */
    public static String getAdmissionApprovedBody(String studentName) {
        return
                "<p>Congratulations!</p>" +
                        "<p>The admission form for <strong>" + studentName + "</strong> has been <strong>approved</strong>.</p>" +
                        "<p>We are excited to welcome your child to Sunshine Preschool! Our team will reach out soon with enrollment details.</p>" +
                        "<p>For any questions, please email <a href=\"mailto:info@sunshinepreschool.edu\">info@sunshinepreschool.edu</a> or call (555) 123-4567.</p>" +
                        "<p>Best regards,<br/>Sunshine Preschool</p>";
    }

    /**
     * Fragment khi đơn bị từ chối
     */
    public static String getAdmissionRejectedBody(String studentName, String reason) {
        return
                "<p>Dear Parent,</p>" +
                        "<p>We are sorry to inform you that the admission form for <strong>" + studentName + "</strong> has been <strong>rejected</strong>.</p>" +
                        "<p><strong>Reason:</strong> " + reason + "</p>" +
                        "<p>If you have any questions or wish to discuss this decision, please contact us at <a href=\"mailto:info@sunshinepreschool.edu\">info@sunshinepreschool.edu</a> or (555) 123-4567.</p>" +
                        "<p>Best regards,<br/>Sunshine Preschool</p>";
    }

    /**
     * Fragment khi đơn bị hủy
     */
    public static String getAdmissionCancelledBody(String parentName) {
        return
                "<p>Dear " + parentName + ",</p>\n" +
                        "<p>Your admission form has been <strong>cancelled</strong> successfully.</p>" +
                        "<p>If this was a mistake or you wish to reapply, you may submit a new form via our portal.</p>" +
                        "<p>For assistance, contact <a href=\"mailto:info@sunshinepreschool.edu\">info@sunshinepreschool.edu</a> or (555) 123-4567.</p>" +
                        "<p>Best regards,<br/>Sunshine Preschool</p>";
    }

    /**
     * Fragment cho trường hợp tài khoản bị khoá
     */
    public static String getAccountBannedBody(String name) {
        return
                "<p>Dear " + name + ",</p>\n" +
                        "<p>Your account has been <strong>suspended</strong> due to a violation of our terms of service.</p>" +
                        "<p>If you believe this is a mistake or require further information, please contact support at <a href=\"mailto:info@sunshinepreschool.edu\">info@sunshinepreschool.edu</a>.</p>" +
                        "<p>Best regards,<br/>Sunshine Preschool</p>";
    }

    /**
     * Fragment cho trường hợp tài khoản được kích hoạt lại
     */
    public static String getAccountReactivatedBody(String name) {
        return
                "<p>Dear " + name + ",</p>\n" +
                        "<p>Your account has been <strong>reactivated</strong> successfully. You may now log in and continue using our services.</p>" +
                        "<p>If you experience any issues, please contact our support team at <a href=\"mailto:info@sunshinepreschool.edu\">info@sunshinepreschool.edu</a> or (555) 123-4567.</p>" +
                        "<p>Best regards,<br/>Sunshine Preschool</p>";
    }

    /**
     * Fragment khi phụ huynh đăng ký tài khoản thành công
     */
    public static String getParentRegisterFormat(String parentName, String email) {
        return
                "<p>Dear " + (parentName != null ? parentName : "Parent") + ",</p>\n" +
                        "<p>Your account has been successfully registered with Sunshine Preschool.</p>" +
                        "<ul style=\"padding-left:16px;\">" +
                        "  <li><strong>Email:</strong> " + email + "</li>" +
                        "</ul>" +
                        "<p>For assistance, contact us at <a href=\"mailto:info@sunshinepreschool.edu\">info@sunshinepreschool.edu</a> or call (555) 123-4567.</p>" +
                        "<p>Best regards,<br/>Sunshine Preschool</p>";
    }

    /**
     * Fragment cho email xác thực đăng ký tài khoản
     */
    public static String getEmailVerificationBody(String verificationLink) {
        return "<p>Dear User,</p>" +
                "<p>You have requested to register an account with Sunshine Preschool. Please click the link below to complete your registration:</p>" +
                "<div style=\"background-color:#eef5f0;padding:20px;text-align:center;margin:20px 0;border-radius:6px;\">" +
                "  <a href=\"" + verificationLink + "\" style=\"color:#1C5A2A;text-decoration:none;font-weight:bold;\">Complete Registration</a>" +
                "</div>" +
                "<p><strong>Note:</strong> This link will expire in <strong> 5 minutes</strong>.</p>" +
                "<p>If you did not request this registration, please ignore this message.</p>" +
                "<p>Best regards,<br/>Sunshine Preschool</p>";
    }

    /**
     * Fragment khi phụ huynh yêu cầu đổi mật khẩu thành công
     */
    public static String getPasswordChangedFormat(String name) {
        return
                "<p>Dear " + (name != null ? name : "User") + ",</p>\n" +
                        "<p>Your password has been changed successfully.</p>" +
                        "<p>If you did not perform this action, please contact support immediately.</p>" +
                        "<p>Best regards,<br/>Sunshine Preschool</p>";
    }

    /**
     * Fragment khi yêu cầu đổi mật khẩu thành công
     */
    public static String getRenewPasswordSuccessBody(String name) {
        return
                "<p>Dear " + (name != null ? name : "User") + ",</p>\n" +
                        "<p>Your password has been renewed successfully.</p>" +
                        "<p>If you did not perform this action, please contact support immediately.</p>" +
                        "<p>Best regards,<br/>Sunshine Preschool</p>";
    }

    /**
     * Fragment for registration OTP email
     */
    public static String getRegisterOtpBody(String otp, int expiryMinutes) {
        return "<p>Your OTP code for registration is: <b>" + otp + "</b></p>"
                + "<p>This code will expire in " + expiryMinutes + " minutes.</p>";
    }

    /**
     * Fragment cho email forgot password
     */
    public static String getForgotPasswordBody(String code) {
        return
                "<p>Dear User,</p>" +
                        "<p>You have requested to reset your password. Please use the reset code below:</p>" +
                        "<div style=\"background-color:#eef5f0;padding:20px;text-align:center;margin:20px 0;border-radius:6px;\">" +
                        "  <a href=\"" + code + "\" style=\"color:#1C5A2A;text-decoration:none;font-weight:bold;\">Reset Password</a>" +
                        "</div>" +
                        "<p><strong>Note:</strong> This code will expire in <strong> 5 minutes</strong>.</p>" +
                        "<p>If you did not request a password reset, please ignore this message or contact support.</p>" +
                        "<p>Best regards,<br/>Sunshine Preschool</p>";
    }

    public static String getRegisterEventBody(
            String parentName,
            String eventName,
            LocalDateTime eventTime,
            List<String> studentNames
    ) {
        String formattedTime = eventTime.format(DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm"));
        StringBuilder studentListHtml = new StringBuilder();
        for (String name : studentNames) {
            studentListHtml.append("<li>").append(name).append("</li>");
        }

        return "<p>Dear " + (parentName != null ? parentName : "Parent") + ",</p>"
                + "<p>You have successfully registered the following student"
                + (studentNames.size() > 1 ? "s" : "") + " for the event <strong>\"" + eventName + "\"</strong>:</p>"
                + "<ul style=\"padding-left:20px\">" + studentListHtml + "</ul>"
                + "<p><strong>Event Time:</strong> " + formattedTime + "</p>"
                + "<p>If you have any questions, feel free to contact us "
                + "<p>Best regards,<br/>Sunshine Preschool</p>";
    }

    public static String getCancelEventForParentBody(String parentName,
                                                     String childName,
                                                     String eventName,
                                                     LocalDateTime startTime,
                                                     String reasons) {
        String formattedStart = startTime.format(DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm"));

        return "<p>Dear " + parentName + ",</p>"
                + "<p>We regret to inform you that the event \"" + eventName + "\" scheduled at "
                + formattedStart
                + " for your child " + childName
                + " has been canceled.</p>"
                + "<p>Reason: " + reasons + "</p>"
                + "<p>We apologize for any inconvenience caused.</p>"
                + "<p>Best regards,<br/>SunShine Preschool</p>";
    }

    public static String getCancelEventForTeacherBody(String teacherName,
                                                      String eventName,
                                                      LocalDateTime startTime,
                                                      String reasons) {
        String formattedStart = startTime.format(DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm"));

        return "<p>Dear " + teacherName + ",</p>"
                + "<p>The event \"" + eventName + "\" scheduled at "
                + formattedStart
                + " has been cancelled. </p>"
                + "<p>Reason: " + reasons + "</p>"
                + "<p>Best regards,<br/>SunShine Preschool</p>";
    }

    public static String getAssignClassSuccessfulForParentBody(
            String parentName,
            String studentName,
            String className,
            String teacherName,
            String startDate
    ) {
        return "<p>Dear " + parentName + ",</p>"
                + "<p>We are pleased to inform you that your child, <strong>" + studentName + "</strong>, "
                + "has been successfully assigned to <strong>class " + className + "</strong> for the upcoming academic term.</p>"
                + "<p>This class will be taught by <strong>teacher " + teacherName + "</strong>.</p>"
                + "<p>The class will officially start on <strong>" + startDate + "</strong>.</p>"
                + "<p>If you have any questions or need further information, please do not hesitate to contact us.</p>"
                + "<p>Sincerely,<br/>The School Administration</p>";
    }

    public static String getAssignClassSuccessfulForTeacherBody(
            String teacherName, String className, String startDateStr
    ) {
        return String.format(
                "Dear %s,\n\n" +
                        "Congratulations! You have been assigned as the homeroom teacher for class %s.\n" +
                        "The class will officially start on %s.\n\n" +
                        "Please check your teacher portal for more details about your class schedule and student list.\n\n" +
                        "Best regards,\n" +
                        "School Administration"
                , teacherName, className, startDateStr
        );
    }

    /**
     * Fragment khi thanh toán học phí thành công
     */
    public static String getPaymentSuccessBody(String parentName, String studentName, String txnRef, long amount, LocalDateTime paymentDate) {
        String formattedDate = paymentDate.format(DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm"));
        String formattedAmount = String.format("%,d", amount);

        return "<p>Dear " + parentName + ",</p>" +
                "<p>We are pleased to inform you that your tuition fee payment has been <strong>successfully processed</strong>.</p>" +
                "<p><strong>Details:</strong></p>" +
                "<ul style=\"padding-left:16px;\">" +
                "  <li><strong>Student:</strong> " + studentName + "</li>" +
                "  <li><strong>Transaction Ref:</strong> " + txnRef + "</li>" +
                "  <li><strong>Amount:</strong> " + formattedAmount + " VND</li>" +
                "  <li><strong>Date:</strong> " + formattedDate + "</li>" +
                "</ul>" +
                "<p>Thank you for completing the payment. You may now track your child's enrollment status in our portal.</p>" +
                "<p>For any questions, feel free to contact us at <a href=\"mailto:info@sunshinepreschool.edu\">info@sunshinepreschool.edu</a>.</p>" +
                "<p>Best regards,<br/>Sunshine Preschool</p>";
    }

    /**
     * Fragment cho email xác thực đăng ký tài khoản (đã sửa tham số và nội dung)
     */
    public static String getVerifyEmailBody(String verificationLink, String name) { // Đổi tên tham số từ expiryMinutes sang name
        return "<p>Dear " + (name != null ? name : "User") + ",</p>" +
                "<p>You have requested to register an account with Sunshine Preschool. Please click the link below to complete your registration:</p>" +
                "<div style=\"background-color:#eef5f0;padding:20px;text-align:center;margin:20px 0;border-radius:6px;\">" +
                "  <a href=\"" + verificationLink + "\" style=\"color:#1C5A2A;text-decoration:none;font-weight:bold;display:inline-block;padding:10px 20px;background-color:#4CAF50;color:white!important;border-radius:5px;\">Complete Registration</a>" +
                "</div>" +
                "<p><strong>Note:</strong> This link is valid for a limited time.</p>" + // Bỏ số phút cụ thể vì JWT tự quản lý
                "<p>If you did not request this registration, please ignore this message.</p>" +
                "<p>Best regards,<br/>Sunshine Preschool</p>";
    }

    /**
     * Fragment khi đăng ký tài khoản thành công
     */
    public static String getRegistrationSuccessBody(String name) {
        return "<p>Dear " + (name != null ? name : "User") + ",</p>" +
                "<p>Congratulations! Your account has been successfully registered and activated with Sunshine Preschool.</p>" +
                "<p>We are excited to welcome you to our community. You can now log in and explore our services.</p>" +
                "<p>For any questions, feel free to contact us at <a href=\"mailto:info@sunshinepreschool.edu\">info@sunshinepreschool.edu</a> or call (555) 123-4567.</p>" +
                "<p>Best regards,<br/>Sunshine Preschool</p>";
    }

    /**
     * Fragment khi phụ huynh thêm con thành công
     */
    public static String getChildAddedSuccessBody(String parentName, String childName, String childGender, String dateOfBirth) {
        return "<p>Dear " + parentName + ",</p>" +
                "<p>We are pleased to confirm that your child has been successfully added to your account.</p>" +
                "<p><strong>Child Details:</strong></p>" +
                "<ul style=\"padding-left:16px;\">" +
                "  <li><strong>Name:</strong> " + childName + "</li>" +
                "  <li><strong>Gender:</strong> " + childGender + "</li>" +
                "  <li><strong>Date of Birth:</strong> " + dateOfBirth + "</li>" +
                "</ul>" +
                "<p>You can now proceed to submit an admission form for " + childName + " when an admission term becomes available.</p>" +
                "<p>For any questions, feel free to contact us at <a href=\"mailto:info@sunshinepreschool.edu\">info@sunshinepreschool.edu</a> or call (555) 123-4567.</p>" +
                "<p>Best regards,<br/>Sunshine Preschool</p>";
    }

    /**
     * Fragment khi phụ huynh cập nhật thông tin con thành công
     */
    public static String getChildUpdatedSuccessBody(String parentName, String childName, int remainingUpdates) {
        return "<p>Dear " + parentName + ",</p>" +
                "<p>We are pleased to confirm that the information for your child <strong>" + childName + "</strong> has been successfully updated.</p>" +
                "<p><strong>Important Information:</strong></p>" +
                "<ul style=\"padding-left:16px;\">" +
                "  <li>Update completed on: " + LocalDateTime.now().format(DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm")) + "</li>" +
                "  <li>Remaining updates allowed: <strong>" + remainingUpdates + " out of 5</strong></li>" +
                "</ul>" +
                "<p><strong>Please note:</strong> Child information can only be updated 5 times for security reasons. After submitting an admission form, updates will not be allowed.</p>" +
                "<p>For any questions, feel free to contact us at <a href=\"mailto:info@sunshinepreschool.edu\">info@sunshinepreschool.edu</a> or call (555) 123-4567.</p>" +
                "<p>Best regards,<br/>Sunshine Preschool</p>";
    }
}
