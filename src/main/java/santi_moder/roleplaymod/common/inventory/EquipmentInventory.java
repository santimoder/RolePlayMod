package santi_moder.roleplaymod.common.inventory;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import santi_moder.roleplaymod.item.ItemBackpackHuge;
import santi_moder.roleplaymod.item.ItemBackpackLarge;
import santi_moder.roleplaymod.item.ItemBackpackMedium;
import santi_moder.roleplaymod.item.ItemBackpackSmall;

public class EquipmentInventory {

    private final ItemStack[] slots = new ItemStack[8];

    public EquipmentInventory() {
        for (int i = 0; i < slots.length; i++) {
            slots[i] = ItemStack.EMPTY;
        }
    }

    public CompoundTag serializeNBT() {
        CompoundTag tag = new CompoundTag();

        for (int i = 0; i < slots.length; i++) {
            if (!slots[i].isEmpty()) {
                CompoundTag itemTag = new CompoundTag();
                slots[i].save(itemTag);
                tag.put("slot_" + i, itemTag);
            }
        }

        return tag;
    }

    public void deserializeNBT(CompoundTag tag) {
        for (int i = 0; i < slots.length; i++) {
            if (tag.contains("slot_" + i)) {
                slots[i] = ItemStack.of(tag.getCompound("slot_" + i));
            } else {
                slots[i] = ItemStack.EMPTY;
            }
        }
    }

    public int getBackpackRows() {
        if (!hasBackpack()) {
            return 0;
        }

        ItemStack backpack = slots[1];

        if (backpack.getItem() instanceof ItemBackpackSmall) return 1;
        if (backpack.getItem() instanceof ItemBackpackMedium) return 2;
        if (backpack.getItem() instanceof ItemBackpackLarge) return 3;
        if (backpack.getItem() instanceof ItemBackpackHuge) return 4;

        return 0;
    }

    public void dropAndClear(Player player) {
        for (int i = 0; i < slots.length; i++) {
            ItemStack stack = slots[i];

            if (!stack.isEmpty()) {
                player.getCommandSenderWorld().addFreshEntity(new ItemEntity(
                        player.getCommandSenderWorld(),
                        player.getX(),
                        player.getY(),
                        player.getZ(),
                        stack.copy()
                ));

                slots[i] = ItemStack.EMPTY;
            }
        }
    }

    public int getContainerSize() {
        return slots.length;
    }
    public ItemStack[] getSlots() {
        return slots;
    }
    public ItemStack getItem(int slot) {
        if (slot < 0 || slot >= slots.length) return ItemStack.EMPTY;
        return slots[slot] == null ? ItemStack.EMPTY : slots[slot];
    }
    public void setItem(int slot, ItemStack stack) {
        if (slot < 0 || slot >= slots.length) return;
        slots[slot] = (stack == null || stack.isEmpty()) ? ItemStack.EMPTY : stack.copy();
    }



    public boolean hasItem(int slot) {
        return slot >= 0 && slot < slots.length && !slots[slot].isEmpty();
    }
    public boolean hasMask() { return hasItem(0); }
    public boolean hasBackpack() { return hasItem(1); }
    public boolean hasVest() { return hasItem(2); }
    public boolean hasBelt() { return hasItem(3); }
    public boolean hasHat() { return hasItem(4); }
    public boolean hasJacket() { return hasItem(5); }
    public boolean hasPants() { return hasItem(6); }
    public boolean hasShoes() { return hasItem(7); }
}