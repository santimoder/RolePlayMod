package santi_moder.roleplaymod.client.screen;

import com.mojang.blaze3d.systems.RenderSystem;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.item.ItemStack;
import org.lwjgl.glfw.GLFW;
import santi_moder.roleplaymod.client.data.ClientInventoryData;
import santi_moder.roleplaymod.common.inventory.EquipmentInventory;
import santi_moder.roleplaymod.common.inventory.logic.GroundItemLogic;
import santi_moder.roleplaymod.common.inventory.slots.InventorySlot;
import santi_moder.roleplaymod.common.inventory.slots.RPInventorySlots;
import santi_moder.roleplaymod.network.*;

import java.util.ArrayList;
import java.util.List;

public class RPInventoryScreen extends Screen {

    private static final ResourceLocation GUI =
            new ResourceLocation("roleplaymod", "textures/gui/rp_inventory.png");
    private static final ResourceLocation INVENTORY_SLOTS =
            new ResourceLocation("roleplaymod", "textures/gui/inventory_slots.png");

    private static final int GUI_WIDTH = 176;
    private static final int GUI_HEIGHT = 166;

    private static final int SLOT_ADJUST_X = 0;
    private static final int SLOT_ADJUST_Y = -6;

    private static final int SLOT_SIZE = 18;
    private final List<ItemEntity> groundItems = new ArrayList<>();
    private final List<InventorySlot> slots = new ArrayList<>();
    private int lastMouseX;
    private int lastMouseY;
    private EquipmentInventory lastEquipmentState;
    private long lastGroundRefresh = 0L;

    public RPInventoryScreen() {
        super(Component.literal("RP Inventory"));
    }

    private EquipmentInventory getEquipment() {
        return santi_moder.roleplaymod.client.data.ClientInventoryData.getEquipment();
    }

    private boolean equipmentChanged(EquipmentInventory current) {

        if (lastEquipmentState == null) return true;

        for (int i = 0; i < current.getContainerSize(); i++) {

            if (!ItemStack.matches(
                    current.getItem(i),
                    lastEquipmentState.getItem(i)
            )) {
                return true;
            }
        }

        return false;
    }

    private void cacheEquipment(EquipmentInventory equipment) {

        lastEquipmentState = new EquipmentInventory();

        for (int i = 0; i < equipment.getContainerSize(); i++) {
            lastEquipmentState.setItem(i, equipment.getItem(i).copy());
        }
    }

    @Override
    protected void init() {

        super.init();

        groundItems.clear();
        Minecraft mc = Minecraft.getInstance();

        if (mc.player != null) {
            groundItems.addAll(
                    GroundItemLogic.scanGroundItems(mc.player)
            );
        }

        EquipmentInventory equipment = getEquipment();
        if (equipment == null) return;

        slots.clear();
        slots.addAll(
                RPInventorySlots.generateSlots(
                        this.width,
                        this.height,
                        equipment
                )
        );
        cacheEquipment(equipment);
    }

    @Override
    public void render(GuiGraphics graphics, int mouseX, int mouseY, float partialTicks) {
        this.lastMouseX = mouseX;
        this.lastMouseY = mouseY;

        this.renderBackground(graphics);

        EquipmentInventory equipment = getEquipment();

        if (equipment == null) {
            super.render(graphics, mouseX, mouseY, partialTicks);
            return;
        }

        if (equipmentChanged(equipment)) {
            refreshSlots();
            cacheEquipment(equipment);
        }

        int x = (this.width - GUI_WIDTH) / 2;
        int y = (this.height - GUI_HEIGHT) / 2;

        RenderSystem.setShaderTexture(0, GUI);
        graphics.blit(GUI, x, y, 0, 0, GUI_WIDTH, GUI_HEIGHT, GUI_WIDTH, GUI_HEIGHT);

        renderClothingSlots(graphics, x, y);
        renderSlots(graphics, mouseX, mouseY);

        if (this.minecraft != null && this.minecraft.player != null) {
            long now = System.currentTimeMillis();
            if (now - lastGroundRefresh >= 300) {
                groundItems.clear();
                groundItems.addAll(GroundItemLogic.scanGroundItems(this.minecraft.player));
                lastGroundRefresh = now;
            }
        }

        renderGroundItems(graphics, x + GUI_WIDTH + 10, y + 20);

        super.render(graphics, mouseX, mouseY, partialTicks);

        ItemStack carried = ClientInventoryData.getCarried();

        if (!carried.isEmpty()) {
            graphics.renderItem(carried, mouseX - 8, mouseY - 8);
            graphics.renderItemDecorations(this.font, carried, mouseX - 8, mouseY - 8);
        }
    }

