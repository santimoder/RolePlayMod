package santi_moder.roleplaymod.client.phone.app.whatsapp;

import net.minecraft.client.gui.GuiGraphics;
import santi_moder.roleplaymod.client.phone.ui.PhoneUi;
import santi_moder.roleplaymod.client.screen.PhoneScreen;

public final class WhatsappContactPhotoView {

    private static final int TITLE_Y = 30;
    private static final int PHOTO_RADIUS = 42;
    private static final int PHOTO_COLOR = 0xFF25D366;
    private static final int PHOTO_TEXT_COLOR = 0xFF081C15;

    public void render(PhoneScreen screen, GuiGraphics guiGraphics, int mouseX, int mouseY, WhatsappState state) {
        WhatsappContact contact = state.getSelectedContact();
        if (contact == null) {
            return;
        }

        PhoneUi.drawBackButton(screen, guiGraphics, mouseX, mouseY);

        guiGraphics.drawCenteredString(screen.getPhoneFont(), "Foto", screen.getPhoneCenterX(), screen.getPhoneY() + TITLE_Y, PhoneUi.COLOR_TEXT);

        int centerX = screen.getPhoneCenterX();
        int centerY = screen.getPhoneCenterY() - 8;

        guiGraphics.fill(centerX - PHOTO_RADIUS, centerY - PHOTO_RADIUS, centerX + PHOTO_RADIUS, centerY + PHOTO_RADIUS, PHOTO_COLOR);
        guiGraphics.drawCenteredString(screen.getPhoneFont(), contact.getInitials(), centerX, centerY - 4, PHOTO_TEXT_COLOR);

        guiGraphics.drawCenteredString(screen.getPhoneFont(), contact.displayName(), centerX, centerY + PHOTO_RADIUS + 10, PhoneUi.COLOR_TEXT);
    }

    public boolean mouseClicked(PhoneScreen screen, double mouseX, double mouseY, int button, WhatsappState state) {
        if (button != 0) {
            return false;
        }

        if (PhoneUi.isBackButtonClicked(screen, mouseX, mouseY)) {
            state.backFromChatSubscreen();
            return true;
        }

        return true;
    }
}