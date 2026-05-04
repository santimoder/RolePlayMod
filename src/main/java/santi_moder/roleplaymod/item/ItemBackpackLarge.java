package santi_moder.roleplaymod.item;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import santi_moder.roleplaymod.common.inventory.item.StorageItem;

import javax.annotation.Nonnull;

public class ItemBackpackLarge extends Item implements StorageItem {

    public ItemBackpackLarge(Properties properties) {
        super(properties);
    }

    @Override
    public int getMaxStackSize(ItemStack stack) {
        return 1;
    }

    @Override
    @Nonnull
    public ItemStack getDefaultInstance() {
        ItemStack stack = super.getDefaultInstance();
        stack.setTag(new CompoundTag());
        return stack;
    }

    @Override
    public int getSlotCount() {
        return 12;
    }
}