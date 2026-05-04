package santi_moder.roleplaymod.common.whatsapp.server;

import net.minecraft.server.level.ServerPlayer;

public final class WhatsappPresenceResolver {

    private WhatsappPresenceResolver() {
    }

    public static boolean isPlayerOnlineInServer(ServerPlayer player) {
        return player != null && !player.hasDisconnected();
    }

    public static boolean hasInternet(ServerPlayer player) {
        return player != null;

        // TODO: conectar con tu sistema real de internet del mod.
    }

    public static boolean hasBattery(ServerPlayer player) {
        return player != null;

        // TODO: conectar con tu sistema real de batería del teléfono/item.
    }
}