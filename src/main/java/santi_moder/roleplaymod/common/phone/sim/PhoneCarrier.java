package santi_moder.roleplaymod.common.phone.sim;

import java.util.List;
import java.util.Locale;
import java.util.Random;

public enum PhoneCarrier {
    ANTEL("Antel", List.of("091", "092", "098", "099")),
    MOVISTAR("Movistar", List.of("093", "094", "095")),
    CLARO("Claro", List.of("096", "097"));

    private final String displayName;
    private final List<String> prefixes;

    PhoneCarrier(String displayName, List<String> prefixes) {
        this.displayName = displayName;
        this.prefixes = prefixes;
    }

    public String displayName() {
        return displayName;
    }

    public List<String> prefixes() {
        return prefixes;
    }

    public String randomPrefix(Random random) {
        return prefixes.get(random.nextInt(prefixes.size()));
    }

    public static PhoneCarrier fromName(String value) {
        if (value == null || value.isBlank()) {
            return ANTEL;
        }

        String normalized = value.trim().toLowerCase(Locale.ROOT);

        return switch (normalized) {
            case "antel" -> ANTEL;
            case "movistar" -> MOVISTAR;
            case "claro" -> CLARO;
            default -> ANTEL;
        };
    }
}