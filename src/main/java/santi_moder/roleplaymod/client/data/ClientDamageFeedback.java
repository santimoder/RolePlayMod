package santi_moder.roleplaymod.client.data;

public final class ClientDamageFeedback {

    private static int damageTicks = 0;
    private static int bleedingTicks = 0;

    private static float damageIntensity = 0.0F;
    private static float bleedingIntensity = 0.0F;

    private ClientDamageFeedback() {
    }

    public static void triggerDamage(float intensity) {
        damageTicks = 40;
        damageIntensity = clamp(intensity);
    }

    public static void triggerBleeding(float intensity) {
        bleedingTicks = Math.max(bleedingTicks, 60);
        bleedingIntensity = Math.max(bleedingIntensity, clamp(intensity));
    }

    public static void tick() {
        if (damageTicks > 0) {
            damageTicks--;

            // Fade suave de intensidad
            damageIntensity *= 0.92F;

            if (damageTicks <= 0) {
                damageIntensity = 0.0F;
            }
        }

        if (bleedingTicks > 0) {
            bleedingTicks--;

            bleedingIntensity *= 0.97F; // más lento

            if (bleedingTicks <= 0) {
                bleedingIntensity = 0.0F;
            }
        }
    }

    public static int getDamageTicks() {
        return damageTicks;
    }

    public static int getBleedingTicks() {
        return bleedingTicks;
    }

    public static float getDamageIntensity() {
        return damageIntensity;
    }

    public static float getBleedingIntensity() {
        return bleedingIntensity;
    }

    public static boolean hasDamageEffect() {
        return damageTicks > 0;
    }

    public static boolean hasBleedingEffect() {
        return bleedingTicks > 0;
    }

    private static float clamp(float value) {
        return Math.max(0.15F, Math.min(1.0F, value));
    }
}