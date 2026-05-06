package santi_moder.roleplaymod.network.phone.whatsapp;

import net.minecraft.network.FriendlyByteBuf;
import net.minecraftforge.network.NetworkEvent;
import santi_moder.roleplaymod.client.phone.app.whatsapp.WhatsappClientSyncApplier;
import santi_moder.roleplaymod.common.whatsapp.sync.WhatsappSyncProfile;

import java.util.function.Supplier;

public final class WhatsappProfileUpdatedS2CPacket {

    private final WhatsappSyncProfile profile;

    public WhatsappProfileUpdatedS2CPacket(WhatsappSyncProfile profile) {
        this.profile = profile;
    }

    public static void encode(WhatsappProfileUpdatedS2CPacket packet, FriendlyByteBuf buf) {
        buf.writeUtf(packet.profile.photoId(), 64);
        buf.writeUtf(packet.profile.about(), 20);
        buf.writeUtf(packet.profile.displayName(), 30);
        buf.writeUtf(packet.profile.phoneNumber(), 20);
    }

    public static WhatsappProfileUpdatedS2CPacket decode(FriendlyByteBuf buf) {
        return new WhatsappProfileUpdatedS2CPacket(
                new WhatsappSyncProfile(
                        buf.readUtf(64),
                        buf.readUtf(20),
                        buf.readUtf(30),
                        buf.readUtf(20)
                )
        );
    }

    public static void handle(WhatsappProfileUpdatedS2CPacket packet, Supplier<NetworkEvent.Context> supplier) {
        NetworkEvent.Context context = supplier.get();

        context.enqueueWork(() -> {
            WhatsappClientSyncApplier.setPendingProfile(packet.profile);
        });

        context.setPacketHandled(true);
    }
}