package santi_moder.roleplaymod.common.radio;

import net.minecraft.world.item.ItemStack;

public interface RadioStackAccess {

    ItemStack get();

    void set(ItemStack stack);
}