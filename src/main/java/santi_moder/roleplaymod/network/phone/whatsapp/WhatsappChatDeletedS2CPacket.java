package santi_moder.roleplaymod.network.phone.whatsapp;

import net.minecraft.network.FriendlyByteBuf;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.fml.DistExecutor;
import net.minecraftforge.network.NetworkEvent;
import santi_moder.roleplaymod.client.phone.app.whatsapp.WhatsappClientSyncApplier;

import java.util.function.Supplier;

public record WhatsappChatDeletedS2CPacket(String chatId) {

    public WhatsappChatDeletedS2CPacket(String chatId) {
        this.chatId = chatId == null ? "" : chatId;
    }

    public static void encode(WhatsappChatDeletedS2CPacket packet, FriendlyByteBuf buf) {
        buf.writeUtf(packet.chatId);
    }

    public static WhatsappChatDeletedS2CPacket decode(FriendlyByteBuf buf) {
        return new WhatsappChatDeletedS2CPacket(buf.readUtf());
    }

    public static void handle(WhatsappChatDeletedS2CPacket packet, Supplier<NetworkEvent.Context> supplier) {
        NetworkEvent.Context context = supplier.get();

        context.enqueueWork(() ->
                DistExecutor.unsafeRunWhenOn(Dist.CLIENT, () -> () ->
                        WhatsappClientSyncApplier.applyDeletedChatId(packet.chatId()))
        );

        context.setPacketHandled(true);
    }
}