package santi_moder.roleplaymod.network.phone.whatsapp;

import net.minecraft.network.FriendlyByteBuf;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.fml.DistExecutor;
import net.minecraftforge.network.NetworkEvent;
import santi_moder.roleplaymod.client.phone.app.whatsapp.WhatsappClientSyncApplier;
import santi_moder.roleplaymod.common.whatsapp.sync.WhatsappInitialStateSnapshot;

import java.util.function.Supplier;

public record WhatsappInitialStateS2CPacket(WhatsappInitialStateSnapshot snapshot) {

    public static void encode(WhatsappInitialStateS2CPacket packet, FriendlyByteBuf buf) {
        WhatsappInitialStateSnapshot.encode(buf, packet.snapshot);
    }

    public static WhatsappInitialStateS2CPacket decode(FriendlyByteBuf buf) {
        return new WhatsappInitialStateS2CPacket(WhatsappInitialStateSnapshot.decode(buf));
    }

    public static void handle(WhatsappInitialStateS2CPacket packet, Supplier<NetworkEvent.Context> supplier) {
        NetworkEvent.Context context = supplier.get();

        context.enqueueWork(() ->
                DistExecutor.unsafeRunWhenOn(Dist.CLIENT, () -> () ->
                        WhatsappClientSyncApplier.applyInitialState(packet.snapshot()))
        );

        context.setPacketHandled(true);
    }
}
