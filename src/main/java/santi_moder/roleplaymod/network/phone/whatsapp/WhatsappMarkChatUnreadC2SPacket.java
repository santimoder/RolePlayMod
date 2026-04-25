package santi_moder.roleplaymod.network.phone.whatsapp;

import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.server.level.ServerPlayer;
import net.minecraftforge.network.NetworkEvent;
import santi_moder.roleplaymod.common.whatsapp.server.WhatsappServerService;

import java.util.function.Supplier;

public final class WhatsappMarkChatUnreadC2SPacket {

    private final String chatId;
    private final boolean unread;

    public WhatsappMarkChatUnreadC2SPacket(String chatId, boolean unread) {
        this.chatId = chatId == null ? "" : chatId;
        this.unread = unread;
    }

    public String chatId() {
        return chatId;
    }

    public boolean unread() {
        return unread;
    }

    public static void encode(WhatsappMarkChatUnreadC2SPacket packet, FriendlyByteBuf buf) {
        buf.writeUtf(packet.chatId);
        buf.writeBoolean(packet.unread);
    }

    public static WhatsappMarkChatUnreadC2SPacket decode(FriendlyByteBuf buf) {
        return new WhatsappMarkChatUnreadC2SPacket(
                buf.readUtf(),
                buf.readBoolean()
        );
    }

    public static void handle(WhatsappMarkChatUnreadC2SPacket packet, Supplier<NetworkEvent.Context> supplier) {
        NetworkEvent.Context context = supplier.get();

        context.enqueueWork(() -> {
            ServerPlayer player = context.getSender();
            if (player != null) {
                WhatsappServerService.handleMarkChatUnread(player, packet.chatId(), packet.unread());
            }
        });

        context.setPacketHandled(true);
    }
}