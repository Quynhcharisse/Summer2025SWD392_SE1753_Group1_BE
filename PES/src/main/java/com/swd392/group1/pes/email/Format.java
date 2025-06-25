package com.swd392.group1.pes.email;

public class Format {
    /** Fragment cho tài khoản giáo viên */
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

    /** Fragment khi phụ huynh nộp đơn lần đầu */
    public static String getAdmissionSubmittedBody(String parentName, String dateTime) {
        return
                "<p>Dear " + parentName + ",</p>\n" +
                        "<p>We have received your admission form on <strong>" + dateTime + "</strong>.</p>" +
                        "<p>Please wait while our Admission Manager reviews your submission.</p>" +
                        "<p>For any questions, feel free to contact us at <a href=\"mailto:info@sunshinepreschool.edu\">info@sunshinepreschool.edu</a> or (555) 123-4567.</p>" +
                        "<p>Best regards,<br/>Sunshine Preschool</p>";
    }

    /** Fragment khi phụ huynh refill đơn (resubmit) */
    public static String getAdmissionRefilledBody(String parentName, String dateTime) {
        return
                "<p>Dear " + parentName + ",</p>\n" +
                        "<p>Your admission form has been <strong>resubmitted</strong> on <strong>" + dateTime + "</strong>.</p>" +
                        "<p>Our Admissions Team will review the updated information and get back to you shortly.</p>" +
                        "<p>If you need further assistance, contact us at <a href=\"mailto:info@sunshinepreschool.edu\">info@sunshinepreschool.edu</a> or (555) 123-4567.</p>" +
                        "<p>Best regards,<br/>Sunshine Preschool</p>";
    }


    /** Fragment khi đơn được duyệt */
    public static String getAdmissionApprovedBody(String studentName) {
        return
                "<p>Congratulations!</p>" +
                        "<p>The admission form for <strong>" + studentName + "</strong> has been <strong>approved</strong>.</p>" +
                        "<p>We are excited to welcome your child to Sunshine Preschool! Our team will reach out soon with enrollment details.</p>" +
                        "<p>For any questions, please email <a href=\"mailto:info@sunshinepreschool.edu\">info@sunshinepreschool.edu</a> or call (555) 123-4567.</p>" +
                        "<p>Best regards,<br/>Sunshine Preschool</p>";
    }

    /** Fragment khi đơn bị từ chối */
    public static String getAdmissionRejectedBody(String studentName, String reason) {
        return
                "<p>Dear Parent,</p>" +
                        "<p>We are sorry to inform you that the admission form for <strong>" + studentName + "</strong> has been <strong>rejected</strong>.</p>" +
                        "<p><strong>Reason:</strong> " + reason + "</p>" +
                        "<p>If you have any questions or wish to discuss this decision, please contact us at <a href=\"mailto:info@sunshinepreschool.edu\">info@sunshinepreschool.edu</a> or (555) 123-4567.</p>" +
                        "<p>Best regards,<br/>Sunshine Preschool</p>";
    }

    /** Fragment khi đơn bị hủy */
    public static String getAdmissionCancelledBody(String parentName) {
        return
                "<p>Dear " + parentName + ",</p>\n" +
                        "<p>Your admission form has been <strong>cancelled</strong> successfully.</p>" +
                        "<p>If this was a mistake or you wish to reapply, you may submit a new form via our portal.</p>" +
                        "<p>For assistance, contact <a href=\"mailto:info@sunshinepreschool.edu\">info@sunshinepreschool.edu</a> or (555) 123-4567.</p>" +
                        "<p>Best regards,<br/>Sunshine Preschool</p>";
    }

    /** Fragment cho trường hợp tài khoản bị khoá */
    public static String getAccountBannedBody(String name) {
        return
                "<p>Dear " + name + ",</p>\n" +
                        "<p>Your account has been <strong>suspended</strong> due to a violation of our terms of service.</p>" +
                        "<p>If you believe this is a mistake or require further information, please contact support at <a href=\"mailto:info@sunshinepreschool.edu\">info@sunshinepreschool.edu</a>.</p>" +
                        "<p>Best regards,<br/>Sunshine Preschool</p>";
    }

