package santi_moder.roleplaymod.server.combat.weapon;

public record WeaponDamageProfile(
        WeaponCategory category,
        float bodyDamageMultiplier,
        float bloodLossMultiplier,
        float shockMultiplier,
        boolean causesHeavyBleeding,
        boolean isHighPenetration
) {
    public static WeaponDamageProfile forCategory(WeaponCategory category) {
        return switch (category) {
            case PISTOL -> new WeaponDamageProfile(category, 1.00F, 1.00F, 1.00F, false, false);
            case SMG -> new WeaponDamageProfile(category, 0.75F, 0.85F, 0.85F, false, false);
            case RIFLE -> new WeaponDamageProfile(category, 1.35F, 1.25F, 1.30F, true, true);
            case SHOTGUN -> new WeaponDamageProfile(category, 1.60F, 1.50F, 1.45F, true, false);
            case SNIPER -> new WeaponDamageProfile(category, 2.20F, 1.80F, 2.00F, true, true);
            case EXPLOSIVE -> new WeaponDamageProfile(category, 1.80F, 1.50F, 2.00F, true, false);
            case MELEE -> new WeaponDamageProfile(category, 0.60F, 0.35F, 0.55F, false, false);
            case GENERIC -> new WeaponDamageProfile(category, 1.00F, 1.00F, 1.00F, false, false);
        };
    }
}