    private void renderClothingSlots(GuiGraphics graphics, int guiX, int guiY) {

        EquipmentInventory equipment = getEquipment();
        if (equipment == null) return;

        RenderSystem.setShaderTexture(0, INVENTORY_SLOTS);

        // =========================
        // JACKET
        // =========================
        if (equipment.hasJacket()) {

            int x = guiX + 98 + SLOT_ADJUST_X;
            int y = guiY + 20 + SLOT_ADJUST_Y;

            graphics.blit(
                    INVENTORY_SLOTS,
                    x,
                    y,
                    0,
                    0,
                    72,
                    18,
                    72,
                    18
            );
        }

        // =========================
        // PANTS
        // =========================
        if (equipment.hasPants()) {

            int x = guiX + 98 + SLOT_ADJUST_X;
            int y = guiY + 44 + SLOT_ADJUST_Y;

            graphics.blit(
                    INVENTORY_SLOTS,
                    x,
                    y,
                    0,
                    0,
                    72,
                    18,
                    72,
                    18
            );
        }

        // =========================
        // VEST
        // =========================
        if (equipment.hasVest()) {

            int x = guiX + 98 + SLOT_ADJUST_X;
            int y = guiY + 68 + SLOT_ADJUST_Y;

            graphics.blit(
                    INVENTORY_SLOTS,
                    x,
                    y,
                    0,
                    0,
                    72,
                    18,
                    72,
                    18
            );
        }

        // =========================
        // BELT
        // =========================
        if (equipment.hasBelt()) {

            int x = guiX + 14 + SLOT_ADJUST_X;
            int y = guiY + 94 + SLOT_ADJUST_Y;

            graphics.blit(
                    INVENTORY_SLOTS,
                    x,
                    y,
                    0,
                    0,
                    72,
                    18,
                    72,
                    18
            );
        }

        // =========================
        // BACKPACK
        // =========================

        int rows = RPInventorySlots.getBackpackRows(equipment);

        for (int r = 0; r < rows; r++) {

            int x = guiX + 98 + SLOT_ADJUST_X;
            int y = guiY + 94 + r * 18 + SLOT_ADJUST_Y;

            graphics.blit(
                    INVENTORY_SLOTS,
                    x,
                    y,
                    0,
                    0,
                    72,
                    18,
                    72,
                    18
            );
        }
    }

    private void renderSlots(GuiGraphics graphics, int mouseX, int mouseY) {

        EquipmentInventory equipment = getEquipment();
        if (equipment == null) return;

        Minecraft mc = Minecraft.getInstance();

        for (InventorySlot slot : slots) {

            int x = slot.x() + SLOT_ADJUST_X;
            int y = slot.y() + SLOT_ADJUST_Y;

            boolean hovered =
                    mouseX >= x &&
                            mouseX <= x + SLOT_SIZE &&
                            mouseY >= y &&
                            mouseY <= y + SLOT_SIZE;

            if (hovered) {

                graphics.fill(
                        x + 1,
                        y + 1,
                        x + 17,
                        y + 17,
                        0x33000000
                );
            }

            ItemStack stack = ItemStack.EMPTY;

            switch (slot.type()) {

                case EQUIPMENT, CLOTHS -> {

                    stack = equipment.getItem(slot.index());
                }

                case HOTBAR -> {

                    int vanilla = slot.vanillaIndex();

                    if (vanilla >= 0 && vanilla < mc.player.getInventory().items.size()) {

                        stack = mc.player.getInventory().getItem(vanilla);
                    }
                }

                case JACKET_STORAGE -> {

                    stack = santi_moder.roleplaymod.common.inventory.item.ItemInventory
                            .getItem(equipment.getItem(5), slot.storageIndex());
                }

                case PANTS_STORAGE -> {

                    stack = santi_moder.roleplaymod.common.inventory.item.ItemInventory
                            .getItem(equipment.getItem(6), slot.storageIndex());
                }

                case VEST_STORAGE -> {

                    stack = santi_moder.roleplaymod.common.inventory.item.ItemInventory
                            .getItem(equipment.getItem(2), slot.storageIndex());
                }

                case BELT_STORAGE -> {

                    stack = santi_moder.roleplaymod.common.inventory.item.ItemInventory
                            .getItem(equipment.getItem(3), slot.storageIndex());
                }

                case BACKPACK_STORAGE -> {

                    stack = santi_moder.roleplaymod.common.inventory.item.ItemInventory
                            .getItem(equipment.getItem(1), slot.storageIndex());
                }
            }

            if (!stack.isEmpty()) {

                graphics.renderItem(stack, x, y);
                graphics.renderItemDecorations(mc.font, stack, x, y);

                if (hovered) {

                    graphics.renderTooltip(
                            mc.font,
                            stack,
                            mouseX,
                            mouseY
                    );
                }
            }
        }
    }

