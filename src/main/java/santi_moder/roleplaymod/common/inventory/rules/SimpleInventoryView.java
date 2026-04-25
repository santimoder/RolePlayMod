package santi_moder.roleplaymod.common.inventory.rules;

import net.minecraft.world.item.ItemStack;

import java.util.function.IntFunction;

public class SimpleInventoryView implements InventoryView {

    private final int size;
    private final IntFunction<ItemStack> getter;

    public SimpleInventoryView(int size, IntFunction<ItemStack> getter) {
        this.size = size;
        this.getter = getter;
    }

    @Override
    public int getContainerSize() {
        return size;
    }

    @Override
    public ItemStack getItem(int slot) {
        return getter.apply(slot);
    }
}