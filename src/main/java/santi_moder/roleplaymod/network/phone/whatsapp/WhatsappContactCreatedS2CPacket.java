package santi_moder.roleplaymod.network.phone.whatsapp;

import net.minecraft.network.FriendlyByteBuf;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.fml.DistExecutor;
import net.minecraftforge.network.NetworkEvent;
import santi_moder.roleplaymod.client.phone.app.whatsapp.WhatsappClientSyncApplier;
import santi_moder.roleplaymod.common.whatsapp.sync.WhatsappContactPayload;

import java.util.function.Supplier;

public record WhatsappContactCreatedS2CPacket(WhatsappContactPayload payload, boolean openChatAfterCreate) {

    public static void encode(WhatsappContactCreatedS2CPacket packet, FriendlyByteBuf buf) {
        WhatsappContactPayload.encode(buf, packet.payload);
        buf.writeBoolean(packet.openChatAfterCreate);
    }

    public static WhatsappContactCreatedS2CPacket decode(FriendlyByteBuf buf) {
        return new WhatsappContactCreatedS2CPacket(
                WhatsappContactPayload.decode(buf),
                buf.readBoolean()
        );
    }

    public static void handle(WhatsappContactCreatedS2CPacket packet, Supplier<NetworkEvent.Context> supplier) {
        NetworkEvent.Context context = supplier.get();

        context.enqueueWork(() ->
                DistExecutor.unsafeRunWhenOn(Dist.CLIENT, () -> () ->
                        WhatsappClientSyncApplier.applyCreatedContact(packet.payload(), packet.openChatAfterCreate()))
        );

        context.setPacketHandled(true);
    }
}