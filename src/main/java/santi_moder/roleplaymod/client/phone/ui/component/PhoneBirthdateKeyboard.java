package santi_moder.roleplaymod.client.phone.ui.component;

import net.minecraft.client.gui.GuiGraphics;
import santi_moder.roleplaymod.client.phone.ui.PhoneUi;
import santi_moder.roleplaymod.client.screen.PhoneScreen;

public final class PhoneBirthdateKeyboard {

    public static final String KEY_SLASH = "/";
    public static final String KEY_BACKSPACE = "BOR";
    public static final String KEY_OK = "OK";
    public static final String KEY_CLEAR = "CLR";

    private static final String[][] LAYOUT = {
            {"1", "2", "3"},
            {"4", "5", "6"},
            {"7", "8", "9"},
            {KEY_SLASH, "0", KEY_BACKSPACE}
    };

    private final int startX;
    private final int startY;
    private final int keySize;
    private final int gap;

    public PhoneBirthdateKeyboard(int startX, int startY, int keySize, int gap) {
        this.startX = startX;
        this.startY = startY;
        this.keySize = keySize;
        this.gap = gap;
    }

    public static PhoneBirthdateKeyboard defaultKeyboard(PhoneScreen screen) {
        return new PhoneBirthdateKeyboard(
                screen.getPhoneX() + 20,
                screen.getPhoneY() + 102,
                24,
                8
        );
    }

    public void render(PhoneScreen screen, GuiGraphics guiGraphics, int mouseX, int mouseY) {
        for (int row = 0; row < LAYOUT.length; row++) {
            for (int col = 0; col < LAYOUT[row].length; col++) {
                int x = startX + col * (keySize + gap);
                int y = startY + row * (keySize + gap);

                boolean hover = screen.isInside(mouseX, mouseY, x, y, keySize, keySize);
                PhoneUi.drawFilledButton(screen, guiGraphics, x, y, keySize, keySize, hover, LAYOUT[row][col]);
            }
        }

        int okX = screen.getPhoneX() + 24;
        int clearX = screen.getPhoneX() + 82;
        int controlsY = screen.getPhoneY() + 218;

        PhoneUi.drawFilledButton(
                screen,
                guiGraphics,
                okX,
                controlsY,
                44,
                14,
                screen.isInside(mouseX, mouseY, okX, controlsY, 44, 14),
                KEY_OK
        );

        PhoneUi.drawFilledButton(
                screen,
                guiGraphics,
                clearX,
                controlsY,
                44,
                14,
                screen.isInside(mouseX, mouseY, clearX, controlsY, 44, 14),
                KEY_CLEAR
        );
    }

    public String getKeyAt(PhoneScreen screen, double mouseX, double mouseY) {
        for (int row = 0; row < LAYOUT.length; row++) {
            for (int col = 0; col < LAYOUT[row].length; col++) {
                int x = startX + col * (keySize + gap);
                int y = startY + row * (keySize + gap);

                if (screen.isInside(mouseX, mouseY, x, y, keySize, keySize)) {
                    return LAYOUT[row][col];
                }
            }
        }

        int okX = screen.getPhoneX() + 24;
        int clearX = screen.getPhoneX() + 82;
        int controlsY = screen.getPhoneY() + 218;

        if (screen.isInside(mouseX, mouseY, okX, controlsY, 44, 14)) {
            return KEY_OK;
        }

        if (screen.isInside(mouseX, mouseY, clearX, controlsY, 44, 14)) {
            return KEY_CLEAR;
        }

        return null;
    }
}