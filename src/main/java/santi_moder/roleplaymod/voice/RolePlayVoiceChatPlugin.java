package santi_moder.roleplaymod.voice;

import de.maxhenkel.voicechat.api.ForgeVoicechatPlugin;
import de.maxhenkel.voicechat.api.VoicechatPlugin;
import de.maxhenkel.voicechat.api.VoicechatServerApi;
import de.maxhenkel.voicechat.api.events.EventRegistration;
import de.maxhenkel.voicechat.api.events.MicrophonePacketEvent;
import de.maxhenkel.voicechat.api.events.PlayerDisconnectedEvent;
import de.maxhenkel.voicechat.api.events.VoicechatServerStartedEvent;
import de.maxhenkel.voicechat.api.events.VoicechatServerStoppedEvent;
import santi_moder.roleplaymod.common.radio.voice.RadioTransmissionManager;

@ForgeVoicechatPlugin
public class RolePlayVoiceChatPlugin implements VoicechatPlugin {

    @Override
    public String getPluginId() {
        return "roleplaymod_radio";
    }

    @Override
    public void registerEvents(EventRegistration registration) {
        registration.registerEvent(VoicechatServerStartedEvent.class, this::onServerStarted);
        registration.registerEvent(VoicechatServerStoppedEvent.class, this::onServerStopped);
        registration.registerEvent(MicrophonePacketEvent.class, this::onMicrophonePacket);
        registration.registerEvent(PlayerDisconnectedEvent.class, this::onPlayerDisconnected);
    }

    private void onServerStarted(VoicechatServerStartedEvent event) {
        santi_moder.roleplaymod.RolePlayMod.LOGGER.info("[RADIO] Voicechat server started");
        VoicechatServerApi api = event.getVoicechat();
        VoiceChatRadioBridge.setServerApi(api);
    }

    private void onServerStopped(VoicechatServerStoppedEvent event) {
        VoiceChatRadioBridge.setServerApi(null);
    }

    private void onMicrophonePacket(MicrophonePacketEvent event) {
        santi_moder.roleplaymod.RolePlayMod.LOGGER.info("[RADIO] onMicrophonePacket disparado");
        VoiceChatRadioBridge.handleMicrophonePacket(event);
    }

    private void onPlayerDisconnected(PlayerDisconnectedEvent event) {
        VoiceChatRadioBridge.cleanup(event.getPlayerUuid());
        RadioTransmissionManager.removeByUuid(event.getPlayerUuid());
    }
}