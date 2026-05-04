package santi_moder.roleplaymod.network.phone.whatsapp;

import net.minecraft.network.FriendlyByteBuf;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.fml.DistExecutor;
import net.minecraftforge.network.NetworkEvent;
import santi_moder.roleplaymod.client.phone.app.whatsapp.WhatsappClientSyncApplier;
import santi_moder.roleplaymod.common.whatsapp.sync.WhatsappChatPayload;

import java.util.function.Supplier;

public record WhatsappMessageAddedS2CPacket(WhatsappChatPayload chatPayload) {

    public static void encode(WhatsappMessageAddedS2CPacket packet, FriendlyByteBuf buf) {
        WhatsappChatPayload.encode(buf, packet.chatPayload);
    }

    public static WhatsappMessageAddedS2CPacket decode(FriendlyByteBuf buf) {
        return new WhatsappMessageAddedS2CPacket(WhatsappChatPayload.decode(buf));
    }

    public static void handle(WhatsappMessageAddedS2CPacket packet, Supplier<NetworkEvent.Context> supplier) {
        NetworkEvent.Context context = supplier.get();

        context.enqueueWork(() ->
                DistExecutor.unsafeRunWhenOn(Dist.CLIENT, () -> () ->
                        WhatsappClientSyncApplier.applyChatPayload(packet.chatPayload()))
        );

        context.setPacketHandled(true);
    }
}