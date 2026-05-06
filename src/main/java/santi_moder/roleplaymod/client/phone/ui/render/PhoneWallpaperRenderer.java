package santi_moder.roleplaymod.client.phone.ui.render;

import com.mojang.blaze3d.systems.RenderSystem;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;
import santi_moder.roleplaymod.client.phone.ui.render.PhoneWallpaperCatalog.WallpaperOption;
import santi_moder.roleplaymod.client.screen.PhoneScreen;
import santi_moder.roleplaymod.common.phone.PhoneData;

public final class PhoneWallpaperRenderer {

    private static final int WALLPAPER_OVERLAY_COLOR = 0x16000000;
    private static final int PREVIEW_FRAME_COLOR = 0xAA111111;
    private static final int PREVIEW_OVERLAY_COLOR = 0x12000000;
    private static final int PREVIEW_DOCK_COLOR = 0x66333344;
    private static final int PREVIEW_LOCK_TEXT = 0xFFFFFFFF;
    private static final int PREVIEW_LOCK_SUBTEXT = 0xFFDADADA;
    private static final int FALLBACK_COLOR = 0xFF2C3E50;

    private PhoneWallpaperRenderer() {
    }

    public static void render(PhoneScreen screen, GuiGraphics guiGraphics, ItemStack stack) {
        renderArea(
                guiGraphics,
                stack,
                screen.getContentX(),
                screen.getContentY(),
                screen.getContentWidth(),
                screen.getContentHeight(),
                true
        );
    }

    public static void renderPreviewHome(GuiGraphics guiGraphics, ItemStack stack, int x, int y, int width, int height) {
        renderArea(guiGraphics, stack, x, y, width, height, false);

        int dockMargin = 6;
        int dockHeight = 18;

        guiGraphics.fill(
                x + dockMargin,
                y + height - dockHeight - 6,
                x + width - dockMargin,
                y + height - 6,
                PREVIEW_DOCK_COLOR
        );

        renderPreviewAppGrid(guiGraphics, x, y, width, height);
    }

    public static void renderPreviewLock(GuiGraphics guiGraphics, ItemStack stack, int x, int y, int width, int height) {
        renderArea(guiGraphics, stack, x, y, width, height, false);

        int centerX = x + width / 2;

        guiGraphics.drawCenteredString(
                net.minecraft.client.Minecraft.getInstance().font,
                "12:45",
                centerX,
                y + 16,
                PREVIEW_LOCK_TEXT
        );

        guiGraphics.drawCenteredString(
                net.minecraft.client.Minecraft.getInstance().font,
                "26 Mar",
                centerX,
                y + 28,
                PREVIEW_LOCK_SUBTEXT
        );
    }

    private static void renderArea(
            GuiGraphics guiGraphics,
            ItemStack stack,
            int x,
            int y,
            int width,
            int height,
            boolean includeOverlay
    ) {
        WallpaperOption option = PhoneWallpaperCatalog.byId(PhoneData.getWallpaper(stack));
        ResourceLocation texture = option.texture();

        guiGraphics.fill(x, y, x + width, y + height, PREVIEW_FRAME_COLOR);

        if (texture != null) {
            RenderSystem.setShaderTexture(0, texture);
            guiGraphics.blit(texture, x, y, 0, 0, width, height, width, height);
        } else {
            guiGraphics.fill(x, y, x + width, y + height, FALLBACK_COLOR);
        }

        if (!includeOverlay) {
            guiGraphics.fill(x, y, x + width, y + height, PREVIEW_OVERLAY_COLOR);
        }
    }

    public static void renderPreviewWallpaperOnly(GuiGraphics guiGraphics, ItemStack stack, int x, int y, int width, int height) {
        renderArea(guiGraphics, stack, x, y, width, height, false);
    }

    private static void renderPreviewAppGrid(GuiGraphics guiGraphics, int x, int y, int width, int height) {
        int iconSize = 8;
        int gap = 5;

        int columns = 3;
        int rows = 3;

        int totalWidth = columns * iconSize + (columns - 1) * gap;
        int startX = x + (width - totalWidth) / 2;
        int startY = y + 14;

        for (int row = 0; row < rows; row++) {
            for (int col = 0; col < columns; col++) {
                int iconX = startX + col * (iconSize + gap);
                int iconY = startY + row * (iconSize + gap);

                guiGraphics.fill(iconX, iconY, iconX + iconSize, iconY + iconSize, 0xCCFFFFFF);
            }
        }
    }
}