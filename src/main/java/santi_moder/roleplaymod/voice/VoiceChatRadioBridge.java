package santi_moder.roleplaymod.voice;

import de.maxhenkel.voicechat.api.VoicechatConnection;
import de.maxhenkel.voicechat.api.VoicechatServerApi;
import de.maxhenkel.voicechat.api.audiochannel.StaticAudioChannel;
import de.maxhenkel.voicechat.api.events.MicrophonePacketEvent;
import net.minecraft.server.level.ServerPlayer;
import net.minecraftforge.server.ServerLifecycleHooks;
import santi_moder.roleplaymod.RolePlayMod;
import santi_moder.roleplaymod.common.radio.voice.RadioTransmissionManager;

import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class VoiceChatRadioBridge {

    private static final Map<UUID, Map<UUID, StaticAudioChannel>> RADIO_CHANNELS = new HashMap<>();

    private static VoicechatServerApi serverApi;

    private VoiceChatRadioBridge() {
    }

    public static void setServerApi(VoicechatServerApi api) {
        serverApi = api;
        RolePlayMod.LOGGER.info("[RADIO] VoiceChat API {}", api != null ? "cargada" : "limpiada");

        if (api == null) {
            RADIO_CHANNELS.clear();
        }
    }

    public static VoicechatServerApi getServerApi() {
        return serverApi;
    }

    public static boolean isAvailable() {
        return serverApi != null;
    }

    public static void handleMicrophonePacket(MicrophonePacketEvent event) {
        if (serverApi == null) return;
        if (ServerLifecycleHooks.getCurrentServer() == null) return;
        if (event == null || event.getSenderConnection() == null) return;

        ServerPlayer sender = resolveSender(event);
        if (sender == null) {
            RolePlayMod.LOGGER.warn("[RADIO] No se pudo resolver el jugador emisor desde la conexión de voz");
            return;
        }

        if (!RadioTransmissionManager.shouldSendViaRadio(sender)) {
            return;
        }

        float frequency = RadioTransmissionManager.getFrequency(sender);
        UUID senderId = sender.getUUID();

        Map<UUID, StaticAudioChannel> senderChannels =
                RADIO_CHANNELS.computeIfAbsent(senderId, id -> new HashMap<>());

        for (ServerPlayer listener : ServerLifecycleHooks.getCurrentServer().getPlayerList().getPlayers()) {
            if (listener == sender) {
                continue;
            }

            if (!RadioTransmissionManager.canHearFrequency(listener, frequency)) {
                removeChannel(senderChannels, listener);
                continue;
            }

            VoicechatConnection listenerConnection = serverApi.getConnectionOf(listener.getUUID());
            if (listenerConnection == null ||
                    !listenerConnection.isInstalled() ||
                    listenerConnection.isDisabled() ||
                    !listenerConnection.isConnected()) {
                removeChannel(senderChannels, listener);
                continue;
            }

            StaticAudioChannel channel = senderChannels.get(listener.getUUID());

            if (channel == null) {
                UUID channelId = UUID.nameUUIDFromBytes(
                        (senderId.toString() + ":" + listener.getUUID()).getBytes(StandardCharsets.UTF_8)
                );

                channel = serverApi.createStaticAudioChannel(
                        channelId,
                        serverApi.fromServerLevel(sender.serverLevel()),
                        listenerConnection
                );

                if (channel == null) {
                    RolePlayMod.LOGGER.warn("[RADIO] No se pudo crear canal estático para {} -> {}",
                            sender.getName().getString(),
                            listener.getName().getString());
                    continue;
                }

                senderChannels.put(listener.getUUID(), channel);
                RolePlayMod.LOGGER.info("[RADIO] Canal creado para {} -> {} en frecuencia {}",
                        sender.getName().getString(),
                        listener.getName().getString(),
                        frequency);
            }

            channel.send(event.getPacket());
        }
    }

    private static ServerPlayer resolveSender(MicrophonePacketEvent event) {
        VoicechatConnection connection = event.getSenderConnection();
        if (connection == null) {
            return null;
        }

        de.maxhenkel.voicechat.api.ServerPlayer apiPlayer = connection.getPlayer();
        if (apiPlayer == null) {
            return null;
        }

        UUID uuid = apiPlayer.getUuid();

        return ServerLifecycleHooks
                .getCurrentServer()
                .getPlayerList()
                .getPlayer(uuid);
    }

    private static void removeChannel(Map<UUID, StaticAudioChannel> senderChannels, ServerPlayer listener) {
        StaticAudioChannel removed = senderChannels.remove(listener.getUUID());
        if (removed != null) {
            removed.flush();
        }
    }

    public static void cleanup(UUID playerUuid) {
        Map<UUID, StaticAudioChannel> ownChannels = RADIO_CHANNELS.remove(playerUuid);
        int cleanedOwn = 0;
        int cleanedAsListener = 0;

        if (ownChannels != null) {
            cleanedOwn = ownChannels.size();
            for (StaticAudioChannel channel : ownChannels.values()) {
                channel.flush();
            }
            ownChannels.clear();
        }

        for (Map<UUID, StaticAudioChannel> channels : RADIO_CHANNELS.values()) {
            StaticAudioChannel removed = channels.remove(playerUuid);
            if (removed != null) {
                removed.flush();
                cleanedAsListener++;
            }
        }

        if (cleanedOwn > 0 || cleanedAsListener > 0) {
            RolePlayMod.LOGGER.info("[RADIO] Cleanup {} -> canales propios: {}, como listener: {}",
                    playerUuid, cleanedOwn, cleanedAsListener);
        }
    }

    public static void stopTransmission(UUID playerUuid) {
        cleanup(playerUuid);
    }
}