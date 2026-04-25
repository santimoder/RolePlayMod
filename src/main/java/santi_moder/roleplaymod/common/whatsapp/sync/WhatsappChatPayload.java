package santi_moder.roleplaymod.common.whatsapp.sync;

import net.minecraft.network.FriendlyByteBuf;

import java.util.ArrayList;
import java.util.List;

public record WhatsappChatPayload(
        String id,
        String contactId,
        boolean pinned,
        boolean archived,
        int unreadCount,
        List<WhatsappSyncMessage> messages
) {
    public static void encode(FriendlyByteBuf buf, WhatsappChatPayload value) {
        buf.writeUtf(value.id());
        buf.writeUtf(value.contactId());
        buf.writeBoolean(value.pinned());
        buf.writeBoolean(value.archived());
        buf.writeInt(value.unreadCount());

        buf.writeInt(value.messages().size());
        for (WhatsappSyncMessage message : value.messages()) {
            WhatsappSyncMessage.encode(buf, message);
        }
    }

    public static WhatsappChatPayload decode(FriendlyByteBuf buf) {
        String id = buf.readUtf();
        String contactId = buf.readUtf();
        boolean pinned = buf.readBoolean();
        boolean archived = buf.readBoolean();
        int unreadCount = buf.readInt();

        int size = buf.readInt();
        List<WhatsappSyncMessage> messages = new ArrayList<>(size);
        for (int i = 0; i < size; i++) {
            messages.add(WhatsappSyncMessage.decode(buf));
        }

        return new WhatsappChatPayload(id, contactId, pinned, archived, unreadCount, messages);
    }
}