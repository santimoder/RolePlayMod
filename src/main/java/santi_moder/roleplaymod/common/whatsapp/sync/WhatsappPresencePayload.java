package santi_moder.roleplaymod.common.whatsapp.sync;

import net.minecraft.network.FriendlyByteBuf;

public record WhatsappPresencePayload(
        String contactId,
        boolean onlineInServer,
        boolean hasInternet,
        boolean hasBattery,
        long lastSeenTimestamp
) {
    public static void encode(FriendlyByteBuf buf, WhatsappPresencePayload value) {
        buf.writeUtf(value.contactId());
        buf.writeBoolean(value.onlineInServer());
        buf.writeBoolean(value.hasInternet());
        buf.writeBoolean(value.hasBattery());
        buf.writeLong(value.lastSeenTimestamp());
    }

    public static WhatsappPresencePayload decode(FriendlyByteBuf buf) {
        return new WhatsappPresencePayload(
                buf.readUtf(),
                buf.readBoolean(),
                buf.readBoolean(),
                buf.readBoolean(),
                buf.readLong()
        );
    }
}