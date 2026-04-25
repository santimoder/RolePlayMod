package santi_moder.roleplaymod.common.phone;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.Tag;
import net.minecraft.world.item.ItemStack;
import santi_moder.roleplaymod.client.phone.app.whatsapp.WhatsappChat;
import santi_moder.roleplaymod.client.phone.app.whatsapp.WhatsappChatScreen;
import santi_moder.roleplaymod.client.phone.app.whatsapp.WhatsappContact;
import santi_moder.roleplaymod.client.phone.app.whatsapp.WhatsappMessage;
import santi_moder.roleplaymod.client.phone.app.whatsapp.WhatsappMessageStatus;
import santi_moder.roleplaymod.client.phone.app.whatsapp.WhatsappPresence;
import santi_moder.roleplaymod.client.phone.app.whatsapp.WhatsappProfile;
import santi_moder.roleplaymod.client.phone.app.whatsapp.WhatsappState;

import java.util.ArrayList;
import java.util.List;

public final class PhoneWhatsappData {

    private static final String ROOT_TAG = "rp_whatsapp";
    private static final String CHATS_TAG = "chats";
    private static final String CONTACTS_TAG = "contacts";
    private static final String PRESENCES_TAG = "presences";
    private static final String SELECTED_CHAT_ID_TAG = "selected_chat_id";
    private static final String DRAFT_MESSAGE_TAG = "draft_message";
    private static final String PROFILE_TAG = "profile";
    private static final String CHAT_SCREEN_TAG = "chat_screen";

    private static final String LOCAL_IN_SERVER_TAG = "local_in_server";
    private static final String LOCAL_HAS_INTERNET_TAG = "local_has_internet";
    private static final String LOCAL_HAS_BATTERY_TAG = "local_has_battery";

    private static final String CHAT_ID_TAG = "id";
    private static final String CHAT_CONTACT_ID_TAG = "contact_id";
    private static final String CHAT_PINNED_TAG = "pinned";
    private static final String CHAT_ARCHIVED_TAG = "archived";
    private static final String CHAT_UNREAD_TAG = "unread";
    private static final String CHAT_MESSAGES_TAG = "messages";

    private static final String CONTACT_ID_TAG = "id";
    private static final String CONTACT_NAME_TAG = "display_name";
    private static final String CONTACT_PHONE_TAG = "phone_number";
    private static final String CONTACT_PHOTO_TAG = "photo_id";
    private static final String CONTACT_ABOUT_TAG = "about";
    private static final String CONTACT_BLOCKED_TAG = "blocked";
    private static final String CONTACT_COMMON_GROUPS_TAG = "common_groups";
    private static final String CONTACT_MEDIA_COUNT_TAG = "media_count";

    private static final String PRESENCE_CONTACT_ID_TAG = "contact_id";
    private static final String PRESENCE_IN_SERVER_TAG = "online_in_server";
    private static final String PRESENCE_HAS_INTERNET_TAG = "has_internet";
    private static final String PRESENCE_HAS_BATTERY_TAG = "has_battery";
    private static final String PRESENCE_LAST_SEEN_TAG = "last_seen";

    private static final String MESSAGE_ID_TAG = "id";
    private static final String MESSAGE_TEXT_TAG = "text";
    private static final String MESSAGE_SENT_BY_ME_TAG = "sent_by_me";
    private static final String MESSAGE_TIME_TEXT_TAG = "time_text";
    private static final String MESSAGE_SORT_TIMESTAMP_TAG = "sort_timestamp";
    private static final String MESSAGE_STATUS_TAG = "status";
    private static final String MESSAGE_LAST_STATUS_UPDATE_AT_TAG = "last_status_update_at";

    private static final String PROFILE_PHOTO_ID_TAG = "photo_id";
    private static final String PROFILE_ABOUT_TAG = "about";
    private static final String PROFILE_NAME_TAG = "display_name";
    private static final String PROFILE_PHONE_TAG = "phone_number";

    private PhoneWhatsappData() {
    }

