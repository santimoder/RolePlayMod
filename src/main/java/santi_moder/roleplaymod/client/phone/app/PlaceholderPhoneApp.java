package santi_moder.roleplaymod.client.phone.app;

import net.minecraft.client.gui.GuiGraphics;
import santi_moder.roleplaymod.client.phone.ui.PhoneUi;
import santi_moder.roleplaymod.client.screen.PhoneScreen;
import santi_moder.roleplaymod.common.phone.PhoneAppId;

public class PlaceholderPhoneApp extends AbstractPhoneApp {

    public PlaceholderPhoneApp(PhoneAppId appId) {
        super(appId);
    }

    @Override
    public void render(PhoneScreen screen, GuiGraphics guiGraphics, int mouseX, int mouseY, float partialTick) {
        PhoneUi.drawHeaderTitle(screen, guiGraphics, getAppId().getDisplayName());
        PhoneUi.drawBackButton(screen, guiGraphics, mouseX, mouseY);
        PhoneUi.drawPanel(screen, guiGraphics);

        PhoneUi.drawCenteredLines(
                screen,
                guiGraphics,
                screen.getPhoneY() + 98,
                16,
                "App en desarrollo",
                "La estructura ya esta lista",
                "Volvé con Inicio o Atras"
        );
    }

    @Override
    public boolean mouseClicked(PhoneScreen screen, double mouseX, double mouseY, int button) {
        if (button != 0) {
            return false;
        }

        if (PhoneUi.isBackButtonClicked(screen, mouseX, mouseY)) {
            screen.navigateBackTo(PhoneAppId.HOME);
            return true;
        }

        return false;
    }
}