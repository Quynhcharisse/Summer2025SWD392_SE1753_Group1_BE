package com.swd392.group1.pes.utils;

import java.util.Map;
import java.util.Random;
import java.util.concurrent.ConcurrentHashMap;

public class OtpUtil {
    public static final Map<String, OtpInfo> otpStore = new ConcurrentHashMap<>();
    public static final int OTP_EXPIRY_MINUTES = 5;

    // Store OTP for an email with expiry
    public static void otpStore(String email, String otp, long expiry) {
        OtpInfo otpInfo = new OtpInfo(otp, expiry);
        otpStore.put(email, otpInfo);
    }

    // Get OTP info if not expired, else return null
    public static OtpInfo getOtpInfo(String email) {
        OtpInfo otpInfo = otpStore.get(email);
        if (otpInfo != null && System.currentTimeMillis() < otpInfo.expiryTime) {
            return otpInfo;
        }
        return null;
    }

    // Remove OTP for an email
    public static void removeOtp(String email) {
        otpStore.remove(email);
    }

    public static class OtpInfo {
        public String otp;
        public long expiryTime;
        public boolean verified;
        OtpInfo(String otp, long expiryTime) {
            this.otp = otp;
            this.expiryTime = expiryTime;
            this.verified = false;
        }
    }

    // Generate a 6-digit OTP
    public static String generateOtp() {
        Random r = new Random();
        int otp = 100000 + r.nextInt(900000);
        return String.valueOf(otp);
    }
}
