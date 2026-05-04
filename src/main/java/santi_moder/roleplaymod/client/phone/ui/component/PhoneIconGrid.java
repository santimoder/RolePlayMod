package santi_moder.roleplaymod.client.phone.ui.component;

import com.mojang.blaze3d.systems.RenderSystem;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.resources.ResourceLocation;
import santi_moder.roleplaymod.client.screen.PhoneScreen;
import santi_moder.roleplaymod.common.phone.PhoneAppId;

import java.util.ArrayList;
import java.util.List;

public final class PhoneIconGrid {

    private static final int COLOR_ICON = 0xFF3B82F6;
    private static final int COLOR_ICON_HOVER = 0xFF60A5FA;
    private static final int COLOR_TEXT = 0xFFFFFFFF;

    private final int startX;
    private final int startY;
    private final int columns;
    private final int iconSize;
    private final int spacingX;
    private final int spacingY;

    private final List<IconEntry> entries = new ArrayList<>();

    public PhoneIconGrid(
            int startX,
            int startY,
            int columns,
            int iconSize,
            int spacingX,
            int spacingY
    ) {
        this.startX = startX;
        this.startY = startY;
        this.columns = columns;
        this.iconSize = iconSize;
        this.spacingX = spacingX;
        this.spacingY = spacingY;
    }

    public void setEntries(List<IconEntry> newEntries) {
        entries.clear();
        entries.addAll(newEntries);
    }

    public void render(PhoneScreen screen, GuiGraphics guiGraphics, int mouseX, int mouseY) {
        for (int i = 0; i < entries.size(); i++) {
            IconEntry entry = entries.get(i);

            int col = i % columns;
            int row = i / columns;

            int x = startX + col * (iconSize + spacingX);
            int y = startY + row * (iconSize + spacingY);

            boolean hover = screen.isInside(mouseX, mouseY, x, y, iconSize, iconSize);

            ResourceLocation icon = entry.icon();

            if (icon != null) {
                RenderSystem.enableBlend();

                guiGraphics.blit(
                        icon,
                        x,
                        y,
                        0,
                        0,
                        iconSize,
                        iconSize,
                        iconSize,
                        iconSize
                );
            } else {
                int color = hover ? COLOR_ICON_HOVER : COLOR_ICON;
                guiGraphics.fill(x, y, x + iconSize, y + iconSize, color);
            }

            guiGraphics.drawCenteredString(
                    screen.getPhoneFont(),
                    entry.label(),
                    x + iconSize / 2,
                    y + iconSize + 4,
                    COLOR_TEXT
            );
        }
    }

    public ClickedIcon getClickedIcon(PhoneScreen screen, double mouseX, double mouseY) {
        for (int i = 0; i < entries.size(); i++) {
            IconEntry entry = entries.get(i);

            int col = i % columns;
            int row = i / columns;

            int x = startX + col * (iconSize + spacingX);
            int y = startY + row * (iconSize + spacingY);

            if (screen.isInside(mouseX, mouseY, x, y, iconSize, iconSize)) {
                return new ClickedIcon(entry.appId(), entry.label(), x, y, iconSize, iconSize);
            }
        }

        return null;
    }

    public record IconEntry(PhoneAppId appId, String label) {

        public ResourceLocation icon() {
                return appId.getIcon();
            }
        }

    public record ClickedIcon(PhoneAppId appId, String label, int x, int y, int width, int height) {
    }
}