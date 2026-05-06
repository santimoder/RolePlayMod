package santi_moder.roleplaymod.client.phone.app.whatsapp;

import net.minecraft.client.gui.GuiGraphics;
import santi_moder.roleplaymod.client.phone.ui.PhoneThemeColors;
import santi_moder.roleplaymod.client.phone.ui.PhoneUi;
import santi_moder.roleplaymod.client.screen.PhoneScreen;
import santi_moder.roleplaymod.common.whatsapp.model.WhatsappContact;

public final class WhatsappContactPhotoView {

    private static final int TITLE_Y = 30;
    private static final int PHOTO_RADIUS = 42;

    public void render(PhoneScreen screen, GuiGraphics guiGraphics, int mouseX, int mouseY, WhatsappState state) {
        guiGraphics.fill(
                screen.getPhoneX() + 4,
                screen.getPhoneY() + 4,
                screen.getPhoneX() + screen.getPhoneWidth() - 4,
                screen.getPhoneY() + screen.getPhoneHeight() - 4,
                PhoneThemeColors.appBackground(screen.getPhoneStack())
        );

        WhatsappContact contact = state.getSelectedContact();
        if (contact == null) {
            return;
        }

        PhoneUi.drawBackButton(screen, guiGraphics, mouseX, mouseY);

        PhoneUi.drawCenteredText(
        screen,
        guiGraphics,
        "Foto",
        screen.getPhoneCenterX(),
        screen.getPhoneY() + TITLE_Y,
        PhoneThemeColors.text(screen.getPhoneStack())
);

        int centerX = screen.getPhoneCenterX();
        int centerY = screen.getPhoneCenterY() - 8;

        WhatsappTextureResolver.drawProfilePhoto(
                guiGraphics,
                contact.photoId(),
                centerX - PHOTO_RADIUS,
                centerY - PHOTO_RADIUS,
                PHOTO_RADIUS * 2
        );

        PhoneUi.drawCenteredText(
        screen,
        guiGraphics,
        contact.displayName(),
        centerX,
        centerY + PHOTO_RADIUS + 10,
        PhoneThemeColors.text(screen.getPhoneStack())
);
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