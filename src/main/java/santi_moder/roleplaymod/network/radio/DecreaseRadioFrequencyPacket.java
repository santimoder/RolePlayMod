package santi_moder.roleplaymod.network.radio;

import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.server.level.ServerPlayer;
import net.minecraftforge.network.NetworkEvent;
import santi_moder.roleplaymod.common.radio.RadioManager;

import java.util.function.Supplier;

public class DecreaseRadioFrequencyPacket {

    public DecreaseRadioFrequencyPacket() {
    }

    public static void encode(DecreaseRadioFrequencyPacket msg, FriendlyByteBuf buf) {
    }

    public static DecreaseRadioFrequencyPacket decode(FriendlyByteBuf buf) {
        return new DecreaseRadioFrequencyPacket();
    }

    public static void handle(DecreaseRadioFrequencyPacket msg, Supplier<NetworkEvent.Context> ctx) {
        ctx.get().enqueueWork(() -> {
            ServerPlayer player = ctx.get().getSender();
            if (player != null) {
                RadioManager.decreaseFrequency(player);
            }
        });
        ctx.get().setPacketHandled(true);
    }
}