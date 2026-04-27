package santi_moder.roleplaymod.client.phone.app.whatsapp;

import net.minecraft.client.gui.GuiGraphics;
import santi_moder.roleplaymod.client.phone.ui.PhoneUi;
import santi_moder.roleplaymod.client.screen.PhoneScreen;
import santi_moder.roleplaymod.common.whatsapp.model.WhatsappProfile;

public final class WhatsappProfileView {

    private static final int TITLE_Y = 30;

    private static final int PHOTO_CENTER_Y = 58;
    private static final int PHOTO_RADIUS = 22;
    private static final int PHOTO_COLOR = 0xFF25D366;
    private static final int PHOTO_TEXT_COLOR = 0xFF081C15;

    private static final int EDIT_LABEL_Y = 84;
    private static final int EDIT_LABEL_COLOR = 0xFF53BDEB;

    private static final int SECTION_START_Y = 104;
    private static final int SECTION_GAP = 34;

    private static final int FIELD_LABEL_X = 16;
    private static final int FIELD_BOX_X = 16;
    private static final int FIELD_BOX_WIDTH_OFFSET = 32;
    private static final int FIELD_BOX_HEIGHT = 16;
    private static final int FIELD_TEXT_PADDING_X = 5;
    private static final int FIELD_TEXT_PADDING_Y = 4;

    private static final int COLOR_FIELD_BG = 0xCC1A1A1A;
    private static final int COLOR_FIELD_BG_ACTIVE = 0xFF263238;
    private static final int COLOR_VALUE = 0xFFFFFFFF;
    private static final int COLOR_HINT = 0xFF9A9A9A;
    private static final int COLOR_SECTION = 0xFFBEBEBE;

    private static final int SHEET_SIDE_MARGIN = 8;
    private static final int SHEET_BOTTOM_OFFSET = 8;
    private static final int SHEET_OPTION_HEIGHT = 18;
    private static final int SHEET_TOP_PADDING = 8;
    private static final int SHEET_BOTTOM_PADDING = 8;
    private static final int SHEET_HANDLE_WIDTH = 26;
    private static final int SHEET_HANDLE_HEIGHT = 3;

    private static final int COLOR_SHEET_BG = 0xEE111111;
    private static final int COLOR_SHEET_HANDLE = 0x66FFFFFF;
    private static final int COLOR_SHEET_OPTION = 0x00000000;
    private static final int COLOR_SHEET_OPTION_HOVER = 0x2233FF99;
    private static final int COLOR_SHEET_TEXT = 0xFFFFFFFF;
    private static final int COLOR_SHEET_DISABLED = 0xFF6F6F6F;
    private static final int COLOR_SHEET_SEPARATOR = 0x22FFFFFF;
    private static final int COLOR_OVERLAY = 0x66000000;

    private static final int MAX_ABOUT_LENGTH = 80;
    private static final int MAX_NAME_LENGTH = 28;
    private static final int MAX_PHONE_LENGTH = 20;

    private Field activeField = Field.NONE;
    private boolean photoSheetOpen = false;

    public void render(PhoneScreen screen, GuiGraphics guiGraphics, int mouseX, int mouseY, WhatsappState state) {
        WhatsappProfile profile = state.getProfile();

        PhoneUi.drawBackButton(screen, guiGraphics, mouseX, mouseY);

        guiGraphics.drawCenteredString(
                screen.getPhoneFont(),
                "Perfil",
                screen.getPhoneCenterX(),
                screen.getPhoneY() + TITLE_Y,
                PhoneUi.COLOR_TEXT
        );

        renderPhoto(screen, guiGraphics, profile);
        renderEditLabel(screen, guiGraphics, mouseX, mouseY);
        renderFields(screen, guiGraphics, profile);

        if (photoSheetOpen) {
            renderPhotoSheetOverlay(screen, guiGraphics);
            renderPhotoSheet(screen, guiGraphics, mouseX, mouseY, profile);
        }
    }

