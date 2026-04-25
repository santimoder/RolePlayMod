package santi_moder.roleplaymod.client.phone.ui.component;

import net.minecraft.client.gui.GuiGraphics;
import santi_moder.roleplaymod.client.phone.ui.PhoneUi;
import santi_moder.roleplaymod.client.screen.PhoneScreen;

public final class PhoneTextKeyboard {

    public static final String KEY_SPACE = "ESP";
    public static final String KEY_BACKSPACE = "BOR";
    public static final String KEY_OK = "OK";
    public static final String KEY_CLEAR = "CLR";

    private static final String[] ROW_1 = {"Q","W","E","R","T","Y","U","I","O","P"};
    private static final String[] ROW_2 = {"A","S","D","F","G","H","J","K","L"};
    private static final String[] ROW_3 = {"Z","X","C","V","B","N","M"};

    private final int keyWidth;
    private final int keyHeight;
    private final int gap;

    private final int row1X;
    private final int row1Y;

    private final int row2X;
    private final int row2Y;

    private final int row3X;
    private final int row3Y;

    private final int controlsY;

    public PhoneTextKeyboard(
            int keyWidth,
            int keyHeight,
            int gap,
            int row1X,
            int row1Y,
            int row2X,
            int row2Y,
            int row3X,
            int row3Y,
            int controlsY
    ) {
        this.keyWidth = keyWidth;
        this.keyHeight = keyHeight;
        this.gap = gap;
        this.row1X = row1X;
        this.row1Y = row1Y;
        this.row2X = row2X;
        this.row2Y = row2Y;
        this.row3X = row3X;
        this.row3Y = row3Y;
        this.controlsY = controlsY;
    }

    public static PhoneTextKeyboard defaultKeyboard(PhoneScreen screen) {
        int keyW = 11;
        int keyH = 14;
        int gap = 2;

        int row1Y = screen.getPhoneY() + 102;
        int row2Y = row1Y + keyH + 4;
        int row3Y = row2Y + keyH + 4;
        int controlsY = row3Y + keyH + 6;

        return new PhoneTextKeyboard(
                keyW,
                keyH,
                gap,
                screen.getPhoneX() + 11,
                row1Y,
                screen.getPhoneX() + 17,
                row2Y,
                screen.getPhoneX() + 29,
                row3Y,
                controlsY
        );
    }

    public void render(PhoneScreen screen, GuiGraphics guiGraphics, int mouseX, int mouseY) {
        drawRow(screen, guiGraphics, mouseX, mouseY, row1X, row1Y, ROW_1);
        drawRow(screen, guiGraphics, mouseX, mouseY, row2X, row2Y, ROW_2);
        drawRow(screen, guiGraphics, mouseX, mouseY, row3X, row3Y, ROW_3);

        drawSpecialKey(screen, guiGraphics, mouseX, mouseY, screen.getPhoneX() + 14, controlsY, 26, keyHeight, KEY_SPACE);
        drawSpecialKey(screen, guiGraphics, mouseX, mouseY, screen.getPhoneX() + 44, controlsY, 26, keyHeight, KEY_BACKSPACE);
        drawSpecialKey(screen, guiGraphics, mouseX, mouseY, screen.getPhoneX() + 74, controlsY, 26, keyHeight, KEY_OK);
        drawSpecialKey(screen, guiGraphics, mouseX, mouseY, screen.getPhoneX() + 104, controlsY, 26, keyHeight, KEY_CLEAR);
    }

    public String getKeyAt(PhoneScreen screen, double mouseX, double mouseY) {
        String key = findInRow(screen, mouseX, mouseY, row1X, row1Y, ROW_1);
        if (key != null) return key;

        key = findInRow(screen, mouseX, mouseY, row2X, row2Y, ROW_2);
        if (key != null) return key;

        key = findInRow(screen, mouseX, mouseY, row3X, row3Y, ROW_3);
        if (key != null) return key;

        if (screen.isInside(mouseX, mouseY, screen.getPhoneX() + 14, controlsY, 26, keyHeight)) return KEY_SPACE;
        if (screen.isInside(mouseX, mouseY, screen.getPhoneX() + 44, controlsY, 26, keyHeight)) return KEY_BACKSPACE;
        if (screen.isInside(mouseX, mouseY, screen.getPhoneX() + 74, controlsY, 26, keyHeight)) return KEY_OK;
        if (screen.isInside(mouseX, mouseY, screen.getPhoneX() + 104, controlsY, 26, keyHeight)) return KEY_CLEAR;

        return null;
    }

    private void drawRow(
            PhoneScreen screen,
            GuiGraphics guiGraphics,
            int mouseX,
            int mouseY,
            int startX,
            int y,
            String[] row
    ) {
        for (int i = 0; i < row.length; i++) {
            int x = startX + i * (keyWidth + gap);
            boolean hover = screen.isInside(mouseX, mouseY, x, y, keyWidth, keyHeight);
            PhoneUi.drawFilledButton(screen, guiGraphics, x, y, keyWidth, keyHeight, hover, row[i]);
        }
    }

    private void drawSpecialKey(
            PhoneScreen screen,
            GuiGraphics guiGraphics,
            int mouseX,
            int mouseY,
            int x,
            int y,
            int w,
            int h,
            String label
    ) {
        boolean hover = screen.isInside(mouseX, mouseY, x, y, w, h);
        PhoneUi.drawFilledButton(screen, guiGraphics, x, y, w, h, hover, label);
    }

    private String findInRow(
            PhoneScreen screen,
            double mouseX,
            double mouseY,
            int startX,
            int y,
            String[] row
    ) {
        for (int i = 0; i < row.length; i++) {
            int x = startX + i * (keyWidth + gap);
            if (screen.isInside(mouseX, mouseY, x, y, keyWidth, keyHeight)) {
                return row[i];
            }
        }
        return null;
    }
}