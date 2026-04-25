package santi_moder.roleplaymod.common.radio;

import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import santi_moder.roleplaymod.common.inventory.EquipmentInventory;
import santi_moder.roleplaymod.common.inventory.item.ItemInventory;
import santi_moder.roleplaymod.item.RadioItem;

public class RadioLocator {

    private static final int VEST_EQUIPMENT_SLOT = 2;
    private static final int BELT_EQUIPMENT_SLOT = 3;
    private static final int BELT_VISIBLE_SLOTS = 4;

    public static LocatedRadio findUsableRadio(Player player) {
        LocatedRadio mainHand = findInMainHand(player);
        if (mainHand != null) return mainHand;

        LocatedRadio offHand = findInOffHand(player);
        if (offHand != null) return offHand;

        LocatedRadio belt = findInBelt(player);
        if (belt != null) return belt;

        LocatedRadio vest = findInVestSlot0(player);
        if (vest != null) return vest;

        return null;
    }

    public static LocatedRadio findInMainHand(Player player) {
        if (isRadio(player.getMainHandItem())) {
            return new LocatedRadio(
                    RadioLocationType.MAIN_HAND,
                    player.getInventory().selected,
                    new RadioStackAccess() {
                        @Override
                        public ItemStack get() {
                            return player.getMainHandItem();
                        }

                        @Override
                        public void set(ItemStack stack) {
                            player.getInventory().setItem(player.getInventory().selected, stack);
                            player.getInventory().setChanged();
                        }
                    }
            );
        }

        return null;
    }

    public static LocatedRadio findInOffHand(Player player) {
        if (isRadio(player.getOffhandItem())) {
            return new LocatedRadio(
                    RadioLocationType.OFF_HAND,
                    0,
                    new RadioStackAccess() {
                        @Override
                        public ItemStack get() {
                            return player.getOffhandItem();
                        }

                        @Override
                        public void set(ItemStack stack) {
                            player.getInventory().offhand.set(0, stack);
                            player.getInventory().setChanged();
                        }
                    }
            );
        }

        return null;
    }

    public static LocatedRadio findInBelt(Player player) {
        EquipmentInventory equipment = RadioEquipmentResolver.getEquipment(player);
        if (equipment == null || !equipment.hasBelt()) return null;

        ItemStack beltStack = equipment.getItem(BELT_EQUIPMENT_SLOT);
        if (beltStack.isEmpty()) return null;

        int size = Math.min(ItemInventory.getSize(beltStack), BELT_VISIBLE_SLOTS);

        for (int i = 0; i < size; i++) {
            final int slot = i;
            ItemStack inside = ItemInventory.getItem(beltStack, slot);

            if (isRadio(inside)) {
                return new LocatedRadio(
                        RadioLocationType.BELT,
                        slot,
                        new RadioStackAccess() {
                            @Override
                            public ItemStack get() {
                                return ItemInventory.getItem(beltStack, slot);
                            }

                            @Override
                            public void set(ItemStack updated) {
                                ItemInventory.setItem(beltStack, slot, updated);
                                equipment.setItem(BELT_EQUIPMENT_SLOT, beltStack);
                            }
                        }
                );
            }
        }

        return null;
    }

    public static LocatedRadio findInVestSlot0(Player player) {
        EquipmentInventory equipment = RadioEquipmentResolver.getEquipment(player);
        if (equipment == null || !equipment.hasVest()) return null;

        ItemStack vestStack = equipment.getItem(VEST_EQUIPMENT_SLOT);
        if (vestStack.isEmpty()) return null;
        if (ItemInventory.getSize(vestStack) <= 0) return null;

        ItemStack slot0 = ItemInventory.getItem(vestStack, 0);
        if (!isRadio(slot0)) return null;

        return new LocatedRadio(
                RadioLocationType.VEST_SLOT_0,
                0,
                new RadioStackAccess() {
                    @Override
                    public ItemStack get() {
                        return ItemInventory.getItem(vestStack, 0);
                    }

                    @Override
                    public void set(ItemStack updated) {
                        ItemInventory.setItem(vestStack, 0, updated);
                        equipment.setItem(VEST_EQUIPMENT_SLOT, vestStack);
                    }
                }
        );
    }

    public static boolean isRadio(ItemStack stack) {
        return !stack.isEmpty() && stack.getItem() instanceof RadioItem;
    }
}