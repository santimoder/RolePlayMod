package santi_moder.roleplaymod.common.inventory.logic;

import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import santi_moder.roleplaymod.common.inventory.EquipmentInventory;
import santi_moder.roleplaymod.common.inventory.item.ItemInventory;
import santi_moder.roleplaymod.common.inventory.rules.ContainerRules;
import santi_moder.roleplaymod.common.inventory.rules.ContainerType;
import santi_moder.roleplaymod.common.inventory.security.InventorySlotSecurity;
import santi_moder.roleplaymod.common.inventory.slots.InventorySlot;
import santi_moder.roleplaymod.common.inventory.slots.SlotType;
import santi_moder.roleplaymod.common.inventory.validation.SlotValidator;
import santi_moder.roleplaymod.item.ItemBackpackHuge;
import santi_moder.roleplaymod.item.ItemBackpackLarge;
import santi_moder.roleplaymod.item.ItemBackpackMedium;
import santi_moder.roleplaymod.item.ItemBackpackSmall;

public final class InventoryInteraction {

    private InventoryInteraction() {
    }

    public static ItemStack clickSlot(
            InventorySlot slot,
            EquipmentInventory equipment,
            ItemStack carriedStack,
            Player player,
            int mouseButton
    ) {
        if (slot == null || equipment == null || player == null) {
            return safeCarried(carriedStack);
        }

        if (mouseButton != 0 && mouseButton != 1) {
            return safeCarried(carriedStack);
        }

        if (!InventorySlotSecurity.isSlotAccessible(slot, equipment, player)) {
            return safeCarried(carriedStack);
        }

        ItemStack carried = safeCarried(carriedStack);
        ItemStack slotStack = getStack(slot, equipment, player);

        if (!carried.isEmpty()) {
            ItemStack containerStack = getContainerStack(slot, equipment);
            int validationIndex = getValidationIndex(slot);

            if (!SlotValidator.isValid(carried, slot.getType(), validationIndex, containerStack)) {
                return carried;
            }
        }

        if (mouseButton == 0) {
            return handleLeftClick(slot, equipment, player, carried, slotStack);
        }

        return handleRightClick(slot, equipment, player, carried, slotStack);
    }

    private static ItemStack handleLeftClick(
            InventorySlot slot,
            EquipmentInventory equipment,
            Player player,
            ItemStack carried,
            ItemStack slotStack
    ) {
        if (carried.isEmpty()) {
            setStack(slot, equipment, player, ItemStack.EMPTY);
            return slotStack.copy();
        }

        if (slotStack.isEmpty()) {
            int maxStack = getMaxStackForSlot(slot, equipment, carried);

            if (maxStack <= 0) {
                return carried;
            }

            ItemStack toPlace = carried.copy();
            toPlace.setCount(Math.min(carried.getCount(), maxStack));
            setStack(slot, equipment, player, toPlace);

            ItemStack remaining = carried.copy();
            remaining.shrink(toPlace.getCount());

            return remaining.isEmpty() ? ItemStack.EMPTY : remaining;
        }

        if (ItemStack.isSameItemSameTags(slotStack, carried)) {
            int maxStack = getMaxStackForSlot(slot, equipment, carried);
            int space = maxStack - slotStack.getCount();

            if (space > 0) {
                int moveAmount = Math.min(space, carried.getCount());

                ItemStack newSlot = slotStack.copy();
                newSlot.grow(moveAmount);
                setStack(slot, equipment, player, newSlot);

                ItemStack remaining = carried.copy();
                remaining.shrink(moveAmount);

                return remaining.isEmpty() ? ItemStack.EMPTY : remaining;
            }
        }

        setStack(slot, equipment, player, carried.copy());
        return slotStack.copy();
    }

    private static ItemStack handleRightClick(
            InventorySlot slot,
            EquipmentInventory equipment,
            Player player,
            ItemStack carried,
            ItemStack slotStack
    ) {
        if (carried.isEmpty()) {
            if (slotStack.isEmpty()) {
                return ItemStack.EMPTY;
            }

            int half = (slotStack.getCount() + 1) / 2;

            ItemStack taken = slotStack.copy();
            taken.setCount(half);

            ItemStack remaining = slotStack.copy();
            remaining.shrink(half);

            setStack(slot, equipment, player, remaining.isEmpty() ? ItemStack.EMPTY : remaining);
            return taken;
        }

        if (slotStack.isEmpty()) {
            int maxStack = getMaxStackForSlot(slot, equipment, carried);

            if (maxStack <= 0) {
                return carried;
            }

            ItemStack one = carried.copy();
            one.setCount(1);
            setStack(slot, equipment, player, one);

            ItemStack newCarried = carried.copy();
            newCarried.shrink(1);

            return newCarried.isEmpty() ? ItemStack.EMPTY : newCarried;
        }

        if (ItemStack.isSameItemSameTags(slotStack, carried)) {
            int maxStack = getMaxStackForSlot(slot, equipment, carried);

            if (slotStack.getCount() < maxStack) {
                ItemStack newSlot = slotStack.copy();
                newSlot.grow(1);
                setStack(slot, equipment, player, newSlot);

                ItemStack newCarried = carried.copy();
                newCarried.shrink(1);

                return newCarried.isEmpty() ? ItemStack.EMPTY : newCarried;
            }
        }

        return carried;
    }