    public boolean mouseClicked(PhoneScreen screen, double mouseX, double mouseY, int button, WhatsappState state) {
        if (button != 0) {
            return false;
        }

        if (PhoneUi.isBackButtonClicked(screen, mouseX, mouseY) && !photoSheetOpen) {
            return false;
        }

        WhatsappProfile profile = state.getProfile();

        if (photoSheetOpen) {
            if (isInsideTakePhoto(screen, mouseX, mouseY)) {
                photoSheetOpen = false;
                return true;
            }

            if (isInsideSelectPhoto(screen, mouseX, mouseY)) {
                photoSheetOpen = false;
                return true;
            }

            if (isInsideDeletePhoto(screen, mouseX, mouseY)) {
                if (hasCustomPhoto(profile)) {
                    profile.setPhotoId(WhatsappProfile.DEFAULT_PHOTO);
                }
                photoSheetOpen = false;
                return true;
            }

            if (!isInsidePhotoSheet(screen, mouseX, mouseY)) {
                photoSheetOpen = false;
                return true;
            }

            return true;
        }

        if (isInsidePhotoArea(screen, mouseX, mouseY) || isInsideEditLabel(screen, mouseX, mouseY)) {
            photoSheetOpen = true;
            activeField = Field.NONE;
            return true;
        }

        if (isInsideAboutField(screen, mouseX, mouseY)) {
            activeField = Field.ABOUT;
            return true;
        }

        if (isInsideNameField(screen, mouseX, mouseY)) {
            activeField = Field.NAME;
            return true;
        }

        if (isInsidePhoneField(screen, mouseX, mouseY)) {
            activeField = Field.PHONE;
            return true;
        }

        activeField = Field.NONE;
        return false;
    }

    public boolean charTyped(char codePoint, int modifiers, WhatsappState state) {
        if (activeField == Field.NONE || photoSheetOpen) {
            return false;
        }

        if (Character.isISOControl(codePoint)) {
            return false;
        }

        WhatsappProfile profile = state.getProfile();

        return switch (activeField) {
            case ABOUT -> appendAbout(profile, codePoint);
            case NAME -> appendName(profile, codePoint);
            case PHONE -> appendPhone(profile, codePoint);
            case NONE -> false;
        };
    }

    public boolean keyPressed(int keyCode, WhatsappState state) {
        if (keyCode == 256) { // ESC
            if (photoSheetOpen) {
                photoSheetOpen = false;
                return true;
            }

            if (activeField != Field.NONE) {
                activeField = Field.NONE;
                return true;
            }

            return false;
        }

        if (keyCode == 257 || keyCode == 335) { // ENTER
            if (activeField != Field.NONE) {
                activeField = Field.NONE;
                return true;
            }
            return false;
        }

        if (keyCode != 259 || photoSheetOpen) { // BACKSPACE
            return false;
        }

        WhatsappProfile profile = state.getProfile();

        return switch (activeField) {
            case ABOUT -> backspaceAbout(profile);
            case NAME -> backspaceName(profile);
            case PHONE -> backspacePhone(profile);
            case NONE -> false;
        };
    }

    public void resetTransientState() {
        activeField = Field.NONE;
        photoSheetOpen = false;
    }

    public boolean isPhotoSheetOpen() {
        return photoSheetOpen;
    }

    private void renderPhoto(PhoneScreen screen, GuiGraphics guiGraphics, WhatsappProfile profile) {
        int centerX = screen.getPhoneCenterX();
        int centerY = screen.getPhoneY() + PHOTO_CENTER_Y;

        guiGraphics.fill(
                centerX - PHOTO_RADIUS,
                centerY - PHOTO_RADIUS,
                centerX + PHOTO_RADIUS,
                centerY + PHOTO_RADIUS,
                PHOTO_COLOR
        );

        String initials = resolveInitials(profile.displayName());
        guiGraphics.drawCenteredString(
                screen.getPhoneFont(),
                initials,
                centerX,
                centerY - 4,
                PHOTO_TEXT_COLOR
        );
    }

    private void renderEditLabel(PhoneScreen screen, GuiGraphics guiGraphics, int mouseX, int mouseY) {
        String label = "Editar";
        int width = screen.getPhoneFont().width(label);
        int x = screen.getPhoneCenterX() - width / 2;
        int y = screen.getPhoneY() + EDIT_LABEL_Y;

        int color = isInsideEditLabel(screen, mouseX, mouseY) ? 0xFF7DD3FC : EDIT_LABEL_COLOR;

        guiGraphics.drawString(
                screen.getPhoneFont(),
                label,
                x,
                y,
                color,
                false
        );
    }

