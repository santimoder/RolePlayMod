package santi_moder.roleplaymod.client.effects;

public final class ClientMedicalEffects {

    private static int damageHitTicks = 0;
    private static int bleedPulseTicks = 0;

    private ClientMedicalEffects() {}

    public static void triggerDamageHit() {
        damageHitTicks = 35; // menos de 2 segundos
    }

    public static void triggerBleedPulse() {
        bleedPulseTicks = 50;
    }

    public static void tick() {
        if (damageHitTicks > 0) damageHitTicks--;
        if (bleedPulseTicks > 0) bleedPulseTicks--;
    }

    public static float getDamageHitIntensity() {
        return damageHitTicks <= 0 ? 0.0f : damageHitTicks / 35.0f;
    }

    public static float getBleedPulseIntensity() {
        return bleedPulseTicks <= 0 ? 0.0f : bleedPulseTicks / 50.0f;
    }
}