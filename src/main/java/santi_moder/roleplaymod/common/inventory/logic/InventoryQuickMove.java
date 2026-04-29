package santi_moder.roleplaymod.common.inventory.logic;

import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import santi_moder.roleplaymod.common.inventory.EquipmentInventory;
import santi_moder.roleplaymod.common.inventory.item.ItemInventory;
import santi_moder.roleplaymod.common.inventory.rules.ItemCategory;
import santi_moder.roleplaymod.common.inventory.rules.ItemMetadataResolver;
import santi_moder.roleplaymod.common.inventory.security.InventorySlotSecurity;
import santi_moder.roleplaymod.common.inventory.slots.InventorySlot;
import santi_moder.roleplaymod.common.inventory.slots.RPInventorySlots;
import santi_moder.roleplaymod.common.inventory.slots.SlotType;
import santi_moder.roleplaymod.common.inventory.validation.SlotValidator;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

public final class InventoryQuickMove {

    private InventoryQuickMove() {
    }

    public static void moveFromSlot(InventorySlot source, EquipmentInventory equipment, Player player) {
        if (source == null || equipment == null || player == null) return;
        if (!InventorySlotSecurity.isSlotAccessible(source, equipment, player)) return;

        ItemStack sourceStack = getStack(source, equipment, player);
        if (sourceStack.isEmpty()) return;

        ItemStack remaining = sourceStack.copy();

        List<InventorySlot> targets = getPrioritizedTargets(remaining, source, equipment, player);

        for (InventorySlot target : targets) {
            remaining = tryInsert(target, equipment, player, remaining);
            if (remaining.isEmpty()) break;
        }

        setStack(source, equipment, player, remaining);

        if (player instanceof ServerPlayer serverPlayer) {
            VestAmmoSync.refreshFromVest(serverPlayer, equipment);
        }
    }

    public static ItemStack moveCarried(ItemStack carried, EquipmentInventory equipment, Player player) {
        if (carried == null || carried.isEmpty() || equipment == null || player == null) {
            return ItemStack.EMPTY;
        }

        ItemStack remaining = carried.copy();

        List<InventorySlot> targets = getPrioritizedTargets(remaining, null, equipment, player);

        for (InventorySlot target : targets) {
            remaining = tryInsert(target, equipment, player, remaining);
            if (remaining.isEmpty()) break;
        }

        if (player instanceof ServerPlayer serverPlayer) {
            VestAmmoSync.refreshFromVest(serverPlayer, equipment);
        }

        return remaining;
    }

    private static List<InventorySlot> getPrioritizedTargets(
            ItemStack stack,
            InventorySlot source,
            EquipmentInventory equipment,
            Player player
    ) {
        List<InventorySlot> allSlots = RPInventorySlots.generateSlots(176, 166, equipment);
        List<InventorySlot> validTargets = new ArrayList<>();

        for (InventorySlot target : allSlots) {
            if (source != null && isSameSlot(source, target)) continue;
            if (source != null && isSameContainer(source, target)) continue;
            if (source != null && wouldInsertItemIntoItself(source, target)) continue;
            if (!InventorySlotSecurity.isSlotAccessible(target, equipment, player)) continue;

            ItemStack containerStack = getContainerStack(target, equipment);
            int validationIndex = getValidationIndex(target);

            if (!SlotValidator.isValid(stack, target.getType(), validationIndex, containerStack)) {
                continue;
            }

            validTargets.add(target);
        }

        validTargets.sort(
                Comparator
                        .comparingInt((InventorySlot slot) -> getTargetPriority(stack, slot))
                        .thenComparingInt(slot -> getStack(slot, equipment, player).isEmpty() ? 1 : 0)
                        .thenComparingInt(InventorySlot::getStorageIndex)
                        .thenComparingInt(InventorySlot::getIndex)
        );

        return validTargets;
    }

    private static int getTargetPriority(ItemStack stack, InventorySlot target) {
        ItemCategory category = ItemMetadataResolver.getCategory(stack);

        if (category == ItemCategory.RADIO) {
            if (target.getType() == SlotType.VEST_STORAGE && target.getStorageIndex() == 0) return 0;
            if (target.getType() == SlotType.BELT_STORAGE) return 1;
            if (target.getType() == SlotType.BACKPACK_STORAGE) return 20;
            return 100;
        }

        if (category == ItemCategory.AMMO) {
            if (target.getType() == SlotType.VEST_STORAGE) return 0;
            if (target.getType() == SlotType.BACKPACK_STORAGE) return 10;
            return 100;
        }

        if (category == ItemCategory.PISTOL
                || category == ItemCategory.TASER
                || category == ItemCategory.HANDCUFFS
                || category == ItemCategory.BATON) {
            if (target.getType() == SlotType.BELT_STORAGE) return 0;
            if (target.getType() == SlotType.BACKPACK_STORAGE) return 10;
            return 100;
        }

        return switch (ItemMetadataResolver.getSize(stack)) {
            case SMALL -> {
                if (target.getType() == SlotType.PANTS_STORAGE) yield 0;
                if (target.getType() == SlotType.JACKET_STORAGE) yield 1;
                if (target.getType() == SlotType.BACKPACK_STORAGE) yield 10;
                yield 100;
            }

            case MEDIUM -> {
                if (target.getType() == SlotType.BACKPACK_STORAGE) yield 0;
                yield 100;
            }

            case LARGE -> {
                if (target.getType() == SlotType.BACKPACK_STORAGE) yield 0;
                yield 100;
            }
        };
    }

