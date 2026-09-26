package com.hospital.validation;

import java.util.Arrays;
import java.util.List;
import java.util.regex.Pattern;

public class IndiaValidationUtils {

    // Accepts 10 digits starting with 6-9, or prefixed with +91 or +91- or +91 
    private static final Pattern PHONE_PATTERN = Pattern.compile("^(?:\\+91[\\s-]?)?[6-9]\\d{9}$");

    // 6-digit PIN Code not starting with 0
    private static final Pattern PINCODE_PATTERN = Pattern.compile("^[1-9][0-9]{5}$");

    public static final List<String> INDIAN_STATES_AND_UTS = Arrays.asList(
            // States
            "Andhra Pradesh", "Arunachal Pradesh", "Assam", "Bihar", "Chhattisgarh",
            "Goa", "Gujarat", "Haryana", "Himachal Pradesh", "Jharkhand", "Karnataka",
            "Kerala", "Madhya Pradesh", "Maharashtra", "Manipur", "Meghalaya", "Mizoram",
            "Nagaland", "Odisha", "Punjab", "Rajasthan", "Sikkim", "Tamil Nadu",
            "Telangana", "Tripura", "Uttar Pradesh", "Uttarakhand", "West Bengal",
            // Union Territories
            "Andaman and Nicobar Islands", "Chandigarh",
            "Dadra and Nagar Haveli and Daman and Diu", "Delhi",
            "Jammu and Kashmir", "Ladakh", "Lakshadweep", "Puducherry"
    );

    public static boolean isValidIndianPhone(String phone) {
        if (phone == null || phone.trim().isEmpty()) return false;
        String cleanPhone = phone.trim().replaceAll("[\\s-]", "");
        if (cleanPhone.equals("+910000000000") || cleanPhone.equals("0000000000") ||
            cleanPhone.equals("1234567890") || cleanPhone.equals("1111111111")) {
            return false;
        }
        return PHONE_PATTERN.matcher(phone.trim()).matches();
    }

    public static String normalizeIndianPhone(String phone) {
        if (phone == null) return null;
        String digits = phone.trim().replaceAll("[^0-9]", "");
        if (digits.length() == 10) {
            return "+91" + digits;
        } else if (digits.length() == 12 && digits.startsWith("91")) {
            return "+" + digits;
        }
        return phone.trim();
    }

    public static boolean isValidPincode(String pincode) {
        if (pincode == null || pincode.trim().isEmpty()) return false;
        return PINCODE_PATTERN.matcher(pincode.trim()).matches();
    }

    public static boolean isValidState(String state) {
        if (state == null || state.trim().isEmpty()) return true; // Optional field
        return INDIAN_STATES_AND_UTS.stream().anyMatch(s -> s.equalsIgnoreCase(state.trim()));
    }
}
