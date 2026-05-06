package santi_moder.roleplaymod.client.phone.app.whatsapp;

import net.minecraft.client.gui.GuiGraphics;
import santi_moder.roleplaymod.client.phone.ui.PhoneThemeColors;
import santi_moder.roleplaymod.client.phone.ui.PhoneUi;
import santi_moder.roleplaymod.client.screen.PhoneScreen;
import santi_moder.roleplaymod.common.whatsapp.model.WhatsappProfile;
import santi_moder.roleplaymod.network.ModNetwork;
import santi_moder.roleplaymod.network.phone.whatsapp.WhatsappUpdateProfileC2SPacket;

public final class WhatsappProfileView {

    private static final int TITLE_Y = 30;

    private static final int PHOTO_CENTER_Y = 76;
    private static final int PHOTO_RADIUS = 22;
    private static final int EDIT_LABEL_Y = 102;

    private static final int SECTION_START_Y = 122;
    private static final int SECTION_GAP = 34;

    private static final int FIELD_LABEL_X = 16;
    private static final int FIELD_BOX_X = 16;
    private static final int FIELD_BOX_WIDTH_OFFSET = 32;
    private static final int FIELD_BOX_HEIGHT = 16;
    private static final int FIELD_TEXT_PADDING_X = 5;
    private static final int FIELD_TEXT_PADDING_Y = 4;

    private static final int SHEET_SIDE_MARGIN = 8;
    private static final int SHEET_BOTTOM_OFFSET = 34;
    private static final int SHEET_OPTION_HEIGHT = 18;
    private static final int SHEET_TOP_PADDING = 8;
    private static final int SHEET_BOTTOM_PADDING = 8;
    private static final int SHEET_HANDLE_WIDTH = 26;
    private static final int SHEET_HANDLE_HEIGHT = 3;

    private static final int MAX_ABOUT_LENGTH = 20;
    private static final int MAX_NAME_LENGTH = 30;

    private Field activeField = Field.NONE;
    private boolean photoSheetOpen = false;

