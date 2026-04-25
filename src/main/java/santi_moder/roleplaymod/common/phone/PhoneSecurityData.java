package santi_moder.roleplaymod.common.phone;

import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;

public final class PhoneSecurityData {

    private PhoneSecurityData() {
    }

    public static boolean isLocked(ItemStack stack) {
        return PhoneTagAccess.getBoolean(stack, PhoneDataKeys.TAG_LOCKED, false);
    }

    public static void setLocked(ItemStack stack, boolean locked) {
        PhoneTagAccess.setBoolean(stack, PhoneDataKeys.TAG_LOCKED, locked);
    }

    public static boolean hasPassword(ItemStack stack) {
        return PhoneTagAccess.getBoolean(stack, PhoneDataKeys.TAG_HAS_PASSWORD, false);
    }

    public static void setHasPassword(ItemStack stack, boolean hasPassword) {
        PhoneTagAccess.setBoolean(stack, PhoneDataKeys.TAG_HAS_PASSWORD, hasPassword);

        if (!hasPassword) {
            setLocked(stack, false);
            clearFaceId(stack);
        }
    }

    public static String getPasscode(ItemStack stack) {
        return PhoneTagAccess.getString(
                stack,
                PhoneDataKeys.TAG_PASSCODE,
                PhoneDataDefaults.DEFAULT_PASSCODE
        );
    }

    public static void setPasscode(ItemStack stack, String passcode) {
        PhoneTagAccess.setString(stack, PhoneDataKeys.TAG_PASSCODE, sanitizePasscode(passcode));
    }

    public static boolean validatePasscode(ItemStack stack, String input) {
        return getPasscode(stack).equals(sanitizePasscode(input));
    }

    public static boolean hasFaceId(ItemStack stack) {
        return hasPassword(stack) && PhoneTagAccess.getBoolean(stack, PhoneDataKeys.TAG_HAS_FACE_ID, false);
    }

    public static void setHasFaceId(ItemStack stack, boolean enabled) {
        if (!hasPassword(stack)) {
            enabled = false;
        }

        PhoneTagAccess.setBoolean(stack, PhoneDataKeys.TAG_HAS_FACE_ID, enabled);

        if (!enabled) {
            PhoneTagAccess.setString(stack, PhoneDataKeys.TAG_FACE_ID_UUID, "");
            PhoneTagAccess.setString(stack, PhoneDataKeys.TAG_FACE_ID_NAME, "");
        }
    }

    public static void registerFaceId(ItemStack stack, Player player) {
        if (stack.isEmpty() || player == null || !hasPassword(stack)) {
            return;
        }

        PhoneTagAccess.setBoolean(stack, PhoneDataKeys.TAG_HAS_FACE_ID, true);
        PhoneTagAccess.setString(stack, PhoneDataKeys.TAG_FACE_ID_UUID, player.getUUID().toString());
        PhoneTagAccess.setString(stack, PhoneDataKeys.TAG_FACE_ID_NAME, player.getGameProfile().getName());
    }

    public static void clearFaceId(ItemStack stack) {
        PhoneTagAccess.setBoolean(stack, PhoneDataKeys.TAG_HAS_FACE_ID, false);
        PhoneTagAccess.setString(stack, PhoneDataKeys.TAG_FACE_ID_UUID, "");
        PhoneTagAccess.setString(stack, PhoneDataKeys.TAG_FACE_ID_NAME, "");
    }

    public static String getFaceIdName(ItemStack stack) {
        if (!hasFaceId(stack)) {
            return "Sin configurar";
        }

        return PhoneTagAccess.getString(stack, PhoneDataKeys.TAG_FACE_ID_NAME, "Jugador");
    }

    public static boolean shouldOpenLocked(ItemStack stack) {
        return hasPassword(stack);
    }

    public static boolean canUnlockWithFaceId(ItemStack stack, Player player) {
        if (stack.isEmpty() || player == null) {
            return false;
        }

        if (!hasPassword(stack) || !hasFaceId(stack)) {
            return false;
        }

        String uuid = PhoneTagAccess.getString(stack, PhoneDataKeys.TAG_FACE_ID_UUID, "");
        return !uuid.isEmpty() && uuid.equals(player.getUUID().toString());
    }

    public static String getSecuritySummary(ItemStack stack) {
        if (!hasPassword(stack)) {
            return "Sin contraseña";
        }

        return hasFaceId(stack) ? "PIN + Face ID" : "Solo PIN";
    }

    public static String sanitizePasscode(String input) {
        if (input == null) {
            return PhoneDataDefaults.DEFAULT_PASSCODE;
        }

        StringBuilder digits = new StringBuilder(4);
        for (int i = 0; i < input.length(); i++) {
            char c = input.charAt(i);
            if (Character.isDigit(c)) {
                digits.append(c);
            }
            if (digits.length() == 4) {
                break;
            }
        }

        while (digits.length() < 4) {
            digits.append('0');
        }

        return digits.substring(0, 4);
    }
}