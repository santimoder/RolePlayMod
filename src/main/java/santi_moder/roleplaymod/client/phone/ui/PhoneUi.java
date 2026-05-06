package santi_moder.roleplaymod.client.phone.ui;

import net.minecraft.client.gui.GuiGraphics;
import santi_moder.roleplaymod.client.screen.PhoneScreen;

public final class PhoneUi {

    public static final int HEADER_TITLE_Y = 30;

    public static final int BACK_X = 12;
    public static final int BACK_Y = 26;
    public static final int BACK_W = 14;
    public static final int BACK_H = 14;

    public static final int PANEL_MARGIN_X = 10;
    public static final int PANEL_TOP_Y = 46;
    public static final int PANEL_BOTTOM_OFFSET = 34;

    public static final int CONTENT_LEFT_X = 18;
    public static final int CONTENT_TOP_Y = 58;

    public static final int ACTION_BUTTON_W = 114;
    public static final int ACTION_BUTTON_H = 16;

    public static final int LIST_BUTTON_H = 18;
    public static final int LIST_BUTTON_GAP = 6;

    public static final int COLOR_TEXT = 0xFFFFFFFF;
    public static final int COLOR_SUBTEXT = 0xFFCCCCCC;
    public static final int COLOR_HINT = 0xFFB5B5B5;
    public static final int COLOR_ERROR = 0xFFFF8080;

    public static final int COLOR_PANEL = 0x33111111;
    public static final int COLOR_PANEL_DARK = 0xCC111111;

    public static final int COLOR_BUTTON = 0xFF2C2C3A;
    public static final int COLOR_BUTTON_HOVER = 0xFF404050;

    public static final int COLOR_INPUT = 0xFF2C2C3A;

    private PhoneUi() {
    }

    public static void drawHeaderTitle(PhoneScreen screen, GuiGraphics guiGraphics, String title) {
        drawCenteredText(
                screen,
                guiGraphics,
                title,
                screen.getPhoneCenterX(),
                screen.getPhoneY() + HEADER_TITLE_Y,
                PhoneThemeColors.text(screen.getPhoneStack())
        );
    }

    public static void drawBackButton(PhoneScreen screen, GuiGraphics guiGraphics, int mouseX, int mouseY) {
        int x = screen.getPhoneX() + BACK_X;
        int y = screen.getPhoneY() + BACK_Y;
        boolean hover = screen.isInside(mouseX, mouseY, x, y, BACK_W, BACK_H);

        drawFilledButton(screen, guiGraphics, x, y, BACK_W, BACK_H, hover, "<");
    }

    public static boolean isBackButtonClicked(PhoneScreen screen, double mouseX, double mouseY) {
        int x = screen.getPhoneX() + BACK_X;
        int y = screen.getPhoneY() + BACK_Y;
        return screen.isInside(mouseX, mouseY, x, y, BACK_W, BACK_H);
    }

    public static void drawPanel(PhoneScreen screen, GuiGraphics guiGraphics) {
        int x1 = screen.getPhoneX() + PANEL_MARGIN_X;
        int y1 = screen.getPhoneY() + PANEL_TOP_Y;
        int x2 = screen.getPhoneX() + screen.getPhoneWidth() - PANEL_MARGIN_X;
        int y2 = screen.getPhoneY() + screen.getPhoneHeight() - PANEL_BOTTOM_OFFSET;

        guiGraphics.fill(x1, y1, x2, y2, PhoneThemeColors.card(screen.getPhoneStack()));
    }

    public static void drawDarkPanel(PhoneScreen screen, GuiGraphics guiGraphics) {
        int x1 = screen.getPhoneX() + 8;
        int y1 = screen.getPhoneY() + 46;
        int x2 = screen.getPhoneX() + screen.getPhoneWidth() - 8;
        int y2 = screen.getPhoneY() + screen.getPhoneHeight() - 34;

        guiGraphics.fill(x1, y1, x2, y2, PhoneThemeColors.card(screen.getPhoneStack()));
    }

