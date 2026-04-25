package santi_moder.roleplaymod.client.phone.app.whatsapp;

import java.util.Objects;
import java.util.UUID;

public final class WhatsappMessage {

    private final String id;
    private final String text;
    private final boolean sentByMe;
    private final String timeText;
    private final long sortTimestamp;
    private final WhatsappMessageStatus status;
    private final long lastStatusUpdateAt;

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

    public String id() {
        return id;
    }

    public String text() {
        return text;
    }

    public boolean sentByMe() {
        return sentByMe;
    }

    public String timeText() {
        return timeText;
    }

    public long sortTimestamp() {
        return sortTimestamp;
    }

    public WhatsappMessageStatus status() {
        return status;
    }

    public long lastStatusUpdateAt() {
        return lastStatusUpdateAt;
    }

    public WhatsappMessage withStatus(WhatsappMessageStatus newStatus, long updatedAt) {
        return new WhatsappMessage(id, text, sentByMe, timeText, sortTimestamp, newStatus, updatedAt);
    }

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