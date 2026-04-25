package santi_moder.roleplaymod.client.phone.app.whatsapp;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.UUID;

public final class WhatsappState {

    private final List<WhatsappChat> chats = new ArrayList<>();
    private final List<WhatsappContact> contacts = new ArrayList<>();
    private final List<WhatsappPresence> presences = new ArrayList<>();

    private String selectedChatId;
    private String draftMessage = "";
    private WhatsappProfile profile = WhatsappProfile.createDefault("", "");
    private WhatsappChatScreen chatScreen = WhatsappChatScreen.LIST;

    private String newContactName = "";
    private String newContactSurname = "";
    private String newContactPhone = "";

    private boolean waitingOpenConversationFromServer;
    private boolean localUserInServer = true;
    private boolean localUserHasInternet = true;
    private boolean localUserHasBattery = true;

    public WhatsappState() {
    }

    public WhatsappState(
            List<WhatsappChat> initialChats,
            List<WhatsappContact> initialContacts,
            List<WhatsappPresence> initialPresences,
            String selectedChatId,
            String draftMessage,
            WhatsappProfile profile,
            WhatsappChatScreen chatScreen
    ) {
        if (initialChats != null) {
            chats.addAll(initialChats);
        }
        if (initialContacts != null) {
            contacts.addAll(initialContacts);
        }
        if (initialPresences != null) {
            presences.addAll(initialPresences);
        }

        this.selectedChatId = selectedChatId;
        this.draftMessage = draftMessage == null ? "" : draftMessage;
        this.profile = profile == null ? WhatsappProfile.createDefault("", "") : profile;
        this.chatScreen = chatScreen == null ? WhatsappChatScreen.LIST : chatScreen;

        sortContacts();
        sortChats();
        ensurePresenceForAllContacts();
        validateSelectedChat();
        validateChatScreen();
    }

    public boolean isWaitingOpenConversationFromServer() {
        return waitingOpenConversationFromServer;
    }

    public void setWaitingOpenConversationFromServer(boolean waitingOpenConversationFromServer) {
        this.waitingOpenConversationFromServer = waitingOpenConversationFromServer;
    }

    public List<WhatsappChat> getChats() {
        return chats;
    }

    public List<WhatsappContact> getContacts() {
        return contacts;
    }

    public List<WhatsappPresence> getPresences() {
        return presences;
    }

    public void replaceChats(List<WhatsappChat> newChats) {
        chats.clear();
        if (newChats != null) {
            chats.addAll(newChats);
        }
        sortChats();
        validateSelectedChat();
        validateChatScreen();
    }

    public void replaceContacts(List<WhatsappContact> newContacts) {
        contacts.clear();
        if (newContacts != null) {
            contacts.addAll(newContacts);
        }
        sortContacts();
        ensurePresenceForAllContacts();
        validateSelectedChat();
        validateChatScreen();
    }

    public void replacePresences(List<WhatsappPresence> newPresences) {
        presences.clear();
        if (newPresences != null) {
            presences.addAll(newPresences);
        }
        ensurePresenceForAllContacts();
    }

    public boolean isEmpty() {
        return chats.isEmpty() && contacts.isEmpty();
    }

    public WhatsappChat getSelectedChat() {
        if (selectedChatId == null || selectedChatId.isBlank()) {
            return null;
        }

        for (WhatsappChat chat : chats) {
            if (chat.id().equals(selectedChatId)) {
                return chat;
            }
        }

        return null;
    }

    public WhatsappContact getSelectedContact() {
        WhatsappChat chat = getSelectedChat();
        if (chat == null) {
            return null;
        }
        return findContactById(chat.contactId());
    }

    public String getSelectedChatId() {
        return selectedChatId;
    }

    public void setSelectedChatId(String selectedChatId) {
        this.selectedChatId = selectedChatId;
        validateSelectedChat();
        validateChatScreen();
    }

    public String getDraftMessage() {
        return draftMessage;
    }

    public void setDraftMessage(String draftMessage) {
        this.draftMessage = draftMessage == null ? "" : draftMessage;
    }

    public void clearDraftMessage() {
        draftMessage = "";
    }

    public WhatsappProfile getProfile() {
        return profile;
    }

    public void setProfile(WhatsappProfile profile) {
        this.profile = profile == null ? WhatsappProfile.createDefault("", "") : profile;
    }

