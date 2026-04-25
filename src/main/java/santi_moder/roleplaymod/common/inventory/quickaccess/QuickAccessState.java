package santi_moder.roleplaymod.common.inventory.quickaccess;

import net.minecraft.world.item.ItemStack;

public class QuickAccessState {

    private boolean active;
    private QuickAccessType type;
    private int sourceEquipmentSlot;
    private int sourceStorageSlot;
    private int targetHotbarSlot;
    private ItemStack originalStack = ItemStack.EMPTY;

    public boolean isActive() {
        return active;
    }

    public void activate(QuickAccessType type, int sourceEquipmentSlot, int sourceStorageSlot, int targetHotbarSlot, ItemStack originalStack) {
        this.active = true;
        this.type = type;
        this.sourceEquipmentSlot = sourceEquipmentSlot;
        this.sourceStorageSlot = sourceStorageSlot;
        this.targetHotbarSlot = targetHotbarSlot;
        this.originalStack = originalStack.copy();
    }

    public void clear() {
        this.active = false;
        this.type = null;
        this.sourceEquipmentSlot = -1;
        this.sourceStorageSlot = -1;
        this.targetHotbarSlot = -1;
        this.originalStack = ItemStack.EMPTY;
    }

    public QuickAccessType getType() {
        return type;
    }

    public int getSourceEquipmentSlot() {
        return sourceEquipmentSlot;
    }

    public int getSourceStorageSlot() {
        return sourceStorageSlot;
    }

    public int getTargetHotbarSlot() {
        return targetHotbarSlot;
    }

    public ItemStack getOriginalStack() {
        return originalStack;
    }
}