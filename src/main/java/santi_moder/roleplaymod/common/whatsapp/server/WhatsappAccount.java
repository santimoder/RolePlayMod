package santi_moder.roleplaymod.common.whatsapp.server;

import net.minecraft.nbt.CompoundTag;

import java.util.UUID;

public final class WhatsappAccount {

    private final UUID accountId;
    private String simId;
    private String phoneNumber;
    private String displayName;
    private String about;
    private String photoId;
    private boolean registered;
    private UUID lastKnownPlayerUuid;

    public WhatsappAccount(
            UUID accountId,
            String simId,
            String phoneNumber,
            String displayName,
            String about,
            String photoId,
            boolean registered,
            UUID lastKnownPlayerUuid
    ) {
        this.accountId = accountId == null ? UUID.randomUUID() : accountId;
        this.simId = safe(simId);
        this.phoneNumber = safe(phoneNumber);
        this.displayName = safe(displayName);
        this.about = safe(about);
        this.photoId = photoId == null || photoId.isBlank() ? "default" : photoId;
        this.registered = registered;
        this.lastKnownPlayerUuid = lastKnownPlayerUuid;
    }

    public static WhatsappAccount load(CompoundTag tag) {
        if (tag == null) {
            return null;
        }

        UUID accountId;

        if (tag.hasUUID("accountId")) {
            accountId = tag.getUUID("accountId");
        } else if (tag.hasUUID("playerUuid")) {
            // Compatibilidad con saves anteriores.
            accountId = tag.getUUID("playerUuid");
        } else {
            accountId = UUID.randomUUID();
        }

        UUID lastKnownPlayerUuid = tag.hasUUID("lastKnownPlayerUuid")
                ? tag.getUUID("lastKnownPlayerUuid")
                : null;

        return new WhatsappAccount(
                accountId,
                tag.getString("simId"),
                tag.getString("phoneNumber"),
                tag.getString("displayName"),
                tag.getString("about"),
                tag.getString("photoId"),
                tag.getBoolean("registered"),
                lastKnownPlayerUuid
        );
    }

    private static String safe(String value) {
        return value == null ? "" : value;
    }

    public UUID accountId() {
        return accountId;
    }

    public String simId() {
        return simId;
    }

    public void setSimId(String simId) {
        this.simId = safe(simId);
    }

    public String phoneNumber() {
        return phoneNumber;
    }

    public void setPhoneNumber(String phoneNumber) {
        this.phoneNumber = safe(phoneNumber);
    }

    public String displayName() {
        return displayName;
    }

    public void setDisplayName(String displayName) {
        this.displayName = safe(displayName);
    }

    public String about() {
        return about;
    }

    public void setAbout(String about) {
        this.about = safe(about);
    }

    public String photoId() {
        return photoId;
    }

    public void setPhotoId(String photoId) {
        this.photoId = photoId == null || photoId.isBlank() ? "default" : photoId;
    }

    public boolean registered() {
        return registered;
    }

    public void setRegistered(boolean registered) {
        this.registered = registered;
    }

    public UUID lastKnownPlayerUuid() {
        return lastKnownPlayerUuid;
    }

    public void setLastKnownPlayerUuid(UUID lastKnownPlayerUuid) {
        this.lastKnownPlayerUuid = lastKnownPlayerUuid;
    }

    /**
     * Compatibilidad temporal con código viejo.
     * Más adelante eliminamos este método cuando todo use accountId.
     */
    public UUID playerUuid() {
        return accountId;
    }

    public CompoundTag save() {
        CompoundTag tag = new CompoundTag();

        tag.putUUID("accountId", accountId);
        tag.putString("simId", simId);
        tag.putString("phoneNumber", phoneNumber);
        tag.putString("displayName", displayName);
        tag.putString("about", about);
        tag.putString("photoId", photoId);
        tag.putBoolean("registered", registered);

        if (lastKnownPlayerUuid != null) {
            tag.putUUID("lastKnownPlayerUuid", lastKnownPlayerUuid);
        }

        return tag;
    }
}