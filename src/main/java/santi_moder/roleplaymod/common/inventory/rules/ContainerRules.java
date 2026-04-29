package santi_moder.roleplaymod.common.inventory.rules;

import net.minecraft.world.item.ItemStack;
import santi_moder.roleplaymod.common.inventory.item.StorageItem;

public class ContainerRules {

    public static boolean canInsert(ContainerType type, int slot, ItemStack stack, InventoryView inventory) {
        if (stack.isEmpty()) return true;

        return switch (type) {
            case BELT -> canInsertBelt(slot, stack, inventory);
            case VEST -> canInsertVest(slot, stack, inventory);
            case PANTS -> canInsertPants(slot, stack, inventory);
            case JACKET -> canInsertJacket(slot, stack, inventory);
            case BACKPACK_SMALL -> canInsertBackpackSmall(slot, stack, inventory);
            case BACKPACK_MEDIUM -> canInsertBackpackMedium(slot, stack, inventory);
            case BACKPACK_LARGE -> canInsertBackpackLarge(slot, stack, inventory);
            case BACKPACK_HUGE -> canInsertBackpackHuge(slot, stack, inventory);
        };
    }

    public static int getSlotMaxStackSize(ContainerType type, int slot, ItemStack stack) {
        return switch (type) {
            case VEST -> getVestMaxStackSize(slot, stack);
            default -> 64;
        };
    }

    public static float getMaxWeight(ContainerType type) {
        return switch (type) {
            case BELT -> 8.0f;
            case VEST -> 10.0f;
            case PANTS -> 6.0f;
            case JACKET -> 8.0f;
            case BACKPACK_SMALL -> 18.0f;
            case BACKPACK_MEDIUM -> 30.0f;
            case BACKPACK_LARGE -> 40.0f;
            case BACKPACK_HUGE -> 50.0f;
        };
    }

    private static boolean canInsertBelt(int slot, ItemStack stack, InventoryView inventory) {
        ItemCategory category = ItemMetadataResolver.getCategory(stack);

        boolean allowed = category == ItemCategory.PISTOL
                || category == ItemCategory.RADIO
                || category == ItemCategory.TASER
                || category == ItemCategory.BATON
                || category == ItemCategory.HANDCUFFS;

        if (!allowed) return false;

        return !hasDuplicateCategory(inventory, slot, category);
    }

    private static boolean canInsertVest(int slot, ItemStack stack, InventoryView inventory) {
        ItemCategory category = ItemMetadataResolver.getCategory(stack);

        if (slot == 0) {
            return category == ItemCategory.RADIO;
        }

        return category == ItemCategory.AMMO;
    }

    private static int getVestMaxStackSize(int slot, ItemStack stack) {
        if (slot == 0) return 1;

        ItemCategory category = ItemMetadataResolver.getCategory(stack);
        if (category == ItemCategory.AMMO) {
            return 30;
        }

        return 1;
    }

    private static boolean canInsertPants(int slot, ItemStack stack, InventoryView inventory) {
        return !isContainerItem(stack)
                && isSizeAllowed(ItemSize.SMALL, stack);
    }

    private static boolean canInsertJacket(int slot, ItemStack stack, InventoryView inventory) {
        return !isContainerItem(stack)
                && isSizeAllowed(ItemSize.SMALL, stack);
    }

    private static boolean canInsertBackpackSmall(int slot, ItemStack stack, InventoryView inventory) {
        if (isContainerItem(stack)) return false;

        ItemSize size = ItemMetadataResolver.getSize(stack);
        return size == ItemSize.SMALL || size == ItemSize.MEDIUM;
    }

    private static boolean canInsertBackpackMedium(int slot, ItemStack stack, InventoryView inventory) {
        if (isContainerItem(stack)) return false;

        ItemSize size = ItemMetadataResolver.getSize(stack);
        return size == ItemSize.SMALL || size == ItemSize.MEDIUM;
    }

    private static boolean canInsertBackpackLarge(int slot, ItemStack stack, InventoryView inventory) {
        if (isContainerItem(stack)) return false;

        ItemSize size = ItemMetadataResolver.getSize(stack);
        return size == ItemSize.SMALL || size == ItemSize.MEDIUM;
    }

    private static boolean canInsertBackpackHuge(int slot, ItemStack stack, InventoryView inventory) {
        return !isContainerItem(stack);
    }

    private static boolean isContainerItem(ItemStack stack) {
        return stack.getItem() instanceof StorageItem;
    }

    private static boolean isSizeAllowed(ItemSize maxAllowed, ItemStack stack) {
        ItemSize size = ItemMetadataResolver.getSize(stack);

        return switch (maxAllowed) {
            case SMALL -> size == ItemSize.SMALL;
            case MEDIUM -> size == ItemSize.SMALL || size == ItemSize.MEDIUM;
            case LARGE -> true;
        };
    }

    private static boolean hasDuplicateCategory(InventoryView inventory, int targetSlot, ItemCategory category) {
        for (int i = 0; i < inventory.getContainerSize(); i++) {
            if (i == targetSlot) continue;

            ItemStack other = inventory.getItem(i);
            if (other.isEmpty()) continue;

            ItemCategory otherCategory = ItemMetadataResolver.getCategory(other);
            if (otherCategory == category) {
                return true;
            }
        }

        return false;
    }
}