package santi_moder.roleplaymod.common.phone;

import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;

public final class PhoneData {

    public static final String WALLPAPER_DEFAULT = PhoneDataDefaults.WALLPAPER_DEFAULT;
    public static final String WALLPAPER_BLUE = PhoneDataDefaults.WALLPAPER_BLUE;
    public static final String WALLPAPER_DARK = PhoneDataDefaults.WALLPAPER_DARK;
    public static final String WALLPAPER_CUSTOM = "custom";
    public static final String DEFAULT_DEVICE_NAME = PhoneDataDefaults.DEFAULT_DEVICE_NAME;
    public static final String DEFAULT_MODEL = PhoneDataDefaults.DEFAULT_MODEL;
    public static final String DEFAULT_PASSCODE = PhoneDataDefaults.DEFAULT_PASSCODE;
    public static final String THEME_DARK = PhoneDataDefaults.THEME_DARK;
    public static final String THEME_LIGHT = PhoneDataDefaults.THEME_LIGHT;
    public static final String SIZE_SMALL = PhoneDataDefaults.SIZE_SMALL;
    public static final String SIZE_NORMAL = PhoneDataDefaults.SIZE_NORMAL;
    public static final String SIZE_LARGE = PhoneDataDefaults.SIZE_LARGE;
    private PhoneData() {
    }

    public static boolean isCustomWallpaperSelected(ItemStack stack) {
        return WALLPAPER_CUSTOM.equals(getWallpaper(stack));
    }

    public static void initializeIfMissing(ItemStack stack) {
        PhoneTagAccess.getOrCreatePhoneTag(stack);
        PhoneInstalledAppsData.ensureDefaultInstalledApps(stack);
    }

    public static String getPhoneId(ItemStack stack) {
        return PhoneTagAccess.getString(stack, PhoneDataKeys.TAG_PHONE_ID, "");
    }

    public static boolean hasPhoneId(ItemStack stack) {
        return !getPhoneId(stack).isBlank();
    }

    public static boolean hasSim(ItemStack stack) {
        return PhoneTagAccess.getBoolean(stack, PhoneDataKeys.TAG_HAS_SIM, false);
    }

    public static String getSimId(ItemStack stack) {
        return PhoneTagAccess.getString(stack, PhoneDataKeys.TAG_SIM_ID, "");
    }

    public static String getPhoneNumber(ItemStack stack) {
        return PhoneTagAccess.getString(stack, PhoneDataKeys.TAG_PHONE_NUMBER, "");
    }

    public static void installSim(ItemStack stack, String simId, String phoneNumber) {
        PhoneTagAccess.setBoolean(stack, PhoneDataKeys.TAG_HAS_SIM, true);
        PhoneTagAccess.setString(stack, PhoneDataKeys.TAG_SIM_ID, simId);
        PhoneTagAccess.setString(stack, PhoneDataKeys.TAG_PHONE_NUMBER, phoneNumber);
        PhoneTagAccess.setString(stack, PhoneDataKeys.TAG_CARRIER, "Antel");
    }

    public static void removeSim(ItemStack stack) {
        PhoneTagAccess.setBoolean(stack, PhoneDataKeys.TAG_HAS_SIM, false);
        PhoneTagAccess.setString(stack, PhoneDataKeys.TAG_SIM_ID, "");
        PhoneTagAccess.setString(stack, PhoneDataKeys.TAG_PHONE_NUMBER, "");
    }

    public static String getCarrier(ItemStack stack) {
        return PhoneTagAccess.getString(stack, PhoneDataKeys.TAG_CARRIER, "Antel");
    }

    public static void setCarrier(ItemStack stack, String carrier) {
        PhoneTagAccess.setString(stack, PhoneDataKeys.TAG_CARRIER, carrier);
    }

    public static PhoneBrand getBrand(ItemStack stack) {
        return PhoneSettingsData.getBrand(stack);
    }

    public static void setBrand(ItemStack stack, PhoneBrand brand) {
        PhoneSettingsData.setBrand(stack, brand);
    }

    public static String getModel(ItemStack stack) {
        return PhoneSettingsData.getModel(stack);
    }

    public static void setModel(ItemStack stack, String model) {
        PhoneSettingsData.setModel(stack, model);
    }

    public static String getWallpaper(ItemStack stack) {
        return PhoneSettingsData.getWallpaper(stack);
    }

    public static void setWallpaper(ItemStack stack, String wallpaper) {
        PhoneSettingsData.setWallpaper(stack, wallpaper);
    }

    public static void cycleWallpaper(ItemStack stack) {
        PhoneSettingsData.cycleWallpaper(stack);
    }

    public static String getDeviceName(ItemStack stack) {
        return PhoneSettingsData.getDeviceName(stack);
    }

    public static void setDeviceName(ItemStack stack, String deviceName) {
        PhoneSettingsData.setDeviceName(stack, deviceName);
    }

    public static void cycleDeviceName(ItemStack stack) {
        PhoneSettingsData.cycleDeviceName(stack);
    }

    public static boolean isLocked(ItemStack stack) {
        return PhoneSecurityData.isLocked(stack);
    }

    public static void setLocked(ItemStack stack, boolean locked) {
        PhoneSecurityData.setLocked(stack, locked);
    }

    public static boolean hasPassword(ItemStack stack) {
        return PhoneSecurityData.hasPassword(stack);
    }

    public static void setHasPassword(ItemStack stack, boolean hasPassword) {
        PhoneSecurityData.setHasPassword(stack, hasPassword);
    }

    public static String getPasscode(ItemStack stack) {
        return PhoneSecurityData.getPasscode(stack);
    }

