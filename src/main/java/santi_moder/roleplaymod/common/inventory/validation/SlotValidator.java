package santi_moder.roleplaymod.common.inventory.validation;

import net.minecraft.world.item.ItemStack;
import santi_moder.roleplaymod.common.inventory.item.ItemInventory;
import santi_moder.roleplaymod.common.inventory.rules.ContainerRules;
import santi_moder.roleplaymod.common.inventory.rules.ContainerType;
import santi_moder.roleplaymod.common.inventory.rules.InventoryView;
import santi_moder.roleplaymod.common.inventory.rules.SimpleInventoryView;
import santi_moder.roleplaymod.common.inventory.slots.SlotType;
import santi_moder.roleplaymod.item.*;

public class SlotValidator {

    public static boolean isValid(ItemStack stack, SlotType type, int slotIndex, ItemStack containerStack) {
        if (stack.isEmpty()) return true;

        switch (type) {
            case EQUIPMENT:
                switch (slotIndex) {
                    case 0:
                        return false; // máscara aún no implementada
                    case 1:
                        return stack.getItem() instanceof ItemBackpackSmall
                                || stack.getItem() instanceof ItemBackpackMedium
                                || stack.getItem() instanceof ItemBackpackLarge
                                || stack.getItem() instanceof ItemBackpackHuge;
                    case 2:
                        return stack.getItem() instanceof ItemVest;
                    case 3:
                        return stack.getItem() instanceof ItemBelt;
                    default:
                        return false;
                }

            case CLOTHS:
                switch (slotIndex) {
                    case 4:
                        return false; // sombrero aún no implementado
                    case 5:
                        return stack.getItem() instanceof ItemJacket;
                    case 6:
                        return stack.getItem() instanceof ItemPants;
                    case 7:
                        return false; // zapatos aún no implementados
                    default:
                        return false;
                }

            case BELT_STORAGE:
                return validateContainer(ContainerType.BELT, slotIndex, stack, containerStack);

            case VEST_STORAGE:
                return validateContainer(ContainerType.VEST, slotIndex, stack, containerStack);

            case JACKET_STORAGE:
                return validateContainer(ContainerType.JACKET, slotIndex, stack, containerStack);

            case PANTS_STORAGE:
                return validateContainer(ContainerType.PANTS, slotIndex, stack, containerStack);

            case BACKPACK_STORAGE:
                return validateBackpack(slotIndex, stack, containerStack);

            case HOTBAR:
                return true;

            default:
                return false;
        }
    }

    private static boolean validateContainer(ContainerType containerType, int slotIndex, ItemStack stack, ItemStack containerStack) {
        if (containerStack == null || containerStack.isEmpty()) {
            return false;
        }

        InventoryView view = new SimpleInventoryView(
                ItemInventory.getSize(containerStack),
                i -> ItemInventory.getItem(containerStack, i)
        );

        return ContainerRules.canInsert(containerType, slotIndex, stack, view);
    }

    private static boolean validateBackpack(int slotIndex, ItemStack stack, ItemStack containerStack) {
        if (containerStack == null || containerStack.isEmpty()) {
            return false;
        }

        ContainerType backpackType = resolveBackpackType(containerStack);
        if (backpackType == null) {
            return false;
        }

        InventoryView view = new SimpleInventoryView(
                ItemInventory.getSize(containerStack),
                i -> ItemInventory.getItem(containerStack, i)
        );

        return ContainerRules.canInsert(backpackType, slotIndex, stack, view);
    }

    private static ContainerType resolveBackpackType(ItemStack containerStack) {
        if (containerStack.getItem() instanceof ItemBackpackSmall) {
            return ContainerType.BACKPACK_SMALL;
        }
        if (containerStack.getItem() instanceof ItemBackpackMedium) {
            return ContainerType.BACKPACK_MEDIUM;
        }
        if (containerStack.getItem() instanceof ItemBackpackLarge) {
            return ContainerType.BACKPACK_LARGE;
        }
        if (containerStack.getItem() instanceof ItemBackpackHuge) {
            return ContainerType.BACKPACK_HUGE;
        }

        return null;
    }
}