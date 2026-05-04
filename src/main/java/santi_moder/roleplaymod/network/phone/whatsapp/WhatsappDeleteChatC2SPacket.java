package santi_moder.roleplaymod.network.phone.whatsapp;

import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.server.level.ServerPlayer;
import net.minecraftforge.network.NetworkEvent;
import santi_moder.roleplaymod.common.whatsapp.server.WhatsappServerService;

import java.util.function.Supplier;

public record WhatsappDeleteChatC2SPacket(String chatId) {

    public WhatsappDeleteChatC2SPacket(String chatId) {
        this.chatId = chatId == null ? "" : chatId;
    }

    public static void encode(WhatsappDeleteChatC2SPacket packet, FriendlyByteBuf buf) {
        buf.writeUtf(packet.chatId);
    }

    public static WhatsappDeleteChatC2SPacket decode(FriendlyByteBuf buf) {
        return new WhatsappDeleteChatC2SPacket(buf.readUtf());
    }

    public static void handle(WhatsappDeleteChatC2SPacket packet, Supplier<NetworkEvent.Context> supplier) {
        NetworkEvent.Context context = supplier.get();

        context.enqueueWork(() -> {
            ServerPlayer player = context.getSender();
            if (player != null) {
                WhatsappServerService.handleDeleteChat(player, packet.chatId());
            }
        });

        context.setPacketHandled(true);
    }
}