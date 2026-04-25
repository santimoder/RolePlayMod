package santi_moder.roleplaymod.common.phone;

import net.minecraft.world.item.ItemStack;

public final class PhoneProfileData {

    private static final String[] PROFILE_PHOTOS = {
            "default",
            "avatar_blue",
            "avatar_dark"
    };

    private PhoneProfileData() {
    }

    public static String getProfileName(ItemStack stack) {
        return PhoneTagAccess.getString(stack, PhoneDataKeys.TAG_PROFILE_NAME, "");
    }

    public static void setProfileName(ItemStack stack, String value) {
        PhoneTagAccess.setString(stack, PhoneDataKeys.TAG_PROFILE_NAME, value == null ? "" : value);
    }

    public static String getProfileSurname(ItemStack stack) {
        return PhoneTagAccess.getString(stack, PhoneDataKeys.TAG_PROFILE_SURNAME, "");
    }

    public static void setProfileSurname(ItemStack stack, String value) {
        PhoneTagAccess.setString(stack, PhoneDataKeys.TAG_PROFILE_SURNAME, value == null ? "" : value);
    }

    public static String getProfileBirthdate(ItemStack stack) {
        return PhoneTagAccess.getString(stack, PhoneDataKeys.TAG_PROFILE_BIRTHDATE, "");
    }

    public static void setProfileBirthdate(ItemStack stack, String value) {
        PhoneTagAccess.setString(stack, PhoneDataKeys.TAG_PROFILE_BIRTHDATE, value == null ? "" : value);
    }

    public static String getProfilePhoto(ItemStack stack) {
        return PhoneTagAccess.getString(
                stack,
                PhoneDataKeys.TAG_PROFILE_PHOTO,
                PhoneDataDefaults.DEFAULT_PROFILE_PHOTO
        );
    }

    public static void setProfilePhoto(ItemStack stack, String value) {
        PhoneTagAccess.setString(
                stack,
                PhoneDataKeys.TAG_PROFILE_PHOTO,
                value == null || value.isBlank() ? PhoneDataDefaults.DEFAULT_PROFILE_PHOTO : value
        );
    }

    public static void cycleProfilePhoto(ItemStack stack) {
        setProfilePhoto(stack, nextOf(PROFILE_PHOTOS, getProfilePhoto(stack)));
    }

    public static void cycleProfileName(ItemStack stack) {
        String current = getProfileName(stack);

        if (current.isEmpty()) {
            setProfileName(stack, "Santi");
            return;
        }

        if ("Santi".equals(current)) {
            setProfileName(stack, "Alex");
            return;
        }

        setProfileName(stack, "");
    }

    public static void cycleProfileSurname(ItemStack stack) {
        String current = getProfileSurname(stack);

        if (current.isEmpty()) {
            setProfileSurname(stack, "Gomez");
            return;
        }

        if ("Gomez".equals(current)) {
            setProfileSurname(stack, "Ruiz");
            return;
        }

        setProfileSurname(stack, "");
    }

    public static void cycleProfileBirthdate(ItemStack stack) {
        String current = getProfileBirthdate(stack);

        if (current.isEmpty()) {
            setProfileBirthdate(stack, "14/08/1998");
            return;
        }

        if ("14/08/1998".equals(current)) {
            setProfileBirthdate(stack, "03/02/2001");
            return;
        }

        setProfileBirthdate(stack, "");
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