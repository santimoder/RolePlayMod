package santi_moder.roleplaymod.server.combat;

public enum DamageSeverity {
    LIGHT,
    MEDIUM,
    HEAVY,
    CRITICAL;

    public static DamageSeverity fromDamage(float rawDamage) {
        if (rawDamage >= 10.0F) return CRITICAL;
        if (rawDamage >= 7.0F) return HEAVY;
        if (rawDamage >= 4.0F) return MEDIUM;
        return LIGHT;
    }
}