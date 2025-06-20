package com.swd392.group1.pes.validations.ParentValidation;

import com.swd392.group1.pes.requests.RefillFormRequest;

public class RefillFormValidation {
    public static String validate(RefillFormRequest request) {
        // 1. Địa chỉ hộ khẩu
        if (request.getHouseholdRegistrationAddress() == null || request.getHouseholdRegistrationAddress().trim().isEmpty()) {
            return "Household registration address is required.";
        }

        if (request.getHouseholdRegistrationAddress().length() > 150) {
            return "Household registration address must not exceed 150 characters.";
        }

        // 2. Hình cam kết
        if (request.getCommitmentImg() == null) {
            return "Commitment image is required.";
        }

        if (!isValidImage(request.getCommitmentImg())) {
            return "Commitment image must be a valid image (.jpg, .jpeg, .png, .gif, .bmp, .webp)";
        }

        // 3. Hình đánh giá đặc điểm trẻ
        if (request.getChildCharacteristicsFormImg() == null) {
            return "Child characteristics form image is required.";
        }

        if (!isValidImage(request.getChildCharacteristicsFormImg())) {
            return "Child characteristics form image must be a valid image (.jpg, .jpeg, .png, .gif, .bmp, .webp)";
        }

        // 4. Ghi chú (không bắt buộc nhưng có thể giới hạn độ dài)
        if (request.getNote() != null && request.getNote().length() > 300) {
            return "Note must not exceed 300 characters.";
        }

        return "";
    }
    private static boolean isValidImage(String fileName) {
        return fileName.matches("(?i)^.+\\.(jpg|jpeg|png|gif|bmp|webp)$");
    }
}