    public WhatsappChatScreen getChatScreen() {
        return chatScreen;
    }

    public void setChatScreen(WhatsappChatScreen chatScreen) {
        this.chatScreen = chatScreen == null ? WhatsappChatScreen.LIST : chatScreen;
        validateChatScreen();
    }

    public boolean isConversationOpen() {
        return getChatScreen() == WhatsappChatScreen.CONVERSATION && getSelectedChat() != null;
    }

    public boolean isContactInfoOpen() {
        return getChatScreen() == WhatsappChatScreen.CONTACT_INFO && getSelectedContact() != null;
    }

    public boolean isContactPhotoOpen() {
        return getChatScreen() == WhatsappChatScreen.CONTACT_PHOTO && getSelectedContact() != null;
    }

    public void openChat(String chatId) {
        if (chatId == null || chatId.isBlank()) {
            return;
        }

        for (WhatsappChat chat : chats) {
            if (chat.id().equals(chatId)) {
                selectedChatId = chatId;
                chatScreen = WhatsappChatScreen.CONVERSATION;
                return;
            }
        }
    }

    public void openChat(WhatsappChat chat) {
        if (chat == null) {
            return;
        }
        openChat(chat.id());
    }

    public void closeChat() {
        selectedChatId = null;
        draftMessage = "";
        chatScreen = WhatsappChatScreen.LIST;
    }

    public void openSelectedContactInfo() {
        if (getSelectedContact() != null) {
            chatScreen = WhatsappChatScreen.CONTACT_INFO;
        }
    }

    public void openSelectedContactPhoto() {
        if (getSelectedContact() != null) {
            chatScreen = WhatsappChatScreen.CONTACT_PHOTO;
        }
    }

    public void openNewChatScreen() {
        chatScreen = WhatsappChatScreen.NEW_CHAT;
    }

    public void openNewContactScreen() {
        chatScreen = WhatsappChatScreen.NEW_CONTACT;
    }

    public void backFromChatSubscreen() {
        switch (chatScreen) {
            case CONTACT_INFO, CONTACT_PHOTO -> chatScreen = WhatsappChatScreen.CONVERSATION;
            case CONVERSATION -> closeChat();
            case NEW_CONTACT -> chatScreen = WhatsappChatScreen.NEW_CHAT;
            case NEW_CHAT -> chatScreen = WhatsappChatScreen.LIST;
            case LIST -> chatScreen = WhatsappChatScreen.LIST;
        }
    }

    public void clearSelectedChatMessages() {
        WhatsappChat selectedChat = getSelectedChat();
        if (selectedChat == null) {
            return;
        }

        selectedChat.clearMessages();
        selectedChat.clearUnreadCount();
        sortChats();
    }

    public void toggleSelectedContactBlocked() {
        WhatsappContact selectedContact = getSelectedContact();
        if (selectedContact == null) {
            return;
        }

        selectedContact.setBlocked(!selectedContact.blocked());
    }

    public WhatsappChat findChatById(String chatId) {
        if (chatId == null || chatId.isBlank()) {
            return null;
        }

        for (WhatsappChat chat : chats) {
            if (chat.id().equals(chatId)) {
                return chat;
            }
        }

        return null;
    }

    public WhatsappContact findContactById(String contactId) {
        if (contactId == null || contactId.isBlank()) {
            return null;
        }

        for (WhatsappContact contact : contacts) {
            if (contact.id().equals(contactId)) {
                return contact;
            }
        }

        return null;
    }

    public WhatsappPresence findPresenceByContactId(String contactId) {
        if (contactId == null || contactId.isBlank()) {
            return null;
        }

        for (WhatsappPresence presence : presences) {
            if (contactId.equals(presence.contactId())) {
                return presence;
            }
        }

        return null;
    }

    public WhatsappChat findChatByContactId(String contactId) {
        if (contactId == null || contactId.isBlank()) {
            return null;
        }

        for (WhatsappChat chat : chats) {
            if (contactId.equals(chat.contactId())) {
                return chat;
            }
        }

        return null;
    }

    public void addContact(WhatsappContact contact) {
        if (contact == null) {
            return;
        }

        contacts.add(contact);
        sortContacts();
        ensurePresenceForAllContacts();
    }

    public void sortContacts() {
        contacts.sort(Comparator.comparing(WhatsappContact::displayName, String.CASE_INSENSITIVE_ORDER));
    }

