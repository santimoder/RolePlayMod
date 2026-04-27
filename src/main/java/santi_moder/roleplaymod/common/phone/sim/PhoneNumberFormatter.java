package santi_moder.roleplaymod.common.phone.sim;

public final class PhoneNumberFormatter {

    private PhoneNumberFormatter() {
    }

    public static String formatLocal(String rawNineDigits) {
        String digits = onlyDigits(rawNineDigits);

        if (digits.length() != 9) {
            return digits;
        }

        return digits.substring(0, 3)
                + " "
                + digits.substring(3, 6)
                + " "
                + digits.substring(6, 9);
    }

    public static String normalize(String value) {
        String digits = onlyDigits(value);

        if (digits.startsWith("598") && digits.length() == 12) {
            digits = digits.substring(3);
        }

        return digits;
    }

    public static boolean isValidNineDigitMobile(String value) {
        String digits = normalize(value);
        return digits.length() == 9 && digits.startsWith("09");
    }

    public static String onlyDigits(String value) {
        if (value == null || value.isBlank()) {
            return "";
        }

        StringBuilder builder = new StringBuilder();

        for (int i = 0; i < value.length(); i++) {
            char c = value.charAt(i);
            if (Character.isDigit(c)) {
                builder.append(c);
            }
        }

        return builder.toString();
    }
}