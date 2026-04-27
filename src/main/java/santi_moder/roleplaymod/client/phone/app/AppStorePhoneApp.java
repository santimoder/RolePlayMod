package santi_moder.roleplaymod.client.phone.app;

import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.world.item.ItemStack;
import santi_moder.roleplaymod.client.phone.ui.PhoneUi;
import santi_moder.roleplaymod.client.screen.PhoneScreen;
import santi_moder.roleplaymod.common.phone.PhoneAppId;
import santi_moder.roleplaymod.common.phone.PhoneData;
import santi_moder.roleplaymod.network.ModNetwork;
import santi_moder.roleplaymod.network.phone.PhoneAppInstallC2SPacket;

public class AppStorePhoneApp extends AbstractPhoneApp {

    private static final int TITLE_Y = 34;
    private static final int START_Y = 66;
    private static final int ROW_HEIGHT = 34;

    private static final int COLOR_TEXT = 0xFFFFFFFF;
    private static final int COLOR_SUBTEXT = 0xFFBFC3C7;
    private static final int COLOR_BUTTON = 0xFF2563EB;
    private static final int COLOR_BUTTON_DISABLED = 0xFF555555;
    private static final int COLOR_ROW = 0x33111111;

    private static final StoreAppSpec[] AVAILABLE_APPS = {
            new StoreAppSpec(PhoneAppId.WHATSAPP, "WhatsApp", "Mensajeria RP", true),
            new StoreAppSpec(PhoneAppId.INSTAGRAM, "Instagram", "No disponible", false),
            new StoreAppSpec(PhoneAppId.TWITTER, "Twitter", "No disponible", false),
            new StoreAppSpec(PhoneAppId.SPOTIFY, "Spotify", "No disponible", false)
    };

    public AppStorePhoneApp() {
        super(PhoneAppId.APP_STORE);
    }

    @Override
    public void render(PhoneScreen screen, GuiGraphics guiGraphics, int mouseX, int mouseY, float partialTick) {
        ItemStack phoneStack = screen.getPhoneStack();
        PhoneData.initializeIfMissing(phoneStack);

        guiGraphics.drawCenteredString(
                screen.getPhoneFont(),
                "AppStore",
                screen.getPhoneCenterX(),
                screen.getPhoneY() + TITLE_Y,
                COLOR_TEXT
        );

        int x = screen.getSafeLeft();
        int y = screen.getPhoneY() + START_Y;
        int width = screen.getSafeWidth();

        for (int i = 0; i < AVAILABLE_APPS.length; i++) {
            StoreAppSpec app = AVAILABLE_APPS[i];
            int rowY = y + i * ROW_HEIGHT;

            renderAppRow(screen, guiGraphics, phoneStack, app, x, rowY, width, mouseX, mouseY);
        }
    }

    private void renderAppRow(
            PhoneScreen screen,
            GuiGraphics guiGraphics,
            ItemStack phoneStack,
            StoreAppSpec app,
            int x,
            int y,
            int width,
            int mouseX,
            int mouseY
    ) {
        guiGraphics.fill(x, y, x + width, y + ROW_HEIGHT - 4, COLOR_ROW);

        guiGraphics.drawString(
                screen.getPhoneFont(),
                app.name(),
                x + 6,
                y + 5,
                COLOR_TEXT,
                false
        );

        guiGraphics.drawString(
                screen.getPhoneFont(),
                app.description(),
                x + 6,
                y + 17,
                COLOR_SUBTEXT,
                false
        );

        boolean installed = PhoneData.isAppInstalled(phoneStack, app.appId());
        String buttonText;

        if (installed) {
            buttonText = "Abrir";
        } else if (app.available()) {
            buttonText = "Obtener";
        } else {
            buttonText = "Prox.";
        }

        int buttonWidth = 42;
        int buttonHeight = 16;
        int buttonX = x + width - buttonWidth - 6;
        int buttonY = y + 8;

        boolean hover = isInside(mouseX, mouseY, buttonX, buttonY, buttonWidth, buttonHeight);
        int buttonColor = app.available() ? COLOR_BUTTON : COLOR_BUTTON_DISABLED;

        if (hover && app.available()) {
            buttonColor = 0xFF3B82F6;
        }

        guiGraphics.fill(buttonX, buttonY, buttonX + buttonWidth, buttonY + buttonHeight, buttonColor);

        guiGraphics.drawCenteredString(
                screen.getPhoneFont(),
                buttonText,
                buttonX + buttonWidth / 2,
                buttonY + 4,
                COLOR_TEXT
        );
    }

    @Override
    public boolean mouseClicked(PhoneScreen screen, double mouseX, double mouseY, int button) {
        if (button != 0) {
            return false;
        }

        ItemStack phoneStack = screen.getPhoneStack();
        PhoneData.initializeIfMissing(phoneStack);

        int x = screen.getSafeLeft();
        int y = screen.getPhoneY() + START_Y;
        int width = screen.getSafeWidth();

        for (int i = 0; i < AVAILABLE_APPS.length; i++) {
            StoreAppSpec app = AVAILABLE_APPS[i];
            int rowY = y + i * ROW_HEIGHT;

            int buttonWidth = 42;
            int buttonHeight = 16;
            int buttonX = x + width - buttonWidth - 6;
            int buttonY = rowY + 8;

            if (!isInside(mouseX, mouseY, buttonX, buttonY, buttonWidth, buttonHeight)) {
                continue;
            }

            if (!app.available()) {
                return true;
            }

            if (PhoneData.isAppInstalled(phoneStack, app.appId())) {
                screen.openApp(app.appId());
                return true;
            }

            PhoneData.installApp(phoneStack, app.appId());
            ModNetwork.sendInventoryToServer(new PhoneAppInstallC2SPacket(app.appId()));
            return true;
        }

        return false;
    }

    private boolean isInside(double mouseX, double mouseY, int x, int y, int width, int height) {
        return mouseX >= x && mouseX < x + width && mouseY >= y && mouseY < y + height;
    }

    private record StoreAppSpec(
            PhoneAppId appId,
            String name,
            String description,
            boolean available
    ) {
    }
}