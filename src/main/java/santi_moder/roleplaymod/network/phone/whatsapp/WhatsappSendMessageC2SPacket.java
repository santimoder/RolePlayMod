package santi_moder.roleplaymod.network.phone.whatsapp;

import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.server.level.ServerPlayer;
import net.minecraftforge.network.NetworkEvent;
import santi_moder.roleplaymod.common.whatsapp.server.WhatsappServerService;

import java.util.function.Supplier;

public record WhatsappSendMessageC2SPacket(String contactId, String text) {

    public WhatsappSendMessageC2SPacket(String contactId, String text) {
        this.contactId = contactId == null ? "" : contactId;
        this.text = text == null ? "" : text;
    }

    public static void encode(WhatsappSendMessageC2SPacket packet, FriendlyByteBuf buf) {
        buf.writeUtf(packet.contactId);
        buf.writeUtf(packet.text);
    }

    public static WhatsappSendMessageC2SPacket decode(FriendlyByteBuf buf) {
        return new WhatsappSendMessageC2SPacket(
                buf.readUtf(),
                buf.readUtf()
        );
    }

    public static void handle(WhatsappSendMessageC2SPacket packet, Supplier<NetworkEvent.Context> supplier) {
        NetworkEvent.Context context = supplier.get();

        context.enqueueWork(() -> {
            ServerPlayer player = context.getSender();
            if (player == null) {
                return;
            }

            WhatsappServerService.handleSendMessage(player, packet.contactId(), packet.text());
        });

        context.setPacketHandled(true);
    }
}