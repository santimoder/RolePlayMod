package santi_moder.roleplaymod.network.phone.whatsapp;

import net.minecraft.network.FriendlyByteBuf;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.fml.DistExecutor;
import net.minecraftforge.network.NetworkEvent;
import santi_moder.roleplaymod.client.phone.app.whatsapp.WhatsappClientSyncApplier;
import santi_moder.roleplaymod.common.whatsapp.sync.WhatsappChatPayload;

import java.util.function.Supplier;

public record WhatsappChatClearedS2CPacket(WhatsappChatPayload payload) {

    public static void encode(WhatsappChatClearedS2CPacket packet, FriendlyByteBuf buf) {
        WhatsappChatPayload.encode(buf, packet.payload);
    }

    public static WhatsappChatClearedS2CPacket decode(FriendlyByteBuf buf) {
        return new WhatsappChatClearedS2CPacket(WhatsappChatPayload.decode(buf));
    }

    public static void handle(WhatsappChatClearedS2CPacket packet, Supplier<NetworkEvent.Context> supplier) {
        NetworkEvent.Context context = supplier.get();

        context.enqueueWork(() ->
                DistExecutor.unsafeRunWhenOn(Dist.CLIENT, () -> () ->
                        WhatsappClientSyncApplier.applyClearedChatPayload(packet.payload()))
        );

        context.setPacketHandled(true);
    }
}