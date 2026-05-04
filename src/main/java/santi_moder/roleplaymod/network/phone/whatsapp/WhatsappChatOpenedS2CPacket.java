package santi_moder.roleplaymod.network.phone.whatsapp;

import net.minecraft.network.FriendlyByteBuf;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.fml.DistExecutor;
import net.minecraftforge.network.NetworkEvent;
import santi_moder.roleplaymod.client.phone.app.whatsapp.WhatsappClientSyncApplier;
import santi_moder.roleplaymod.common.whatsapp.sync.WhatsappChatPayload;

import java.util.function.Supplier;

public record WhatsappChatOpenedS2CPacket(WhatsappChatPayload payload) {

    public static void encode(WhatsappChatOpenedS2CPacket packet, FriendlyByteBuf buf) {
        WhatsappChatPayload.encode(buf, packet.payload);
    }

    public static WhatsappChatOpenedS2CPacket decode(FriendlyByteBuf buf) {
        return new WhatsappChatOpenedS2CPacket(WhatsappChatPayload.decode(buf));
    }

    public static void handle(WhatsappChatOpenedS2CPacket packet, Supplier<NetworkEvent.Context> supplier) {
        NetworkEvent.Context context = supplier.get();

        context.enqueueWork(() ->
                DistExecutor.unsafeRunWhenOn(Dist.CLIENT, () -> () ->
                        WhatsappClientSyncApplier.applyOpenedChatPayload(packet.payload()))
        );

        context.setPacketHandled(true);
    }
}