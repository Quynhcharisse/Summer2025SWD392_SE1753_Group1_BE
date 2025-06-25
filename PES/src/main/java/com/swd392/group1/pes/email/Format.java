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
                "      <strong>PES Support Team</strong><br>\n" +
                "      Preschool Enrollment Management System\n" +
                "    </p>\n" +
                "  </div>\n" +
                "</body>\n" +
                "</html>";
    }


}
