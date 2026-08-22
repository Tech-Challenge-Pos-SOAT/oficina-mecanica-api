package com.postech.oficinamecanica.domain.customer;

public record Document(String value) {

    public Document {
        String digits = value == null ? "" : value.replaceAll("\\D", "");
        if (!isValidCpf(digits) && !isValidCnpj(digits)) {
            throw new InvalidDocumentException(value);
        }
        value = format(digits);
    }

    public String unformatted() {
        return value.replaceAll("\\D", "");
    }

    private static String format(String digits) {
        if (digits.length() == 11) {
            return digits.replaceFirst("(\\d{3})(\\d{3})(\\d{3})(\\d{2})", "$1.$2.$3-$4");
        }
        return digits.replaceFirst("(\\d{2})(\\d{3})(\\d{3})(\\d{4})(\\d{2})", "$1.$2.$3/$4-$5");
    }

    private static boolean isValidCpf(String digits) {
        if (digits.length() != 11 || isAllSameDigit(digits)) {
            return false;
        }
        int dv1 = checkDigit(digits.substring(0, 9), 10);
        int dv2 = checkDigit(digits.substring(0, 9) + dv1, 11);
        return digits.equals(digits.substring(0, 9) + dv1 + dv2);
    }

    private static boolean isValidCnpj(String digits) {
        if (digits.length() != 14 || isAllSameDigit(digits)) {
            return false;
        }
        int dv1 = checkDigit(digits.substring(0, 12), 5);
        int dv2 = checkDigit(digits.substring(0, 12) + dv1, 6);
        return digits.equals(digits.substring(0, 12) + dv1 + dv2);
    }

    private static int checkDigit(String base, int firstWeight) {
        int sum = 0;
        int weight = firstWeight;
        for (int i = 0; i < base.length(); i++) {
            sum += Character.getNumericValue(base.charAt(i)) * weight;
            weight--;
            if (weight < 2) {
                weight = 9;
            }
        }
        int remainder = sum % 11;
        return remainder < 2 ? 0 : 11 - remainder;
    }

    private static boolean isAllSameDigit(String digits) {
        return digits.chars().distinct().count() == 1;
    }
}
