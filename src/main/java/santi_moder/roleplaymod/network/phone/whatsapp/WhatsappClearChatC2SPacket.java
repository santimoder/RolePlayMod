package santi_moder.roleplaymod.network.phone.whatsapp;

import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.server.level.ServerPlayer;
import net.minecraftforge.network.NetworkEvent;
import santi_moder.roleplaymod.common.whatsapp.server.WhatsappServerService;

import java.util.function.Supplier;

public final class WhatsappClearChatC2SPacket {

    private final String chatId;

    public WhatsappClearChatC2SPacket(String chatId) {
        this.chatId = chatId == null ? "" : chatId;
    }

    public String chatId() {
        return chatId;
    }

    public static void encode(WhatsappClearChatC2SPacket packet, FriendlyByteBuf buf) {
        buf.writeUtf(packet.chatId);
    }

    public static WhatsappClearChatC2SPacket decode(FriendlyByteBuf buf) {
        return new WhatsappClearChatC2SPacket(buf.readUtf());
    }

    public static void handle(WhatsappClearChatC2SPacket packet, Supplier<NetworkEvent.Context> supplier) {
        NetworkEvent.Context context = supplier.get();

        context.enqueueWork(() -> {
            ServerPlayer player = context.getSender();
            if (player == null) {
                return;
            }

            WhatsappServerService.handleClearChat(player, packet.chatId());
        });

        context.setPacketHandled(true);
    }
}