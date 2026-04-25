package santi_moder.roleplaymod.common.radio;

import net.minecraft.world.item.ItemStack;

public class LocatedRadio {

    private final RadioLocationType locationType;
    private final int slotIndex;
    private final RadioStackAccess access;

    public LocatedRadio(RadioLocationType locationType, int slotIndex, RadioStackAccess access) {
        this.locationType = locationType;
        this.slotIndex = slotIndex;
        this.access = access;
    }

    public RadioLocationType getLocationType() {
        return locationType;
    }

    public int getSlotIndex() {
        return slotIndex;
    }

    public ItemStack getStack() {
        return access.get();
    }

    public void save(ItemStack stack) {
        access.set(stack);
    }

    public boolean isValid() {
        ItemStack stack = getStack();
        return stack != null && !stack.isEmpty();
    }
}