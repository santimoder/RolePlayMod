package santi_moder.roleplaymod.client.phone.app.whatsapp;

import net.minecraft.client.gui.GuiGraphics;
import santi_moder.roleplaymod.client.phone.ui.PhoneThemeColors;
import santi_moder.roleplaymod.client.phone.ui.PhoneUi;
import santi_moder.roleplaymod.client.screen.PhoneScreen;

public final class WhatsappNewContactView {

    private static final int TITLE_Y = 30;

    private static final int LABEL_X = 16;
    private static final int FIELD_X = 16;
    private static final int FIELD_W_OFFSET = 32;
    private static final int FIELD_H = 16;

    private static final int NAME_LABEL_Y = 52;
    private static final int NAME_FIELD_Y = 62;

    private static final int SURNAME_LABEL_Y = 86;
    private static final int SURNAME_FIELD_Y = 96;

    private static final int PHONE_LABEL_Y = 120;
    private static final int PHONE_PREFIX_Y = 130;
    private static final int PHONE_FIELD_Y = 146;

    private static final int SAVE_BUTTON_Y = 176;
    private static final int SAVE_BUTTON_W = 90;
    private static final int SAVE_BUTTON_H = 16;

    private static final int MAX_NAME = 20;
    private static final int MAX_SURNAME = 24;
    private static final int MAX_PHONE = 12;

    private Field activeField = Field.NONE;

    public void render(PhoneScreen screen, GuiGraphics guiGraphics, int mouseX, int mouseY, WhatsappState state) {
        guiGraphics.fill(
                screen.getPhoneX() + 4,
                screen.getPhoneY() + 4,
                screen.getPhoneX() + screen.getPhoneWidth() - 4,
                screen.getPhoneY() + screen.getPhoneHeight() - 4,
                PhoneThemeColors.appBackground(screen.getPhoneStack())
        );

        PhoneUi.drawBackButton(screen, guiGraphics, mouseX, mouseY);

        PhoneUi.drawCenteredText(
                screen,
                guiGraphics,
                "Nuevo contacto",
                screen.getPhoneCenterX(),
                screen.getPhoneY() + TITLE_Y,
                PhoneThemeColors.text(screen.getPhoneStack())
        );

        renderField(
                screen,
                guiGraphics,
                "Nombre",
                state.getNewContactName(),
                "Nombre",
                screen.getPhoneY() + NAME_LABEL_Y,
                screen.getPhoneY() + NAME_FIELD_Y,
                activeField == Field.NAME
        );

        renderField(
                screen,
                guiGraphics,
                "Apellidos",
                state.getNewContactSurname(),
                "Apellidos",
                screen.getPhoneY() + SURNAME_LABEL_Y,
                screen.getPhoneY() + SURNAME_FIELD_Y,
                activeField == Field.SURNAME
        );

        guiGraphics.drawString(
                screen.getPhoneFont(),
                "Teléfono (Uruguay)",
                screen.getPhoneX() + LABEL_X,
                screen.getPhoneY() + PHONE_LABEL_Y,
                PhoneThemeColors.subtext(screen.getPhoneStack()),
                false
        );

        guiGraphics.drawString(
                screen.getPhoneFont(),
                "Móvil +598",
                screen.getPhoneX() + LABEL_X,
                screen.getPhoneY() + PHONE_PREFIX_Y,
                PhoneThemeColors.subtext(screen.getPhoneStack()),
                false
        );

        renderPhoneField(screen, guiGraphics, state.getNewContactPhone(), activeField == Field.PHONE);

        renderSaveButton(screen, guiGraphics, mouseX, mouseY, state.canCreateNewContact());
    }

