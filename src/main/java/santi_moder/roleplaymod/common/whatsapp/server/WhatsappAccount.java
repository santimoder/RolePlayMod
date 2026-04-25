package santi_moder.roleplaymod.common.whatsapp.server;

import java.util.UUID;

public final class WhatsappAccount {

    private final UUID playerUuid;
    private String phoneNumber;
    private String displayName;
    private String about;
    private String photoId;
    private boolean registered;

    public WhatsappAccount(
            UUID playerUuid,
            String phoneNumber,
            String displayName,
            String about,
            String photoId,
            boolean registered
    ) {
        this.playerUuid = playerUuid;
        this.phoneNumber = phoneNumber == null ? "" : phoneNumber;
        this.displayName = displayName == null ? "" : displayName;
        this.about = about == null ? "" : about;
        this.photoId = photoId == null || photoId.isBlank() ? "default" : photoId;
        this.registered = registered;
    }

    public UUID playerUuid() {
        return playerUuid;
    }

    public String phoneNumber() {
        return phoneNumber;
    }

    public void setPhoneNumber(String phoneNumber) {
        this.phoneNumber = phoneNumber == null ? "" : phoneNumber;
    }

    public String displayName() {
        return displayName;
    }

    public void setDisplayName(String displayName) {
        this.displayName = displayName == null ? "" : displayName;
    }

    public String about() {
        return about;
    }

    public void setAbout(String about) {
        this.about = about == null ? "" : about;
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
}