package santi_moder.roleplaymod.server.combat.weapon;

public record AmmoCaliberProfile(
        AmmoCaliber caliber,
        float baseDamage,
        float bloodMultiplier,
        float shockMultiplier,
        boolean highPenetration,
        boolean heavyBleedPotential
) {
    public static AmmoCaliberProfile of(AmmoCaliber caliber) {
        return switch (caliber) {
            case NINE_MM -> new AmmoCaliberProfile(caliber, 5.2F, 0.75F, 0.85F, false, false);
            case FORTY_FIVE_ACP -> new AmmoCaliberProfile(caliber, 6.0F, 0.90F, 1.00F, false, false);
            case FIFTY_AE -> new AmmoCaliberProfile(caliber, 9.2F, 1.15F, 1.30F, true, true);

            case TWELVE_GAUGE -> new AmmoCaliberProfile(caliber, 11.0F, 1.35F, 1.40F, false, true);

            case FIVE_FIVE_SIX -> new AmmoCaliberProfile(caliber, 8.0F, 1.05F, 1.10F, true, true);
            case SEVEN_SIX_TWO -> new AmmoCaliberProfile(caliber, 9.5F, 1.15F, 1.25F, true, true);
            case THREE_ZERO_EIGHT -> new AmmoCaliberProfile(caliber, 7.2F, 1.00F, 1.05F, true, true);

            case THIRTY_ZERO_SIX -> new AmmoCaliberProfile(caliber, 14.5F, 1.45F, 1.60F, true, true);
            case FIFTY_BMG -> new AmmoCaliberProfile(caliber, 20.0F, 1.90F, 2.10F, true, true);

            case RPG_ROCKET -> new AmmoCaliberProfile(caliber, 22.0F, 1.60F, 2.20F, false, true);
        };
    }
}