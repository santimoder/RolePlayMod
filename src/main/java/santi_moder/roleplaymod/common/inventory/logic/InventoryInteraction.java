package santi_moder.roleplaymod.common.inventory.logic;

import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import santi_moder.roleplaymod.common.inventory.EquipmentInventory;
import santi_moder.roleplaymod.common.inventory.item.ItemInventory;
import santi_moder.roleplaymod.common.inventory.rules.ContainerRules;
import santi_moder.roleplaymod.common.inventory.rules.ContainerType;
import santi_moder.roleplaymod.common.inventory.slots.InventorySlot;
import santi_moder.roleplaymod.common.inventory.validation.SlotValidator;
import santi_moder.roleplaymod.item.ItemBackpackHuge;
import santi_moder.roleplaymod.item.ItemBackpackLarge;
import santi_moder.roleplaymod.item.ItemBackpackMedium;
import santi_moder.roleplaymod.item.ItemBackpackSmall;

public class InventoryInteraction {

    public static ItemStack clickSlot(
            InventorySlot slot,
            EquipmentInventory equipment,
            ItemStack carriedStack,
            Player player,
            int mouseButton
    ) {
        ItemStack slotStack = getStack(slot, equipment, player);

        if (!carriedStack.isEmpty()) {
            ItemStack containerStack = getContainerStack(slot, equipment);
            int validationIndex = getValidationIndex(slot);

            if (!SlotValidator.isValid(carriedStack, slot.getType(), validationIndex, containerStack)) {
                return carriedStack;
            }
        }

        // CLICK IZQUIERDO
        if (mouseButton == 0) {
            if (carriedStack.isEmpty()) {
                setStack(slot, equipment, player, ItemStack.EMPTY);
                return slotStack.copy();
            }

            if (slotStack.isEmpty()) {
                int maxStack = getMaxStackForSlot(slot, equipment, carriedStack);

                ItemStack toPlace = carriedStack.copy();
                toPlace.setCount(Math.min(carriedStack.getCount(), maxStack));
                setStack(slot, equipment, player, toPlace);

                ItemStack remaining = carriedStack.copy();
                remaining.shrink(toPlace.getCount());
                return remaining.isEmpty() ? ItemStack.EMPTY : remaining;
            }

            if (ItemStack.isSameItemSameTags(slotStack, carriedStack)) {
                int maxStack = getMaxStackForSlot(slot, equipment, carriedStack);
                int space = maxStack - slotStack.getCount();

                if (space > 0) {
                    int moveAmount = Math.min(space, carriedStack.getCount());

                    ItemStack newSlot = slotStack.copy();
                    newSlot.grow(moveAmount);
                    setStack(slot, equipment, player, newSlot);

                    ItemStack remaining = carriedStack.copy();
                    remaining.shrink(moveAmount);
                    return remaining.isEmpty() ? ItemStack.EMPTY : remaining;
                }
            }

            setStack(slot, equipment, player, carriedStack.copy());
            return slotStack.copy();
        }

        // CLICK DERECHO
        if (mouseButton == 1) {
            if (carriedStack.isEmpty() && !slotStack.isEmpty()) {
                int half = (slotStack.getCount() + 1) / 2;
                ItemStack taken = slotStack.copy();
                taken.setCount(half);

                ItemStack remaining = slotStack.copy();
                remaining.shrink(half);
                setStack(slot, equipment, player, remaining);

                return taken;
            }

            if (!carriedStack.isEmpty()) {
                if (slotStack.isEmpty()) {
                    ItemStack one = carriedStack.copy();
                    one.setCount(1);
                    setStack(slot, equipment, player, one);

                    ItemStack newCarried = carriedStack.copy();
                    newCarried.shrink(1);
                    return newCarried;
                }

                if (ItemStack.isSameItemSameTags(slotStack, carriedStack)) {
                    int maxStack = getMaxStackForSlot(slot, equipment, carriedStack);

                    if (slotStack.getCount() < maxStack) {
                        ItemStack newSlot = slotStack.copy();
                        newSlot.grow(1);
                        setStack(slot, equipment, player, newSlot);

                        ItemStack newCarried = carriedStack.copy();
                        newCarried.shrink(1);
                        return newCarried;
                    }
                }
            }
        }

        return carriedStack;
    }

