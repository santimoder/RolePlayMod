package santi_moder.roleplaymod.common.inventory.logic;

import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.item.ItemStack;
import santi_moder.roleplaymod.common.inventory.EquipmentInventory;
import santi_moder.roleplaymod.common.inventory.item.ItemInventory;
import santi_moder.roleplaymod.common.inventory.rules.ItemCategory;
import santi_moder.roleplaymod.common.inventory.rules.ItemMetadataResolver;

public class VestAmmoSync {

    // hotbar visual 3,4,5
    private static final int[] HOTBAR_SLOTS = {2, 3, 4};

    // chaleco: slot 0 = radio, 1/2/3 = ammo
    private static final int[] VEST_AMMO_SLOTS = {1, 2, 3};

    // usar cuando editás el chaleco manualmente
    public static void refreshFromVest(ServerPlayer player, EquipmentInventory equipment) {
        ItemStack vest = equipment.getItem(2);

        clearHotbar(player);

        if (!vest.isEmpty()) {
            copyVestToHotbar(player, vest);
        }

        player.getInventory().setChanged();
        player.containerMenu.broadcastChanges();
    }

    // usar en tick para copiar el consumo del hotbar al chaleco
    public static void syncConsumption(ServerPlayer player, EquipmentInventory equipment) {
        ItemStack vest = equipment.getItem(2);

        if (vest.isEmpty()) {
            clearHotbar(player);
            player.getInventory().setChanged();
            player.containerMenu.broadcastChanges();
            return;
        }

        copyHotbarToVest(player, vest);

        player.getInventory().setChanged();
        player.containerMenu.broadcastChanges();
    }

    private static void clearHotbar(ServerPlayer player) {
        for (int hotbarSlot : HOTBAR_SLOTS) {
            player.getInventory().items.set(hotbarSlot, ItemStack.EMPTY);
        }
    }

    // MAPE0 FIJO:
    // vest 1 -> hotbar 2
    // vest 2 -> hotbar 3
    // vest 3 -> hotbar 4
    private static void copyVestToHotbar(ServerPlayer player, ItemStack vest) {
        for (int i = 0; i < VEST_AMMO_SLOTS.length; i++) {
            int vestSlot = VEST_AMMO_SLOTS[i];
            int hotbarSlot = HOTBAR_SLOTS[i];

            ItemStack vestStack = ItemInventory.getItem(vest, vestSlot);

            if (vestStack.isEmpty()) continue;
            if (ItemMetadataResolver.getCategory(vestStack) != ItemCategory.AMMO) continue;

            ItemStack copy = vestStack.copy();
            copy.setCount(Math.min(copy.getCount(), 30));

            player.getInventory().items.set(hotbarSlot, copy);
        }
    }

    // MAPE0 FIJO:
    // hotbar 2 -> vest 1
    // hotbar 3 -> vest 2
    // hotbar 4 -> vest 3
    private static void copyHotbarToVest(ServerPlayer player, ItemStack vest) {
        for (int i = 0; i < HOTBAR_SLOTS.length; i++) {
            int hotbarSlot = HOTBAR_SLOTS[i];
            int vestSlot = VEST_AMMO_SLOTS[i];

            ItemStack hotbarStack = player.getInventory().items.get(hotbarSlot);
            ItemStack vestStack = ItemInventory.getItem(vest, vestSlot);

            if (vestStack.isEmpty()) continue;
            if (ItemMetadataResolver.getCategory(vestStack) != ItemCategory.AMMO) continue;

            // si se consumió todo en hotbar, vaciar ese slot del chaleco
            if (hotbarStack.isEmpty()) {
                ItemInventory.setItem(vest, vestSlot, ItemStack.EMPTY);
                continue;
            }

            // mismo item base: copiar cantidad actual del hotbar al chaleco
            if (ItemStack.isSameItem(hotbarStack, vestStack)) {
                ItemStack newVestStack = vestStack.copy();
                newVestStack.setCount(Math.min(hotbarStack.getCount(), 30));
                ItemInventory.setItem(vest, vestSlot, newVestStack);
            }
        }
    }
}