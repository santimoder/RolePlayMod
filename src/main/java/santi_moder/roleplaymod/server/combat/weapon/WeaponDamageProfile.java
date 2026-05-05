package santi_moder.roleplaymod.server.combat.weapon;

public record WeaponDamageProfile(
        String weaponId,
        String displayName,
        WeaponCategory category,
        AmmoCaliber caliber,
        float weaponDamageMultiplier,
        float bodyDamageMultiplier,
        float bloodLossMultiplier,
        float shockMultiplier,
        float effectiveRange,
        float maxRange,
        boolean enabled
) {
    public static WeaponDamageProfile disabled(String weaponId) {
        return new WeaponDamageProfile(
                weaponId,
                "Disabled",
                WeaponCategory.GENERIC,
                AmmoCaliber.NINE_MM,
                0.0F,
                0.0F,
                0.0F,
                0.0F,
                0.0F,
                0.0F,
                false
        );
    }

    public static WeaponDamageProfile generic() {
        return disabled("unknown");
    }

    public AmmoCaliberProfile caliberProfile() {
        return AmmoCaliberProfile.of(caliber);
    }

    public boolean causesHeavyBleeding() {
        return caliberProfile().heavyBleedPotential();
    }

    public boolean isHighPenetration() {
        return caliberProfile().highPenetration();
    }

    public float baseDamage() {
        return caliberProfile().baseDamage() * weaponDamageMultiplier;
    }
}