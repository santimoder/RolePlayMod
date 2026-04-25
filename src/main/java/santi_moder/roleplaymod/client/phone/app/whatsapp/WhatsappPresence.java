package santi_moder.roleplaymod.client.phone.app.whatsapp;

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
}