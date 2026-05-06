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

    public static int onSuccess(ItemStack stack) {
        return isDark(stack) ? 0xFF062E14 : 0xFF081C15;
    }

    public static int successHover(ItemStack stack) {
        return isDark(stack) ? 0xFF4ADE80 : 0xFF53E07E;
    }

    public static int disabledInput(ItemStack stack) {
        return isDark(stack) ? 0xFF202020 : 0xFFE6E6E6;
    }

    public static int whatsappHeader(ItemStack stack) {
        return isDark(stack) ? 0xCC111111 : 0xEEFFFFFF;
    }

    public static int whatsappHeaderIcon(ItemStack stack) {
        return isDark(stack) ? 0xFFFFFFFF : 0xFF111111;
    }

    public static int whatsappBubbleSent(ItemStack stack) {
        return isDark(stack) ? 0xFF005C4B : 0xFFDCF8C6;
    }

    public static int whatsappBubbleReceived(ItemStack stack) {
        return isDark(stack) ? 0xFF202C33 : 0xFFFFFFFF;
    }

    public static int whatsappMessageTextSent(ItemStack stack) {
        return isDark(stack) ? 0xFFFFFFFF : 0xFF111111;
    }

    public static int whatsappMessageTextReceived(ItemStack stack) {
        return isDark(stack) ? 0xFFFFFFFF : 0xFF111111;
    }

    public static int whatsappMetaSent(ItemStack stack) {
        return isDark(stack) ? 0xFFB7D7CB : 0xFF5F6F5F;
    }

    public static int whatsappMetaReceived(ItemStack stack) {
        return isDark(stack) ? 0xFFB8C8CC : 0xFF777777;
    }

    public static int whatsappActionButton(ItemStack stack) {
        return isDark(stack) ? 0xFF2A3942 : 0xFFEDEDED;
    }


}