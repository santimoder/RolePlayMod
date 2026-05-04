package santi_moder.roleplaymod.common.whatsapp.model;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.Tag;

import java.util.*;

public final class WhatsappChat {

    private final String id;
    private final List<WhatsappMessage> messages = new ArrayList<>();
    private String contactId;
    private boolean pinned;
    private boolean archived;
    private int unreadCount;

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

    public static WhatsappChat load(CompoundTag tag) {
        if (tag == null) {
            return null;
        }

        String id = tag.getString("id");
        String contactId = tag.getString("contactId");
        boolean pinned = tag.getBoolean("pinned");
        boolean archived = tag.getBoolean("archived");
        int unreadCount = tag.getInt("unreadCount");

        List<WhatsappMessage> messages = new ArrayList<>();

        if (tag.contains("messages", Tag.TAG_LIST)) {
            ListTag messagesTag = tag.getList("messages", Tag.TAG_COMPOUND);

            for (int i = 0; i < messagesTag.size(); i++) {
                WhatsappMessage message = WhatsappMessage.load(messagesTag.getCompound(i));
                if (message != null) {
                    messages.add(message);
                }
            }
        }

        return new WhatsappChat(
                id,
                contactId,
                pinned,
                archived,
                unreadCount,
                messages
        );
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

    public CompoundTag save() {
        CompoundTag tag = new CompoundTag();

        tag.putString("id", id);
        tag.putString("contactId", contactId);
        tag.putBoolean("pinned", pinned);
        tag.putBoolean("archived", archived);
        tag.putInt("unreadCount", unreadCount);

        ListTag messagesTag = new ListTag();
        for (WhatsappMessage message : messages) {
            if (message != null) {
                messagesTag.add(message.save());
            }
        }

        tag.put("messages", messagesTag);
        return tag;
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