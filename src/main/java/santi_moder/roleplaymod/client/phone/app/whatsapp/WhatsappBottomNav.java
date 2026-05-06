package santi_moder.roleplaymod.client.phone.app.whatsapp;

import net.minecraft.client.gui.GuiGraphics;
import santi_moder.roleplaymod.client.phone.ui.PhoneThemeColors;
import santi_moder.roleplaymod.client.screen.PhoneScreen;

public final class WhatsappBottomNav {

    private static final int BAR_HEIGHT = 20;
    private static final int SIDE_PADDING = 6;
    private static final int BOTTOM_PADDING = 6;

    private static final int DEFAULT_TAB_HORIZONTAL_PADDING = 6;
    private static final int MIN_TAB_HORIZONTAL_PADDING = 3;
    private static final int TAB_GAP = 3;

    private static final float DEFAULT_LABEL_SCALE = 0.70F;
    private static final float MIN_LABEL_SCALE = 0.58F;

    public void render(PhoneScreen screen, GuiGraphics guiGraphics, int mouseX, int mouseY, WhatsappTab activeTab) {
        int barX = screen.getPhoneX() + SIDE_PADDING;
        int barY = screen.getPhoneY() + screen.getPhoneHeight() - BAR_HEIGHT - BOTTOM_PADDING;
        int barW = screen.getPhoneWidth() - SIDE_PADDING * 2;
        int barH = BAR_HEIGHT;

        guiGraphics.fill(barX, barY, barX + barW, barY + barH, PhoneThemeColors.sheet(screen.getPhoneStack()));

        TabLayoutResult result = buildLayouts(screen, barX, barY, barW, barH);

        for (TabLayout layout : result.layouts()) {
            boolean hover = screen.isInside(mouseX, mouseY, layout.x(), layout.y(), layout.width(), layout.height());
            boolean active = layout.tab() == activeTab;

            if (hover && !active) {
                guiGraphics.fill(
                        layout.x(),
                        layout.y(),
                        layout.x() + layout.width(),
                        layout.y() + layout.height(),
                        PhoneThemeColors.cardHover(screen.getPhoneStack())
                );
            }

            if (active) {
                guiGraphics.fill(
                        layout.x() + 2,
                        layout.y() + layout.height() - 3,
                        layout.x() + layout.width() - 2,
                        layout.y() + layout.height() - 1,
                        PhoneThemeColors.success(screen.getPhoneStack())
                );
            }

            drawScaledCenteredLabel(
                    guiGraphics,
                    screen,
                    layout.tab().getLabel(),
                    layout.x() + layout.width() / 2,
                    layout.y() + 6,
                    active ? PhoneThemeColors.text(screen.getPhoneStack()) : PhoneThemeColors.subtext(screen.getPhoneStack()),
                    result.labelScale()
            );
        }
    }

    public WhatsappTab getClickedTab(PhoneScreen screen, double mouseX, double mouseY) {
        int barX = screen.getPhoneX() + SIDE_PADDING;
        int barY = screen.getPhoneY() + screen.getPhoneHeight() - BAR_HEIGHT - BOTTOM_PADDING;
        int barW = screen.getPhoneWidth() - SIDE_PADDING * 2;
        int barH = BAR_HEIGHT;

        if (!screen.isInside(mouseX, mouseY, barX, barY, barW, barH)) {
            return null;
        }

        TabLayoutResult result = buildLayouts(screen, barX, barY, barW, barH);

        for (TabLayout layout : result.layouts()) {
            if (screen.isInside(mouseX, mouseY, layout.x(), layout.y(), layout.width(), layout.height())) {
                return layout.tab();
            }
        }

        return null;
    }

    private TabLayoutResult buildLayouts(PhoneScreen screen, int barX, int barY, int barW, int barH) {
        WhatsappTab[] tabs = WhatsappTab.values();

        float labelScale = DEFAULT_LABEL_SCALE;
        int horizontalPadding = DEFAULT_TAB_HORIZONTAL_PADDING;

        int[] widths = computeWidths(screen, tabs, labelScale, horizontalPadding);
        int totalWidth = computeTotalWidth(widths);

        while (totalWidth > barW && horizontalPadding > MIN_TAB_HORIZONTAL_PADDING) {
            horizontalPadding--;
            widths = computeWidths(screen, tabs, labelScale, horizontalPadding);
            totalWidth = computeTotalWidth(widths);
        }

        while (totalWidth > barW && labelScale > MIN_LABEL_SCALE) {
            labelScale -= 0.02F;
            widths = computeWidths(screen, tabs, labelScale, horizontalPadding);
            totalWidth = computeTotalWidth(widths);
        }

        if (totalWidth > barW) {
            int available = barW - TAB_GAP * (tabs.length - 1);
            int evenWidth = Math.max(12, available / tabs.length);

            widths = new int[tabs.length];
            for (int i = 0; i < tabs.length; i++) {
                widths[i] = evenWidth;
            }
            totalWidth = computeTotalWidth(widths);
        }

        int startX = barX + Math.max(0, (barW - totalWidth) / 2);
        int currentX = startX;

        TabLayout[] layouts = new TabLayout[tabs.length];
        for (int i = 0; i < tabs.length; i++) {
            int width = widths[i];
            if (currentX + width > barX + barW) {
                width = Math.max(8, (barX + barW) - currentX);
            }

            layouts[i] = new TabLayout(currentX, barY, width, barH, tabs[i]);
            currentX += width + TAB_GAP;
        }

        return new TabLayoutResult(layouts, labelScale);
    }

    private int[] computeWidths(PhoneScreen screen, WhatsappTab[] tabs, float labelScale, int horizontalPadding) {
        int[] widths = new int[tabs.length];

        for (int i = 0; i < tabs.length; i++) {
            int labelWidth = scaledTextWidth(screen, tabs[i].getLabel(), labelScale);
            widths[i] = labelWidth + horizontalPadding * 2;
        }

        return widths;
    }

    private int computeTotalWidth(int[] widths) {
        int total = 0;
        for (int width : widths) {
            total += width;
        }
        total += TAB_GAP * (widths.length - 1);
        return total;
    }

    private int scaledTextWidth(PhoneScreen screen, String text, float scale) {
        return Math.round(screen.getPhoneFont().width(text) * scale);
    }

    private void drawScaledCenteredLabel(
            GuiGraphics guiGraphics,
            PhoneScreen screen,
            String text,
            int centerX,
            int y,
            int color,
            float scale
    ) {
        guiGraphics.pose().pushPose();
        guiGraphics.pose().scale(scale, scale, 1.0F);

        float scaledCenterX = centerX / scale;
        float scaledY = y / scale;
        float textX = scaledCenterX - (screen.getPhoneFont().width(text) / 2.0F);

        guiGraphics.drawString(
                screen.getPhoneFont(),
                text,
                (int) textX,
                (int) scaledY,
                color,
                false
        );

        guiGraphics.pose().popPose();
    }

    private record TabLayout(int x, int y, int width, int height, WhatsappTab tab) {
    }

    private record TabLayoutResult(TabLayout[] layouts, float labelScale) {
    }
}