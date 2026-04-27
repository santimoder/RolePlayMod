package santi_moder.roleplaymod.common.phone.sim;

import java.util.Random;

public final class PhoneNumberGenerator {

    private PhoneNumberGenerator() {
    }

    public static String generateRawNumber(PhoneCarrier carrier, Random random) {
        PhoneCarrier safeCarrier = carrier == null ? PhoneCarrier.ANTEL : carrier;
        Random safeRandom = random == null ? new Random() : random;

        String prefix = safeCarrier.randomPrefix(safeRandom);
        int suffix = safeRandom.nextInt(1_000_000);

        return prefix + String.format("%06d", suffix);
    }

    public static String generateFormattedNumber(PhoneCarrier carrier, Random random) {
        return PhoneNumberFormatter.formatLocal(generateRawNumber(carrier, random));
    }
}