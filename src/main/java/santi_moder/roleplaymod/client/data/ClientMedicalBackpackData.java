package santi_moder.roleplaymod.client.data;

import net.minecraft.world.item.ItemStack;

import java.util.ArrayList;
import java.util.List;

public final class ClientMedicalBackpackData {

    private static final List<ItemStack> ITEMS = new ArrayList<>();

    private ClientMedicalBackpackData() {
    }

    public static void setItems(List<ItemStack> items) {
        ITEMS.clear();

        if (items == null) return;

        for (ItemStack stack : items) {
            ITEMS.add(stack == null ? ItemStack.EMPTY : stack.copy());
        }
    }

    public static int size() {
        return ITEMS.size();
    }

    public static ItemStack getItem(int slot) {
        if (slot < 0 || slot >= ITEMS.size()) {
            return ItemStack.EMPTY;
        }

        return ITEMS.get(slot);
    }

    public static boolean hasBackpack() {
        return !ITEMS.isEmpty();
    }

    public static void clear() {
        ITEMS.clear();
    }
}