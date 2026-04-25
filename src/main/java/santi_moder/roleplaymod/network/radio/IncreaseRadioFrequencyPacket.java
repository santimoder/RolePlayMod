package santi_moder.roleplaymod.network.radio;

import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.server.level.ServerPlayer;
import net.minecraftforge.network.NetworkEvent;
import santi_moder.roleplaymod.common.radio.RadioManager;

import java.util.function.Supplier;

public class IncreaseRadioFrequencyPacket {

    public IncreaseRadioFrequencyPacket() {
    }

    public static void encode(IncreaseRadioFrequencyPacket msg, FriendlyByteBuf buf) {
    }

    public static IncreaseRadioFrequencyPacket decode(FriendlyByteBuf buf) {
        return new IncreaseRadioFrequencyPacket();
    }

    public static void handle(IncreaseRadioFrequencyPacket msg, Supplier<NetworkEvent.Context> ctx) {
        ctx.get().enqueueWork(() -> {
            ServerPlayer player = ctx.get().getSender();
            if (player != null) {
                RadioManager.increaseFrequency(player);
            }
        });
        ctx.get().setPacketHandled(true);
    }
}