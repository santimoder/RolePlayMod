package santi_moder.roleplaymod.client.phone.ui;

import net.minecraft.world.item.ItemStack;
import santi_moder.roleplaymod.common.phone.PhoneData;

public final class PhoneThemeColors {

    private PhoneThemeColors() {
    }

    public static boolean isDark(ItemStack stack) {
        return PhoneData.THEME_DARK.equals(PhoneData.getThemeMode(stack));
    }

    public static int appBackground(ItemStack stack) {
        return isDark(stack) ? 0xFF0B0B0B : 0xFFF7F7F7;
    }

    public static int card(ItemStack stack) {
        return isDark(stack) ? 0xFF1A1A1A : 0xFFFFFFFF;
    }

    public static int cardHover(ItemStack stack) {
        return isDark(stack) ? 0xFF303030 : 0xFFEFEFEF;
    }

    public static int divider(ItemStack stack) {
        return isDark(stack) ? 0xFF2A2A2A : 0xFFE0E0E0;
    }

    public static int text(ItemStack stack) {
        return isDark(stack) ? 0xFFFFFFFF : 0xFF111111;
    }

    public static int subtext(ItemStack stack) {
        return isDark(stack) ? 0xFFBEBEBE : 0xFF666666;
    }

    public static int hint(ItemStack stack) {
        return isDark(stack) ? 0xFF8F8F8F : 0xFF999999;
    }

    public static int input(ItemStack stack) {
        return isDark(stack) ? 0xFF1A1A1A : 0xFFFFFFFF;
    }

    public static int inputActive(ItemStack stack) {
        return isDark(stack) ? 0xFF263238 : 0xFFEDEDED;
    }

    public static int sheet(ItemStack stack) {
        return isDark(stack) ? 0xEE111111 : 0xEEFFFFFF;
    }

    public static int overlay() {
        return 0x66000000;
    }

    public static int danger(ItemStack stack) {
        return isDark(stack) ? 0xFFFF8080 : 0xFFE53935;
    }

    public static int success(ItemStack stack) {
        return isDark(stack) ? 0xFF7CFF9C : 0xFF25D366;
    }
}