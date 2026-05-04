package santi_moder.roleplaymod.network.phone.whatsapp;

import net.minecraft.network.FriendlyByteBuf;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.fml.DistExecutor;
import net.minecraftforge.network.NetworkEvent;
import santi_moder.roleplaymod.client.phone.app.whatsapp.WhatsappClientSyncApplier;
import santi_moder.roleplaymod.common.whatsapp.sync.WhatsappContactPayload;

import java.util.function.Supplier;

public record WhatsappContactUpdatedS2CPacket(WhatsappContactPayload payload) {

    public static void encode(WhatsappContactUpdatedS2CPacket packet, FriendlyByteBuf buf) {
        WhatsappContactPayload.encode(buf, packet.payload);
    }

    public static WhatsappContactUpdatedS2CPacket decode(FriendlyByteBuf buf) {
        return new WhatsappContactUpdatedS2CPacket(WhatsappContactPayload.decode(buf));
    }

    public static void handle(WhatsappContactUpdatedS2CPacket packet, Supplier<NetworkEvent.Context> supplier) {
        NetworkEvent.Context context = supplier.get();

        context.enqueueWork(() ->
                DistExecutor.unsafeRunWhenOn(Dist.CLIENT, () -> () ->
                        WhatsappClientSyncApplier.applyContactPayload(packet.payload()))
        );

        context.setPacketHandled(true);
    }
}