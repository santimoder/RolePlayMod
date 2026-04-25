package santi_moder.roleplaymod.client.phone.app;

import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.world.item.ItemStack;
import santi_moder.roleplaymod.client.screen.PhoneScreen;
import santi_moder.roleplaymod.common.phone.PhoneAppId;

public abstract class AbstractPhoneApp {

    private final PhoneAppId appId;

    protected AbstractPhoneApp(PhoneAppId appId) {
        this.appId = appId;
    }

    public final PhoneAppId getAppId() {
        return appId;
    }

    public void onOpen(PhoneScreen screen, ItemStack phoneStack) {
    }

    public void onClose(PhoneScreen screen, ItemStack phoneStack) {
    }

    public void tick(PhoneScreen screen, ItemStack phoneStack) {
    }

    public void onResize(PhoneScreen screen, int width, int height) {
    }

    public abstract void render(
            PhoneScreen screen,
            GuiGraphics guiGraphics,
            int mouseX,
            int mouseY,
            float partialTick
    );

    public boolean mouseScrolled(PhoneScreen screen, double mouseX, double mouseY, double scrollDelta) {
        return false;
    }

    public boolean charTyped(PhoneScreen screen, char codePoint, int modifiers) {
        return false;
    }

    public boolean mouseClicked(PhoneScreen screen, double mouseX, double mouseY, int button) {
        return false;
    }

    public boolean mousePressed(PhoneScreen screen, double mouseX, double mouseY, int button) {
        return false;
    }

    public boolean mouseReleased(PhoneScreen screen, double mouseX, double mouseY, int button) {
        return false;
    }

    public boolean mouseDragged(
            PhoneScreen screen,
            double mouseX,
            double mouseY,
            int button,
            double dragX,
            double dragY
    ) {
        return false;
    }

    public boolean keyPressed(PhoneScreen screen, int keyCode, int scanCode, int modifiers) {
        return false;
    }
}