    public boolean mouseClicked(PhoneScreen screen, double mouseX, double mouseY, int button, WhatsappState state) {
        if (button != 0) {
            return false;
        }

        if (PhoneUi.isBackButtonClicked(screen, mouseX, mouseY)) {
            state.backFromChatSubscreen();
            resetViewState();
            return true;
        }

        if (isInsideNameField(screen, mouseX, mouseY)) {
            activeField = Field.NAME;
            return true;
        }

        if (isInsideSurnameField(screen, mouseX, mouseY)) {
            activeField = Field.SURNAME;
            return true;
        }

        if (isInsidePhoneField(screen, mouseX, mouseY)) {
            activeField = Field.PHONE;
            return true;
        }

        if (isInsideSaveButton(screen, mouseX, mouseY) && state.canCreateNewContact()) {
            santi_moder.roleplaymod.network.ModNetwork.sendWhatsappToServer(
                    new santi_moder.roleplaymod.network.phone.whatsapp.WhatsappCreateContactC2SPacket(
                            state.buildNewContactDisplayName(),
                            state.buildNewContactPhoneFull()
                    )
            );
            resetViewState();
            return true;
        }

        activeField = Field.NONE;
        return true;
    }

    public boolean charTyped(char codePoint, int modifiers, WhatsappState state) {
        if (activeField == Field.NONE || Character.isISOControl(codePoint)) {
            return false;
        }

        switch (activeField) {
            case NAME -> {
                if (state.getNewContactName().length() < MAX_NAME) {
                    state.setNewContactName(state.getNewContactName() + codePoint);
                }
                return true;
            }
            case SURNAME -> {
                if (state.getNewContactSurname().length() < MAX_SURNAME) {
                    state.setNewContactSurname(state.getNewContactSurname() + codePoint);
                }
                return true;
            }
            case PHONE -> {
                if (Character.isDigit(codePoint) && state.getNewContactPhone().length() < MAX_PHONE) {
                    state.setNewContactPhone(state.getNewContactPhone() + codePoint);
                }
                return true;
            }
            case NONE -> {
                return false;
            }
        }

        return false;
    }

    public boolean keyPressed(int keyCode, WhatsappState state) {
        if (keyCode == 256) { // ESC
            if (activeField != Field.NONE) {
                activeField = Field.NONE;
                return true;
            }

            state.backFromChatSubscreen();
            return true;
        }

        if (keyCode == 257 || keyCode == 335) { // ENTER
            if (state.canCreateNewContact()) {
                santi_moder.roleplaymod.network.ModNetwork.sendWhatsappToServer(
                        new santi_moder.roleplaymod.network.phone.whatsapp.WhatsappCreateContactC2SPacket(
                                state.buildNewContactDisplayName(),
                                state.buildNewContactPhoneFull()
                        )
                );
                resetViewState();
                return true;
            }

            activeField = Field.NONE;
            return true;
        }

        if (keyCode == 259) { // BACKSPACE
            switch (activeField) {
                case NAME -> {
                    if (!state.getNewContactName().isEmpty()) {
                        state.setNewContactName(state.getNewContactName().substring(0, state.getNewContactName().length() - 1));
                    }
                    return true;
                }
                case SURNAME -> {
                    if (!state.getNewContactSurname().isEmpty()) {
                        state.setNewContactSurname(state.getNewContactSurname().substring(0, state.getNewContactSurname().length() - 1));
                    }
                    return true;
                }
                case PHONE -> {
                    if (!state.getNewContactPhone().isEmpty()) {
                        state.setNewContactPhone(state.getNewContactPhone().substring(0, state.getNewContactPhone().length() - 1));
                    }
                    return true;
                }
                case NONE -> {
                    return false;
                }
            }
        }

        return false;
    }

    public void resetViewState() {
        activeField = Field.NONE;
    }

    private void renderField(
            PhoneScreen screen,
            GuiGraphics guiGraphics,
            String label,
            String value,
            String placeholder,
            int labelY,
            int fieldY,
            boolean active
    ) {
        guiGraphics.drawString(
                screen.getPhoneFont(),
                label,
                screen.getPhoneX() + LABEL_X,
                labelY,
                PhoneThemeColors.subtext(screen.getPhoneStack()),
                false
        );

        int x = screen.getPhoneX() + FIELD_X;
        int w = screen.getPhoneWidth() - FIELD_W_OFFSET;

        guiGraphics.fill(
                x,
                fieldY,
                x + w,
                fieldY + FIELD_H,
                active ? PhoneThemeColors.inputActive(screen.getPhoneStack()) : PhoneThemeColors.input(screen.getPhoneStack())
        );

        boolean empty = value == null || value.isEmpty();
        guiGraphics.drawString(
                screen.getPhoneFont(),
                empty ? placeholder : value,
                x + 5,
                fieldY + 4,
                empty ? PhoneThemeColors.hint(screen.getPhoneStack()) : PhoneThemeColors.text(screen.getPhoneStack()),
                false
        );
    }

