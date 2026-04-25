package santi_moder.roleplaymod.network.radio;

import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.server.level.ServerPlayer;
import net.minecraftforge.network.NetworkEvent;
import net.minecraftforge.network.PacketDistributor;
import santi_moder.roleplaymod.common.radio.voice.RadioTransmissionManager;
import santi_moder.roleplaymod.network.ModNetwork;

import java.util.function.Supplier;

public class StartRadioTransmissionPacket {

    public static void encode(StartRadioTransmissionPacket msg, FriendlyByteBuf buf) {
    }

    public static StartRadioTransmissionPacket decode(FriendlyByteBuf buf) {
        return new StartRadioTransmissionPacket();
    }

    public static void handle(StartRadioTransmissionPacket msg, Supplier<NetworkEvent.Context> ctx) {
        ctx.get().enqueueWork(() -> {
            ServerPlayer player = ctx.get().getSender();
            if (player == null) return;

            boolean wasTransmitting = RadioTransmissionManager.isTransmitting(player);

            RadioTransmissionManager.start(player);

            boolean isNowTransmitting = RadioTransmissionManager.isTransmitting(player);

            if (!wasTransmitting && isNowTransmitting) {
                ModNetwork.sendInventoryToClient(
                        new RadioTransmissionStatePacket(true),
                        PacketDistributor.PLAYER.with(() -> player)
                );
            }
        });
        ctx.get().setPacketHandled(true);
    }
}