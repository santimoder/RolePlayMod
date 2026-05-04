package santi_moder.roleplaymod.client.phone.ui.component;

import net.minecraft.client.gui.GuiGraphics;
import santi_moder.roleplaymod.client.phone.ui.PhoneUi;
import santi_moder.roleplaymod.client.screen.PhoneScreen;

public final class PhoneNumericKeypad {

    private static final String[][] LAYOUT = {
            {"1", "2", "3"},
            {"4", "5", "6"},
            {"7", "8", "9"},
            {"", "0", ""}
    };

    private final int startX;
    private final int startY;
    private final int keySize;
    private final int gap;

    public PhoneNumericKeypad(int startX, int startY, int keySize, int gap) {
        this.startX = startX;
        this.startY = startY;
        this.keySize = keySize;
        this.gap = gap;
    }

    public static PhoneNumericKeypad defaultPinPad(PhoneScreen screen) {
        int keySize = 26;
        int gap = 8;

        int totalWidth = getLayoutColumns() * keySize + (getLayoutColumns() - 1) * gap;
        int startX = screen.getPhoneCenterX() - totalWidth / 2;

        return new PhoneNumericKeypad(
                startX,
                screen.getPhoneY() + 98,
                keySize,
                gap
        );
    }

    private static int getLayoutColumns() {
        return 3;
    }

    public void render(PhoneScreen screen, GuiGraphics guiGraphics, int mouseX, int mouseY) {
        for (int row = 0; row < LAYOUT.length; row++) {
            for (int col = 0; col < LAYOUT[row].length; col++) {
                String label = LAYOUT[row][col];

                if (label.isEmpty()) {
                    continue;
                }

                int x = startX + col * (keySize + gap);
                int y = startY + row * (keySize + gap);

                boolean hover = screen.isInside(mouseX, mouseY, x, y, keySize, keySize);
                PhoneUi.drawFilledButton(screen, guiGraphics, x, y, keySize, keySize, hover, label);
            }
        }
    }

    public String getKeyAt(PhoneScreen screen, double mouseX, double mouseY) {
        for (int row = 0; row < LAYOUT.length; row++) {
            for (int col = 0; col < LAYOUT[row].length; col++) {
                String label = LAYOUT[row][col];

                if (label.isEmpty()) {
                    continue;
                }

                int x = startX + col * (keySize + gap);
                int y = startY + row * (keySize + gap);

                if (screen.isInside(mouseX, mouseY, x, y, keySize, keySize)) {
                    return label;
                }
            }
        }

        return null;
    }
}