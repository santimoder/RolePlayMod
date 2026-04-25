package santi_moder.roleplaymod.common.inventory.slots;

import santi_moder.roleplaymod.common.inventory.EquipmentInventory;

import java.util.ArrayList;
import java.util.List;

public class RPInventorySlots {

    private static final int GUI_WIDTH = 176;
    private static final int GUI_HEIGHT = 166;

    private static final int SLOT_SIZE = 18;

    public static List<InventorySlot> generateSlots(int screenWidth, int screenHeight, EquipmentInventory equipment) {

        List<InventorySlot> slots = new ArrayList<>();

        int guiX = (screenWidth - GUI_WIDTH) / 2;
        int guiY = (screenHeight - GUI_HEIGHT) / 2;

        int index = 0;

        // =========================
        // EQUIPMENT SLOTS (LEFT)
        // =========================

        for (int i = 0; i < 4; i++) {

            int x = guiX + 7;
            int y = guiY + 17 + i * SLOT_SIZE;

            slots.add(new InventorySlot(
                    x,
                    y,
                    index++,
                    SlotType.EQUIPMENT
            ));
        }

        // =========================
        // CLOTHS SLOTS (RIGHT)
        // =========================

        for (int i = 0; i < 4; i++) {

            int x = guiX + 76;
            int y = guiY + 17 + i * SLOT_SIZE;

            slots.add(new InventorySlot(
                    x,
                    y,
                    index++,
                    SlotType.CLOTHS
            ));
        }

        // =========================
        // JACKET STORAGE
        // =========================

        if (equipment.hasJacket()) {

            for (int c = 0; c < 4; c++) {

                int x = guiX + 98 + c * SLOT_SIZE;
                int y = guiY + 20;

                slots.add(new InventorySlot(
                        x,
                        y,
                        index++,
                        SlotType.JACKET_STORAGE,
                        c,
                        -1
                ));
            }
        }

        // =========================
        // PANTS STORAGE
        // =========================

        if (equipment.hasPants()) {

            for (int c = 0; c < 4; c++) {

                int x = guiX + 98 + c * SLOT_SIZE;
                int y = guiY + 44;

                slots.add(new InventorySlot(
                        x,
                        y,
                        index++,
                        SlotType.PANTS_STORAGE,
                        c,
                        -1
                ));
            }
        }

        // =========================
        // VEST STORAGE
        // =========================

        if (equipment.hasVest()) {

            for (int c = 0; c < 4; c++) {

                int x = guiX + 98 + c * SLOT_SIZE;
                int y = guiY + 68;

                slots.add(new InventorySlot(
                        x,
                        y,
                        index++,
                        SlotType.VEST_STORAGE,
                        c,
                        -1
                ));
            }
        }

        // =========================
        // BELT STORAGE
        // =========================

        if (equipment.hasBelt()) {

            for (int c = 0; c < 4; c++) {

                int x = guiX + 14 + c * SLOT_SIZE;
                int y = guiY + 94;

                slots.add(new InventorySlot(
                        x,
                        y,
                        index++,
                        SlotType.BELT_STORAGE,
                        c,
                        -1
                ));
            }
        }

        // =========================
        // BACKPACK STORAGE
        // =========================

        int rows = getBackpackRows(equipment);

        int storageIndex = 0;

        for (int r = 0; r < rows; r++) {
            for (int c = 0; c < 4; c++) {

                int x = guiX + 98 + c * SLOT_SIZE;
                int y = guiY + 94 + r * SLOT_SIZE;

                slots.add(new InventorySlot(
                        x,
                        y,
                        index++,
                        SlotType.BACKPACK_STORAGE,
                        storageIndex++,
                        -1
                ));
            }
        }

        // HOTBAR (2 slots)
        for (int i = 0; i < 2; i++) {

            int x = guiX + 33 + i * SLOT_SIZE;
            int y = guiY + 148;

            slots.add(new InventorySlot(
                    x,
                    y,
                    index++,
                    SlotType.HOTBAR,
                    i
            ));
        }

        return slots;
    }

    public static int getBackpackRows(EquipmentInventory equipment) {

        if (!equipment.hasBackpack()) {
            return 0;
        }

        return equipment.getBackpackRows();
    }
}