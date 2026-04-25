package santi_moder.roleplaymod.common.whatsapp.sync;

import net.minecraft.network.FriendlyByteBuf;

public record WhatsappSyncContact(
        String id,
        String displayName,
        String phoneNumber,
        String photoId,
        String about,
        boolean blocked,
        int commonGroupsCount,
        int mediaCount
) {
    public static void encode(FriendlyByteBuf buf, WhatsappSyncContact value) {
        buf.writeUtf(value.id());
        buf.writeUtf(value.displayName());
        buf.writeUtf(value.phoneNumber());
        buf.writeUtf(value.photoId());
        buf.writeUtf(value.about());
        buf.writeBoolean(value.blocked());
        buf.writeInt(value.commonGroupsCount());
        buf.writeInt(value.mediaCount());
    }

    public static WhatsappSyncContact decode(FriendlyByteBuf buf) {
        return new WhatsappSyncContact(
                buf.readUtf(),
                buf.readUtf(),
                buf.readUtf(),
                buf.readUtf(),
                buf.readUtf(),
                buf.readBoolean(),
                buf.readInt(),
                buf.readInt()
        );
    }
}