    public static void drawField(PhoneScreen screen, GuiGraphics guiGraphics, String label, String value, int x, int y) {
        guiGraphics.drawString(screen.getPhoneFont(), label, x, y, PhoneThemeColors.text(screen.getPhoneStack()), false);
        guiGraphics.drawString(screen.getPhoneFont(), value, x, y + 12, PhoneThemeColors.subtext(screen.getPhoneStack()), false);
    }

    public static void drawTextInput(PhoneScreen screen, GuiGraphics guiGraphics, int x1, int y1, int x2, int y2, String value) {
        guiGraphics.fill(x1, y1, x2, y2, PhoneThemeColors.input(screen.getPhoneStack()));
        guiGraphics.drawString(
                screen.getPhoneFont(),
                value == null || value.isEmpty() ? "(vacio)" : value,
                x1 + 4,
                y1 + 4,
                PhoneThemeColors.text(screen.getPhoneStack()),
                false
        );
    }

    public static void drawActionButton(
            PhoneScreen screen,
            GuiGraphics guiGraphics,
            int mouseX,
            int mouseY,
            int x,
            int y,
            String label
    ) {
        boolean hover = screen.isInside(mouseX, mouseY, x, y, ACTION_BUTTON_W, ACTION_BUTTON_H);
        drawFilledButton(screen, guiGraphics, x, y, ACTION_BUTTON_W, ACTION_BUTTON_H, hover, label);
    }

    public static void drawListButton(
            PhoneScreen screen,
            GuiGraphics guiGraphics,
            int mouseX,
            int mouseY,
            int x,
            int y,
            int w,
            String label
    ) {
        boolean hover = screen.isInside(mouseX, mouseY, x, y, w, LIST_BUTTON_H);

        guiGraphics.fill(
                x,
                y,
                x + w,
                y + LIST_BUTTON_H,
                hover ? PhoneThemeColors.cardHover(screen.getPhoneStack()) : PhoneThemeColors.card(screen.getPhoneStack())
        );

        guiGraphics.drawString(screen.getPhoneFont(), label, x + 8, y + 5, PhoneThemeColors.text(screen.getPhoneStack()), false);
    }

    public static void drawFilledButton(
            PhoneScreen screen,
            GuiGraphics guiGraphics,
            int x,
            int y,
            int w,
            int h,
            boolean hover,
            String label
    ) {
        guiGraphics.fill(
                x,
                y,
                x + w,
                y + h,
                hover ? PhoneThemeColors.cardHover(screen.getPhoneStack()) : PhoneThemeColors.card(screen.getPhoneStack())
        );

        drawCenteredText(
                screen,
                guiGraphics,
                label,
                x + w / 2,
                y + 4,
                PhoneThemeColors.text(screen.getPhoneStack())
        );
    }

    public static void drawCenteredLines(
            PhoneScreen screen,
            GuiGraphics guiGraphics,
            int startY,
            int lineGap,
            String... lines
    ) {
        int y = startY;
        for (int i = 0; i < lines.length; i++) {
            int color = i == 0
                    ? PhoneThemeColors.text(screen.getPhoneStack())
                    : PhoneThemeColors.subtext(screen.getPhoneStack());

            drawCenteredText(screen, guiGraphics, lines[i], screen.getPhoneCenterX(), y, color);
            y += lineGap;
        }
    }

    public static String valueOrEmpty(String value) {
        return value == null || value.isEmpty() ? "(vacio)" : value;
    }

    public static String buildDots(String value, int length) {
        StringBuilder builder = new StringBuilder();

        for (int i = 0; i < length; i++) {
            builder.append(i < value.length() ? "●" : "○");
            if (i < length - 1) {
                builder.append(" ");
            }
        }

        return builder.toString();
    }

    public static void drawCenteredText(
            PhoneScreen screen,
            GuiGraphics guiGraphics,
            String text,
            int centerX,
            int y,
            int color
    ) {
        int x = centerX - screen.getPhoneFont().width(text) / 2;

        guiGraphics.drawString(
                screen.getPhoneFont(),
                text,
                x,
                y,
                color,
                false
        );
    }

}