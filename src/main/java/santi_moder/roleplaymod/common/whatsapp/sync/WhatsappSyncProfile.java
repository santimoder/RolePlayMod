package santi_moder.roleplaymod.common.whatsapp.sync;

import net.minecraft.network.FriendlyByteBuf;

public record WhatsappSyncProfile(
        String photoId,
        String about,
        String displayName,
        String phoneNumber
) {
    public static void encode(FriendlyByteBuf buf, WhatsappSyncProfile value) {
        buf.writeUtf(value.photoId());
        buf.writeUtf(value.about());
        buf.writeUtf(value.displayName());
        buf.writeUtf(value.phoneNumber());
    }

    public static WhatsappSyncProfile decode(FriendlyByteBuf buf) {
        return new WhatsappSyncProfile(
                buf.readUtf(),
                buf.readUtf(),
                buf.readUtf(),
                buf.readUtf()
        );
    }
}