    public String getChatDisplayName(WhatsappChat chat) {
        WhatsappContact contact = chat == null ? null : findContactById(chat.contactId());
        return contact == null ? "Chat" : contact.displayName();
    }

    public String getChatPhotoId(WhatsappChat chat) {
        WhatsappContact contact = chat == null ? null : findContactById(chat.contactId());
        return contact == null ? WhatsappContact.DEFAULT_PHOTO : contact.photoId();
    }

    public String getChatInitials(WhatsappChat chat) {
        WhatsappContact contact = chat == null ? null : findContactById(chat.contactId());
        return contact == null ? "CT" : contact.getInitials();
    }

    public void sortChats() {
        chats.sort((a, b) -> {
            if (a.archived() != b.archived()) {
                return a.archived() ? 1 : -1;
            }

            if (a.pinned() != b.pinned()) {
                return a.pinned() ? -1 : 1;
            }

            return Long.compare(b.getLastMessageTimestamp(), a.getLastMessageTimestamp());
        });
    }

    public void validateSelectedChat() {
        if (selectedChatId == null || selectedChatId.isBlank()) {
            selectedChatId = null;
            return;
        }

        WhatsappChat selected = findChatById(selectedChatId);
        if (selected == null || findContactById(selected.contactId()) == null) {
            selectedChatId = null;
        }
    }

    private void validateChatScreen() {
        if (getSelectedChat() == null) {
            if (chatScreen == WhatsappChatScreen.CONVERSATION
                    || chatScreen == WhatsappChatScreen.CONTACT_INFO
                    || chatScreen == WhatsappChatScreen.CONTACT_PHOTO) {
                chatScreen = WhatsappChatScreen.LIST;
            }
        }
    }

    public String getNewContactName() {
        return newContactName;
    }

    public void setNewContactName(String newContactName) {
        this.newContactName = newContactName == null ? "" : newContactName;
    }

    public String getNewContactSurname() {
        return newContactSurname;
    }

    public void setNewContactSurname(String newContactSurname) {
        this.newContactSurname = newContactSurname == null ? "" : newContactSurname;
    }

    public String getNewContactPhone() {
        return newContactPhone;
    }

    public void setNewContactPhone(String newContactPhone) {
        this.newContactPhone = newContactPhone == null ? "" : newContactPhone;
    }

    public void clearNewContactDraft() {
        newContactName = "";
        newContactSurname = "";
        newContactPhone = "";
    }

    public String buildNewContactDisplayName() {
        String name = newContactName == null ? "" : newContactName.trim();
        String surname = newContactSurname == null ? "" : newContactSurname.trim();
        String full = (name + " " + surname).trim();
        return full.isBlank() ? "" : full;
    }

    public String buildNewContactPhoneFull() {
        String digits = normalizeUruguayPhoneDigits(newContactPhone);
        return digits.isEmpty() ? "" : "+598 " + digits;
    }

    public boolean canCreateNewContact() {
        return !buildNewContactDisplayName().isBlank() && !normalizeUruguayPhoneDigits(newContactPhone).isBlank();
    }

    private String normalizeUruguayPhoneDigits(String value) {
        if (value == null || value.isBlank()) {
            return "";
        }

        StringBuilder builder = new StringBuilder();
        for (int i = 0; i < value.length(); i++) {
            char c = value.charAt(i);
            if (Character.isDigit(c)) {
                builder.append(c);
            }
        }
        return builder.toString();
    }

    public boolean isLocalUserInServer() {
        return localUserInServer;
    }

    public void setLocalUserInServer(boolean localUserInServer) {
        this.localUserInServer = localUserInServer;
    }

    public boolean isLocalUserHasInternet() {
        return localUserHasInternet;
    }

    public void setLocalUserHasInternet(boolean localUserHasInternet) {
        this.localUserHasInternet = localUserHasInternet;
    }

    public boolean isLocalUserHasBattery() {
        return localUserHasBattery;
    }

    public void setLocalUserHasBattery(boolean localUserHasBattery) {
        this.localUserHasBattery = localUserHasBattery;
    }

    public void ensurePresenceForAllContacts() {
        for (WhatsappContact contact : contacts) {
            if (findPresenceByContactId(contact.id()) == null) {
                presences.add(new WhatsappPresence(
                        contact.id(),
                        false,
                        false,
                        false,
                        0L
                ));
            }
        }
    }
}