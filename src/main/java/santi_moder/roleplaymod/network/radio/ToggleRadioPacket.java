package santi_moder.roleplaymod.network.radio;

import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.server.level.ServerPlayer;
import net.minecraftforge.network.NetworkEvent;
import santi_moder.roleplaymod.common.radio.RadioManager;

import java.util.function.Supplier;

public class ToggleRadioPacket {

    public ToggleRadioPacket() {
    }

    public static void encode(ToggleRadioPacket msg, FriendlyByteBuf buf) {
    }

    public static ToggleRadioPacket decode(FriendlyByteBuf buf) {
        return new ToggleRadioPacket();
    }

    public static void handle(ToggleRadioPacket msg, Supplier<NetworkEvent.Context> ctx) {
        ctx.get().enqueueWork(() -> {
            ServerPlayer player = ctx.get().getSender();
            if (player != null) {
                RadioManager.toggleRadio(player);
            }
        });
        ctx.get().setPacketHandled(true);
    }
}