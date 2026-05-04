package santi_moder.roleplaymod.client.data;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.item.ItemStack;
import santi_moder.roleplaymod.common.inventory.EquipmentInventory;

public class ClientInventoryData {

    private static final EquipmentInventory EQUIPMENT = new EquipmentInventory();
    private static ItemStack carried = ItemStack.EMPTY;

    public static void update(CompoundTag tag, ItemStack newCarried) {
        if (tag != null) {
            EQUIPMENT.deserializeNBT(tag);
        }
        carried = newCarried == null ? ItemStack.EMPTY : newCarried.copy();
    }

    public static EquipmentInventory getEquipment() {
        return EQUIPMENT;
    }

    public static ItemStack getCarried() {
        return carried.copy();
    }

    public static void setCarried(ItemStack stack) {
        carried = stack == null ? ItemStack.EMPTY : stack.copy();
    }
}