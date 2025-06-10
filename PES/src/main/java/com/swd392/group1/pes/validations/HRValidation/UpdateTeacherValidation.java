package com.swd392.group1.pes.validations.HRValidation;

import com.swd392.group1.pes.repositories.AccountRepo;
import com.swd392.group1.pes.requests.UpdateTeacherRequest;

public class UpdateTeacherValidation {
    public static String validate(UpdateTeacherRequest request, AccountRepo accountRepo) {

        // Kiểm tra tên không được để trống
        if (request.getName().trim().isEmpty()) {
            return "Name is required";
        }

        // Kiểm tra tên chỉ chứa chữ cái, khoảng trắng, dấu gạch ngang và dấu nháy đơn
        if (!request.getName().trim().matches("^[a-zA-Z\\s'-]+$")) {
            return "Name can only contain letters, spaces, hyphens, and apostrophes";
        }

        // Kiểm tra độ dài của tên
        if (request.getName().trim().length() < 2 || request.getName().trim().length() > 50) {
            return "Name must be between 2 and 50 characters";
        }

        // Kiểm tra số điện thoại không được để trống
        if (request.getPhone().trim().isEmpty()) {
            return "Phone number is required";
        }

        // Kiểm tra định dạng số điện thoại
        if (!request.getPhone().trim().matches("^(03|05|07|08|09)\\d{8}$")) {
            return "Phone number must start with a valid region prefix and be 10 digits";
        }

        // Kiểm tra giới tính không được để trống
        if (request.getGender().trim().isEmpty()) {
            return "Gender is required";
        }

        // Kiểm tra giới tính hợp lệ
        if (!request.getGender().equals("male") &&
                !request.getGender().equals("female")) {
            return "Gender must be male, female";
        }

        return "";
    }
}
