package santi_moder.roleplaymod.common.util;

import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.damagesource.DamageSource;
import santi_moder.roleplaymod.common.player.BodyPart;
import santi_moder.roleplaymod.common.player.IPlayerData;

public final class MedicalUtils {

    private MedicalUtils() {
    }

    public static boolean checkAndKill(ServerPlayer player, IPlayerData data) {
        if (isMedicallyDead(data)) {
            forceMedicalDeath(player);
            return true;
        }

        return false;
    }

    public static boolean isMedicallyDead(IPlayerData data) {
        return data.getSangre() <= 0
                || data.getBodyHp(BodyPart.HEAD) <= 0
                || data.getBodyHp(BodyPart.TORSO) <= 0;
    }

    public static void forceMedicalDeath(ServerPlayer player) {
        if (player.isDeadOrDying()) return;

        DamageSource source = player.damageSources().genericKill();

        player.setHealth(0.0F);
        player.die(source);
    }

    public static boolean shouldBeUnconscious(IPlayerData data) {
        if (isMedicallyDead(data)) return false;

        if (data.getShock() >= 85) return true;

        if (data.getBodyHp(BodyPart.HEAD) <= 1 && data.getBodyHp(BodyPart.HEAD) > 0) {
            return true;
        }

        if (data.getSangre() <= 20 && data.getSangre() > 0) {
            return true;
        }

        return data.getContadorInconsciencias() >= 3;
    }

    public static boolean canWakeUp(IPlayerData data) {
        return data.getSangre() >= 40
                && data.getShock() <= 45
                && data.getBodyHp(BodyPart.HEAD) >= 3
                && data.getBodyHp(BodyPart.TORSO) > 0;
    }
}