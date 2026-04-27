package santi_moder.roleplaymod.common.whatsapp.model;

import net.minecraft.nbt.CompoundTag;

public final class WhatsappPresence {

    private final String contactId;
    private boolean onlineInServer;
    private boolean hasInternet;
    private boolean hasBattery;
    private long lastSeenTimestamp;

    public WhatsappPresence(
            String contactId,
            boolean onlineInServer,
            boolean hasInternet,
            boolean hasBattery,
            long lastSeenTimestamp
    ) {
        this.contactId = contactId == null ? "" : contactId;
        this.onlineInServer = onlineInServer;
        this.hasInternet = hasInternet;
        this.hasBattery = hasBattery;
        this.lastSeenTimestamp = lastSeenTimestamp;
    }

    public String contactId() {
        return contactId;
    }

    public boolean onlineInServer() {
        return onlineInServer;
    }

    public void setOnlineInServer(boolean onlineInServer) {
        this.onlineInServer = onlineInServer;
    }

    public boolean hasInternet() {
        return hasInternet;
    }

    public void setHasInternet(boolean hasInternet) {
        this.hasInternet = hasInternet;
    }

    public boolean hasBattery() {
        return hasBattery;
    }

    public void setHasBattery(boolean hasBattery) {
        this.hasBattery = hasBattery;
    }

    public long lastSeenTimestamp() {
        return lastSeenTimestamp;
    }

    public void setLastSeenTimestamp(long lastSeenTimestamp) {
        this.lastSeenTimestamp = lastSeenTimestamp;
    }

    public boolean canReceiveMessages() {
        return onlineInServer && hasInternet && hasBattery;
    }

    public CompoundTag save() {
        CompoundTag tag = new CompoundTag();

        tag.putString("contactId", contactId);
        tag.putBoolean("onlineInServer", onlineInServer);
        tag.putBoolean("hasInternet", hasInternet);
        tag.putBoolean("hasBattery", hasBattery);
        tag.putLong("lastSeenTimestamp", lastSeenTimestamp);

        return tag;
    }

    public static WhatsappPresence load(CompoundTag tag) {
        if (tag == null) {
            return null;
        }

        return new WhatsappPresence(
                tag.getString("contactId"),
                tag.getBoolean("onlineInServer"),
                tag.getBoolean("hasInternet"),
                tag.getBoolean("hasBattery"),
                tag.getLong("lastSeenTimestamp")
        );
    }
}