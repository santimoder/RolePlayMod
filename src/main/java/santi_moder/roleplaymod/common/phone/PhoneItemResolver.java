package santi_moder.roleplaymod.common.phone;

import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.item.ItemStack;
import santi_moder.roleplaymod.item.PhoneItem;

public final class PhoneItemResolver {

    private PhoneItemResolver() {
    }

    public static ItemStack getActivePhone(ServerPlayer player) {
        if (player == null) {
            return ItemStack.EMPTY;
        }

        ItemStack mainHand = player.getItemInHand(InteractionHand.MAIN_HAND);
        if (isPhone(mainHand)) {
            PhoneData.initializeIfMissing(mainHand);
            return mainHand;
        }

        ItemStack offHand = player.getItemInHand(InteractionHand.OFF_HAND);
        if (isPhone(offHand)) {
            PhoneData.initializeIfMissing(offHand);
            return offHand;
        }

        return ItemStack.EMPTY;
    }

    public static boolean hasActivePhone(ServerPlayer player) {
        return !getActivePhone(player).isEmpty();
    }

    public static boolean hasActivePhoneWithSim(ServerPlayer player) {
        ItemStack phone = getActivePhone(player);
        return !phone.isEmpty()
                && PhoneData.hasSim(phone)
                && !PhoneData.getSimId(phone).isBlank()
                && !PhoneData.getPhoneNumber(phone).isBlank();
    }

    public static String getActivePhoneId(ServerPlayer player) {
        ItemStack phone = getActivePhone(player);
        return phone.isEmpty() ? "" : PhoneData.getPhoneId(phone);
    }

    public static String getActiveSimId(ServerPlayer player) {
        ItemStack phone = getActivePhone(player);
        return phone.isEmpty() ? "" : PhoneData.getSimId(phone);
    }

    public static String getActivePhoneNumber(ServerPlayer player) {
        ItemStack phone = getActivePhone(player);
        return phone.isEmpty() ? "" : PhoneData.getPhoneNumber(phone);
    }

    private static boolean isPhone(ItemStack stack) {
        if (stack == null || stack.isEmpty()) {
            return false;
        }

        return stack.getItem() instanceof PhoneItem;
    }
}