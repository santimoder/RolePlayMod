package santi_moder.roleplaymod.common.phone;

import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.StringTag;
import net.minecraft.nbt.Tag;
import net.minecraft.world.item.ItemStack;

import java.util.EnumSet;
import java.util.Set;

public final class PhoneInstalledAppsData {

    private static final Set<PhoneAppId> FACTORY_APPS = EnumSet.of(
            PhoneAppId.SETTINGS,
            PhoneAppId.APP_STORE,
            PhoneAppId.PHOTOS,
            PhoneAppId.CAMERA,
            PhoneAppId.CALENDAR,
            PhoneAppId.CLOCK,
            PhoneAppId.SAFARI,
            PhoneAppId.PHONE,
            PhoneAppId.MESSAGES,
            PhoneAppId.FACETIME,
            PhoneAppId.CONTACTS,
            PhoneAppId.MAPS,
            PhoneAppId.WEATHER,
            PhoneAppId.CALCULATOR,
            PhoneAppId.NOTES
    );

    private PhoneInstalledAppsData() {
    }

    public static boolean isInstalled(ItemStack stack, PhoneAppId appId) {
        if (appId == null) {
            return false;
        }

        if (appId.isSystemApp()) {
            return true;
        }

        ensureDefaultInstalledApps(stack);
        return getInstalledApps(stack).contains(appId);
    }

    public static Set<PhoneAppId> getInstalledApps(ItemStack stack) {
        ensureDefaultInstalledApps(stack);

        Set<PhoneAppId> result = EnumSet.noneOf(PhoneAppId.class);
        var phoneTag = PhoneTagAccess.getOrCreatePhoneTag(stack);

        ListTag list = phoneTag.getList(PhoneDataKeys.TAG_INSTALLED_APPS, Tag.TAG_STRING);
        for (int i = 0; i < list.size(); i++) {
            try {
                result.add(PhoneAppId.valueOf(list.getString(i)));
            } catch (IllegalArgumentException ignored) {
            }
        }

        return result;
    }

    public static void installApp(ItemStack stack, PhoneAppId appId) {
        if (stack.isEmpty() || appId == null || appId.isSystemApp()) {
            return;
        }

        Set<PhoneAppId> apps = getInstalledApps(stack);
        apps.add(appId);
        writeInstalledApps(stack, apps);
    }

    public static void uninstallApp(ItemStack stack, PhoneAppId appId) {
        if (stack.isEmpty() || appId == null || isFactoryApp(appId)) {
            return;
        }

        Set<PhoneAppId> apps = getInstalledApps(stack);
        apps.remove(appId);
        writeInstalledApps(stack, apps);
    }

    public static boolean isFactoryApp(PhoneAppId appId) {
        return appId != null && FACTORY_APPS.contains(appId);
    }

    public static void ensureDefaultInstalledApps(ItemStack stack) {
        if (stack.isEmpty()) {
            return;
        }

        var phoneTag = PhoneTagAccess.getOrCreatePhoneTag(stack);
        if (!phoneTag.contains(PhoneDataKeys.TAG_INSTALLED_APPS, Tag.TAG_LIST)) {
            writeInstalledApps(stack, FACTORY_APPS);
        }
    }

    private static void writeInstalledApps(ItemStack stack, Set<PhoneAppId> apps) {
        var phoneTag = PhoneTagAccess.getOrCreatePhoneTag(stack);
        ListTag list = new ListTag();

        for (PhoneAppId appId : apps) {
            if (appId != null && appId.isVisibleOnHome()) {
                list.add(StringTag.valueOf(appId.name()));
            }
        }

        phoneTag.put(PhoneDataKeys.TAG_INSTALLED_APPS, list);
    }
}