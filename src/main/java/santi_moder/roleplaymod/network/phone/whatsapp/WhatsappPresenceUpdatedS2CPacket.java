package santi_moder.roleplaymod.network.phone.whatsapp;

import net.minecraft.network.FriendlyByteBuf;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.fml.DistExecutor;
import net.minecraftforge.network.NetworkEvent;
import santi_moder.roleplaymod.client.phone.app.whatsapp.WhatsappClientSyncApplier;
import santi_moder.roleplaymod.common.whatsapp.sync.WhatsappPresencePayload;

import java.util.function.Supplier;

public record WhatsappPresenceUpdatedS2CPacket(WhatsappPresencePayload payload) {

    public static void encode(WhatsappPresenceUpdatedS2CPacket packet, FriendlyByteBuf buf) {
        WhatsappPresencePayload.encode(buf, packet.payload);
    }

    public static WhatsappPresenceUpdatedS2CPacket decode(FriendlyByteBuf buf) {
        return new WhatsappPresenceUpdatedS2CPacket(WhatsappPresencePayload.decode(buf));
    }

    public static void handle(WhatsappPresenceUpdatedS2CPacket packet, Supplier<NetworkEvent.Context> supplier) {
        NetworkEvent.Context context = supplier.get();

        context.enqueueWork(() ->
                DistExecutor.unsafeRunWhenOn(Dist.CLIENT, () -> () ->
                        WhatsappClientSyncApplier.applyPresencePayload(packet.payload()))
        );

        context.setPacketHandled(true);
    }
}