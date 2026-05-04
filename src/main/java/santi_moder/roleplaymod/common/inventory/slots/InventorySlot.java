package santi_moder.roleplaymod.common.inventory.slots;

public record InventorySlot(int x, int y, int index, SlotType type, int storageIndex, int vanillaIndex) {

    public InventorySlot(int x, int y, int index, SlotType type) {
        this(x, y, index, type, -1, -1);
    }

    public InventorySlot(int x, int y, int index, SlotType type, int vanillaIndex) {
        this(x, y, index, type, -1, vanillaIndex);
    }

}