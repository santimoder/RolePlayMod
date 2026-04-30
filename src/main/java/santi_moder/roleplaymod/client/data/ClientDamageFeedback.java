package santi_moder.roleplaymod.client.data;

public final class ClientDamageFeedback {

    private static int damageTicks = 0;
    private static int bleedingTicks = 0;

    private ClientDamageFeedback() {
    }

    public static void triggerDamage() {
        damageTicks = Math.max(damageTicks, 14);
    }

    public static void triggerBleeding() {
        bleedingTicks = Math.max(bleedingTicks, 60);
    }

    public static void tick() {
        if (damageTicks > 0) damageTicks--;
        if (bleedingTicks > 0) bleedingTicks--;
    }

    public static int getDamageTicks() {
        return damageTicks;
    }

    public static int getBleedingTicks() {
        return bleedingTicks;
    }

    public static boolean hasDamageEffect() {
        return damageTicks > 0;
    }

    public static boolean hasBleedingEffect() {
        return bleedingTicks > 0;
    }
}