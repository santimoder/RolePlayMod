package santi_moder.roleplaymod.common.util;

import net.minecraft.server.level.ServerPlayer;
import santi_moder.roleplaymod.common.player.BodyPart;
import santi_moder.roleplaymod.common.player.IPlayerData;

public class MedicalUtils {

    // ===== MUERTE =====
    public static boolean checkAndKill(ServerPlayer player, IPlayerData data) {
        if (data.getSangre() <= 0
                || data.getBodyHp(BodyPart.HEAD) <= 0
                || data.getBodyHp(BodyPart.TORSO) <= 0) {

            player.kill();
            return true;
        }
        return false;
    }

    // ===== INCONSCIENCIA =====
    public static boolean shouldBeUnconscious(IPlayerData data) {

        // Cabeza crítica (2–1 HP)
        if (data.getBodyHp(BodyPart.HEAD) <= 1
                && data.getBodyHp(BodyPart.HEAD) > 0) {
            return true;
        }

        // Sangre críticamente baja
        if (data.getSangre() <= 20 && data.getSangre() > 0) {
            return true;
        }

        // Límite de desmayos
        return data.getContadorInconsciencias() >= 3;
    }

    public static boolean canWakeUp(IPlayerData data) {
        return data.getSangre() >= 40
                && data.getBodyHp(BodyPart.HEAD) >= 3;
    }
}