    private static ContainerType resolveContainerType(InventorySlot slot, ItemStack containerStack) {
        return switch (slot.getType()) {
            case BELT_STORAGE -> ContainerType.BELT;
            case VEST_STORAGE -> ContainerType.VEST;
            case PANTS_STORAGE -> ContainerType.PANTS;
            case JACKET_STORAGE -> ContainerType.JACKET;

            case BACKPACK_STORAGE -> {
                if (containerStack.getItem() instanceof ItemBackpackSmall)
                    yield ContainerType.BACKPACK_SMALL;
                if (containerStack.getItem() instanceof ItemBackpackMedium)
                    yield ContainerType.BACKPACK_MEDIUM;
                if (containerStack.getItem() instanceof ItemBackpackLarge)
                    yield ContainerType.BACKPACK_LARGE;
                if (containerStack.getItem() instanceof ItemBackpackHuge)
                    yield ContainerType.BACKPACK_HUGE;

                yield null;
            }

            default -> null;
        };
    }

    private static int getMaxStackForSlot(
            InventorySlot slot,
            EquipmentInventory equipment,
            ItemStack stack
    ) {
        ItemStack containerStack = getContainerStack(slot, equipment);
        int slotIndex = getValidationIndex(slot);

        if (containerStack == null || containerStack.isEmpty()) {
            return stack.getMaxStackSize();
        }

        var containerType = resolveContainerType(slot, containerStack);

        if (containerType == null) {
            return stack.getMaxStackSize();
        }

        return ContainerRules.getSlotMaxStackSize(containerType, slotIndex, stack);
    }

    private static ItemStack getContainerStack(InventorySlot slot, EquipmentInventory equipment) {
        return switch (slot.getType()) {
            case JACKET_STORAGE -> equipment.getItem(5);
            case PANTS_STORAGE -> equipment.getItem(6);
            case VEST_STORAGE -> equipment.getItem(2);
            case BELT_STORAGE -> equipment.getItem(3);
            case BACKPACK_STORAGE -> equipment.getItem(1);
            default -> ItemStack.EMPTY;
        };
    }

    private static int getValidationIndex(InventorySlot slot) {
        return switch (slot.getType()) {
            case JACKET_STORAGE, PANTS_STORAGE, VEST_STORAGE, BELT_STORAGE, BACKPACK_STORAGE ->
                    slot.getStorageIndex();
            default ->
                    slot.getIndex();
        };
    }

    private static ItemStack getStack(
            InventorySlot slot,
            EquipmentInventory equipment,
            Player player
    ) {
        return switch (slot.getType()) {
            case EQUIPMENT, CLOTHS ->
                    equipment.getItem(slot.getIndex());

            case HOTBAR ->
                    player.getInventory().items.get(slot.getVanillaIndex());

            case JACKET_STORAGE ->
                    ItemInventory.getItem(equipment.getItem(5), slot.getStorageIndex());

            case PANTS_STORAGE ->
                    ItemInventory.getItem(equipment.getItem(6), slot.getStorageIndex());

            case VEST_STORAGE ->
                    ItemInventory.getItem(equipment.getItem(2), slot.getStorageIndex());

            case BELT_STORAGE ->
                    ItemInventory.getItem(equipment.getItem(3), slot.getStorageIndex());

            case BACKPACK_STORAGE ->
                    ItemInventory.getItem(equipment.getItem(1), slot.getStorageIndex());
        };
    }

    private static void setStack(
            InventorySlot slot,
            EquipmentInventory equipment,
            Player player,
            ItemStack stack
    ) {
        switch (slot.getType()) {
            case EQUIPMENT, CLOTHS ->
                    equipment.setItem(slot.getIndex(), stack);

            case HOTBAR ->
                    player.getInventory().items.set(
                            slot.getVanillaIndex(),
                            stack == null || stack.isEmpty() ? ItemStack.EMPTY : stack.copy()
                    );

            case JACKET_STORAGE ->
                    ItemInventory.setItem(equipment.getItem(5), slot.getStorageIndex(), stack);

            case PANTS_STORAGE ->
                    ItemInventory.setItem(equipment.getItem(6), slot.getStorageIndex(), stack);

            case VEST_STORAGE ->
                    ItemInventory.setItem(equipment.getItem(2), slot.getStorageIndex(), stack);

            case BELT_STORAGE ->
                    ItemInventory.setItem(equipment.getItem(3), slot.getStorageIndex(), stack);

            case BACKPACK_STORAGE ->
                    ItemInventory.setItem(equipment.getItem(1), slot.getStorageIndex(), stack);
        }

        if (player instanceof ServerPlayer serverPlayer) {
            VestAmmoSync.refreshFromVest(serverPlayer, equipment);
        }
    }
}