package santi_moder.roleplaymod.common.radio.voice;

import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.player.Player;
import santi_moder.roleplaymod.common.radio.RadioManager;

import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

public class RadioTransmissionManager {

    private static final Map<UUID, RadioVoiceState> STATES = new ConcurrentHashMap<>();

    private RadioTransmissionManager() {
    }

    public static RadioVoiceState get(Player player) {
        return STATES.computeIfAbsent(player.getUUID(), id -> new RadioVoiceState());
    }

    public static boolean canStartTransmission(Player player) {
        return player != null
                && RadioManager.hasUsableRadio(player)
                && RadioManager.isRadioPowered(player);
    }

    public static boolean isTransmitting(Player player) {
        return player != null && get(player).isTransmitting();
    }

    public static float getFrequency(Player player) {
        if (player == null) {
            return 0F;
        }

        RadioVoiceState state = get(player);

        if (state.isTransmitting()) {
            return state.getActiveFrequency();
        }

        return RadioManager.getFrequency(player);
    }

    public static void start(ServerPlayer player) {
        if (player == null) {
            return;
        }

        if (!canStartTransmission(player)) {
            stop(player);
            return;
        }

        RadioVoiceState state = get(player);
        state.setTransmitting(true);
        state.setActiveFrequency(RadioManager.getFrequency(player));

        System.out.println("[RADIO] start transmission -> " + player.getName().getString()
                + " | frequency=" + state.getActiveFrequency());
    }

    public static void stop(Player player) {
        if (player == null) {
            return;
        }

        get(player).clear();

        System.out.println("[RADIO] stop transmission -> " + player.getName().getString());
    }

    public static boolean shouldSendViaRadio(Player player) {
        if (player == null) {
            return false;
        }

        RadioVoiceState state = get(player);
        if (!state.isTransmitting()) {
            return false;
        }

        return RadioManager.canTransmit(player);
    }

    public static boolean canHearFrequency(Player listener, float frequency) {
        if (listener == null) {
            return false;
        }

        if (!RadioManager.hasUsableRadio(listener)) {
            return false;
        }

        if (!RadioManager.isRadioPowered(listener)) {
            return false;
        }

        float listenerFrequency = RadioManager.getFrequency(listener);
        return Math.abs(listenerFrequency - frequency) < 0.001F;
    }

    public static void remove(Player player) {
        if (player == null) {
            return;
        }

        STATES.remove(player.getUUID());
    }

    public static void removeByUuid(UUID uuid) {
        if (uuid == null) {
            return;
        }

        STATES.remove(uuid);
    }
}