    public void refreshSlots() {
        EquipmentInventory equipment = getEquipment();
        if (equipment == null) return;
        slots.clear();
        slots.addAll(RPInventorySlots.generateSlots(this.width, this.height, equipment));
    }

    private void renderGroundItems(GuiGraphics graphics, int startX, int startY) {

        int slotSize = 18;

        for (int i = 0; i < groundItems.size(); i++) {

            ItemStack stack = groundItems.get(i).getItem();

            int x = startX;
            int y = startY + i * slotSize;

            graphics.renderItem(stack, x, y);
            graphics.renderItemDecorations(this.font, stack, x, y);
        }
    }

    private InventorySlot getHoveredSlot(double mouseX, double mouseY) {
        for (InventorySlot slot : slots) {
            int x = slot.x() + SLOT_ADJUST_X;
            int y = slot.y() + SLOT_ADJUST_Y;

            if (mouseX >= x && mouseX <= x + SLOT_SIZE
                    && mouseY >= y && mouseY <= y + SLOT_SIZE) {
                return slot;
            }
        }

        return null;
    }

    @Override
    public boolean keyPressed(int keyCode, int scanCode, int modifiers) {

        if (keyCode == GLFW.GLFW_KEY_Q) {
            boolean fullStack = Screen.hasControlDown();

            if (!ClientInventoryData.getCarried().isEmpty()) {
                ModNetwork.INVENTORY_CHANNEL.sendToServer(new DropCarriedPacket(fullStack));
                return true;
            }

            InventorySlot hovered = getHoveredSlot(lastMouseX, lastMouseY);

            if (hovered != null && hovered.type() != null) {
                ModNetwork.INVENTORY_CHANNEL.sendToServer(
                        new DropInventorySlotPacket(
                                hovered.index(),
                                hovered.type().ordinal(),
                                hovered.storageIndex(),
                                hovered.vanillaIndex(),
                                fullStack
                        )
                );
                return true;
            }
        }

        return super.keyPressed(keyCode, scanCode, modifiers);
    }

    @Override
    public boolean mouseClicked(double mouseX, double mouseY, int button) {

        Minecraft mc = Minecraft.getInstance();
        if (mc.player == null) return false;
        ItemStack carried = santi_moder.roleplaymod.client.data.ClientInventoryData.getCarried();

        // ---- CLICK EN INVENTARIO ----
        for (InventorySlot slot : slots) {

            int x = slot.x() + SLOT_ADJUST_X;
            int y = slot.y() + SLOT_ADJUST_Y;

            if (mouseX >= x && mouseX <= x + SLOT_SIZE &&
                    mouseY >= y && mouseY <= y + SLOT_SIZE) {

                if (slot.type() != null) {

                    if (Screen.hasShiftDown()) {
                        ModNetwork.INVENTORY_CHANNEL.sendToServer(
                                new QuickMoveInventorySlotPacket(
                                        slot.index(),
                                        slot.type().ordinal(),
                                        slot.storageIndex(),
                                        slot.vanillaIndex()
                                )
                        );
                        return true;
                    }

                    ModNetwork.INVENTORY_CHANNEL.sendToServer(
                            new InventoryClickPacket(
                                    slot.index(),
                                    slot.type().ordinal(),
                                    slot.storageIndex(),
                                    slot.vanillaIndex(),
                                    button
                            )
                    );
                }

                return true;
            }
        }

        // ---- CLICK EN ITEMS DEL SUELO ----
        int startX = (this.width - GUI_WIDTH) / 2 + GUI_WIDTH + 10;
        int startY = (this.height - GUI_HEIGHT) / 2 + 20;

        for (int i = 0; i < groundItems.size(); i++) {

            int x = startX;
            int y = startY + i * SLOT_SIZE;

            if (mouseX >= x && mouseX <= x + SLOT_SIZE &&
                    mouseY >= y && mouseY <= y + SLOT_SIZE) {

                ItemEntity entity = groundItems.get(i);

                if (entity != null && entity.isAlive()) {
                    if (Screen.hasShiftDown()) {
                        ModNetwork.INVENTORY_CHANNEL.sendToServer(
                                new QuickMoveGroundItemPacket(entity.getId())
                        );
                    } else {
                        ModNetwork.INVENTORY_CHANNEL.sendToServer(
                                new GroundItemClickPacket(entity.getId())
                        );
                    }
                }

                return true;
            }
        }
        // ---- CLICK FUERA DEL INVENTARIO ----
        if (!carried.isEmpty()) {
            ModNetwork.INVENTORY_CHANNEL.sendToServer(new DropCarriedPacket());
            return true;
        }

        return super.mouseClicked(mouseX, mouseY, button);
    }

    @Override
    public boolean isPauseScreen() {
        return false;
    }
}