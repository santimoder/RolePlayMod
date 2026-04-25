package santi_moder.roleplaymod.network.radio;

import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.server.level.ServerPlayer;
import net.minecraftforge.network.NetworkEvent;
import net.minecraftforge.network.PacketDistributor;
import santi_moder.roleplaymod.common.radio.voice.RadioTransmissionManager;
import santi_moder.roleplaymod.network.ModNetwork;
import santi_moder.roleplaymod.voice.VoiceChatRadioBridge;

import java.util.function.Supplier;

public class StopRadioTransmissionPacket {

    public static void encode(StopRadioTransmissionPacket msg, FriendlyByteBuf buf) {
    }

    public static StopRadioTransmissionPacket decode(FriendlyByteBuf buf) {
        return new StopRadioTransmissionPacket();
    }

    public static void handle(StopRadioTransmissionPacket msg, Supplier<NetworkEvent.Context> ctx) {
        ctx.get().enqueueWork(() -> {
            ServerPlayer player = ctx.get().getSender();
            if (player == null) return;

            boolean wasTransmitting = RadioTransmissionManager.isTransmitting(player);

            RadioTransmissionManager.stop(player);
            VoiceChatRadioBridge.stopTransmission(player.getUUID());

            if (wasTransmitting) {
                ModNetwork.sendInventoryToClient(
                        new RadioTransmissionStatePacket(false),
                        PacketDistributor.PLAYER.with(() -> player)
                );
            }
        });
        ctx.get().setPacketHandled(true);
    }
}