    private static ItemStack getStack(
            InventorySlot slot,
            EquipmentInventory equipment,
            Player player
    ) {
        if (!InventorySlotSecurity.isSlotAccessible(slot, equipment, player)) {
            return ItemStack.EMPTY;
        }

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
        if (!InventorySlotSecurity.isSlotAccessible(slot, equipment, player)) {
            return;
        }

        ItemStack safeStack = stack == null || stack.isEmpty() ? ItemStack.EMPTY : stack.copy();

        switch (slot.getType()) {
            case EQUIPMENT, CLOTHS ->
                    equipment.setItem(slot.getIndex(), safeStack);

            case HOTBAR ->
                    player.getInventory().items.set(slot.getVanillaIndex(), safeStack);

            case JACKET_STORAGE ->
                    ItemInventory.setItem(equipment.getItem(5), slot.getStorageIndex(), safeStack);

            case PANTS_STORAGE ->
                    ItemInventory.setItem(equipment.getItem(6), slot.getStorageIndex(), safeStack);

            case VEST_STORAGE ->
                    ItemInventory.setItem(equipment.getItem(2), slot.getStorageIndex(), safeStack);

            case BELT_STORAGE ->
                    ItemInventory.setItem(equipment.getItem(3), slot.getStorageIndex(), safeStack);

            case BACKPACK_STORAGE ->
                    ItemInventory.setItem(equipment.getItem(1), slot.getStorageIndex(), safeStack);
        }

        if (player instanceof ServerPlayer serverPlayer) {
            VestAmmoSync.refreshFromVest(serverPlayer, equipment);
        }
    }

    private static int getMaxStackForSlot(
            InventorySlot slot,
            EquipmentInventory equipment,
            ItemStack stack
    ) {
        if (stack == null || stack.isEmpty()) {
            return 0;
        }

        ItemStack containerStack = getContainerStack(slot, equipment);
        int vanillaMax = stack.getMaxStackSize();

        if (containerStack.isEmpty()) {
            return vanillaMax;
        }

        ContainerType containerType = resolveContainerType(slot, containerStack);

        if (containerType == null) {
            return vanillaMax;
        }

        int ruleMax = ContainerRules.getSlotMaxStackSize(
                containerType,
                getValidationIndex(slot),
                stack
        );

        return Math.max(0, Math.min(vanillaMax, ruleMax));
    }

    private static ContainerType resolveContainerType(InventorySlot slot, ItemStack containerStack) {
        if (slot == null || containerStack == null || containerStack.isEmpty()) {
            return null;
        }

        return switch (slot.getType()) {
            case BELT_STORAGE -> ContainerType.BELT;
            case VEST_STORAGE -> ContainerType.VEST;
            case PANTS_STORAGE -> ContainerType.PANTS;
            case JACKET_STORAGE -> ContainerType.JACKET;

            case BACKPACK_STORAGE -> {
                if (containerStack.getItem() instanceof ItemBackpackSmall) {
                    yield ContainerType.BACKPACK_SMALL;
                }
                if (containerStack.getItem() instanceof ItemBackpackMedium) {
                    yield ContainerType.BACKPACK_MEDIUM;
                }
                if (containerStack.getItem() instanceof ItemBackpackLarge) {
                    yield ContainerType.BACKPACK_LARGE;
                }
                if (containerStack.getItem() instanceof ItemBackpackHuge) {
                    yield ContainerType.BACKPACK_HUGE;
                }

                yield null;
            }

            default -> null;
        };
    }

    private static ItemStack getContainerStack(InventorySlot slot, EquipmentInventory equipment) {
        if (slot == null || equipment == null || slot.getType() == null) {
            return ItemStack.EMPTY;
        }

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

    private static ItemStack safeCarried(ItemStack stack) {
        return stack == null || stack.isEmpty() ? ItemStack.EMPTY : stack.copy();
    }
}