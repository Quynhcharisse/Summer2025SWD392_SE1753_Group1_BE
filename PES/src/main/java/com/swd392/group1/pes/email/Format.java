package com.swd392.group1.pes.email;

public class Format {
    public static String getTeacherFormat(String email, String password) {
        return "<!DOCTYPE html>\n" +
                "<html lang=\"en\">\n" +
                "<head>\n" +
                "  <meta charset=\"UTF-8\">\n" +
                "  <title>New Teacher Account Created</title>\n" +
                "  <style>\n" +
                "    body {\n" +
                "      font-family: Arial, sans-serif;\n" +
                "      color: #333;\n" +
                "      background-color: #f4f4f4;\n" +
                "      padding: 20px;\n" +
                "    }\n" +
                "    .container {\n" +
                "      background-color: #fff;\n" +
                "      border-radius: 8px;\n" +
                "      padding: 24px;\n" +
                "      max-width: 600px;\n" +
                "      margin: 0 auto;\n" +
                "      box-shadow: 0 2px 6px rgba(0, 0, 0, 0.1);\n" +
                "    }\n" +
                "    h2 {\n" +
                "      color: #2a7ae2;\n" +
                "    }\n" +
                "    .info {\n" +
                "      margin: 16px 0;\n" +
                "      font-size: 16px;\n" +
                "    }\n" +
                "    .label {\n" +
                "      font-weight: bold;\n" +
                "      color: #444;\n" +
                "    }\n" +
                "    .note {\n" +
                "      font-size: 14px;\n" +
                "      color: #666;\n" +
                "      margin-top: 20px;\n" +
                "      line-height: 1.6;\n" +
                "    }\n" +
                "    .footer {\n" +
                "      margin-top: 30px;\n" +
                "      font-size: 14px;\n" +
                "      color: #888;\n" +
                "    }\n" +
                "    a.button {\n" +
                "      display: inline-block;\n" +
                "      margin-top: 12px;\n" +
                "      padding: 10px 20px;\n" +
                "      background-color: #2a7ae2;\n" +
                "      color: white;\n" +
                "      text-decoration: none;\n" +
                "      border-radius: 4px;\n" +
                "      font-weight: bold;\n" +
                "    }\n" +
                "  </style>\n" +
                "</head>\n" +
                "<body>\n" +
                "  <div class=\"container\">\n" +
                "    <h2>\uD83C\uDF93 New Teacher Account Created</h2>\n" +
                "    <p>Dear Teacher,</p>\n" +
                "    <p class=\"info\">\n" +
                "      The Sunshine preschool has created a new teacher account for you. Please find your login details below:\n" +
                "    </p>\n" +
                "\n" +
                "    <p class=\"info\">\n" +
                "      <span class=\"label\">\uD83D\uDCE7 Email:</span> " + email + "<br>\n" +
                "      <span class=\"label\">\uD83D\uDD10 Temporary Password:</span>" + password + "\n" +
                "    </p>\n" +
                "\n" +
                "    <p class=\"info\">\n" +
                "      <span class=\"label\">\uD83C\uDF10 Login Link:</span><br>\n" +
                "      <a href=\"http://localhost:5173/auth/login\" class=\"button\">Access Your Account</a>\n" +
                "    </p>\n" +
                "\n" +
                "    <p class=\"note\">\n" +
                "      \uD83D\uDD01 <strong>Note:</strong><br>\n" +
                "      - This is a temporary password. Please change it upon your first login.<br>\n" +
                "      - If you experience any login issues, contact the support team via email or internal hotline.\n" +
                "    </p>\n" +
                "\n" +
                "    <p class=\"footer\">\n" +
                "      Best regards,<br>\n" +
                "      <strong>HR</strong><br>\n" +
                "      Sunshine School\n" +
                "    </p>\n" +
                "  </div>\n" +
                "</body>\n" +
                "</html>";
    }

