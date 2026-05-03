package santi_moder.roleplaymod.server.combat.weapon;

public record WeaponDamageProfile(
        String weaponId,
        String displayName,
        WeaponCategory category,
        float baseDamage,
        float bodyDamageMultiplier,
        float bloodLossMultiplier,
        float shockMultiplier,
        boolean causesHeavyBleeding,
        boolean isHighPenetration
) {
    public static WeaponDamageProfile generic() {
        return new WeaponDamageProfile(
                "unknown",
                "Unknown",
                WeaponCategory.GENERIC,
                5.0F,
                1.0F,
                1.0F,
                1.0F,
                false,
                false
        );
    }
}