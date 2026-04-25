package santi_moder.roleplaymod.client.phone.app.whatsapp;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.UUID;

public final class WhatsappChat {

    private final String id;
    private String contactId;
    private boolean pinned;
    private boolean archived;
    private int unreadCount;
    private final List<WhatsappMessage> messages = new ArrayList<>();

    public WhatsappChat(
            String id,
            String contactId,
            boolean pinned,
            boolean archived,
            int unreadCount,
            List<WhatsappMessage> initialMessages
    ) {
        this.id = id == null || id.isBlank() ? UUID.randomUUID().toString() : id;
        this.contactId = contactId == null ? "" : contactId;
        this.pinned = pinned;
        this.archived = archived;
        this.unreadCount = Math.max(0, unreadCount);

        if (initialMessages != null) {
            this.messages.addAll(initialMessages);
            this.messages.sort((a, b) -> Long.compare(a.sortTimestamp(), b.sortTimestamp()));
        }
    }

    public static WhatsappChat of(
            String contactId,
            boolean pinned,
            boolean archived,
            int unreadCount,
            List<WhatsappMessage> initialMessages
    ) {
        return new WhatsappChat(UUID.randomUUID().toString(), contactId, pinned, archived, unreadCount, initialMessages);
    }

    public String id() {
        return id;
    }

    public String contactId() {
        return contactId;
    }

    public void setContactId(String contactId) {
        this.contactId = contactId == null ? "" : contactId;
    }

    public boolean pinned() {
        return pinned;
    }

    public void setPinned(boolean pinned) {
        this.pinned = pinned;
    }

    public boolean archived() {
        return archived;
    }

    public void setArchived(boolean archived) {
        this.archived = archived;
    }

    public int unreadCount() {
        return unreadCount;
    }

    public void setUnreadCount(int unreadCount) {
        this.unreadCount = Math.max(0, unreadCount);
    }

    public void clearUnreadCount() {
        unreadCount = 0;
    }

    public void incrementUnreadCount() {
        unreadCount++;
    }

    public List<WhatsappMessage> messages() {
        return Collections.unmodifiableList(messages);
    }

    public void addMessage(WhatsappMessage message) {
        if (message == null) {
            return;
        }

        messages.add(message);
        messages.sort((a, b) -> Long.compare(a.sortTimestamp(), b.sortTimestamp()));
    }

    public void clearMessages() {
        messages.clear();
    }

    public void updateMessageStatus(String messageId, WhatsappMessageStatus newStatus, long updatedAt) {
        for (int i = 0; i < messages.size(); i++) {
            WhatsappMessage message = messages.get(i);
            if (message.id().equals(messageId)) {
                messages.set(i, message.withStatus(newStatus, updatedAt));
                return;
            }
        }
    }

    public boolean hasMessages() {
        return !messages.isEmpty();
    }

    public WhatsappMessage getLastMessageObject() {
        if (messages.isEmpty()) {
            return null;
        }
        return messages.get(messages.size() - 1);
    }

    public String getLastMessageText() {
        WhatsappMessage last = getLastMessageObject();
        return last == null ? "" : last.text();
    }

    public String getLastMessageTimeText() {
        WhatsappMessage last = getLastMessageObject();
        return last == null ? "--:--" : last.timeText();
    }

    public long getLastMessageTimestamp() {
        WhatsappMessage last = getLastMessageObject();
        return last == null ? Long.MIN_VALUE : last.sortTimestamp();
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) return true;
        if (!(obj instanceof WhatsappChat other)) return false;
        return Objects.equals(id, other.id);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id);
    }
}