    private void renderFields(PhoneScreen screen, GuiGraphics guiGraphics, WhatsappProfile profile) {
        int width = screen.getPhoneWidth() - FIELD_BOX_WIDTH_OFFSET;

        renderField(
                screen,
                guiGraphics,
                "Info",
                profile.about(),
                "Escribí tu descripción",
                screen.getPhoneX() + FIELD_LABEL_X,
                screen.getPhoneY() + SECTION_START_Y,
                screen.getPhoneX() + FIELD_BOX_X,
                screen.getPhoneY() + SECTION_START_Y + 10,
                width,
                activeField == Field.ABOUT
        );

        renderField(
                screen,
                guiGraphics,
                "Nombre",
                profile.displayName(),
                "Escribí tu nombre",
                screen.getPhoneX() + FIELD_LABEL_X,
                screen.getPhoneY() + SECTION_START_Y + SECTION_GAP,
                screen.getPhoneX() + FIELD_BOX_X,
                screen.getPhoneY() + SECTION_START_Y + SECTION_GAP + 10,
                width,
                activeField == Field.NAME
        );

        renderField(
                screen,
                guiGraphics,
                "Número de teléfono",
                profile.phoneNumber(),
                "Escribí tu número",
                screen.getPhoneX() + FIELD_LABEL_X,
                screen.getPhoneY() + SECTION_START_Y + SECTION_GAP * 2,
                screen.getPhoneX() + FIELD_BOX_X,
                screen.getPhoneY() + SECTION_START_Y + SECTION_GAP * 2 + 10,
                width,
                activeField == Field.PHONE
        );
    }

    private void renderField(
            PhoneScreen screen,
            GuiGraphics guiGraphics,
            String label,
            String value,
            String placeholder,
            int labelX,
            int labelY,
            int boxX,
            int boxY,
            int boxWidth,
            boolean active
    ) {
        guiGraphics.drawString(
                screen.getPhoneFont(),
                label,
                labelX,
                labelY,
                COLOR_SECTION,
                false
        );

        guiGraphics.fill(
                boxX,
                boxY,
                boxX + boxWidth,
                boxY + FIELD_BOX_HEIGHT,
                active ? COLOR_FIELD_BG_ACTIVE : COLOR_FIELD_BG
        );

        boolean empty = value == null || value.isEmpty();
        guiGraphics.drawString(
                screen.getPhoneFont(),
                empty ? placeholder : value,
                boxX + FIELD_TEXT_PADDING_X,
                boxY + FIELD_TEXT_PADDING_Y,
                empty ? COLOR_HINT : COLOR_VALUE,
                false
        );
    }

    private void renderPhotoSheetOverlay(PhoneScreen screen, GuiGraphics guiGraphics) {
        guiGraphics.fill(
                screen.getPhoneX() + 4,
                screen.getPhoneY() + 4,
                screen.getPhoneX() + screen.getPhoneWidth() - 4,
                screen.getPhoneY() + screen.getPhoneHeight() - 4,
                COLOR_OVERLAY
        );
    }

    private void renderPhotoSheet(PhoneScreen screen, GuiGraphics guiGraphics, int mouseX, int mouseY, WhatsappProfile profile) {
        int x = getSheetX(screen);
        int y = getSheetY(screen);
        int w = getSheetWidth(screen);
        int h = getSheetHeight();

        guiGraphics.fill(x, y, x + w, y + h, COLOR_SHEET_BG);

        int handleX = x + (w - SHEET_HANDLE_WIDTH) / 2;
        int handleY = y + 4;
        guiGraphics.fill(handleX, handleY, handleX + SHEET_HANDLE_WIDTH, handleY + SHEET_HANDLE_HEIGHT, COLOR_SHEET_HANDLE);

        renderSheetOption(
                screen,
                guiGraphics,
                mouseX,
                mouseY,
                x,
                y + SHEET_TOP_PADDING + 8,
                w,
                "Tomar foto",
                true,
                isInsideTakePhoto(screen, mouseX, mouseY)
        );

        renderSheetSeparator(guiGraphics, x, y + SHEET_TOP_PADDING + 8 + SHEET_OPTION_HEIGHT, w);

        renderSheetOption(
                screen,
                guiGraphics,
                mouseX,
                mouseY,
                x,
                y + SHEET_TOP_PADDING + 8 + SHEET_OPTION_HEIGHT,
                w,
                "Seleccionar foto",
                true,
                isInsideSelectPhoto(screen, mouseX, mouseY)
        );

        renderSheetSeparator(guiGraphics, x, y + SHEET_TOP_PADDING + 8 + SHEET_OPTION_HEIGHT * 2, w);

        boolean canDelete = hasCustomPhoto(profile);
        renderSheetOption(
                screen,
                guiGraphics,
                mouseX,
                mouseY,
                x,
                y + SHEET_TOP_PADDING + 8 + SHEET_OPTION_HEIGHT * 2,
                w,
                "Eliminar foto",
                canDelete,
                isInsideDeletePhoto(screen, mouseX, mouseY)
        );
    }

