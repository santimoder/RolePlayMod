package santi_moder.roleplaymod.common.whatsapp.model;

import net.minecraft.nbt.CompoundTag;

import java.util.Objects;
import java.util.UUID;

public final class WhatsappContact {

    public static final String DEFAULT_PHOTO = "default";

    private final String id;
    private String displayName;
    private String phoneNumber;
    private String photoId;
    private String about;
    private boolean blocked;
    private int commonGroupsCount;
    private int mediaCount;

    public WhatsappContact(
            String id,
            String displayName,
            String phoneNumber,
            String photoId,
            String about,
            boolean blocked,
            int commonGroupsCount,
            int mediaCount
    ) {
        this.id = id == null || id.isBlank() ? UUID.randomUUID().toString() : id;
        this.displayName = safe(displayName, "Contacto");
        this.phoneNumber = safe(phoneNumber, "");
        this.photoId = safe(photoId, DEFAULT_PHOTO);
        this.about = safe(about, "");
        this.blocked = blocked;
        this.commonGroupsCount = Math.max(0, commonGroupsCount);
        this.mediaCount = Math.max(0, mediaCount);
    }

    public static WhatsappContact of(
            String displayName,
            String phoneNumber,
            String photoId,
            String about,
            boolean blocked,
            int commonGroupsCount,
            int mediaCount
    ) {
        return new WhatsappContact(
                UUID.randomUUID().toString(),
                displayName,
                phoneNumber,
                photoId,
                about,
                blocked,
                commonGroupsCount,
                mediaCount
        );
    }

    public String id() {
        return id;
    }

    public String displayName() {
        return displayName;
    }

    public void setDisplayName(String displayName) {
        this.displayName = safe(displayName, "Contacto");
    }

    public String phoneNumber() {
        return phoneNumber;
    }

    public void setPhoneNumber(String phoneNumber) {
        this.phoneNumber = safe(phoneNumber, "");
    }

    public String photoId() {
        return photoId;
    }

    public void setPhotoId(String photoId) {
        this.photoId = safe(photoId, DEFAULT_PHOTO);
    }

    public String about() {
        return about;
    }

    public void setAbout(String about) {
        this.about = safe(about, "");
    }

    public boolean blocked() {
        return blocked;
    }

    public void setBlocked(boolean blocked) {
        this.blocked = blocked;
    }

    public int commonGroupsCount() {
        return commonGroupsCount;
    }

    public void setCommonGroupsCount(int commonGroupsCount) {
        this.commonGroupsCount = Math.max(0, commonGroupsCount);
    }

    public int mediaCount() {
        return mediaCount;
    }

    public void setMediaCount(int mediaCount) {
        this.mediaCount = Math.max(0, mediaCount);
    }

    public String getInitials() {
        if (displayName == null || displayName.isBlank()) {
            return "CT";
        }

        String[] parts = displayName.trim().split("\\s+");
        if (parts.length == 1) {
            String part = parts[0];
            return part.length() >= 2
                    ? part.substring(0, 2).toUpperCase()
                    : part.substring(0, 1).toUpperCase();
        }

        String first = parts[0].isEmpty() ? "" : parts[0].substring(0, 1);
        String second = parts[1].isEmpty() ? "" : parts[1].substring(0, 1);
        return (first + second).toUpperCase();
    }

    public CompoundTag save() {
        CompoundTag tag = new CompoundTag();

        tag.putString("id", id);
        tag.putString("displayName", displayName);
        tag.putString("phoneNumber", phoneNumber);
        tag.putString("photoId", photoId);
        tag.putString("about", about);
        tag.putBoolean("blocked", blocked);
        tag.putInt("commonGroupsCount", commonGroupsCount);
        tag.putInt("mediaCount", mediaCount);

        return tag;
    }

    public static WhatsappContact load(CompoundTag tag) {
        if (tag == null) {
            return null;
        }

        return new WhatsappContact(
                tag.getString("id"),
                tag.getString("displayName"),
                tag.getString("phoneNumber"),
                tag.getString("photoId"),
                tag.getString("about"),
                tag.getBoolean("blocked"),
                tag.getInt("commonGroupsCount"),
                tag.getInt("mediaCount")
        );
    }

    private static String safe(String value, String fallback) {
        return value == null || value.isBlank() ? fallback : value;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) return true;
        if (!(obj instanceof WhatsappContact other)) return false;
        return Objects.equals(id, other.id);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id);
    }
}