    /** Fragment cho trường hợp tài khoản được kích hoạt lại */
    public static String getAccountReactivatedBody(String name) {
        return
                "<p>Dear " + name + ",</p>\n" +
                        "<p>Your account has been <strong>reactivated</strong> successfully. You may now log in and continue using our services.</p>" +
                        "<p>If you experience any issues, please contact our support team at <a href=\"mailto:info@sunshinepreschool.edu\">info@sunshinepreschool.edu</a> or (555) 123-4567.</p>" +
                        "<p>Best regards,<br/>Sunshine Preschool</p>";
    }

    public static String getParentRegisterFormat(String parentName, String email) {
        return "<!DOCTYPE html>\n" +
                "<html lang=\"en\">\n" +
                "<head>\n" +
                "  <meta charset=\"UTF-8\">\n" +
                "  <title>Account Registration Successful</title>\n" +
                "  <style>\n" +
                "    body { font-family: Arial, sans-serif; background-color: #f4f4f4; padding: 20px; }\n" +
                "    .container { background-color: #fff; border-radius: 8px; padding: 24px; max-width: 600px; margin: 0 auto; box-shadow: 0 2px 6px rgba(0,0,0,0.1); }\n" +
                "    h2 { color: #2a7ae2; }\n" +
                "    .info { margin: 16px 0; font-size: 16px; }\n" +
                "    .footer { margin-top: 30px; font-size: 14px; color: #888; }\n" +
                "    a.button { display: inline-block; margin-top: 12px; padding: 10px 20px; background-color: #2a7ae2; color: white; text-decoration: none; border-radius: 4px; font-weight: bold; }\n" +
                "  </style>\n" +
                "</head>\n" +
                "<body>\n" +
                "  <div class=\"container\">\n" +
                "    <h2>🎉 Account Created Successfully</h2>\n" +
                "    <p>Dear " + parentName + ",</p>\n" +
                "    <p class=\"info\">\n" +
                "      Welcome to Sunshine Preschool! Your account has been created successfully.<br>\n" +
                "      <b>Email:</b> " + email + "<br>\n" +
                "      You can now log in and start using our services.\n" +
                "    </p>\n" +
                "    <p class=\"info\">\n" +
                "      <a href=\"http://localhost:5173/auth/login\" class=\"button\">Login to Your Account</a>\n" +
                "    </p>\n" +
                "    <p class=\"footer\">\n" +
                "      Best regards,<br>\n" +
                "      Sunshine Preschool Team\n" +
                "    </p>\n" +
                "  </div>\n" +
                "</body>\n" +
                "</html>";
    }

    public static String getPasswordChangedFormat(String name) {
        return "<!DOCTYPE html>\n" +
                "<html lang=\"en\">\n" +
                "<head>\n" +
                "  <meta charset=\"UTF-8\">\n" +
                "  <title>Password Changed Successfully</title>\n" +
                "  <style>\n" +
                "    body { font-family: Arial, sans-serif; background-color: #f4f4f4; padding: 20px; }\n" +
                "    .container { background-color: #fff; border-radius: 8px; padding: 24px; max-width: 600px; margin: 0 auto; box-shadow: 0 2px 6px rgba(0,0,0,0.1); }\n" +
                "    h2 { color: #2a7ae2; }\n" +
                "    .info { margin: 16px 0; font-size: 16px; }\n" +
                "    .footer { margin-top: 30px; font-size: 14px; color: #888; }\n" +
                "  </style>\n" +
                "</head>\n" +
                "<body>\n" +
                "  <div class=\"container\">\n" +
                "    <h2>🔒 Password Changed</h2>\n" +
                "    <p>Dear " + (name != null ? name : "User") + ",</p>\n" +
                "    <p class=\"info\">\n" +
                "      Your password has been changed successfully.<br>\n" +
                "      If you did not perform this action, please contact support immediately.\n" +
                "    </p>\n" +
                "    <p class=\"footer\">\n" +
                "      Best regards,<br>\n" +
                "      Sunshine Preschool Team\n" +
                "    </p>\n" +
                "  </div>\n" +
                "</body>\n" +
                "</html>";
    }


}
