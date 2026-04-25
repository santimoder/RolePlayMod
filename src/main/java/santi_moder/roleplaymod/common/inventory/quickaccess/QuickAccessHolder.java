package santi_moder.roleplaymod.common.inventory.quickaccess;

import net.minecraft.server.level.ServerPlayer;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class QuickAccessHolder {

    private static final Map<UUID, QuickAccessState> DATA = new HashMap<>();

    public static QuickAccessState get(ServerPlayer player) {
        return DATA.computeIfAbsent(player.getUUID(), id -> new QuickAccessState());
    }

    public static void clear(ServerPlayer player) {
        DATA.remove(player.getUUID());
    }
}