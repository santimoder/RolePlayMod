package santi_moder.roleplaymod.common.inventory.quickaccess;

import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.item.ItemStack;
import santi_moder.roleplaymod.common.inventory.EquipmentInventory;
import santi_moder.roleplaymod.common.inventory.item.ItemInventory;
import santi_moder.roleplaymod.common.inventory.logic.VestAmmoSync;
import santi_moder.roleplaymod.common.inventory.rules.ItemCategory;
import santi_moder.roleplaymod.common.inventory.rules.ItemMetadataResolver;

public class QuickAccessManager {

    private static final int PANTS_EQUIPMENT_SLOT = 6;
    private static final int PANTS_PISTOL_STORAGE_SLOT = 0;
    private static final int TARGET_HOTBAR_SLOT = 0; // tecla 1

    public static void togglePantsPistol(ServerPlayer player, EquipmentInventory equipment) {
        QuickAccessState state = QuickAccessHolder.get(player);

        if (state.isActive()) {
            returnPantsPistol(player, equipment, state);
            return;
        }

        ItemStack pants = equipment.getItem(PANTS_EQUIPMENT_SLOT);
        if (pants.isEmpty()) return;

        ItemStack pistol = ItemInventory.getItem(pants, PANTS_PISTOL_STORAGE_SLOT);
        if (pistol.isEmpty()) return;

        if (ItemMetadataResolver.getCategory(pistol) != ItemCategory.PISTOL) return;

        // el slot hotbar destino tiene que estar vacío
        ItemStack hotbarStack = player.getInventory().items.get(TARGET_HOTBAR_SLOT);
        if (!hotbarStack.isEmpty()) return;

        // sacar del pantalón y poner en hotbar
        ItemInventory.setItem(pants, PANTS_PISTOL_STORAGE_SLOT, ItemStack.EMPTY);
        player.getInventory().items.set(TARGET_HOTBAR_SLOT, pistol.copy());
        player.getInventory().selected = TARGET_HOTBAR_SLOT;

        state.activate(
                QuickAccessType.PANTS_PISTOL,
                PANTS_EQUIPMENT_SLOT,
                PANTS_PISTOL_STORAGE_SLOT,
                TARGET_HOTBAR_SLOT,
                pistol
        );

        player.getInventory().setChanged();
        player.containerMenu.broadcastChanges();
        VestAmmoSync.refreshFromVest(player, equipment);
    }

    public static void handleHotbarSelectionChange(ServerPlayer player, EquipmentInventory equipment, int newSelectedSlot) {
        QuickAccessState state = QuickAccessHolder.get(player);
        if (!state.isActive()) return;
        if (state.getType() != QuickAccessType.PANTS_PISTOL) return;

        if (newSelectedSlot != state.getTargetHotbarSlot()) {
            returnPantsPistol(player, equipment, state);
        }
    }

    public static void returnIfNeeded(ServerPlayer player, EquipmentInventory equipment) {
        QuickAccessState state = QuickAccessHolder.get(player);
        if (!state.isActive()) return;

        returnPantsPistol(player, equipment, state);
    }

    private static void returnPantsPistol(ServerPlayer player, EquipmentInventory equipment, QuickAccessState state) {
        ItemStack pants = equipment.getItem(state.getSourceEquipmentSlot());
        if (pants.isEmpty()) {
            return;
        }

        ItemStack hotbarStack = player.getInventory().items.get(state.getTargetHotbarSlot());
        if (hotbarStack.isEmpty()) {
            state.clear();
            return;
        }

        // seguridad: solo guardar si el slot original del pantalón sigue vacío
        ItemStack currentPantsSlot = ItemInventory.getItem(pants, state.getSourceStorageSlot());
        if (!currentPantsSlot.isEmpty()) {
            return;
        }

        // seguridad: solo guardar si lo que está en hotbar sigue siendo una pistola
        if (ItemMetadataResolver.getCategory(hotbarStack) != ItemCategory.PISTOL) {
            return;
        }

        ItemInventory.setItem(pants, state.getSourceStorageSlot(), hotbarStack.copy());
        player.getInventory().items.set(state.getTargetHotbarSlot(), ItemStack.EMPTY);

        state.clear();

        player.getInventory().setChanged();
        player.containerMenu.broadcastChanges();
        VestAmmoSync.refreshFromVest(player, equipment);
    }
}