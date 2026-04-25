package santi_moder.roleplaymod.common.whatsapp.sync;

import net.minecraft.network.FriendlyByteBuf;

public record WhatsappSyncPresence(
        String contactId,
        boolean onlineInServer,
        boolean hasInternet,
        boolean hasBattery,
        long lastSeenTimestamp
) {
    public static void encode(FriendlyByteBuf buf, WhatsappSyncPresence value) {
        buf.writeUtf(value.contactId());
        buf.writeBoolean(value.onlineInServer());
        buf.writeBoolean(value.hasInternet());
        buf.writeBoolean(value.hasBattery());
        buf.writeLong(value.lastSeenTimestamp());
    }

    public static WhatsappSyncPresence decode(FriendlyByteBuf buf) {
        return new WhatsappSyncPresence(
                buf.readUtf(),
                buf.readBoolean(),
                buf.readBoolean(),
                buf.readBoolean(),
                buf.readLong()
        );
    }
}