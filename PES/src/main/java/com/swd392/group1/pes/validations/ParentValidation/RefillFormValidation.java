package com.swd392.group1.pes.validations.ParentValidation;

import com.swd392.group1.pes.enums.Status;
import com.swd392.group1.pes.models.AdmissionForm;
import com.swd392.group1.pes.models.Student;
import com.swd392.group1.pes.models.TermItem;
import com.swd392.group1.pes.repositories.AdmissionFormRepo;
import com.swd392.group1.pes.repositories.StudentRepo;
import com.swd392.group1.pes.repositories.TermItemRepo;
import com.swd392.group1.pes.requests.RefillFormRequest;

import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.Arrays;
import java.util.List;

public class RefillFormValidation {
    public static String validate(RefillFormRequest request, StudentRepo studentRepo, TermItemRepo termItemRepo, AdmissionFormRepo admissionFormRepo) {
        //Lấy thông tin student
        Student student = studentRepo.findById(request.getStudentId()).orElse(null);
        if (student == null) {
            return "Student not found";
        }

        //kiểm tra độ tuổi phù hợp của Student
        if (!isAgeValidForGrade(student.getDateOfBirth())) {
            return "Student's age (" + calculateAge(student.getDateOfBirth()) + " years) does not meet the required age for admission (3-5 years).";
        }

        //Kiểm tra sự tồn tại và trạng thái của TermItem
        TermItem activeTermItem = termItemRepo.findById(request.getTermItemId()).orElse(null);
        if (activeTermItem == null) {
            return "The admission term item was not found.";
        }

        //ACTIVE_TERM_ITEM là trạng thái cho phép đăng ký ==> hỏi anh quốc
        //Đảm bảo AdmissionTerm không null và cũng đang active
        if (!activeTermItem.getStatus().equals(Status.ACTIVE_TERM_ITEM) ||
                activeTermItem.getAdmissionTerm() == null ||
                !activeTermItem.getAdmissionTerm().getStatus().equals(Status.ACTIVE_TERM)) {
            return "The admission term item is not currently open for registration or is invalid.";
        }

        //xem học sinh đã có form đang hoạt động (không phải REJECTED/CANCELLED) chưa
        List<Status> statusesToExclude = Arrays.asList(Status.REJECTED, Status.CANCELLED);
        List<AdmissionForm> nonRejectedOrCancelledForms = admissionFormRepo.findAllByStudent_IdAndTermItem_IdAndStatusNotIn(
                student.getId(), activeTermItem.getId(), statusesToExclude
        );

        if (!nonRejectedOrCancelledForms.isEmpty()) {
            // Nếu có bất kỳ form nào không bị REJECTED hoặc CANCELLED ==> kiểm tra trạng thái cụ thể
            // (Thực ra, nếu list này không rỗng thì đã có form "active")
            boolean hasPendingForm = nonRejectedOrCancelledForms.stream()
                    .anyMatch(form -> form.getStatus().equals(Status.PENDING_APPROVAL));

            if (hasPendingForm) {
                return "This student already has a pending admission form for the current term. Please wait for approval or cancel that form if you wish to resubmit.";
            } else {
                // Có form ở trạng thái APPROVED, PAID, etc.
                return "This student already has an active or processed admission form for the current term. Refilling is not allowed.";
            }
        }

        //xem có chính xác MỘT form REJECTED hoặc CANCELLED để refill không
        List<Status> statusesToInclude = Arrays.asList(Status.REJECTED, Status.CANCELLED);
        List<AdmissionForm> rejectedOrCancelledForms = admissionFormRepo.findAllByStudent_IdAndTermItem_IdAndStatusIn(
                student.getId(), activeTermItem.getId(), statusesToInclude
        );

        long rejectedOrCancelledCount = rejectedOrCancelledForms.size();

        if (rejectedOrCancelledCount == 0) {
            return "No rejected or cancelled admission form found for this student in the current term to refill. If you wish to submit a new form, please use the 'submit' function.";
        }
        // GIỮ LẠI ĐIỀU KIỆN NÀY ĐỂ ĐẢM BẢO TOÀN VẸN DỮ LIỆU
        // Nếu có nhiều hơn một, đây là vấn đề dữ liệu hoặc nghiệp vụ cần xử lý rõ ràng hơn
        if (rejectedOrCancelledCount > 1) {
            return "Multiple rejected or cancelled forms found for this student in the current term. Please contact support for clarification or manually select which form to refill.";
        }

        // Địa chỉ hộ khẩu
        if (request.getHouseholdRegistrationAddress() == null || request.getHouseholdRegistrationAddress().trim().isEmpty()) {
            return "Household registration address is required.";
        }

        if (request.getHouseholdRegistrationAddress().length() > 150) {
            return "Household registration address must not exceed 150 characters.";
        }

        //Hình cam kết
        if (request.getCommitmentImg() == null) {
            return "Commitment image is required.";
        }

        if (!isValidImage(request.getCommitmentImg())) {
            return "Commitment image must be a valid image (.jpg, .jpeg, .png, .gif, .bmp, .webp)";
        }

        //Hình đánh giá đặc điểm trẻ
        if (request.getChildCharacteristicsFormImg() == null) {
            return "Child characteristics form image is required.";
        }

        if (!isValidImage(request.getChildCharacteristicsFormImg())) {
            return "Child characteristics form image must be a valid image (.jpg, .jpeg, .png, .gif, .bmp, .webp)";
        }

        //Ghi chú (không bắt buộc nhưng có thể giới hạn độ dài)
        if (request.getNote() != null && request.getNote().length() > 300) {
            return "Note must not exceed 300 characters.";
        }

        return "";
    }

    private static boolean isValidImage(String fileName) {
        return fileName.matches("(?i)^.+\\.(jpg|jpeg|png|gif|bmp|webp)$");
    }

    // Hàm tiện ích tính toán tuổi
    private static int calculateAge(LocalDate dob) {
        LocalDate today = LocalDate.now();
        return (int) ChronoUnit.YEARS.between(dob, today);
    }

    // Hàm tiện ích kiểm tra tuổi cho Grade (có thể cần chi tiết hơn trong tương lai)
    private static boolean isAgeValidForGrade(LocalDate dob) {
        int age = calculateAge(dob);
        // Giả sử grade yêu cầu tuổi từ 3 đến 5 tròn
        return age >= 3 && age <= 5;
    }
}
