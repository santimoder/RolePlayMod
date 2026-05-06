package santi_moder.roleplaymod.common.whatsapp.model;

import net.minecraft.nbt.CompoundTag;

import java.util.concurrent.ThreadLocalRandom;

public final class WhatsappProfile {

    public static final String DEFAULT_PHOTO = "default_1";

    public static final String[] DEFAULT_PHOTOS = {
            "default_1",
            "default_2",
            "default_3",
            "default_4"
    };

    private String photoId;
    private String about;
    private String displayName;
    private String phoneNumber;

    public WhatsappProfile(String photoId, String about, String displayName, String phoneNumber) {
        this.photoId = normalizePhotoId(photoId);
        this.about = safe(about);
        this.displayName = safe(displayName);
        this.phoneNumber = safe(phoneNumber);
    }

    public static WhatsappProfile createDefault(String displayName, String phoneNumber) {
        return new WhatsappProfile(
                randomDefaultPhotoId(),
                "",
                safe(displayName),
                safe(phoneNumber)
        );
    }

    public static String randomDefaultPhotoId() {
        return DEFAULT_PHOTOS[ThreadLocalRandom.current().nextInt(DEFAULT_PHOTOS.length)];
    }

    public static boolean isDefaultPhoto(String photoId) {
        if (photoId == null || photoId.isBlank()) {
            return true;
        }

        for (String defaultPhoto : DEFAULT_PHOTOS) {
            if (defaultPhoto.equals(photoId)) {
                return true;
            }
        }

        return "default".equals(photoId);
    }

    private static String normalizePhotoId(String photoId) {
        if (photoId == null || photoId.isBlank() || "default".equals(photoId)) {
            return randomDefaultPhotoId();
        }
        return photoId;
    }

    public static WhatsappProfile load(CompoundTag tag) {
        if (tag == null) {
            return createDefault("", "");
        }

        return new WhatsappProfile(
                tag.getString("photoId"),
                tag.getString("about"),
                tag.getString("displayName"),
                tag.getString("phoneNumber")
        );
    }

    private static String safe(String value) {
        return value == null ? "" : value;
    }

    public String photoId() {
        return photoId;
    }

    public void setPhotoId(String photoId) {
        this.photoId = normalizePhotoId(photoId);
    }

    public String about() {
        return about;
    }

    public void setAbout(String about) {
        this.about = safe(about);
    }

    public String displayName() {
        return displayName;
    }

    public void setDisplayName(String displayName) {
        this.displayName = safe(displayName);
    }

    public String phoneNumber() {
        return phoneNumber;
    }

    public void setPhoneNumber(String phoneNumber) {
        this.phoneNumber = safe(phoneNumber);
    }

    public CompoundTag save() {
        CompoundTag tag = new CompoundTag();

        tag.putString("photoId", photoId);
        tag.putString("about", about);
        tag.putString("displayName", displayName);
        tag.putString("phoneNumber", phoneNumber);

        return tag;
    }
}