    private static ItemStack tryInsert(
            InventorySlot target,
            EquipmentInventory equipment,
            Player player,
            ItemStack stack
    ) {
        if (stack.isEmpty()) return ItemStack.EMPTY;

        ItemStack containerStack = getContainerStack(target, equipment);
        int validationIndex = getValidationIndex(target);

        if (!SlotValidator.isValid(stack, target.getType(), validationIndex, containerStack)) {
            return stack;
        }

        ItemStack targetStack = getStack(target, equipment, player);

        if (targetStack.isEmpty()) {
            setStack(target, equipment, player, stack.copy());
            return ItemStack.EMPTY;
        }

        if (!ItemStack.isSameItemSameTags(targetStack, stack)) {
            return stack;
        }

        int max = Math.min(targetStack.getMaxStackSize(), stack.getMaxStackSize());
        int space = max - targetStack.getCount();

        if (space <= 0) {
            return stack;
        }

        int move = Math.min(space, stack.getCount());

        ItemStack newTarget = targetStack.copy();
        newTarget.grow(move);
        setStack(target, equipment, player, newTarget);

        ItemStack remaining = stack.copy();
        remaining.shrink(move);

        return remaining.isEmpty() ? ItemStack.EMPTY : remaining;
    }

    private static boolean isSameSlot(InventorySlot a, InventorySlot b) {
        return a.getType() == b.getType()
                && a.getIndex() == b.getIndex()
                && a.getStorageIndex() == b.getStorageIndex()
                && a.getVanillaIndex() == b.getVanillaIndex();
    }

    private static boolean isSameContainer(InventorySlot source, InventorySlot target) {
        if (!isStorageSlot(source.getType()) || !isStorageSlot(target.getType())) {
            return false;
        }

        return source.getType() == target.getType();
    }

    private static boolean wouldInsertItemIntoItself(InventorySlot source, InventorySlot target) {
        if (source.getType() != SlotType.EQUIPMENT && source.getType() != SlotType.CLOTHS) {
            return false;
        }

        return switch (source.getIndex()) {
            case 1 -> target.getType() == SlotType.BACKPACK_STORAGE;
            case 2 -> target.getType() == SlotType.VEST_STORAGE;
            case 3 -> target.getType() == SlotType.BELT_STORAGE;
            case 5 -> target.getType() == SlotType.JACKET_STORAGE;
            case 6 -> target.getType() == SlotType.PANTS_STORAGE;
            default -> false;
        };
    }

    private static boolean isStorageSlot(SlotType type) {
        return type == SlotType.JACKET_STORAGE
                || type == SlotType.PANTS_STORAGE
                || type == SlotType.VEST_STORAGE
                || type == SlotType.BELT_STORAGE
                || type == SlotType.BACKPACK_STORAGE;
    }

    private static ItemStack getStack(InventorySlot slot, EquipmentInventory equipment, Player player) {
        return switch (slot.getType()) {
            case EQUIPMENT, CLOTHS -> equipment.getItem(slot.getIndex());
            case HOTBAR -> player.getInventory().items.get(slot.getVanillaIndex());
            case JACKET_STORAGE -> ItemInventory.getItem(equipment.getItem(5), slot.getStorageIndex());
            case PANTS_STORAGE -> ItemInventory.getItem(equipment.getItem(6), slot.getStorageIndex());
            case VEST_STORAGE -> ItemInventory.getItem(equipment.getItem(2), slot.getStorageIndex());
            case BELT_STORAGE -> ItemInventory.getItem(equipment.getItem(3), slot.getStorageIndex());
            case BACKPACK_STORAGE -> ItemInventory.getItem(equipment.getItem(1), slot.getStorageIndex());
        };
    }

    private static void setStack(InventorySlot slot, EquipmentInventory equipment, Player player, ItemStack stack) {
        ItemStack safe = stack == null || stack.isEmpty() ? ItemStack.EMPTY : stack.copy();

        switch (slot.getType()) {
            case EQUIPMENT, CLOTHS -> equipment.setItem(slot.getIndex(), safe);
            case HOTBAR -> player.getInventory().items.set(slot.getVanillaIndex(), safe);
            case JACKET_STORAGE -> ItemInventory.setItem(equipment.getItem(5), slot.getStorageIndex(), safe);
            case PANTS_STORAGE -> ItemInventory.setItem(equipment.getItem(6), slot.getStorageIndex(), safe);
            case VEST_STORAGE -> ItemInventory.setItem(equipment.getItem(2), slot.getStorageIndex(), safe);
            case BELT_STORAGE -> ItemInventory.setItem(equipment.getItem(3), slot.getStorageIndex(), safe);
            case BACKPACK_STORAGE -> ItemInventory.setItem(equipment.getItem(1), slot.getStorageIndex(), safe);
        }
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
            case JACKET_STORAGE, PANTS_STORAGE, VEST_STORAGE, BELT_STORAGE, BACKPACK_STORAGE -> slot.getStorageIndex();
            default -> slot.getIndex();
        };
    }
}