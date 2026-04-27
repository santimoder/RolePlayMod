package santi_moder.roleplaymod.client.phone.app.whatsapp;

import santi_moder.roleplaymod.common.whatsapp.model.*;
import santi_moder.roleplaymod.common.whatsapp.sync.*;

import java.util.ArrayList;
import java.util.List;

public final class WhatsappClientSyncApplier {

    private static WhatsappInitialStateSnapshot pendingInitialSnapshot;
    private static final List<WhatsappChatPayload> pendingChatPayloads = new ArrayList<>();
    private static final List<WhatsappMessageStatusPayload> pendingStatusPayloads = new ArrayList<>();
    private static final List<WhatsappPresencePayload> pendingPresencePayloads = new ArrayList<>();
    private static final List<WhatsappContactPayload> pendingContactPayloads = new ArrayList<>();
    private static final List<WhatsappChatPayload> pendingClearedChatPayloads = new ArrayList<>();
    private static final List<CreatedContactPayload> pendingCreatedContactPayloads = new ArrayList<>();
    private static final List<WhatsappChatPayload> pendingOpenedChatPayloads = new ArrayList<>();
    private static final List<String> pendingDeletedChatIds = new ArrayList<>();

    private WhatsappClientSyncApplier() {
    }

    public static void applyInitialState(WhatsappInitialStateSnapshot snapshot) {
        pendingInitialSnapshot = snapshot;
    }

    public static WhatsappInitialStateSnapshot consumePendingInitialSnapshot() {
        WhatsappInitialStateSnapshot snapshot = pendingInitialSnapshot;
        pendingInitialSnapshot = null;
        return snapshot;
    }

    public static void applyChatPayload(WhatsappChatPayload payload) {
        if (payload != null) {
            pendingChatPayloads.add(payload);
        }
    }

    public static List<WhatsappChatPayload> consumePendingChatPayloads() {
        List<WhatsappChatPayload> copy = new ArrayList<>(pendingChatPayloads);
        pendingChatPayloads.clear();
        return copy;
    }

    public static void applyMessageStatusPayload(WhatsappMessageStatusPayload payload) {
        if (payload != null) {
            pendingStatusPayloads.add(payload);
        }
    }

    public static List<WhatsappMessageStatusPayload> consumePendingStatusPayloads() {
        List<WhatsappMessageStatusPayload> copy = new ArrayList<>(pendingStatusPayloads);
        pendingStatusPayloads.clear();
        return copy;
    }

    public static void applyPresencePayload(WhatsappPresencePayload payload) {
        if (payload != null) {
            pendingPresencePayloads.add(payload);
        }
    }

    public static List<WhatsappPresencePayload> consumePendingPresencePayloads() {
        List<WhatsappPresencePayload> copy = new ArrayList<>(pendingPresencePayloads);
        pendingPresencePayloads.clear();
        return copy;
    }

    public static void applyContactPayload(WhatsappContactPayload payload) {
        if (payload != null) {
            pendingContactPayloads.add(payload);
        }
    }

    public static List<WhatsappContactPayload> consumePendingContactPayloads() {
        List<WhatsappContactPayload> copy = new ArrayList<>(pendingContactPayloads);
        pendingContactPayloads.clear();
        return copy;
    }

    public static void applyClearedChatPayload(WhatsappChatPayload payload) {
        if (payload != null) {
            pendingClearedChatPayloads.add(payload);
        }
    }

    public static List<WhatsappChatPayload> consumePendingClearedChatPayloads() {
        List<WhatsappChatPayload> copy = new ArrayList<>(pendingClearedChatPayloads);
        pendingClearedChatPayloads.clear();
        return copy;
    }

    public static void applyCreatedContact(WhatsappContactPayload payload, boolean openChatAfterCreate) {
        if (payload != null) {
            pendingCreatedContactPayloads.add(new CreatedContactPayload(payload, openChatAfterCreate));
        }
    }

    public static List<CreatedContactPayload> consumePendingCreatedContactPayloads() {
        List<CreatedContactPayload> copy = new ArrayList<>(pendingCreatedContactPayloads);
        pendingCreatedContactPayloads.clear();
        return copy;
    }