    public static WhatsappState loadState(ItemStack stack) {
        if (stack.isEmpty()) {
            return new WhatsappState();
        }

        CompoundTag root = stack.getOrCreateTag();
        if (!root.contains(ROOT_TAG, Tag.TAG_COMPOUND)) {
            WhatsappState state = new WhatsappState();
            saveState(stack, state);
            return state;
        }

        CompoundTag whatsappTag = root.getCompound(ROOT_TAG);

        List<WhatsappChat> chats = readChats(whatsappTag);
        List<WhatsappContact> contacts = readContacts(whatsappTag);
        List<WhatsappPresence> presences = readPresences(whatsappTag);

        String selectedChatId = whatsappTag.getString(SELECTED_CHAT_ID_TAG);
        String draftMessage = whatsappTag.getString(DRAFT_MESSAGE_TAG);
        WhatsappProfile profile = readProfile(whatsappTag);

        WhatsappChatScreen screen = WhatsappChatScreen.LIST;
        String rawScreen = whatsappTag.getString(CHAT_SCREEN_TAG);
        if (!rawScreen.isBlank()) {
            try {
                screen = WhatsappChatScreen.valueOf(rawScreen);
            } catch (IllegalArgumentException ignored) {
                screen = WhatsappChatScreen.LIST;
            }
        }

        WhatsappState state = new WhatsappState(
                chats,
                contacts,
                presences,
                selectedChatId,
                draftMessage,
                profile,
                screen
        );

        if (whatsappTag.contains(LOCAL_IN_SERVER_TAG)) {
            state.setLocalUserInServer(whatsappTag.getBoolean(LOCAL_IN_SERVER_TAG));
        }
        if (whatsappTag.contains(LOCAL_HAS_INTERNET_TAG)) {
            state.setLocalUserHasInternet(whatsappTag.getBoolean(LOCAL_HAS_INTERNET_TAG));
        }
        if (whatsappTag.contains(LOCAL_HAS_BATTERY_TAG)) {
            state.setLocalUserHasBattery(whatsappTag.getBoolean(LOCAL_HAS_BATTERY_TAG));
        }

        return state;
    }

    public static void saveState(ItemStack stack, WhatsappState state) {
        if (stack.isEmpty() || state == null) {
            return;
        }

        CompoundTag root = stack.getOrCreateTag();
        CompoundTag whatsappTag = new CompoundTag();

        whatsappTag.put(CHATS_TAG, writeChats(state.getChats()));
        whatsappTag.put(CONTACTS_TAG, writeContacts(state.getContacts()));
        whatsappTag.put(PRESENCES_TAG, writePresences(state.getPresences()));
        whatsappTag.putString(CHAT_SCREEN_TAG, state.getChatScreen().name());

        whatsappTag.putBoolean(LOCAL_IN_SERVER_TAG, state.isLocalUserInServer());
        whatsappTag.putBoolean(LOCAL_HAS_INTERNET_TAG, state.isLocalUserHasInternet());
        whatsappTag.putBoolean(LOCAL_HAS_BATTERY_TAG, state.isLocalUserHasBattery());

        if (state.getSelectedChatId() != null && !state.getSelectedChatId().isBlank()) {
            whatsappTag.putString(SELECTED_CHAT_ID_TAG, state.getSelectedChatId());
        }

        if (state.getDraftMessage() != null && !state.getDraftMessage().isEmpty()) {
            whatsappTag.putString(DRAFT_MESSAGE_TAG, state.getDraftMessage());
        }

        whatsappTag.put(PROFILE_TAG, writeProfile(state.getProfile()));
        root.put(ROOT_TAG, whatsappTag);
    }

    private static List<WhatsappChat> readChats(CompoundTag whatsappTag) {
        List<WhatsappChat> chats = new ArrayList<>();

        if (!whatsappTag.contains(CHATS_TAG, Tag.TAG_LIST)) {
            return chats;
        }

        ListTag chatList = whatsappTag.getList(CHATS_TAG, Tag.TAG_COMPOUND);
        for (int i = 0; i < chatList.size(); i++) {
            CompoundTag chatTag = chatList.getCompound(i);

            String chatId = chatTag.getString(CHAT_ID_TAG);
            String contactId = chatTag.getString(CHAT_CONTACT_ID_TAG);
            boolean pinned = chatTag.getBoolean(CHAT_PINNED_TAG);
            boolean archived = chatTag.getBoolean(CHAT_ARCHIVED_TAG);
            int unreadCount = chatTag.getInt(CHAT_UNREAD_TAG);

            List<WhatsappMessage> messages = readMessages(chatTag);
            chats.add(new WhatsappChat(chatId, contactId, pinned, archived, unreadCount, messages));
        }

        return chats;
    }

