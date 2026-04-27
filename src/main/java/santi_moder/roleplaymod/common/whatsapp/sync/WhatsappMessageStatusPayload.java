package santi_moder.roleplaymod.common.whatsapp.sync;

import net.minecraft.network.FriendlyByteBuf;
import santi_moder.roleplaymod.common.whatsapp.model.WhatsappMessageStatus;

public record WhatsappMessageStatusPayload(
        String chatId,
        String messageId,
        WhatsappMessageStatus status,
        long lastStatusUpdateAt,
        int unreadCount
) {
    public static void encode(FriendlyByteBuf buf, WhatsappMessageStatusPayload value) {
        buf.writeUtf(value.chatId());
        buf.writeUtf(value.messageId());
        buf.writeEnum(value.status());
        buf.writeLong(value.lastStatusUpdateAt());
        buf.writeInt(value.unreadCount());
    }

    public static WhatsappMessageStatusPayload decode(FriendlyByteBuf buf) {
        return new WhatsappMessageStatusPayload(
                buf.readUtf(),
                buf.readUtf(),
                buf.readEnum(WhatsappMessageStatus.class),
                buf.readLong(),
                buf.readInt()
        );
    }
}