    public static void applyOpenedChatPayload(WhatsappChatPayload payload) {
        if (payload != null) {
            pendingOpenedChatPayloads.add(payload);
        }
    }

    public static List<WhatsappChatPayload> consumePendingOpenedChatPayloads() {
        List<WhatsappChatPayload> copy = new ArrayList<>(pendingOpenedChatPayloads);
        pendingOpenedChatPayloads.clear();
        return copy;
    }

    public static void applySnapshotToState(WhatsappState state, WhatsappInitialStateSnapshot snapshot) {
        if (state == null || snapshot == null) {
            return;
        }

        List<WhatsappChat> chats = new ArrayList<>();
        for (WhatsappSyncChat syncChat : snapshot.chats()) {
            chats.add(toChat(syncChat));
        }

        List<WhatsappContact> contacts = new ArrayList<>();
        for (WhatsappSyncContact syncContact : snapshot.contacts()) {
            contacts.add(new WhatsappContact(
                    syncContact.id(),
                    syncContact.displayName(),
                    syncContact.phoneNumber(),
                    syncContact.photoId(),
                    syncContact.about(),
                    syncContact.blocked(),
                    syncContact.commonGroupsCount(),
                    syncContact.mediaCount()
            ));
        }

        List<WhatsappPresence> presences = new ArrayList<>();
        for (WhatsappSyncPresence syncPresence : snapshot.presences()) {
            presences.add(new WhatsappPresence(
                    syncPresence.contactId(),
                    syncPresence.onlineInServer(),
                    syncPresence.hasInternet(),
                    syncPresence.hasBattery(),
                    syncPresence.lastSeenTimestamp()
            ));
        }

        WhatsappSyncProfile profileSync = snapshot.profile();
        WhatsappProfile profile = new WhatsappProfile(
                profileSync.photoId(),
                profileSync.about(),
                profileSync.displayName(),
                profileSync.phoneNumber()
        );

        state.replaceChats(chats);
        state.replaceContacts(contacts);
        state.replacePresences(presences);
        state.setProfile(profile);

        state.setLocalUserInServer(snapshot.localUserInServer());
        state.setLocalUserHasInternet(snapshot.localUserHasInternet());
        state.setLocalUserHasBattery(snapshot.localUserHasBattery());
    }

    public static void applyChatPayloadToState(WhatsappState state, WhatsappChatPayload payload) {
        if (state == null || payload == null) {
            return;
        }

        WhatsappChat existing = state.findChatById(payload.id());
        WhatsappChat rebuilt = toChat(payload);

        if (existing == null) {
            List<WhatsappChat> copy = new ArrayList<>(state.getChats());
            copy.add(rebuilt);
            state.replaceChats(copy);
            return;
        }

        List<WhatsappChat> updated = new ArrayList<>();
        for (WhatsappChat chat : state.getChats()) {
            if (chat.id().equals(payload.id())) {
                updated.add(rebuilt);
            } else {
                updated.add(chat);
            }
        }

        state.replaceChats(updated);
    }

    public static void applyMessageStatusPayloadToState(WhatsappState state, WhatsappMessageStatusPayload payload) {
        if (state == null || payload == null) {
            return;
        }

        WhatsappChat chat = state.findChatById(payload.chatId());
        if (chat == null) {
            return;
        }

        chat.updateMessageStatus(payload.messageId(), payload.status(), payload.lastStatusUpdateAt());
        chat.setUnreadCount(payload.unreadCount());
        state.sortChats();
    }

    public static void applyPresencePayloadToState(WhatsappState state, WhatsappPresencePayload payload) {
        if (state == null || payload == null) {
            return;
        }

        WhatsappPresence existing = state.findPresenceByContactId(payload.contactId());
        if (existing == null) {
            List<WhatsappPresence> copy = new ArrayList<>(state.getPresences());
            copy.add(new WhatsappPresence(
                    payload.contactId(),
                    payload.onlineInServer(),
                    payload.hasInternet(),
                    payload.hasBattery(),
                    payload.lastSeenTimestamp()
            ));
            state.replacePresences(copy);
            return;
        }

        existing.setOnlineInServer(payload.onlineInServer());
        existing.setHasInternet(payload.hasInternet());
        existing.setHasBattery(payload.hasBattery());
        existing.setLastSeenTimestamp(payload.lastSeenTimestamp());
    }

