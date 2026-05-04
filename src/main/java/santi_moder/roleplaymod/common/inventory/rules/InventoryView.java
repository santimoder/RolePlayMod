package santi_moder.roleplaymod.common.inventory.rules;

import net.minecraft.world.item.ItemStack;

public interface InventoryView {
    int getContainerSize();

    ItemStack getItem(int slot);
}