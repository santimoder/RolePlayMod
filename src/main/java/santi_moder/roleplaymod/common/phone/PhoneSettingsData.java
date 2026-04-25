package santi_moder.roleplaymod.common.phone;

import net.minecraft.world.item.ItemStack;

public final class PhoneSettingsData {

    private static final String[] WALLPAPERS = {
            PhoneDataDefaults.WALLPAPER_DEFAULT,
            PhoneDataDefaults.WALLPAPER_BLUE,
            PhoneDataDefaults.WALLPAPER_DARK
    };

    private static final String[] DEVICE_NAMES = {
            "Mi iPhone",
            "Telefono RP",
            "iFruit"
    };

    private static final String[] SIZE_VALUES = {
            PhoneDataDefaults.SIZE_SMALL,
            PhoneDataDefaults.SIZE_NORMAL,
            PhoneDataDefaults.SIZE_LARGE
    };

    private PhoneSettingsData() {
    }

    public static PhoneBrand getBrand(ItemStack stack) {
        String value = PhoneTagAccess.getString(stack, PhoneDataKeys.TAG_BRAND, PhoneBrand.APPLE.name());

        try {
            return PhoneBrand.valueOf(value);
        } catch (Exception ignored) {
            return PhoneBrand.APPLE;
        }
    }

    public static void setBrand(ItemStack stack, PhoneBrand brand) {
        PhoneTagAccess.setString(stack, PhoneDataKeys.TAG_BRAND, brand == null ? PhoneBrand.APPLE.name() : brand.name());
    }

    public static String getModel(ItemStack stack) {
        return PhoneTagAccess.getString(stack, PhoneDataKeys.TAG_MODEL, PhoneDataDefaults.DEFAULT_MODEL);
    }

    public static void setModel(ItemStack stack, String model) {
        PhoneTagAccess.setString(stack, PhoneDataKeys.TAG_MODEL,
                model == null || model.isBlank() ? PhoneDataDefaults.DEFAULT_MODEL : model);
    }

    public static String getWallpaper(ItemStack stack) {
        return PhoneTagAccess.getString(stack, PhoneDataKeys.TAG_WALLPAPER, PhoneDataDefaults.WALLPAPER_DEFAULT);
    }

    public static void setWallpaper(ItemStack stack, String wallpaper) {
        PhoneTagAccess.setString(
                stack,
                PhoneDataKeys.TAG_WALLPAPER,
                wallpaper == null || wallpaper.isBlank() ? PhoneDataDefaults.WALLPAPER_DEFAULT : wallpaper
        );
    }

    public static void cycleWallpaper(ItemStack stack) {
        setWallpaper(stack, nextOf(WALLPAPERS, getWallpaper(stack)));
    }

    public static String getDeviceName(ItemStack stack) {
        return PhoneTagAccess.getString(stack, PhoneDataKeys.TAG_DEVICE_NAME, PhoneDataDefaults.DEFAULT_DEVICE_NAME);
    }

    public static void setDeviceName(ItemStack stack, String deviceName) {
        PhoneTagAccess.setString(
                stack,
                PhoneDataKeys.TAG_DEVICE_NAME,
                deviceName == null || deviceName.isBlank() ? PhoneDataDefaults.DEFAULT_DEVICE_NAME : deviceName
        );
    }

    public static void cycleDeviceName(ItemStack stack) {
        setDeviceName(stack, nextOf(DEVICE_NAMES, getDeviceName(stack)));
    }

    public static String getThemeMode(ItemStack stack) {
        return PhoneTagAccess.getString(stack, PhoneDataKeys.TAG_THEME_MODE, PhoneDataDefaults.THEME_DARK);
    }

    public static void setThemeMode(ItemStack stack, String mode) {
        String normalized = PhoneDataDefaults.THEME_LIGHT.equals(mode)
                ? PhoneDataDefaults.THEME_LIGHT
                : PhoneDataDefaults.THEME_DARK;

        PhoneTagAccess.setString(stack, PhoneDataKeys.TAG_THEME_MODE, normalized);
    }

