package santi_moder.roleplaymod.network.phone.whatsapp;

import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.server.level.ServerPlayer;
import net.minecraftforge.network.NetworkEvent;
import santi_moder.roleplaymod.common.whatsapp.server.WhatsappServerService;

import java.util.function.Supplier;

public final class WhatsappArchiveChatC2SPacket {

    private final String chatId;

    public WhatsappArchiveChatC2SPacket(String chatId) {
        this.chatId = chatId == null ? "" : chatId;
    }

    public String chatId() {
        return chatId;
    }

    public static void encode(WhatsappArchiveChatC2SPacket packet, FriendlyByteBuf buf) {
        buf.writeUtf(packet.chatId);
    }

    public static WhatsappArchiveChatC2SPacket decode(FriendlyByteBuf buf) {
        return new WhatsappArchiveChatC2SPacket(buf.readUtf());
    }

    public static void handle(WhatsappArchiveChatC2SPacket packet, Supplier<NetworkEvent.Context> supplier) {
        NetworkEvent.Context context = supplier.get();

        context.enqueueWork(() -> {
            ServerPlayer player = context.getSender();
            if (player != null) {
                WhatsappServerService.handleArchiveChat(player, packet.chatId());
            }
        });

        context.setPacketHandled(true);
    }
}