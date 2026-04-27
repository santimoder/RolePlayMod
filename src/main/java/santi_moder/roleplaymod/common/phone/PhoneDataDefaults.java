package santi_moder.roleplaymod.common.phone;

import net.minecraft.nbt.CompoundTag;
import java.util.UUID;

public final class PhoneDataDefaults {

    public static final String WALLPAPER_DEFAULT = "default";
    public static final String WALLPAPER_BLUE = "blue";
    public static final String WALLPAPER_DARK = "dark";

    public static final String DEFAULT_DEVICE_NAME = "Mi iPhone";
    public static final String DEFAULT_MODEL = "iPhone";
    public static final String DEFAULT_PASSCODE = "1234";
    public static final String DEFAULT_PROFILE_PHOTO = "default";

    public static final String THEME_DARK = "dark";
    public static final String THEME_LIGHT = "light";

    public static final String SIZE_SMALL = "small";
    public static final String SIZE_NORMAL = "normal";
    public static final String SIZE_LARGE = "large";

    public static final int DEFAULT_CALL_VOLUME = 70;
    public static final int DEFAULT_NOTIFICATION_VOLUME = 70;

    private PhoneDataDefaults() {
    }

    public static void writeDefaults(CompoundTag phoneTag) {
        phoneTag.putString(PhoneDataKeys.TAG_PHONE_ID, UUID.randomUUID().toString());
        phoneTag.putBoolean(PhoneDataKeys.TAG_HAS_SIM, false);
        phoneTag.putString(PhoneDataKeys.TAG_CARRIER, "Antel");
        phoneTag.putString(PhoneDataKeys.TAG_SIM_ID, "");
        phoneTag.putString(PhoneDataKeys.TAG_PHONE_NUMBER, "");
        phoneTag.putString(PhoneDataKeys.TAG_BRAND, PhoneBrand.APPLE.name());
        phoneTag.putString(PhoneDataKeys.TAG_MODEL, DEFAULT_MODEL);
        phoneTag.putString(PhoneDataKeys.TAG_WALLPAPER, WALLPAPER_DEFAULT);
        phoneTag.putString(PhoneDataKeys.TAG_DEVICE_NAME, DEFAULT_DEVICE_NAME);

        phoneTag.putBoolean(PhoneDataKeys.TAG_LOCKED, false);
        phoneTag.putBoolean(PhoneDataKeys.TAG_HAS_PASSWORD, false);
        phoneTag.putString(PhoneDataKeys.TAG_PASSCODE, DEFAULT_PASSCODE);

        phoneTag.putBoolean(PhoneDataKeys.TAG_HAS_FACE_ID, false);
        phoneTag.putString(PhoneDataKeys.TAG_FACE_ID_UUID, "");
        phoneTag.putString(PhoneDataKeys.TAG_FACE_ID_NAME, "");

        phoneTag.putString(PhoneDataKeys.TAG_PROFILE_NAME, "");
        phoneTag.putString(PhoneDataKeys.TAG_PROFILE_SURNAME, "");
        phoneTag.putString(PhoneDataKeys.TAG_PROFILE_BIRTHDATE, "");
        phoneTag.putString(PhoneDataKeys.TAG_PROFILE_PHOTO, DEFAULT_PROFILE_PHOTO);

        phoneTag.putString(PhoneDataKeys.TAG_THEME_MODE, THEME_DARK);
        phoneTag.putString(PhoneDataKeys.TAG_TEXT_SIZE, SIZE_NORMAL);
        phoneTag.putString(PhoneDataKeys.TAG_ICON_SIZE, SIZE_NORMAL);

        phoneTag.putInt(PhoneDataKeys.TAG_CALL_VOLUME, DEFAULT_CALL_VOLUME);
        phoneTag.putInt(PhoneDataKeys.TAG_NOTIFICATION_VOLUME, DEFAULT_NOTIFICATION_VOLUME);
        phoneTag.putBoolean(PhoneDataKeys.TAG_SILENT_MODE, false);
    }