    private void renderSheetOption(
            PhoneScreen screen,
            GuiGraphics guiGraphics,
            int mouseX,
            int mouseY,
            int x,
            int y,
            int w,
            String label,
            boolean enabled,
            boolean hover
    ) {
        if (hover && enabled) {
            guiGraphics.fill(x, y, x + w, y + SHEET_OPTION_HEIGHT, COLOR_SHEET_OPTION_HOVER);
        } else {
            guiGraphics.fill(x, y, x + w, y + SHEET_OPTION_HEIGHT, COLOR_SHEET_OPTION);
        }

        guiGraphics.drawCenteredString(
                screen.getPhoneFont(),
                label,
                x + w / 2,
                y + 5,
                enabled ? COLOR_SHEET_TEXT : COLOR_SHEET_DISABLED
        );
    }

    private void renderSheetSeparator(GuiGraphics guiGraphics, int x, int y, int w) {
        guiGraphics.fill(x + 8, y, x + w - 8, y + 1, COLOR_SHEET_SEPARATOR);
    }

    private boolean appendAbout(WhatsappProfile profile, char codePoint) {
        String current = profile.about();
        if (current.length() >= MAX_ABOUT_LENGTH) {
            return true;
        }

        profile.setAbout(current + codePoint);
        return true;
    }

    private boolean appendName(WhatsappProfile profile, char codePoint) {
        String current = profile.displayName();
        if (current.length() >= MAX_NAME_LENGTH) {
            return true;
        }

        profile.setDisplayName(current + codePoint);
        return true;
    }

    private boolean appendPhone(WhatsappProfile profile, char codePoint) {
        if (!Character.isDigit(codePoint) && codePoint != '+' && codePoint != ' ' && codePoint != '-') {
            return true;
        }

        String current = profile.phoneNumber();
        if (current.length() >= MAX_PHONE_LENGTH) {
            return true;
        }

        profile.setPhoneNumber(current + codePoint);
        return true;
    }

    private boolean backspaceAbout(WhatsappProfile profile) {
        String current = profile.about();
        if (current.isEmpty()) {
            return true;
        }

        profile.setAbout(current.substring(0, current.length() - 1));
        return true;
    }

    private boolean backspaceName(WhatsappProfile profile) {
        String current = profile.displayName();
        if (current.isEmpty()) {
            return true;
        }

        profile.setDisplayName(current.substring(0, current.length() - 1));
        return true;
    }

    private boolean backspacePhone(WhatsappProfile profile) {
        String current = profile.phoneNumber();
        if (current.isEmpty()) {
            return true;
        }

        profile.setPhoneNumber(current.substring(0, current.length() - 1));
        return true;
    }

    private String resolveInitials(String name) {
        if (name == null || name.isBlank()) {
            return "PP";
        }

        String[] parts = name.trim().split("\\s+");
        if (parts.length == 1) {
            String part = parts[0];
            return part.length() >= 2
                    ? part.substring(0, 2).toUpperCase()
                    : part.substring(0, 1).toUpperCase();
        }

        String first = parts[0].isEmpty() ? "" : parts[0].substring(0, 1);
        String second = parts[1].isEmpty() ? "" : parts[1].substring(0, 1);
        return (first + second).toUpperCase();
    }

    private boolean hasCustomPhoto(WhatsappProfile profile) {
        return profile != null
                && profile.photoId() != null
                && !profile.photoId().isBlank()
                && !WhatsappProfile.DEFAULT_PHOTO.equals(profile.photoId());
    }