    public static void toggleThemeMode(ItemStack stack) {
        setThemeMode(
                stack,
                PhoneDataDefaults.THEME_DARK.equals(getThemeMode(stack))
                        ? PhoneDataDefaults.THEME_LIGHT
                        : PhoneDataDefaults.THEME_DARK
        );
    }

    public static String getTextSize(ItemStack stack) {
        return PhoneTagAccess.getString(stack, PhoneDataKeys.TAG_TEXT_SIZE, PhoneDataDefaults.SIZE_NORMAL);
    }

    public static void setTextSize(ItemStack stack, String size) {
        PhoneTagAccess.setString(stack, PhoneDataKeys.TAG_TEXT_SIZE, normalizeSize(size));
    }

    public static void cycleTextSize(ItemStack stack) {
        setTextSize(stack, nextOf(SIZE_VALUES, getTextSize(stack)));
    }

    public static String getIconSize(ItemStack stack) {
        return PhoneTagAccess.getString(stack, PhoneDataKeys.TAG_ICON_SIZE, PhoneDataDefaults.SIZE_NORMAL);
    }

    public static void setIconSize(ItemStack stack, String size) {
        PhoneTagAccess.setString(stack, PhoneDataKeys.TAG_ICON_SIZE, normalizeSize(size));
    }

    public static void cycleIconSize(ItemStack stack) {
        setIconSize(stack, nextOf(SIZE_VALUES, getIconSize(stack)));
    }

    public static int getCallVolume(ItemStack stack) {
        return clampVolume(PhoneTagAccess.getInt(stack, PhoneDataKeys.TAG_CALL_VOLUME, PhoneDataDefaults.DEFAULT_CALL_VOLUME));
    }

    public static void setCallVolume(ItemStack stack, int volume) {
        PhoneTagAccess.setInt(stack, PhoneDataKeys.TAG_CALL_VOLUME, clampVolume(volume));
    }

    public static void cycleCallVolume(ItemStack stack) {
        int current = getCallVolume(stack);
        setCallVolume(stack, current >= 100 ? 0 : current + 10);
    }

    public static int getNotificationVolume(ItemStack stack) {
        return clampVolume(
                PhoneTagAccess.getInt(stack, PhoneDataKeys.TAG_NOTIFICATION_VOLUME, PhoneDataDefaults.DEFAULT_NOTIFICATION_VOLUME)
        );
    }

    public static void setNotificationVolume(ItemStack stack, int volume) {
        PhoneTagAccess.setInt(stack, PhoneDataKeys.TAG_NOTIFICATION_VOLUME, clampVolume(volume));
    }

    public static void cycleNotificationVolume(ItemStack stack) {
        int current = getNotificationVolume(stack);
        setNotificationVolume(stack, current >= 100 ? 0 : current + 10);
    }

    public static boolean isSilentMode(ItemStack stack) {
        return PhoneTagAccess.getBoolean(stack, PhoneDataKeys.TAG_SILENT_MODE, false);
    }

    public static void setSilentMode(ItemStack stack, boolean silentMode) {
        PhoneTagAccess.setBoolean(stack, PhoneDataKeys.TAG_SILENT_MODE, silentMode);
    }

    public static void toggleSilentMode(ItemStack stack) {
        setSilentMode(stack, !isSilentMode(stack));
    }

    private static int clampVolume(int value) {
        return Math.max(0, Math.min(100, value));
    }

    private static String normalizeSize(String value) {
        if (PhoneDataDefaults.SIZE_SMALL.equals(value)) {
            return PhoneDataDefaults.SIZE_SMALL;
        }
        if (PhoneDataDefaults.SIZE_LARGE.equals(value)) {
            return PhoneDataDefaults.SIZE_LARGE;
        }
        return PhoneDataDefaults.SIZE_NORMAL;
    }

    private static String nextOf(String[] values, String current) {
        for (int i = 0; i < values.length; i++) {
            if (values[i].equals(current)) {
                return values[(i + 1) % values.length];
            }
        }
        return values[0];
    }
}