    private void renderPhoneField(PhoneScreen screen, GuiGraphics guiGraphics, String value, boolean active) {
        int x = screen.getPhoneX() + FIELD_X;
        int w = screen.getPhoneWidth() - FIELD_W_OFFSET;

        guiGraphics.fill(
                x,
                screen.getPhoneY() + PHONE_FIELD_Y,
                x + w,
                screen.getPhoneY() + PHONE_FIELD_Y + FIELD_H,
                active ? PhoneThemeColors.inputActive(screen.getPhoneStack()) : PhoneThemeColors.input(screen.getPhoneStack())
        );

        boolean empty = value == null || value.isEmpty();
        guiGraphics.drawString(
                screen.getPhoneFont(),
                empty ? "Teléfono" : value,
                x + 5,
                screen.getPhoneY() + PHONE_FIELD_Y + 4,
                empty ? PhoneThemeColors.hint(screen.getPhoneStack()) : PhoneThemeColors.text(screen.getPhoneStack()),
                false
        );
    }

    private void renderSaveButton(PhoneScreen screen, GuiGraphics guiGraphics, int mouseX, int mouseY, boolean enabled) {
        int x = screen.getPhoneCenterX() - SAVE_BUTTON_W / 2;
        int y = screen.getPhoneY() + SAVE_BUTTON_Y;
        boolean hover = screen.isInside(mouseX, mouseY, x, y, SAVE_BUTTON_W, SAVE_BUTTON_H);

        int color = enabled
                ? (hover ? PhoneThemeColors.successHover(screen.getPhoneStack()) : PhoneThemeColors.success(screen.getPhoneStack()))
                : PhoneThemeColors.disabledInput(screen.getPhoneStack());

        guiGraphics.fill(x, y, x + SAVE_BUTTON_W, y + SAVE_BUTTON_H, color);

        PhoneUi.drawCenteredText(
                screen,
                guiGraphics,
                "Guardar",
                x + SAVE_BUTTON_W / 2,
                y + 4,
                enabled ? PhoneThemeColors.onSuccess(screen.getPhoneStack()) : PhoneThemeColors.hint(screen.getPhoneStack())
        );
    }

    private boolean isInsideNameField(PhoneScreen screen, double mouseX, double mouseY) {
        return isInsideField(screen, mouseX, mouseY, screen.getPhoneY() + NAME_FIELD_Y);
    }

    private boolean isInsideSurnameField(PhoneScreen screen, double mouseX, double mouseY) {
        return isInsideField(screen, mouseX, mouseY, screen.getPhoneY() + SURNAME_FIELD_Y);
    }

    private boolean isInsidePhoneField(PhoneScreen screen, double mouseX, double mouseY) {
        return isInsideField(screen, mouseX, mouseY, screen.getPhoneY() + PHONE_FIELD_Y);
    }

    private boolean isInsideField(PhoneScreen screen, double mouseX, double mouseY, int y) {
        return screen.isInside(
                mouseX,
                mouseY,
                screen.getPhoneX() + FIELD_X,
                y,
                screen.getPhoneWidth() - FIELD_W_OFFSET,
                FIELD_H
        );
    }

    private boolean isInsideSaveButton(PhoneScreen screen, double mouseX, double mouseY) {
        int x = screen.getPhoneCenterX() - SAVE_BUTTON_W / 2;
        int y = screen.getPhoneY() + SAVE_BUTTON_Y;
        return screen.isInside(mouseX, mouseY, x, y, SAVE_BUTTON_W, SAVE_BUTTON_H);
    }

    private enum Field {
        NONE,
        NAME,
        SURNAME,
        PHONE
    }
}