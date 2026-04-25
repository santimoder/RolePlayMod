package santi_moder.roleplaymod.common.whatsapp.sync;

import net.minecraft.network.FriendlyByteBuf;

import java.util.ArrayList;
import java.util.List;

public record WhatsappInitialStateSnapshot(
        List<WhatsappSyncChat> chats,
        List<WhatsappSyncContact> contacts,
        List<WhatsappSyncPresence> presences,
        WhatsappSyncProfile profile,
        boolean localUserInServer,
        boolean localUserHasInternet,
        boolean localUserHasBattery
) {
    public static void encode(FriendlyByteBuf buf, WhatsappInitialStateSnapshot value) {
        buf.writeInt(value.chats().size());
        for (WhatsappSyncChat chat : value.chats()) {
            WhatsappSyncChat.encode(buf, chat);
        }

        buf.writeInt(value.contacts().size());
        for (WhatsappSyncContact contact : value.contacts()) {
            WhatsappSyncContact.encode(buf, contact);
        }

        buf.writeInt(value.presences().size());
        for (WhatsappSyncPresence presence : value.presences()) {
            WhatsappSyncPresence.encode(buf, presence);
        }

        WhatsappSyncProfile.encode(buf, value.profile());

        buf.writeBoolean(value.localUserInServer());
        buf.writeBoolean(value.localUserHasInternet());
        buf.writeBoolean(value.localUserHasBattery());
    }

    public static WhatsappInitialStateSnapshot decode(FriendlyByteBuf buf) {
        int chatsSize = buf.readInt();
        List<WhatsappSyncChat> chats = new ArrayList<>(chatsSize);
        for (int i = 0; i < chatsSize; i++) {
            chats.add(WhatsappSyncChat.decode(buf));
        }

        int contactsSize = buf.readInt();
        List<WhatsappSyncContact> contacts = new ArrayList<>(contactsSize);
        for (int i = 0; i < contactsSize; i++) {
            contacts.add(WhatsappSyncContact.decode(buf));
        }

        int presencesSize = buf.readInt();
        List<WhatsappSyncPresence> presences = new ArrayList<>(presencesSize);
        for (int i = 0; i < presencesSize; i++) {
            presences.add(WhatsappSyncPresence.decode(buf));
        }

        WhatsappSyncProfile profile = WhatsappSyncProfile.decode(buf);

        boolean localUserInServer = buf.readBoolean();
        boolean localUserHasInternet = buf.readBoolean();
        boolean localUserHasBattery = buf.readBoolean();

        return new WhatsappInitialStateSnapshot(
                chats,
                contacts,
                presences,
                profile,
                localUserInServer,
                localUserHasInternet,
                localUserHasBattery
        );
    }
}