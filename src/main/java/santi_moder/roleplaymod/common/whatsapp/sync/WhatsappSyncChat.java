package santi_moder.roleplaymod.common.whatsapp.sync;

import net.minecraft.network.FriendlyByteBuf;

import java.util.ArrayList;
import java.util.List;

public record WhatsappSyncChat(
        String id,
        String contactId,
        boolean pinned,
        int unreadCount,
        List<WhatsappSyncMessage> messages
) {
    public static void encode(FriendlyByteBuf buf, WhatsappSyncChat value) {
        buf.writeUtf(value.id());
        buf.writeUtf(value.contactId());
        buf.writeBoolean(value.pinned());
        buf.writeInt(value.unreadCount());

        buf.writeInt(value.messages().size());
        for (WhatsappSyncMessage message : value.messages()) {
            WhatsappSyncMessage.encode(buf, message);
        }
    }

    public static WhatsappSyncChat decode(FriendlyByteBuf buf) {
        String id = buf.readUtf();
        String contactId = buf.readUtf();
        boolean pinned = buf.readBoolean();
        int unreadCount = buf.readInt();

        int size = buf.readInt();
        List<WhatsappSyncMessage> messages = new ArrayList<>(size);
        for (int i = 0; i < size; i++) {
            messages.add(WhatsappSyncMessage.decode(buf));
        }

        return new WhatsappSyncChat(id, contactId, pinned, unreadCount, messages);
    }
}