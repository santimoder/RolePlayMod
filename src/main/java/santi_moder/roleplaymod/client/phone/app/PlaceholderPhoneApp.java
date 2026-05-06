package santi_moder.roleplaymod.client.phone.app;

import net.minecraft.client.gui.GuiGraphics;
import santi_moder.roleplaymod.client.phone.ui.PhoneThemeColors;
import santi_moder.roleplaymod.client.phone.ui.PhoneUi;
import santi_moder.roleplaymod.client.screen.PhoneScreen;
import santi_moder.roleplaymod.common.phone.PhoneAppId;

public class PlaceholderPhoneApp extends AbstractPhoneApp {

    public PlaceholderPhoneApp(PhoneAppId appId) {
        super(appId);
    }

    @Override
    public void render(PhoneScreen screen, GuiGraphics guiGraphics, int mouseX, int mouseY, float partialTick) {
        guiGraphics.fill(
                screen.getPhoneX() + 4,
                screen.getPhoneY() + 4,
                screen.getPhoneX() + screen.getPhoneWidth() - 4,
                screen.getPhoneY() + screen.getPhoneHeight() - 4,
                PhoneThemeColors.appBackground(screen.getPhoneStack())
        );

        PhoneUi.drawHeaderTitle(screen, guiGraphics, getAppId().getDisplayName());
        PhoneUi.drawPanel(screen, guiGraphics);

        PhoneUi.drawCenteredLines(
                screen,
                guiGraphics,
                screen.getPhoneY() + 98,
                16,
                "App en desarrollo",
                "La estructura ya esta lista"
        );
    }
}