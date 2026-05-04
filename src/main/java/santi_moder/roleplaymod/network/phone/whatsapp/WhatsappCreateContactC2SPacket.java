package santi_moder.roleplaymod.network.phone.whatsapp;

import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.server.level.ServerPlayer;
import net.minecraftforge.network.NetworkEvent;
import santi_moder.roleplaymod.common.whatsapp.server.WhatsappServerService;

import java.util.function.Supplier;

public record WhatsappCreateContactC2SPacket(String displayName, String phoneNumber) {

    public WhatsappCreateContactC2SPacket(String displayName, String phoneNumber) {
        this.displayName = displayName == null ? "" : displayName;
        this.phoneNumber = phoneNumber == null ? "" : phoneNumber;
    }

    public static void encode(WhatsappCreateContactC2SPacket packet, FriendlyByteBuf buf) {
        buf.writeUtf(packet.displayName);
        buf.writeUtf(packet.phoneNumber);
    }

    public static WhatsappCreateContactC2SPacket decode(FriendlyByteBuf buf) {
        return new WhatsappCreateContactC2SPacket(
                buf.readUtf(),
                buf.readUtf()
        );
    }

    public static void handle(WhatsappCreateContactC2SPacket packet, Supplier<NetworkEvent.Context> supplier) {
        NetworkEvent.Context context = supplier.get();

        context.enqueueWork(() -> {
            ServerPlayer player = context.getSender();
            if (player == null) {
                return;
            }

            WhatsappServerService.handleCreateContact(player, packet.displayName(), packet.phoneNumber());
        });

        context.setPacketHandled(true);
    }
}