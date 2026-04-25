package santi_moder.roleplaymod.common.whatsapp.sync;

import net.minecraft.network.FriendlyByteBuf;
import santi_moder.roleplaymod.client.phone.app.whatsapp.WhatsappMessageStatus;

public record WhatsappSyncMessage(
        String id,
        String text,
        boolean sentByMe,
        String timeText,
        long sortTimestamp,
        WhatsappMessageStatus status,
        long lastStatusUpdateAt
) {
    public static void encode(FriendlyByteBuf buf, WhatsappSyncMessage value) {
        buf.writeUtf(value.id());
        buf.writeUtf(value.text());
        buf.writeBoolean(value.sentByMe());
        buf.writeUtf(value.timeText());
        buf.writeLong(value.sortTimestamp());
        buf.writeEnum(value.status());
        buf.writeLong(value.lastStatusUpdateAt());
    }

    public static WhatsappSyncMessage decode(FriendlyByteBuf buf) {
        return new WhatsappSyncMessage(
                buf.readUtf(),
                buf.readUtf(),
                buf.readBoolean(),
                buf.readUtf(),
                buf.readLong(),
                buf.readEnum(WhatsappMessageStatus.class),
                buf.readLong()
        );
    }
}