    public static void setPasscode(ItemStack stack, String passcode) {
        PhoneSecurityData.setPasscode(stack, passcode);
    }

    public static boolean validatePasscode(ItemStack stack, String input) {
        return PhoneSecurityData.validatePasscode(stack, input);
    }

    public static boolean hasFaceId(ItemStack stack) {
        return PhoneSecurityData.hasFaceId(stack);
    }

    public static void setHasFaceId(ItemStack stack, boolean enabled) {
        PhoneSecurityData.setHasFaceId(stack, enabled);
    }

    public static void registerFaceId(ItemStack stack, Player player) {
        PhoneSecurityData.registerFaceId(stack, player);
    }

    public static void clearFaceId(ItemStack stack) {
        PhoneSecurityData.clearFaceId(stack);
    }

    public static String getFaceIdName(ItemStack stack) {
        return PhoneSecurityData.getFaceIdName(stack);
    }

    public static boolean shouldOpenLocked(ItemStack stack) {
        return PhoneSecurityData.shouldOpenLocked(stack);
    }

    public static boolean canUnlockWithFaceId(ItemStack stack, Player player) {
        return PhoneSecurityData.canUnlockWithFaceId(stack, player);
    }

    public static String getSecuritySummary(ItemStack stack) {
        return PhoneSecurityData.getSecuritySummary(stack);
    }

    public static String getProfileName(ItemStack stack) {
        return PhoneProfileData.getProfileName(stack);
    }

    public static void setProfileName(ItemStack stack, String value) {
        PhoneProfileData.setProfileName(stack, value);
    }

    public static String getProfileSurname(ItemStack stack) {
        return PhoneProfileData.getProfileSurname(stack);
    }

    public static void setProfileSurname(ItemStack stack, String value) {
        PhoneProfileData.setProfileSurname(stack, value);
    }

    public static String getProfileBirthdate(ItemStack stack) {
        return PhoneProfileData.getProfileBirthdate(stack);
    }

    public static void setProfileBirthdate(ItemStack stack, String value) {
        PhoneProfileData.setProfileBirthdate(stack, value);
    }

    public static String getProfilePhoto(ItemStack stack) {
        return PhoneProfileData.getProfilePhoto(stack);
    }

    public static void setProfilePhoto(ItemStack stack, String value) {
        PhoneProfileData.setProfilePhoto(stack, value);
    }

    public static void cycleProfilePhoto(ItemStack stack) {
        PhoneProfileData.cycleProfilePhoto(stack);
    }

    public static void cycleProfileName(ItemStack stack) {
        PhoneProfileData.cycleProfileName(stack);
    }

    public static void cycleProfileSurname(ItemStack stack) {
        PhoneProfileData.cycleProfileSurname(stack);
    }

    public static void cycleProfileBirthdate(ItemStack stack) {
        PhoneProfileData.cycleProfileBirthdate(stack);
    }

    public static String getThemeMode(ItemStack stack) {
        return PhoneSettingsData.getThemeMode(stack);
    }

    public static void setThemeMode(ItemStack stack, String mode) {
        PhoneSettingsData.setThemeMode(stack, mode);
    }

    public static void toggleThemeMode(ItemStack stack) {
        PhoneSettingsData.toggleThemeMode(stack);
    }

    public static String getTextSize(ItemStack stack) {
        return PhoneSettingsData.getTextSize(stack);
    }

    public static void setTextSize(ItemStack stack, String size) {
        PhoneSettingsData.setTextSize(stack, size);
    }

    public static void cycleTextSize(ItemStack stack) {
        PhoneSettingsData.cycleTextSize(stack);
    }

    public static String getIconSize(ItemStack stack) {
        return PhoneSettingsData.getIconSize(stack);
    }

    public static void setIconSize(ItemStack stack, String size) {
        PhoneSettingsData.setIconSize(stack, size);
    }

    public static void cycleIconSize(ItemStack stack) {
        PhoneSettingsData.cycleIconSize(stack);
    }

    public static int getCallVolume(ItemStack stack) {
        return PhoneSettingsData.getCallVolume(stack);
    }

    public static void setCallVolume(ItemStack stack, int volume) {
        PhoneSettingsData.setCallVolume(stack, volume);
    }

    public static void cycleCallVolume(ItemStack stack) {
        PhoneSettingsData.cycleCallVolume(stack);
    }

    public static int getNotificationVolume(ItemStack stack) {
        return PhoneSettingsData.getNotificationVolume(stack);
    }

    public static void setNotificationVolume(ItemStack stack, int volume) {
        PhoneSettingsData.setNotificationVolume(stack, volume);
    }

    public static void cycleNotificationVolume(ItemStack stack) {
        PhoneSettingsData.cycleNotificationVolume(stack);
    }

    public static boolean isSilentMode(ItemStack stack) {
        return PhoneSettingsData.isSilentMode(stack);
    }

    public static void setSilentMode(ItemStack stack, boolean silentMode) {
        PhoneSettingsData.setSilentMode(stack, silentMode);
    }

    public static boolean isAppInstalled(ItemStack stack, PhoneAppId appId) {
        return PhoneInstalledAppsData.isInstalled(stack, appId);
    }

    public static void installApp(ItemStack stack, PhoneAppId appId) {
        PhoneInstalledAppsData.installApp(stack, appId);
    }

    public static void uninstallApp(ItemStack stack, PhoneAppId appId) {
        PhoneInstalledAppsData.uninstallApp(stack, appId);
    }

    public static void toggleSilentMode(ItemStack stack) {
        PhoneSettingsData.toggleSilentMode(stack);
    }
}