    public static String getAdmissionFormSubmitted(String parentName, String dateTime) {
        return "<!DOCTYPE html>\n" +
                "<html lang=\"en\">\n" +
                "<head>\n" +
                "  <meta charset=\"UTF-8\">\n" +
                "  <title>Admission Form Submitted</title>\n" +
                "  <style>\n" +
                "    body {\n" +
                "      font-family: Arial, sans-serif;\n" +
                "      color: #333;\n" +
                "      background-color: #f4f4f4;\n" +
                "      padding: 20px;\n" +
                "    }\n" +
                "    .container {\n" +
                "      background-color: #fff;\n" +
                "      border-radius: 8px;\n" +
                "      padding: 24px;\n" +
                "      max-width: 600px;\n" +
                "      margin: 0 auto;\n" +
                "      box-shadow: 0 2px 6px rgba(0, 0, 0, 0.1);\n" +
                "    }\n" +
                "    h2 {\n" +
                "      color: #2a7ae2;\n" +
                "    }\n" +
                "    .info, .note {\n" +
                "      margin: 16px 0;\n" +
                "      font-size: 16px;\n" +
                "    }\n" +
                "    .note {\n" +
                "      color: #666;\n" +
                "      font-size: 14px;\n" +
                "    }\n" +
                "    .footer {\n" +
                "      margin-top: 30px;\n" +
                "      font-size: 14px;\n" +
                "      color: #888;\n" +
                "    }\n" +
                "  </style>\n" +
                "</head>\n" +
                "<body>\n" +
                "  <div class=\"container\">\n" +
                "    <h2>📨 Admission Form Submitted</h2>\n" +
                "    <p>Dear " + parentName + ",</p>\n" +
                "    <p class=\"info\">\n" +
                "      This is to confirm that your <strong>admission form</strong> has been successfully submitted on:\n" +
                "    </p>\n" +
                "    <p class=\"info\">📅 <strong>" + dateTime + "</strong></p>\n" +
                "\n" +
                "    <p class=\"note\">\n" +
                "      📌 Please wait while our Admission Team reviews your submission. You will receive updates via email.\n" +
                "    </p>\n" +
                "\n" +
                "    <p class=\"footer\">\n" +
                "      Best regards,<br>\n" +
                "<strong>HR</strong><br>\n" +
                "      <strong>SunShine Preschool</strong>\n" +
                "    </p>\n" +
                "  </div>\n" +
                "</body>\n" +
                "</html>";
    }

    public static String getAdmissionFormResubmitted(String parentName, String dateStr) {
        return "<!DOCTYPE html>\n" +
                "<html lang=\"en\">\n" +
                "<head>\n" +
                "  <meta charset=\"UTF-8\">\n" +
                "  <title>Admission Form Resubmitted</title>\n" +
                "  <style>\n" +
                "    body { font-family: Arial, sans-serif; background-color: #f7f7f7; padding: 20px; }\n" +
                "    .container { background-color: #fff; padding: 24px; border-radius: 8px; max-width: 600px; margin: auto; box-shadow: 0 0 10px rgba(0,0,0,0.05); }\n" +
                "    h2 { color: #2a7ae2; }\n" +
                "    .info { font-size: 16px; color: #333; line-height: 1.6; }\n" +
                "    .note { font-size: 14px; color: #555; margin-top: 20px; line-height: 1.6; }\n" +
                "    .footer { margin-top: 30px; font-size: 14px; color: #888; }\n" +
                "  </style>\n" +
                "</head>\n" +
                "<body>\n" +
                "<div class=\"container\">\n" +
                "  <h2>📄 Admission Form Resubmitted</h2>\n" +
                "  <p class=\"info\">\n" +
                "    Dear " + parentName + ",<br><br>\n" +
                "    We have successfully received your <strong>resubmitted admission form</strong> on <strong>" + dateStr + "</strong>.<br>\n" +
                "    Our admission team will review the updated information and notify you of the next steps.\n" +
                "  </p>\n" +
                "  <p class=\"note\">\n" +
                "    🔁 If you need to make further changes or have any questions, please contact us at the support email or hotline.\n" +
                "  </p>\n" +
                "  <p class=\"footer\">\n" +
                "    Regards,<br>\n" +
                "    <strong>PES Admission Team</strong><br>\n" +
                "    Sunshine Preschool\n" +
                "  </p>\n" +
                "</div>\n" +
                "</body>\n" +
                "</html>";
    }

