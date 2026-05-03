package santi_moder.roleplaymod.common.util;

import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.damagesource.DamageSource;
import santi_moder.roleplaymod.common.player.BodyPart;
import santi_moder.roleplaymod.common.player.IPlayerData;

public final class MedicalUtils {

    private MedicalUtils() {
    }

    public static boolean checkAndKill(ServerPlayer player, IPlayerData data) {
        if (player == null || data == null) return false;
        if (player.isDeadOrDying()) return true;

        if (isMedicallyDead(data)) {
            forceMedicalDeath(player);
            return true;
        }

        return false;
    }

    public static boolean isMedicallyDead(IPlayerData data) {
        if (data == null) return false;

        return data.getSangre() <= 0
                || data.getBodyHp(BodyPart.HEAD) <= 0
                || data.getBodyHp(BodyPart.TORSO) <= 0;
    }

    public static void forceMedicalDeath(ServerPlayer player) {
        if (player == null) return;
        if (player.isDeadOrDying()) return;

        DamageSource source = player.damageSources().genericKill();

        player.setHealth(0.0F);
        player.die(source);
    }

    public static int getUnconsciousDurationTicks(IPlayerData data, int recentBloodLoss) {
        if (data == null) return 0;
        if (isMedicallyDead(data)) return 0;

        if (data.getBodyHp(BodyPart.HEAD) > 0 && data.getBodyHp(BodyPart.HEAD) <= 3) {
            return 30 * 20;
        }

        if (data.getSangre() > 0 && data.getSangre() <= 15) {
            return 25 * 20;
        }

        if (data.getShock() >= 90) {
            return 25 * 20;
        }

        if (data.getShock() >= 85) {
            return 15 * 20;
        }

        if (recentBloodLoss >= 25) {
            return 15 * 20;
        }

        return 0;
    }

    public static boolean canWakeUp(IPlayerData data) {
        if (data == null) return false;
        if (isMedicallyDead(data)) return false;

        return data.getUnconsciousTicks() <= 0;
    }
}