package santi_moder.roleplaymod.common.whatsapp.model;

import net.minecraft.nbt.CompoundTag;

import java.util.Objects;
import java.util.UUID;

public record WhatsappMessage(String id, String text, boolean sentByMe, String timeText, long sortTimestamp,
                              WhatsappMessageStatus status, long lastStatusUpdateAt) {

    public WhatsappMessage(
            String id,
            String text,
            boolean sentByMe,
            String timeText,
            long sortTimestamp,
            WhatsappMessageStatus status,
            long lastStatusUpdateAt
    ) {
        this.id = id == null || id.isBlank() ? UUID.randomUUID().toString() : id;
        this.text = text == null ? "" : text;
        this.sentByMe = sentByMe;
        this.timeText = timeText == null ? "--:--" : timeText;
        this.sortTimestamp = sortTimestamp;
        this.status = status == null ? WhatsappMessageStatus.PENDING : status;
        this.lastStatusUpdateAt = lastStatusUpdateAt;
    }

    public static WhatsappMessage of(
            String text,
            boolean sentByMe,
            String timeText,
            long sortTimestamp,
            WhatsappMessageStatus status,
            long lastStatusUpdateAt
    ) {
        return new WhatsappMessage(
                UUID.randomUUID().toString(),
                text,
                sentByMe,
                timeText,
                sortTimestamp,
                status,
                lastStatusUpdateAt
        );
    }

    public static WhatsappMessage load(CompoundTag tag) {
        if (tag == null) {
            return null;
        }

        String id = tag.getString("id");
        String text = tag.getString("text");
        boolean sentByMe = tag.getBoolean("sentByMe");
        String timeText = tag.getString("timeText");
        long sortTimestamp = tag.getLong("sortTimestamp");

        WhatsappMessageStatus status;
        try {
            status = WhatsappMessageStatus.valueOf(tag.getString("status"));
        } catch (Exception e) {
            status = WhatsappMessageStatus.PENDING;
        }

        long lastStatusUpdateAt = tag.getLong("lastStatusUpdateAt");

        return new WhatsappMessage(
                id,
                text,
                sentByMe,
                timeText,
                sortTimestamp,
                status,
                lastStatusUpdateAt
        );
    }

    // =========================
    // SERIALIZACIÓN NBT
    // =========================

    public WhatsappMessage withStatus(WhatsappMessageStatus newStatus, long updatedAt) {
        return new WhatsappMessage(id, text, sentByMe, timeText, sortTimestamp, newStatus, updatedAt);
    }

    public CompoundTag save() {
        CompoundTag tag = new CompoundTag();

        tag.putString("id", id);
        tag.putString("text", text);
        tag.putBoolean("sentByMe", sentByMe);
        tag.putString("timeText", timeText);
        tag.putLong("sortTimestamp", sortTimestamp);
        tag.putString("status", status.name());
        tag.putLong("lastStatusUpdateAt", lastStatusUpdateAt);

        return tag;
    }

    // =========================

    @Override
    public boolean equals(Object obj) {
        if (this == obj) return true;
        if (!(obj instanceof WhatsappMessage other)) return false;
        return Objects.equals(id, other.id);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id);
    }
}