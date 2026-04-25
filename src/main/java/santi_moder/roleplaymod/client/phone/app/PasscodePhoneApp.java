package santi_moder.roleplaymod.client.phone.app;

import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.world.item.ItemStack;
import org.lwjgl.glfw.GLFW;
import santi_moder.roleplaymod.client.phone.ui.PhoneUi;
import santi_moder.roleplaymod.client.phone.ui.component.PhoneNumericKeypad;
import santi_moder.roleplaymod.client.screen.PhoneScreen;
import santi_moder.roleplaymod.common.phone.PhoneAppId;
import santi_moder.roleplaymod.common.phone.PhoneData;

public class PasscodePhoneApp extends AbstractPhoneApp {

    private static final int PASSCODE_LENGTH = 4;

    private static final int TITLE_Y = 58;
    private static final int DOTS_Y = 74;
    private static final int ERROR_Y = 88;

    private static final int CANCEL_WIDTH = 42;
    private static final int CANCEL_HEIGHT = 12;
    private static final int CANCEL_OFFSET_X = 52;
    private static final int CANCEL_OFFSET_Y = 22;

    private static final int ERROR_TICKS_ON_FAIL = 40;

    private String enteredCode = "";
    private int errorTicks = 0;

    public PasscodePhoneApp() {
        super(PhoneAppId.PASSCODE_SCREEN);
    }

    @Override
    public void onOpen(PhoneScreen screen, ItemStack phoneStack) {
        resetState();
    }

    @Override
    public void render(PhoneScreen screen, GuiGraphics guiGraphics, int mouseX, int mouseY, float partialTick) {
        PhoneNumericKeypad keypad = PhoneNumericKeypad.defaultPinPad(screen);

        guiGraphics.drawCenteredString(
                screen.getPhoneFont(),
                "Ingresar codigo",
                screen.getPhoneCenterX(),
                screen.getPhoneY() + TITLE_Y,
                PhoneUi.COLOR_TEXT
        );

        guiGraphics.drawCenteredString(
                screen.getPhoneFont(),
                PhoneUi.buildDots(enteredCode, PASSCODE_LENGTH),
                screen.getPhoneCenterX(),
                screen.getPhoneY() + DOTS_Y,
                PhoneUi.COLOR_TEXT
        );

        if (errorTicks > 0) {
            guiGraphics.drawCenteredString(
                    screen.getPhoneFont(),
                    "Codigo incorrecto",
                    screen.getPhoneCenterX(),
                    screen.getPhoneY() + ERROR_Y,
                    PhoneUi.COLOR_ERROR
            );
            errorTicks--;
        }

        renderCancelButton(screen, guiGraphics, mouseX, mouseY);
        keypad.render(screen, guiGraphics, mouseX, mouseY);
    }

    private void renderCancelButton(PhoneScreen screen, GuiGraphics guiGraphics, int mouseX, int mouseY) {
        int x = getCancelX(screen);
        int y = getCancelY(screen);
        boolean hover = screen.isInside(mouseX, mouseY, x, y, CANCEL_WIDTH, CANCEL_HEIGHT);

        guiGraphics.drawString(
                screen.getPhoneFont(),
                "Cancelar",
                x,
                y,
                hover ? PhoneUi.COLOR_TEXT : 0xFFBFC7FF,
                false
        );
    }

    @Override
    public boolean mouseClicked(PhoneScreen screen, double mouseX, double mouseY, int button) {
        if (button != 0) {
            return false;
        }

        if (screen.isInside(mouseX, mouseY, getCancelX(screen), getCancelY(screen), CANCEL_WIDTH, CANCEL_HEIGHT)) {
            handleCancel(screen);
            return true;
        }

        PhoneNumericKeypad keypad = PhoneNumericKeypad.defaultPinPad(screen);
        String key = keypad.getKeyAt(screen, mouseX, mouseY);

        if (key != null) {
            handleKeyPress(screen, key);
            return true;
        }

        return false;
    }

    @Override
    public boolean keyPressed(PhoneScreen screen, int keyCode, int scanCode, int modifiers) {
        if (keyCode >= GLFW.GLFW_KEY_0 && keyCode <= GLFW.GLFW_KEY_9) {
            handleDigit(screen, String.valueOf((char) ('0' + (keyCode - GLFW.GLFW_KEY_0))));
            return true;
        }

        if (keyCode >= GLFW.GLFW_KEY_KP_0 && keyCode <= GLFW.GLFW_KEY_KP_9) {
            handleDigit(screen, String.valueOf(keyCode - GLFW.GLFW_KEY_KP_0));
            return true;
        }

        if (keyCode == GLFW.GLFW_KEY_BACKSPACE) {
            removeLastDigit();
            return true;
        }

        if (keyCode == GLFW.GLFW_KEY_ENTER || keyCode == GLFW.GLFW_KEY_KP_ENTER) {
            submit(screen);
            return true;
        }

        if (keyCode == GLFW.GLFW_KEY_ESCAPE) {
            handleCancel(screen);
            return true;
        }

        return false;
    }

    private void handleKeyPress(PhoneScreen screen, String key) {
        handleDigit(screen, key);
    }

    private void handleDigit(PhoneScreen screen, String digit) {
        if (enteredCode.length() >= PASSCODE_LENGTH) {
            return;
        }

        enteredCode += digit;

        if (enteredCode.length() == PASSCODE_LENGTH) {
            submit(screen);
        }
    }

    private void submit(PhoneScreen screen) {
        if (enteredCode.length() != PASSCODE_LENGTH) {
            return;
        }

        ItemStack stack = screen.getPhoneStack();
        if (PhoneData.validatePasscode(stack, enteredCode)) {
            resetState();
            screen.unlockPhone();
            return;
        }

        enteredCode = "";
        errorTicks = ERROR_TICKS_ON_FAIL;
    }

    private void removeLastDigit() {
        if (!enteredCode.isEmpty()) {
            enteredCode = enteredCode.substring(0, enteredCode.length() - 1);
        }
    }

    private void handleCancel(PhoneScreen screen) {
        resetState();
        screen.navigateBackTo(PhoneAppId.LOCK_SCREEN);
    }

    private void resetState() {
        enteredCode = "";
        errorTicks = 0;
    }

    private int getCancelX(PhoneScreen screen) {
        return screen.getPhoneX() + screen.getPhoneWidth() - CANCEL_OFFSET_X;
    }

    private int getCancelY(PhoneScreen screen) {
        return screen.getPhoneY() + screen.getPhoneHeight() - CANCEL_OFFSET_Y;
    }
}