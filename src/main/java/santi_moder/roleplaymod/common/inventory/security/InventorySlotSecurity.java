package santi_moder.roleplaymod.common.inventory.security;

import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import santi_moder.roleplaymod.common.inventory.EquipmentInventory;
import santi_moder.roleplaymod.common.inventory.item.ItemInventory;
import santi_moder.roleplaymod.common.inventory.slots.InventorySlot;
import santi_moder.roleplaymod.common.inventory.slots.SlotType;

public final class InventorySlotSecurity {

    private InventorySlotSecurity() {
    }

    public static SlotType resolveSlotType(int slotType) {
        if (slotType < 0 || slotType >= SlotType.values().length) {
            return null;
        }

        return SlotType.values()[slotType];
    }

    public static boolean isValidPacketSlot(
            SlotType type,
            int slotIndex,
            int storageIndex,
            int vanillaIndex,
            Player player
    ) {
        if (type == null || player == null) {
            return false;
        }

        return switch (type) {
            case EQUIPMENT -> slotIndex >= 0
                    && slotIndex <= 3
                    && storageIndex == -1
                    && vanillaIndex == -1;

            case CLOTHS -> slotIndex >= 4
                    && slotIndex <= 7
                    && storageIndex == -1
                    && vanillaIndex == -1;

            case JACKET_STORAGE,
                 PANTS_STORAGE,
                 VEST_STORAGE,
                 BELT_STORAGE -> storageIndex >= 0
                    && storageIndex < 4
                    && vanillaIndex == -1;

            case BACKPACK_STORAGE -> storageIndex >= 0
                    && storageIndex < 16
                    && vanillaIndex == -1;

            case HOTBAR -> vanillaIndex >= 0
                    && vanillaIndex < player.getInventory().items.size()
                    && slotIndex >= 0
                    && storageIndex == -1;
        };
    }

    public static boolean isSlotAccessible(
            InventorySlot slot,
            EquipmentInventory equipment,
            Player player
    ) {
        if (slot == null || equipment == null || player == null || slot.type() == null) {
            return false;
        }

        return switch (slot.type()) {
            case EQUIPMENT -> slot.index() >= 0
                    && slot.index() <= 3
                    && slot.storageIndex() == -1
                    && slot.vanillaIndex() == -1;

            case CLOTHS -> slot.index() >= 4
                    && slot.index() <= 7
                    && slot.storageIndex() == -1
                    && slot.vanillaIndex() == -1;

            case HOTBAR -> isValidVanillaIndex(slot.vanillaIndex(), player);

            case JACKET_STORAGE -> isValidStorageSlot(equipment.getItem(5), slot.storageIndex());

            case PANTS_STORAGE -> isValidStorageSlot(equipment.getItem(6), slot.storageIndex());

            case VEST_STORAGE -> isValidStorageSlot(equipment.getItem(2), slot.storageIndex());

            case BELT_STORAGE -> isValidStorageSlot(equipment.getItem(3), slot.storageIndex());

            case BACKPACK_STORAGE -> isValidStorageSlot(equipment.getItem(1), slot.storageIndex());
        };
    }

    public static boolean isValidStorageSlot(ItemStack containerStack, int storageIndex) {
        if (containerStack == null || containerStack.isEmpty()) {
            return false;
        }

        int size = ItemInventory.getSize(containerStack);
        return storageIndex >= 0 && storageIndex < size;
    }

    public static boolean isValidVanillaIndex(int vanillaIndex, Player player) {
        return player != null
                && vanillaIndex >= 0
                && vanillaIndex < player.getInventory().items.size();
    }
}