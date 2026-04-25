package santi_moder.roleplaymod.common.inventory.slots;

public class InventorySlot {

    private final int x;
    private final int y;

    private final int index;

    private final SlotType type;

    private final int storageIndex;

    private final int vanillaIndex;

    public InventorySlot(int x, int y, int index, SlotType type) {
        this(x, y, index, type, -1, -1);
    }

    public InventorySlot(int x, int y, int index, SlotType type, int vanillaIndex) {
        this(x, y, index, type, -1, vanillaIndex);
    }

    public InventorySlot(int x, int y, int index, SlotType type, int storageIndex, int vanillaIndex) {

        this.x = x;
        this.y = y;
        this.index = index;
        this.type = type;
        this.storageIndex = storageIndex;
        this.vanillaIndex = vanillaIndex;
    }

    public int getX() { return x; }

    public int getY() { return y; }

    public int getIndex() { return index; }

    public SlotType getType() { return type; }

    public int getStorageIndex() { return storageIndex; }

    public int getVanillaIndex() { return vanillaIndex; }
}