    public void render(PhoneScreen screen, GuiGraphics guiGraphics, int mouseX, int mouseY, WhatsappState state) {
        guiGraphics.fill(
                screen.getPhoneX() + 4,
                screen.getPhoneY() + 4,
                screen.getPhoneX() + screen.getPhoneWidth() - 4,
                screen.getPhoneY() + screen.getPhoneHeight() - 4,
                PhoneThemeColors.appBackground(screen.getPhoneStack())
        );

        WhatsappProfile profile = state.getProfile();

        PhoneUi.drawCenteredText(
                screen,
                guiGraphics,
                "Perfil",
                screen.getPhoneCenterX(),
                screen.getPhoneY() + TITLE_Y,
                PhoneThemeColors.text(screen.getPhoneStack())
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


        if (activeField != Field.NONE) {
            saveProfileToServer(profile);
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
            case NONE -> false;
        };
    }

    public boolean keyPressed(int keyCode, WhatsappState state) {
        if (keyCode == 256) {
            if (photoSheetOpen) {
                photoSheetOpen = false;
                return true;
            }

            if (activeField != Field.NONE) {
                saveProfileToServer(state.getProfile());
                activeField = Field.NONE;
                return true;
            }

            return false;
        }

        if (keyCode == 257 || keyCode == 335) {
            if (activeField != Field.NONE) {
                saveProfileToServer(state.getProfile());

                activeField = Field.NONE;
                return true;
            }
            return false;
        }

        if (keyCode != 259 || photoSheetOpen) {
            return false;
        }

        WhatsappProfile profile = state.getProfile();

        return switch (activeField) {
            case ABOUT -> backspaceAbout(profile);
            case NAME -> backspaceName(profile);
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

        WhatsappTextureResolver.drawProfilePhoto(
                guiGraphics,
                profile.photoId(),
                centerX - PHOTO_RADIUS,
                centerY - PHOTO_RADIUS,
                PHOTO_RADIUS * 2
        );
    }

    private void renderEditLabel(PhoneScreen screen, GuiGraphics guiGraphics, int mouseX, int mouseY) {
        String label = "Editar";
        int width = screen.getPhoneFont().width(label);
        int x = screen.getPhoneCenterX() - width / 2;
        int y = screen.getPhoneY() + EDIT_LABEL_Y;

        int color = isInsideEditLabel(screen, mouseX, mouseY)
                ? PhoneThemeColors.successHover(screen.getPhoneStack())
                : PhoneThemeColors.success(screen.getPhoneStack());

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

        renderReadonlyField(
                screen,
                guiGraphics,
                "Número de teléfono",
                profile.phoneNumber(),
                "Sin número",
                screen.getPhoneX() + FIELD_LABEL_X,
                screen.getPhoneY() + SECTION_START_Y + SECTION_GAP * 2,
                screen.getPhoneX() + FIELD_BOX_X,
                screen.getPhoneY() + SECTION_START_Y + SECTION_GAP * 2 + 10,
                width
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
                PhoneThemeColors.subtext(screen.getPhoneStack()),
                false
        );

        guiGraphics.fill(
                boxX,
                boxY,
                boxX + boxWidth,
                boxY + FIELD_BOX_HEIGHT,
                active ? PhoneThemeColors.inputActive(screen.getPhoneStack()) : PhoneThemeColors.input(screen.getPhoneStack())
        );

        boolean empty = value == null || value.isEmpty();

        guiGraphics.drawString(
                screen.getPhoneFont(),
                shortenToFit(screen, empty ? placeholder : value, boxWidth - FIELD_TEXT_PADDING_X * 2),
                boxX + FIELD_TEXT_PADDING_X,
                boxY + FIELD_TEXT_PADDING_Y,
                empty ? PhoneThemeColors.hint(screen.getPhoneStack()) : PhoneThemeColors.text(screen.getPhoneStack()),
                false
        );
    }

    private void renderReadonlyField(
            PhoneScreen screen,
            GuiGraphics guiGraphics,
            String label,
            String value,
            String placeholder,
            int labelX,
            int labelY,
            int boxX,
            int boxY,
            int boxWidth
    ) {
        guiGraphics.drawString(
                screen.getPhoneFont(),
                label,
                labelX,
                labelY,
                PhoneThemeColors.subtext(screen.getPhoneStack()),
                false
        );

        guiGraphics.fill(
                boxX,
                boxY,
                boxX + boxWidth,
                boxY + FIELD_BOX_HEIGHT,
                PhoneThemeColors.disabledInput(screen.getPhoneStack())
        );

        boolean empty = value == null || value.isEmpty();

        guiGraphics.drawString(
                screen.getPhoneFont(),
                shortenToFit(screen, empty ? placeholder : value, boxWidth - FIELD_TEXT_PADDING_X * 2),
                boxX + FIELD_TEXT_PADDING_X,
                boxY + FIELD_TEXT_PADDING_Y,
                empty
                        ? PhoneThemeColors.hint(screen.getPhoneStack())
                        : PhoneThemeColors.subtext(screen.getPhoneStack()),
                false
        );
    }

    private String shortenToFit(PhoneScreen screen, String value, int maxWidth) {
        if (value == null || value.isEmpty()) {
            return "";
        }

        if (screen.getPhoneFont().width(value) <= maxWidth) {
            return value;
        }

        String ellipsis = "…";
        String shortened = value;

        while (!shortened.isEmpty()
                && screen.getPhoneFont().width(shortened + ellipsis) > maxWidth) {
            shortened = shortened.substring(0, shortened.length() - 1);
        }

        return shortened + ellipsis;
    }

    private void renderPhotoSheetOverlay(PhoneScreen screen, GuiGraphics guiGraphics) {
        guiGraphics.fill(
                screen.getPhoneX() + 4,
                screen.getPhoneY() + 4,
                screen.getPhoneX() + screen.getPhoneWidth() - 4,
                screen.getPhoneY() + screen.getPhoneHeight() - 4,
                PhoneThemeColors.overlay()
        );
    }

    private void renderPhotoSheet(PhoneScreen screen, GuiGraphics guiGraphics, int mouseX, int mouseY, WhatsappProfile profile) {
        int x = getSheetX(screen);
        int y = getSheetY(screen);
        int w = getSheetWidth(screen);
        int h = getSheetHeight();

        guiGraphics.fill(x, y, x + w, y + h, PhoneThemeColors.sheet(screen.getPhoneStack()));

        int handleX = x + (w - SHEET_HANDLE_WIDTH) / 2;
        int handleY = y + 4;

        guiGraphics.fill(
                handleX,
                handleY,
                handleX + SHEET_HANDLE_WIDTH,
                handleY + SHEET_HANDLE_HEIGHT,
                PhoneThemeColors.divider(screen.getPhoneStack())
        );

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

        renderSheetSeparator(
                screen,
                guiGraphics,
                x,
                y + SHEET_TOP_PADDING + 8 + SHEET_OPTION_HEIGHT,
                w
        );

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

        renderSheetSeparator(
                screen,
                guiGraphics,
                x,
                y + SHEET_TOP_PADDING + 8 + SHEET_OPTION_HEIGHT * 2,
                w
        );

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
            guiGraphics.fill(
                    x,
                    y,
                    x + w,
                    y + SHEET_OPTION_HEIGHT,
                    PhoneThemeColors.cardHover(screen.getPhoneStack())
            );
        } else {
            guiGraphics.fill(
                    x,
                    y,
                    x + w,
                    y + SHEET_OPTION_HEIGHT,
                    0x00000000
            );
        }

        PhoneUi.drawCenteredText(
                screen,
                guiGraphics,
                label,
                x + w / 2,
                y + 5,
                enabled ? PhoneThemeColors.text(screen.getPhoneStack()) : PhoneThemeColors.hint(screen.getPhoneStack())
        );
    }

    private void renderSheetSeparator(PhoneScreen screen, GuiGraphics guiGraphics, int x, int y, int w) {
        guiGraphics.fill(
                x + 8,
                y,
                x + w - 8,
                y + 1,
                PhoneThemeColors.divider(screen.getPhoneStack())
        );
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

    private void saveProfileToServer(WhatsappProfile profile) {
        if (profile == null) {
            return;
        }

        ModNetwork.sendWhatsappToServer(
                new WhatsappUpdateProfileC2SPacket(
                        profile.displayName(),
                        profile.about()
                )
        );
    }

    private boolean hasCustomPhoto(WhatsappProfile profile) {
        return profile != null
                && profile.photoId() != null
                && !profile.photoId().isBlank()
                && !WhatsappProfile.isDefaultPhoto(profile.photoId());
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
        NAME
    }
}