    private static List<WhatsappContact> readContacts(CompoundTag whatsappTag) {
        List<WhatsappContact> contacts = new ArrayList<>();

        if (!whatsappTag.contains(CONTACTS_TAG, Tag.TAG_LIST)) {
            return contacts;
        }

        ListTag contactList = whatsappTag.getList(CONTACTS_TAG, Tag.TAG_COMPOUND);
        for (int i = 0; i < contactList.size(); i++) {
            CompoundTag contactTag = contactList.getCompound(i);

            contacts.add(new WhatsappContact(
                    contactTag.getString(CONTACT_ID_TAG),
                    contactTag.getString(CONTACT_NAME_TAG),
                    contactTag.getString(CONTACT_PHONE_TAG),
                    contactTag.getString(CONTACT_PHOTO_TAG),
                    contactTag.getString(CONTACT_ABOUT_TAG),
                    contactTag.getBoolean(CONTACT_BLOCKED_TAG),
                    contactTag.getInt(CONTACT_COMMON_GROUPS_TAG),
                    contactTag.getInt(CONTACT_MEDIA_COUNT_TAG)
            ));
        }

        return contacts;
    }

    private static List<WhatsappPresence> readPresences(CompoundTag whatsappTag) {
        List<WhatsappPresence> presences = new ArrayList<>();

        if (!whatsappTag.contains(PRESENCES_TAG, Tag.TAG_LIST)) {
            return presences;
        }

        ListTag presenceList = whatsappTag.getList(PRESENCES_TAG, Tag.TAG_COMPOUND);
        for (int i = 0; i < presenceList.size(); i++) {
            CompoundTag tag = presenceList.getCompound(i);

            presences.add(new WhatsappPresence(
                    tag.getString(PRESENCE_CONTACT_ID_TAG),
                    tag.getBoolean(PRESENCE_IN_SERVER_TAG),
                    tag.getBoolean(PRESENCE_HAS_INTERNET_TAG),
                    tag.getBoolean(PRESENCE_HAS_BATTERY_TAG),
                    tag.getLong(PRESENCE_LAST_SEEN_TAG)
            ));
        }

        return presences;
    }

    private static List<WhatsappMessage> readMessages(CompoundTag chatTag) {
        List<WhatsappMessage> messages = new ArrayList<>();

        if (!chatTag.contains(CHAT_MESSAGES_TAG, Tag.TAG_LIST)) {
            return messages;
        }

        ListTag messageList = chatTag.getList(CHAT_MESSAGES_TAG, Tag.TAG_COMPOUND);
        for (int i = 0; i < messageList.size(); i++) {
            CompoundTag messageTag = messageList.getCompound(i);

            String id = messageTag.getString(MESSAGE_ID_TAG);
            String text = messageTag.getString(MESSAGE_TEXT_TAG);
            boolean sentByMe = messageTag.getBoolean(MESSAGE_SENT_BY_ME_TAG);
            String timeText = messageTag.getString(MESSAGE_TIME_TEXT_TAG);
            long sortTimestamp = messageTag.getLong(MESSAGE_SORT_TIMESTAMP_TAG);
            long lastStatusUpdateAt = messageTag.getLong(MESSAGE_LAST_STATUS_UPDATE_AT_TAG);

            WhatsappMessageStatus status = WhatsappMessageStatus.PENDING;
            String rawStatus = messageTag.getString(MESSAGE_STATUS_TAG);
            if (!rawStatus.isBlank()) {
                try {
                    status = WhatsappMessageStatus.valueOf(rawStatus);
                } catch (IllegalArgumentException ignored) {
                    status = WhatsappMessageStatus.PENDING;
                }
            }

            messages.add(new WhatsappMessage(
                    id,
                    text,
                    sentByMe,
                    timeText,
                    sortTimestamp,
                    status,
                    lastStatusUpdateAt
            ));
        }

        return messages;
    }

    private static WhatsappProfile readProfile(CompoundTag whatsappTag) {
        if (!whatsappTag.contains(PROFILE_TAG, Tag.TAG_COMPOUND)) {
            return WhatsappProfile.createDefault("Jugador", "");
        }

        CompoundTag profileTag = whatsappTag.getCompound(PROFILE_TAG);

        WhatsappProfile profile = WhatsappProfile.createDefault("Jugador", "");
        profile.setPhotoId(profileTag.getString(PROFILE_PHOTO_ID_TAG));
        profile.setAbout(profileTag.getString(PROFILE_ABOUT_TAG));

        String displayName = profileTag.getString(PROFILE_NAME_TAG);
        String phoneNumber = profileTag.getString(PROFILE_PHONE_TAG);

        if (!displayName.isBlank()) {
            profile.setDisplayName(displayName);
        }
        if (!phoneNumber.isBlank()) {
            profile.setPhoneNumber(phoneNumber);
        }

        return profile;
    }

