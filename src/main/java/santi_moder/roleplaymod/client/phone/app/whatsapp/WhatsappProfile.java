package santi_moder.roleplaymod.client.phone.app.whatsapp;

public final class WhatsappProfile {

    public static final String DEFAULT_PHOTO = "default";

    private String photoId;
    private String about;
    private String displayName;
    private String phoneNumber;

    public WhatsappProfile(String photoId, String about, String displayName, String phoneNumber) {
        this.photoId = isBlank(photoId) ? DEFAULT_PHOTO : photoId;
        this.about = safe(about);
        this.displayName = safe(displayName);
        this.phoneNumber = safe(phoneNumber);
    }

    public static WhatsappProfile createDefault(String displayName, String phoneNumber) {
        return new WhatsappProfile(
                DEFAULT_PHOTO,
                "",
                safe(displayName),
                safe(phoneNumber)
        );
    }

    public String photoId() {
        return photoId;
    }

    public void setPhotoId(String photoId) {
        this.photoId = isBlank(photoId) ? DEFAULT_PHOTO : photoId;
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

    private static String safe(String value) {
        return value == null ? "" : value;
    }

    private static boolean isBlank(String value) {
        return value == null || value.isBlank();
    }
}