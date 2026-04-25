package santi_moder.roleplaymod.common.phone;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.item.ItemStack;

final class PhoneTagAccess {

    private PhoneTagAccess() {
    }

    static CompoundTag getOrCreatePhoneTag(ItemStack stack) {
        CompoundTag root = stack.getOrCreateTag();

        if (!root.contains(PhoneDataKeys.TAG_PHONE)) {
            CompoundTag phoneTag = new CompoundTag();
            PhoneDataDefaults.writeDefaults(phoneTag);
            root.put(PhoneDataKeys.TAG_PHONE, phoneTag);
        }

        CompoundTag phoneTag = root.getCompound(PhoneDataKeys.TAG_PHONE);
        PhoneDataDefaults.applyMissingDefaults(phoneTag);
        return phoneTag;
    }

    static String getString(ItemStack stack, String key, String fallback) {
        String value = getOrCreatePhoneTag(stack).getString(key);
        return value == null || value.isEmpty() ? fallback : value;
    }

    static void setString(ItemStack stack, String key, String value) {
        getOrCreatePhoneTag(stack).putString(key, value == null ? "" : value);
    }

    static boolean getBoolean(ItemStack stack, String key, boolean fallback) {
        CompoundTag tag = getOrCreatePhoneTag(stack);
        return tag.contains(key) ? tag.getBoolean(key) : fallback;
    }

    static void setBoolean(ItemStack stack, String key, boolean value) {
        getOrCreatePhoneTag(stack).putBoolean(key, value);
    }

    static int getInt(ItemStack stack, String key, int fallback) {
        CompoundTag tag = getOrCreatePhoneTag(stack);
        return tag.contains(key) ? tag.getInt(key) : fallback;
    }

    static void setInt(ItemStack stack, String key, int value) {
        getOrCreatePhoneTag(stack).putInt(key, value);
    }
}