    private static CompoundTag writeProfile(WhatsappProfile profile) {
        CompoundTag profileTag = new CompoundTag();
        if (profile == null) {
            return profileTag;
        }

        profileTag.putString(PROFILE_PHOTO_ID_TAG, profile.photoId());
        profileTag.putString(PROFILE_ABOUT_TAG, profile.about());
        profileTag.putString(PROFILE_NAME_TAG, profile.displayName());
        profileTag.putString(PROFILE_PHONE_TAG, profile.phoneNumber());

        return profileTag;
    }

    private static ListTag writeChats(List<WhatsappChat> chats) {
        ListTag chatList = new ListTag();

        for (WhatsappChat chat : chats) {
            CompoundTag chatTag = new CompoundTag();
            chatTag.putString(CHAT_ID_TAG, chat.id());
            chatTag.putString(CHAT_CONTACT_ID_TAG, chat.contactId());
            chatTag.putBoolean(CHAT_PINNED_TAG, chat.pinned());
            chatTag.putBoolean(CHAT_ARCHIVED_TAG, chat.archived());
            chatTag.putInt(CHAT_UNREAD_TAG, chat.unreadCount());
            chatTag.put(CHAT_MESSAGES_TAG, writeMessages(chat.messages()));
            chatList.add(chatTag);
        }

        return chatList;
    }

    private static ListTag writeContacts(List<WhatsappContact> contacts) {
        ListTag contactList = new ListTag();

        for (WhatsappContact contact : contacts) {
            CompoundTag tag = new CompoundTag();
            tag.putString(CONTACT_ID_TAG, contact.id());
            tag.putString(CONTACT_NAME_TAG, contact.displayName());
            tag.putString(CONTACT_PHONE_TAG, contact.phoneNumber());
            tag.putString(CONTACT_PHOTO_TAG, contact.photoId());
            tag.putString(CONTACT_ABOUT_TAG, contact.about());
            tag.putBoolean(CONTACT_BLOCKED_TAG, contact.blocked());
            tag.putInt(CONTACT_COMMON_GROUPS_TAG, contact.commonGroupsCount());
            tag.putInt(CONTACT_MEDIA_COUNT_TAG, contact.mediaCount());
            contactList.add(tag);
        }

        return contactList;
    }

    private static ListTag writePresences(List<WhatsappPresence> presences) {
        ListTag list = new ListTag();

        for (WhatsappPresence presence : presences) {
            CompoundTag tag = new CompoundTag();
            tag.putString(PRESENCE_CONTACT_ID_TAG, presence.contactId());
            tag.putBoolean(PRESENCE_IN_SERVER_TAG, presence.onlineInServer());
            tag.putBoolean(PRESENCE_HAS_INTERNET_TAG, presence.hasInternet());
            tag.putBoolean(PRESENCE_HAS_BATTERY_TAG, presence.hasBattery());
            tag.putLong(PRESENCE_LAST_SEEN_TAG, presence.lastSeenTimestamp());
            list.add(tag);
        }

        return list;
    }

    private static ListTag writeMessages(List<WhatsappMessage> messages) {
        ListTag messageList = new ListTag();

        for (WhatsappMessage message : messages) {
            CompoundTag messageTag = new CompoundTag();
            messageTag.putString(MESSAGE_ID_TAG, message.id());
            messageTag.putString(MESSAGE_TEXT_TAG, message.text());
            messageTag.putBoolean(MESSAGE_SENT_BY_ME_TAG, message.sentByMe());
            messageTag.putString(MESSAGE_TIME_TEXT_TAG, message.timeText());
            messageTag.putLong(MESSAGE_SORT_TIMESTAMP_TAG, message.sortTimestamp());
            messageTag.putString(MESSAGE_STATUS_TAG, message.status().name());
            messageTag.putLong(MESSAGE_LAST_STATUS_UPDATE_AT_TAG, message.lastStatusUpdateAt());
            messageList.add(messageTag);
        }

        return messageList;
    }
}