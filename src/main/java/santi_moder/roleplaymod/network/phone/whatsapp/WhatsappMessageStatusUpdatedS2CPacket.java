package santi_moder.roleplaymod.network.phone.whatsapp;

import net.minecraft.network.FriendlyByteBuf;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.fml.DistExecutor;
import net.minecraftforge.network.NetworkEvent;
import santi_moder.roleplaymod.client.phone.app.whatsapp.WhatsappClientSyncApplier;
import santi_moder.roleplaymod.common.whatsapp.sync.WhatsappMessageStatusPayload;

import java.util.function.Supplier;

public final class WhatsappMessageStatusUpdatedS2CPacket {

    private final WhatsappMessageStatusPayload payload;

    public WhatsappMessageStatusUpdatedS2CPacket(WhatsappMessageStatusPayload payload) {
        this.payload = payload;
    }

    public WhatsappMessageStatusPayload payload() {
        return payload;
    }

    public static void encode(WhatsappMessageStatusUpdatedS2CPacket packet, FriendlyByteBuf buf) {
        WhatsappMessageStatusPayload.encode(buf, packet.payload);
    }

    public static WhatsappMessageStatusUpdatedS2CPacket decode(FriendlyByteBuf buf) {
        return new WhatsappMessageStatusUpdatedS2CPacket(WhatsappMessageStatusPayload.decode(buf));
    }

    public static void handle(WhatsappMessageStatusUpdatedS2CPacket packet, Supplier<NetworkEvent.Context> supplier) {
        NetworkEvent.Context context = supplier.get();

        context.enqueueWork(() ->
                DistExecutor.unsafeRunWhenOn(Dist.CLIENT, () -> () ->
                        WhatsappClientSyncApplier.applyMessageStatusPayload(packet.payload()))
        );

        context.setPacketHandled(true);
    }
}