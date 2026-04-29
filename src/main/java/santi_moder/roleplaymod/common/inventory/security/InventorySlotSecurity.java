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
            case EQUIPMENT ->
                    slotIndex >= 0
                            && slotIndex <= 3
                            && storageIndex == -1
                            && vanillaIndex == -1;

            case CLOTHS ->
                    slotIndex >= 4
                            && slotIndex <= 7
                            && storageIndex == -1
                            && vanillaIndex == -1;

            case JACKET_STORAGE,
                 PANTS_STORAGE,
                 VEST_STORAGE,
                 BELT_STORAGE ->
                    storageIndex >= 0
                            && storageIndex < 4
                            && vanillaIndex == -1;

            case BACKPACK_STORAGE ->
                    storageIndex >= 0
                            && storageIndex < 16
                            && vanillaIndex == -1;

            case HOTBAR ->
                    vanillaIndex >= 0
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
        if (slot == null || equipment == null || player == null || slot.getType() == null) {
            return false;
        }

        return switch (slot.getType()) {
            case EQUIPMENT ->
                    slot.getIndex() >= 0
                            && slot.getIndex() <= 3
                            && slot.getStorageIndex() == -1
                            && slot.getVanillaIndex() == -1;

            case CLOTHS ->
                    slot.getIndex() >= 4
                            && slot.getIndex() <= 7
                            && slot.getStorageIndex() == -1
                            && slot.getVanillaIndex() == -1;

            case HOTBAR ->
                    isValidVanillaIndex(slot.getVanillaIndex(), player);

            case JACKET_STORAGE ->
                    isValidStorageSlot(equipment.getItem(5), slot.getStorageIndex());

            case PANTS_STORAGE ->
                    isValidStorageSlot(equipment.getItem(6), slot.getStorageIndex());

            case VEST_STORAGE ->
                    isValidStorageSlot(equipment.getItem(2), slot.getStorageIndex());

            case BELT_STORAGE ->
                    isValidStorageSlot(equipment.getItem(3), slot.getStorageIndex());

            case BACKPACK_STORAGE ->
                    isValidStorageSlot(equipment.getItem(1), slot.getStorageIndex());
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