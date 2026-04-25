package santi_moder.roleplaymod.common.inventory.item;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;

public class ItemInventory {

    private static final String INV_TAG = "rp_inventory";

    public static int getSize(ItemStack stack) {
        if (stack.isEmpty()) return 0;

        if (stack.getItem() instanceof StorageItem storageItem) {
            return storageItem.getSlotCount();
        }

        return 0;
    }

    public static ItemStack getItem(ItemStack stack, int slot) {
        int size = getSize(stack);
        if (slot < 0 || slot >= size) return ItemStack.EMPTY;

        CompoundTag tag = stack.getOrCreateTag();

        if (!tag.contains(INV_TAG)) {
            return ItemStack.EMPTY;
        }

        CompoundTag inv = tag.getCompound(INV_TAG);

        if (!inv.contains("slot_" + slot)) {
            return ItemStack.EMPTY;
        }

        return ItemStack.of(inv.getCompound("slot_" + slot));
    }

    public static void setItem(ItemStack stack, int slot, ItemStack item) {
        int size = getSize(stack);
        if (slot < 0 || slot >= size) return;

        CompoundTag tag = stack.getOrCreateTag();
        CompoundTag inv = tag.contains(INV_TAG) ? tag.getCompound(INV_TAG) : new CompoundTag();

        if (item == null || item.isEmpty()) {
            inv.remove("slot_" + slot);
        } else {
            CompoundTag itemTag = new CompoundTag();
            item.copy().save(itemTag);
            inv.put("slot_" + slot, itemTag);
        }

        tag.put(INV_TAG, inv);
    }

    public static void dropAllItems(ItemStack stack, Player player) {
        for (int i = 0; i < getSize(stack); i++) {
            ItemStack inside = getItem(stack, i);
            if (!inside.isEmpty()) {
                player.drop(inside.copy(), false);
                setItem(stack, i, ItemStack.EMPTY);
            }
        }
    }
}