    public static String getAdmissionFormCancelled(String parentName) {
        return "<!DOCTYPE html>\n" +
                "<html lang=\"en\">\n" +
                "<head>\n" +
                "  <meta charset=\"UTF-8\">\n" +
                "  <title>Admission Form Cancelled</title>\n" +
                "  <style>\n" +
                "    body { font-family: Arial, sans-serif; background-color: #f7f7f7; padding: 20px; }\n" +
                "    .container { background-color: #fff; padding: 24px; border-radius: 8px; max-width: 600px; margin: auto; box-shadow: 0 0 10px rgba(0,0,0,0.05); }\n" +
                "    h2 { color: #d9534f; }\n" +
                "    .info { font-size: 16px; color: #333; line-height: 1.6; }\n" +
                "    .note { font-size: 14px; color: #555; margin-top: 20px; line-height: 1.6; }\n" +
                "    .footer { margin-top: 30px; font-size: 14px; color: #888; }\n" +
                "  </style>\n" +
                "</head>\n" +
                "<body>\n" +
                "<div class=\"container\">\n" +
                "  <h2>❌ Admission Form Cancelled</h2>\n" +
                "  <p class=\"info\">\n" +
                "    Dear " + parentName + ",<br><br>\n" +
                "    Your admission form has been <strong>cancelled</strong> successfully.<br>\n" +
                "    If this was a mistake, you may submit the form again at your convenience.\n" +
                "  </p>\n" +
                "  <p class=\"note\">\n" +
                "    📩 For further assistance, please contact our support team.\n" +
                "  </p>\n" +
                "  <p class=\"footer\">\n" +
                "    Regards,<br>\n" +
                "    <strong>PES Admission Team</strong><br>\n" +
                "    Sunshine Preschool\n" +
                "  </p>\n" +
                "</div>\n" +
                "</body>\n" +
                "</html>";
    }

    public static String getAccountBanned(String name) {
        return "<!DOCTYPE html>\n" +
                "<html lang=\"en\">\n" +
                "<head>\n" +
                "  <meta charset=\"UTF-8\">\n" +
                "  <title>Account Suspended</title>\n" +
                "  <style>\n" +
                "    body { font-family: Arial, sans-serif; background-color: #fcebea; padding: 20px; }\n" +
                "    .container { background-color: #fff; padding: 24px; border-radius: 8px; max-width: 600px; margin: auto; border: 1px solid #f5c6cb; }\n" +
                "    h2 { color: #d9534f; }\n" +
                "    .message { font-size: 16px; color: #333; line-height: 1.6; }\n" +
                "    .footer { margin-top: 30px; font-size: 14px; color: #888; }\n" +
                "  </style>\n" +
                "</head>\n" +
                "<body>\n" +
                "<div class=\"container\">\n" +
                "  <h2>🚫 Account Suspended</h2>\n" +
                "  <p class=\"message\">\n" +
                "    Dear " + name + ",<br><br>\n" +
                "    Your account has been <strong>suspended</strong> due to a violation of our terms of service.<br>\n" +
                "    If you believe this is a mistake, please contact our support team immediately.\n" +
                "  </p>\n" +
                "  <p class=\"footer\">\n" +
                "    Regards,<br>\n" +
                "    <strong>PES Team</strong>\n" +
                "  </p>\n" +
                "</div>\n" +
                "</body>\n" +
                "</html>";
    }