    private int getSheetX(PhoneScreen screen) {
        return screen.getPhoneX() + SHEET_SIDE_MARGIN;
    }

    private int getSheetWidth(PhoneScreen screen) {
        return screen.getPhoneWidth() - SHEET_SIDE_MARGIN * 2;
    }

    private int getSheetHeight() {
        return SHEET_TOP_PADDING + 8 + SHEET_OPTION_HEIGHT * 3 + SHEET_BOTTOM_PADDING;
    }

    private int getSheetY(PhoneScreen screen) {
        return screen.getPhoneY() + screen.getPhoneHeight() - SHEET_BOTTOM_OFFSET - getSheetHeight();
    }

    private boolean isInsidePhotoArea(PhoneScreen screen, double mouseX, double mouseY) {
        int centerX = screen.getPhoneCenterX();
        int centerY = screen.getPhoneY() + PHOTO_CENTER_Y;

        return screen.isInside(
                mouseX,
                mouseY,
                centerX - PHOTO_RADIUS,
                centerY - PHOTO_RADIUS,
                PHOTO_RADIUS * 2,
                PHOTO_RADIUS * 2
        );
    }

    private boolean isInsideEditLabel(PhoneScreen screen, double mouseX, double mouseY) {
        String label = "Editar";
        int width = screen.getPhoneFont().width(label);
        int x = screen.getPhoneCenterX() - width / 2;
        int y = screen.getPhoneY() + EDIT_LABEL_Y;

        return screen.isInside(mouseX, mouseY, x, y, width, 10);
    }

    private boolean isInsideAboutField(PhoneScreen screen, double mouseX, double mouseY) {
        return isInsideField(screen, mouseX, mouseY, screen.getPhoneY() + SECTION_START_Y + 10);
    }

    private boolean isInsideNameField(PhoneScreen screen, double mouseX, double mouseY) {
        return isInsideField(screen, mouseX, mouseY, screen.getPhoneY() + SECTION_START_Y + SECTION_GAP + 10);
    }

    private boolean isInsidePhoneField(PhoneScreen screen, double mouseX, double mouseY) {
        return isInsideField(screen, mouseX, mouseY, screen.getPhoneY() + SECTION_START_Y + SECTION_GAP * 2 + 10);
    }

    private boolean isInsideField(PhoneScreen screen, double mouseX, double mouseY, int y) {
        return screen.isInside(
                mouseX,
                mouseY,
                screen.getPhoneX() + FIELD_BOX_X,
                y,
                screen.getPhoneWidth() - FIELD_BOX_WIDTH_OFFSET,
                FIELD_BOX_HEIGHT
        );
    }

    private boolean isInsidePhotoSheet(PhoneScreen screen, double mouseX, double mouseY) {
        return screen.isInside(
                mouseX,
                mouseY,
                getSheetX(screen),
                getSheetY(screen),
                getSheetWidth(screen),
                getSheetHeight()
        );
    }

    private boolean isInsideTakePhoto(PhoneScreen screen, double mouseX, double mouseY) {
        int x = getSheetX(screen);
        int y = getSheetY(screen) + SHEET_TOP_PADDING + 8;
        int w = getSheetWidth(screen);

        return screen.isInside(mouseX, mouseY, x, y, w, SHEET_OPTION_HEIGHT);
    }

    private boolean isInsideSelectPhoto(PhoneScreen screen, double mouseX, double mouseY) {
        int x = getSheetX(screen);
        int y = getSheetY(screen) + SHEET_TOP_PADDING + 8 + SHEET_OPTION_HEIGHT;
        int w = getSheetWidth(screen);

        return screen.isInside(mouseX, mouseY, x, y, w, SHEET_OPTION_HEIGHT);
    }

    private boolean isInsideDeletePhoto(PhoneScreen screen, double mouseX, double mouseY) {
        int x = getSheetX(screen);
        int y = getSheetY(screen) + SHEET_TOP_PADDING + 8 + SHEET_OPTION_HEIGHT * 2;
        int w = getSheetWidth(screen);

        return screen.isInside(mouseX, mouseY, x, y, w, SHEET_OPTION_HEIGHT);
    }

    private enum Field {
        NONE,
        ABOUT,
        NAME,
        PHONE
    }
}