    public static void applyMissingDefaults(CompoundTag phoneTag) {
        if (!phoneTag.contains(PhoneDataKeys.TAG_PHONE_ID) || phoneTag.getString(PhoneDataKeys.TAG_PHONE_ID).isBlank()) {
            phoneTag.putString(PhoneDataKeys.TAG_PHONE_ID, UUID.randomUUID().toString());
        }
        putBooleanIfMissing(phoneTag, PhoneDataKeys.TAG_HAS_SIM, false);
        putStringIfMissing(phoneTag, PhoneDataKeys.TAG_SIM_ID, "");
        putStringIfMissing(phoneTag, PhoneDataKeys.TAG_CARRIER, "Antel");
        putStringIfMissing(phoneTag, PhoneDataKeys.TAG_PHONE_NUMBER, "");
        putStringIfMissing(phoneTag, PhoneDataKeys.TAG_BRAND, PhoneBrand.APPLE.name());
        putStringIfMissing(phoneTag, PhoneDataKeys.TAG_MODEL, DEFAULT_MODEL);
        putStringIfMissing(phoneTag, PhoneDataKeys.TAG_WALLPAPER, WALLPAPER_DEFAULT);
        putStringIfMissing(phoneTag, PhoneDataKeys.TAG_DEVICE_NAME, DEFAULT_DEVICE_NAME);

        putBooleanIfMissing(phoneTag, PhoneDataKeys.TAG_LOCKED, false);
        putBooleanIfMissing(phoneTag, PhoneDataKeys.TAG_HAS_PASSWORD, false);
        putStringIfMissing(phoneTag, PhoneDataKeys.TAG_PASSCODE, DEFAULT_PASSCODE);

        putBooleanIfMissing(phoneTag, PhoneDataKeys.TAG_HAS_FACE_ID, false);
        putStringIfMissing(phoneTag, PhoneDataKeys.TAG_FACE_ID_UUID, "");
        putStringIfMissing(phoneTag, PhoneDataKeys.TAG_FACE_ID_NAME, "");

        putStringIfMissing(phoneTag, PhoneDataKeys.TAG_PROFILE_NAME, "");
        putStringIfMissing(phoneTag, PhoneDataKeys.TAG_PROFILE_SURNAME, "");
        putStringIfMissing(phoneTag, PhoneDataKeys.TAG_PROFILE_BIRTHDATE, "");
        putStringIfMissing(phoneTag, PhoneDataKeys.TAG_PROFILE_PHOTO, DEFAULT_PROFILE_PHOTO);

        putStringIfMissing(phoneTag, PhoneDataKeys.TAG_THEME_MODE, THEME_DARK);
        putStringIfMissing(phoneTag, PhoneDataKeys.TAG_TEXT_SIZE, SIZE_NORMAL);
        putStringIfMissing(phoneTag, PhoneDataKeys.TAG_ICON_SIZE, SIZE_NORMAL);

        putIntIfMissing(phoneTag, PhoneDataKeys.TAG_CALL_VOLUME, DEFAULT_CALL_VOLUME);
        putIntIfMissing(phoneTag, PhoneDataKeys.TAG_NOTIFICATION_VOLUME, DEFAULT_NOTIFICATION_VOLUME);
        putBooleanIfMissing(phoneTag, PhoneDataKeys.TAG_SILENT_MODE, false);
    }

    private static void putStringIfMissing(CompoundTag tag, String key, String value) {
        if (!tag.contains(key)) {
            tag.putString(key, value);
        }
    }

    private static void putBooleanIfMissing(CompoundTag tag, String key, boolean value) {
        if (!tag.contains(key)) {
            tag.putBoolean(key, value);
        }
    }

    private static void putIntIfMissing(CompoundTag tag, String key, int value) {
        if (!tag.contains(key)) {
            tag.putInt(key, value);
        }
    }
}