    public static String getAccountReactivated(String name) {
        return "<!DOCTYPE html>\n" +
                "<html lang=\"en\">\n" +
                "<head>\n" +
                "  <meta charset=\"UTF-8\">\n" +
                "  <title>Account Reactivated</title>\n" +
                "  <style>\n" +
                "    body { font-family: Arial, sans-serif; background-color: #e6f4ea; padding: 20px; }\n" +
                "    .container { background-color: #fff; padding: 24px; border-radius: 8px; max-width: 600px; margin: auto; border: 1px solid #c3e6cb; }\n" +
                "    h2 { color: #28a745; }\n" +
                "    .message { font-size: 16px; color: #333; line-height: 1.6; }\n" +
                "    .footer { margin-top: 30px; font-size: 14px; color: #888; }\n" +
                "  </style>\n" +
                "</head>\n" +
                "<body>\n" +
                "<div class=\"container\">\n" +
                "  <h2>✅ Account Reactivated</h2>\n" +
                "  <p class=\"message\">\n" +
                "    Dear " + name + ",<br><br>\n" +
                "    Your account has been <strong>reinstated</strong> successfully. You can now log in and continue using our services as usual.<br>\n" +
                "    If you experience any issues, feel free to contact our support team.\n" +
                "  </p>\n" +
                "  <p class=\"footer\">\n" +
                "    Regards,<br>\n" +
                "    <strong>PES Team</strong>\n" +
                "  </p>\n" +
                "</div>\n" +
                "</body>\n" +
                "</html>";
    }

    public static String getAdmissionApproved(String studentName) {
        return "<!DOCTYPE html>\n" +
                "<html lang=\"en\">\n" +
                "<head>\n" +
                "  <meta charset=\"UTF-8\">\n" +
                "  <title>Admission Approved</title>\n" +
                "  <style>\n" +
                "    body { font-family: Arial, sans-serif; background-color: #f0fdf4; padding: 20px; }\n" +
                "    .container { background: #fff; border-radius: 8px; padding: 24px; max-width: 600px; margin: auto; border: 1px solid #b2f5ea; }\n" +
                "    h2 { color: #38a169; }\n" +
                "    .message { font-size: 16px; color: #333; line-height: 1.6; }\n" +
                "    .footer { margin-top: 30px; font-size: 14px; color: #666; }\n" +
                "  </style>\n" +
                "</head>\n" +
                "<body>\n" +
                "<div class=\"container\">\n" +
                "  <h2>🎉 Admission Approved</h2>\n" +
                "  <p class=\"message\">\n" +
                "    Congratulations!<br><br>\n" +
                "    The admission form for <strong>" + studentName + "</strong> has been <strong>approved</strong>.<br>\n" +
                "    We are excited to welcome your child to Sunshine Preschool!<br>\n" +
                "    Further details about enrollment and class schedule will be sent to you soon.\n" +
                "  </p>\n" +
                "  <p class=\"footer\">\n" +
                "    Warm regards,<br>\n" +
                "    PES Admission Team\n" +
                "  </p>\n" +
                "</div>\n" +
                "</body>\n" +
                "</html>";
    }

    public static String getAdmissionRejected(String studentName, String reason) {
        return "<!DOCTYPE html>\n" +
                "<html lang=\"en\">\n" +
                "<head>\n" +
                "  <meta charset=\"UTF-8\">\n" +
                "  <title>Admission Rejected</title>\n" +
                "  <style>\n" +
                "    body { font-family: Arial, sans-serif; background-color: #fef2f2; padding: 20px; }\n" +
                "    .container { background: #fff; border-radius: 8px; padding: 24px; max-width: 600px; margin: auto; border: 1px solid #fca5a5; }\n" +
                "    h2 { color: #e53e3e; }\n" +
                "    .message { font-size: 16px; color: #333; line-height: 1.6; }\n" +
                "    .reason { background: #fef2f2; padding: 12px; border-left: 4px solid #f87171; margin-top: 10px; color: #b91c1c; }\n" +
                "    .footer { margin-top: 30px; font-size: 14px; color: #666; }\n" +
                "  </style>\n" +
                "</head>\n" +
                "<body>\n" +
                "<div class=\"container\">\n" +
                "  <h2>❌ Admission Rejected</h2>\n" +
                "  <p class=\"message\">\n" +
                "    We regret to inform you that the admission form for <strong>" + studentName + "</strong> has been <strong>rejected</strong>.\n" +
                "  </p>\n" +
                "  <div class=\"reason\">\n" +
                "    📌 <strong>Reason:</strong> " + reason + "\n" +
                "  </div>\n" +
                "  <p class=\"footer\">\n" +
                "    Please feel free to contact us for further assistance.<br>\n" +
                "    Regards,<br>\n" +
                "    PES Admission Team\n" +
                "  </p>\n" +
                "</div>\n" +
                "</body>\n" +
                "</html>";
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