    public static void applyContactPayloadToState(WhatsappState state, WhatsappContactPayload payload) {
        if (state == null || payload == null) {
            return;
        }

        List<WhatsappContact> updated = new ArrayList<>();
        boolean replaced = false;

        for (WhatsappContact contact : state.getContacts()) {
            if (contact.id().equals(payload.id())) {
                updated.add(toContact(payload));
                replaced = true;
            } else {
                updated.add(contact);
            }
        }

        if (!replaced) {
            updated.add(toContact(payload));
        }

        state.replaceContacts(updated);
    }

    public static void applyClearedChatPayloadToState(WhatsappState state, WhatsappChatPayload payload) {
        applyChatPayloadToState(state, payload);
    }

    public static void applyCreatedContactPayloadToState(WhatsappState state, CreatedContactPayload payload) {
        if (state == null || payload == null) {
            return;
        }

        applyContactPayloadToState(state, payload.contactPayload());

        if (payload.openChatAfterCreate()) {
            state.clearNewContactDraft();
            state.openNewChatScreen();
        }
    }

    public static void applyOpenedChatPayloadToState(WhatsappState state, WhatsappChatPayload payload) {
        if (state == null || payload == null) {
            return;
        }

        applyChatPayloadToState(state, payload);
    }

    private static WhatsappContact toContact(WhatsappContactPayload payload) {
        return new WhatsappContact(
                payload.id(),
                payload.displayName(),
                payload.phoneNumber(),
                payload.photoId(),
                payload.about(),
                payload.blocked(),
                payload.commonGroupsCount(),
                payload.mediaCount()
        );
    }

    private static WhatsappChat toChat(WhatsappSyncChat syncChat) {
        List<WhatsappMessage> messages = new ArrayList<>();

        for (WhatsappSyncMessage syncMessage : syncChat.messages()) {
            messages.add(new WhatsappMessage(
                    syncMessage.id(),
                    syncMessage.text(),
                    syncMessage.sentByMe(),
                    syncMessage.timeText(),
                    syncMessage.sortTimestamp(),
                    syncMessage.status(),
                    syncMessage.lastStatusUpdateAt()
            ));
        }

        return new WhatsappChat(
                syncChat.id(),
                syncChat.contactId(),
                syncChat.pinned(),
                false,
                syncChat.unreadCount(),
                messages
        );
    }

    private static WhatsappChat toChat(WhatsappChatPayload payload) {
        List<WhatsappMessage> messages = new ArrayList<>();

        for (WhatsappSyncMessage syncMessage : payload.messages()) {
            messages.add(new WhatsappMessage(
                    syncMessage.id(),
                    syncMessage.text(),
                    syncMessage.sentByMe(),
                    syncMessage.timeText(),
                    syncMessage.sortTimestamp(),
                    syncMessage.status(),
                    syncMessage.lastStatusUpdateAt()
            ));
        }

        return new WhatsappChat(
                payload.id(),
                payload.contactId(),
                payload.pinned(),
                payload.archived(),
                payload.unreadCount(),
                messages
        );
    }

    public record CreatedContactPayload(
            WhatsappContactPayload contactPayload,
            boolean openChatAfterCreate
    ) {
    }
    public static void applyDeletedChatId(String chatId) {
        if (chatId != null && !chatId.isBlank()) {
            pendingDeletedChatIds.add(chatId);
        }
    }

    public static List<String> consumePendingDeletedChatIds() {
        List<String> copy = new ArrayList<>(pendingDeletedChatIds);
        pendingDeletedChatIds.clear();
        return copy;
    }

    public static void applyDeletedChatIdToState(WhatsappState state, String chatId) {
        if (state == null || chatId == null || chatId.isBlank()) {
            return;
        }

        List<WhatsappChat> updated = new ArrayList<>();
        for (WhatsappChat chat : state.getChats()) {
            if (!chat.id().equals(chatId)) {
                updated.add(chat);
            }
        }

        state.replaceChats(updated);

        if (chatId.equals(state.getSelectedChatId())) {
            state.closeChat();
        }
    }
}