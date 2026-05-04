package santi_moder.roleplaymod.client.phone.ui.component;

import net.minecraft.client.gui.GuiGraphics;
import santi_moder.roleplaymod.client.phone.ui.PhoneUi;
import santi_moder.roleplaymod.client.screen.PhoneScreen;

public record PhoneModal(int x, int y, int width, int height) {

    public static PhoneModal centered(PhoneScreen screen, int width, int height) {
        int x = screen.getPhoneCenterX() - width / 2;
        int y = screen.getPhoneCenterY() - height / 2;
        return new PhoneModal(x, y, width, height);
    }

    public void render(GuiGraphics guiGraphics) {
        guiGraphics.fill(x, y, x + width, y + height, 0xDD111111);
        guiGraphics.fill(x + 1, y + 1, x + width - 1, y + height - 1, 0xEE1E1E2A);
    }

    public void renderTitle(PhoneScreen screen, GuiGraphics guiGraphics, String title) {
        guiGraphics.drawCenteredString(
                screen.getPhoneFont(),
                title,
                x + width / 2,
                y + 10,
                PhoneUi.COLOR_TEXT
        );
    }

    public void renderMessage(PhoneScreen screen, GuiGraphics guiGraphics, int startY, String... lines) {
        int yOffset = y + startY;

        for (int i = 0; i < lines.length; i++) {
            guiGraphics.drawCenteredString(
                    screen.getPhoneFont(),
                    lines[i],
                    x + width / 2,
                    yOffset,
                    i == 0 ? PhoneUi.COLOR_TEXT : PhoneUi.COLOR_SUBTEXT
            );
            yOffset += 14;
        }
    }

    public void renderButton(
            PhoneScreen screen,
            GuiGraphics guiGraphics,
            int mouseX,
            int mouseY,
            int buttonX,
            int buttonY,
            int buttonW,
            int buttonH,
            String label
    ) {
        boolean hover = screen.isInside(mouseX, mouseY, buttonX, buttonY, buttonW, buttonH);
        PhoneUi.drawFilledButton(screen, guiGraphics, buttonX, buttonY, buttonW, buttonH, hover, label);
    }

    public boolean isInside(double mouseX, double mouseY) {
        return mouseX >= x && mouseX < x + width && mouseY >= y && mouseY < y + height;
    }
}