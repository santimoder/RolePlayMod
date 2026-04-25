package santi_moder.roleplaymod.client.phone.app;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import org.lwjgl.glfw.GLFW;
import santi_moder.roleplaymod.client.phone.ui.PhoneUi;
import santi_moder.roleplaymod.client.phone.ui.component.PhoneBirthdateKeyboard;
import santi_moder.roleplaymod.client.phone.ui.component.PhoneNumericKeypad;
import santi_moder.roleplaymod.client.phone.ui.component.PhoneTextKeyboard;
import santi_moder.roleplaymod.client.phone.ui.render.PhoneWallpaperCatalog;
import santi_moder.roleplaymod.client.phone.ui.render.PhoneWallpaperRenderer;
import santi_moder.roleplaymod.client.screen.PhoneScreen;
import santi_moder.roleplaymod.common.phone.PhoneAppId;
import santi_moder.roleplaymod.common.phone.PhoneData;

import java.time.DateTimeException;
import java.time.LocalDate;
import java.util.List;

public class SettingsPhoneApp extends AbstractPhoneApp {

    private static final int PIN_LENGTH = 4;
    private static final int SECURITY_ERROR_TICKS = 40;
    private static final int EDITOR_ERROR_TICKS = 30;
    private static final int EDITOR_MAX_TEXT_LENGTH = 18;
    private static final int EDITOR_BIRTHDATE_LENGTH = 10;

    private static final int WALLPAPER_PREVIEW_W = 46;
    private static final int WALLPAPER_PREVIEW_H = 82;

    private static final int WALLPAPER_OPTION_W = 34;
    private static final int WALLPAPER_OPTION_H = 56;
    private static final int WALLPAPER_OPTION_GAP = 8;

    private static final int WALLPAPER_PANEL_COLOR = 0x66111111;
    private static final int WALLPAPER_SELECTED_COLOR = 0x99FFFFFF;
    private static final int WALLPAPER_DISABLED_COLOR = 0x55333333;

    private SettingsCategory currentCategory = SettingsCategory.HOME;

    private SecurityFlowState securityState = SecurityFlowState.NONE;
    private String enteredCode = "";
    private String pendingNewCode = "";
    private int securityErrorTicks = 0;

    private EditorMode editorMode = EditorMode.NONE;
    private String editorBuffer = "";
    private int editorErrorTicks = 0;

    public SettingsPhoneApp() {
        super(PhoneAppId.SETTINGS);
    }

    @Override
    public void onOpen(PhoneScreen screen, ItemStack phoneStack) {
        resetAllState();
    }

    private void resetAllState() {
        currentCategory = SettingsCategory.HOME;

        securityState = SecurityFlowState.NONE;
        enteredCode = "";
        pendingNewCode = "";
        securityErrorTicks = 0;

        editorMode = EditorMode.NONE;
        editorBuffer = "";
        editorErrorTicks = 0;
    }

    @Override
    public void render(PhoneScreen screen, GuiGraphics guiGraphics, int mouseX, int mouseY, float partialTick) {
        PhoneUi.drawHeaderTitle(screen, guiGraphics, getCurrentTitle());

        if (canGoBack()) {
            PhoneUi.drawBackButton(screen, guiGraphics, mouseX, mouseY);
        }

        if (isEditorOpen()) {
            renderEditor(screen, guiGraphics, mouseX, mouseY);
            return;
        }

        ItemStack stack = screen.getPhoneStack();

        switch (currentCategory) {
            case HOME -> renderHome(screen, guiGraphics, mouseX, mouseY);
            case PROFILE -> renderProfile(screen, guiGraphics, mouseX, mouseY, stack);
            case WALLPAPER -> renderWallpaper(screen, guiGraphics, mouseX, mouseY, stack);
            case DISPLAY -> renderDisplay(screen, guiGraphics, mouseX, mouseY, stack);
            case NOTIFICATIONS -> renderNotifications(screen, guiGraphics);
            case SOUND -> renderSound(screen, guiGraphics, mouseX, mouseY, stack);
            case SECURITY -> renderSecurity(screen, guiGraphics, mouseX, mouseY, stack);
        }
    }

    private void renderHome(PhoneScreen screen, GuiGraphics guiGraphics, int mouseX, int mouseY) {
        List<ListEntry> entries = List.of(
                new ListEntry("Perfil", SettingsCategory.PROFILE),
                new ListEntry("Fondo de pantalla", SettingsCategory.WALLPAPER),
                new ListEntry("Pantalla y brillo", SettingsCategory.DISPLAY),
                new ListEntry("Notificaciones", SettingsCategory.NOTIFICATIONS),
                new ListEntry("Sonido", SettingsCategory.SOUND),
                new ListEntry("Face ID y codigo", SettingsCategory.SECURITY)
        );

        int x = screen.getPhoneX() + 12;
        int y = screen.getPhoneY() + 52;
        int w = screen.getPhoneWidth() - 24;

        for (int i = 0; i < entries.size(); i++) {
            int buttonY = y + i * (PhoneUi.LIST_BUTTON_H + PhoneUi.LIST_BUTTON_GAP);
            PhoneUi.drawListButton(screen, guiGraphics, mouseX, mouseY, x, buttonY, w, entries.get(i).label());
        }
    }

    private void renderProfile(PhoneScreen screen, GuiGraphics guiGraphics, int mouseX, int mouseY, ItemStack stack) {
        PhoneUi.drawPanel(screen, guiGraphics);

        int left = screen.getPhoneX() + PhoneUi.CONTENT_LEFT_X;
        int y = screen.getPhoneY() + PhoneUi.CONTENT_TOP_Y;

        PhoneUi.drawField(screen, guiGraphics, "Nombre:", PhoneUi.valueOrEmpty(PhoneData.getProfileName(stack)), left, y);
        PhoneUi.drawField(screen, guiGraphics, "Apellido:", PhoneUi.valueOrEmpty(PhoneData.getProfileSurname(stack)), left, y + 34);
        PhoneUi.drawField(screen, guiGraphics, "Nacimiento:", PhoneUi.valueOrEmpty(PhoneData.getProfileBirthdate(stack)), left, y + 68);
        PhoneUi.drawField(screen, guiGraphics, "Foto:", PhoneData.getProfilePhoto(stack), left, y + 102);

        PhoneUi.drawActionButton(screen, guiGraphics, mouseX, mouseY, left, y + 132, "Editar nombre");
        PhoneUi.drawActionButton(screen, guiGraphics, mouseX, mouseY, left, y + 154, "Editar apellido");
        PhoneUi.drawActionButton(screen, guiGraphics, mouseX, mouseY, left, y + 176, "Editar fecha");
        PhoneUi.drawActionButton(screen, guiGraphics, mouseX, mouseY, left, y + 198, "Cambiar foto");
    }

    private void renderWallpaper(PhoneScreen screen, GuiGraphics guiGraphics, int mouseX, int mouseY, ItemStack stack) {
        PhoneUi.drawPanel(screen, guiGraphics);

        int left = screen.getPhoneX() + 10;
        int top = screen.getPhoneY() + 48;

        guiGraphics.drawString(screen.getPhoneFont(), "Vista previa", left, top, PhoneUi.COLOR_TEXT, false);

        int previewY = top + 10;
        int previewGap = 14;
        int previewTotalWidth = WALLPAPER_PREVIEW_W * 2 + previewGap;
        int previewStartX = screen.getPhoneCenterX() - previewTotalWidth / 2;

        int lockX = previewStartX;
        guiGraphics.fill(lockX - 2, previewY - 2, lockX + WALLPAPER_PREVIEW_W + 2, previewY + WALLPAPER_PREVIEW_H + 2, WALLPAPER_PANEL_COLOR);
        PhoneWallpaperRenderer.renderPreviewLock(guiGraphics, stack, lockX, previewY, WALLPAPER_PREVIEW_W, WALLPAPER_PREVIEW_H);
        guiGraphics.drawCenteredString(screen.getPhoneFont(), "Bloqueo", lockX + WALLPAPER_PREVIEW_W / 2, previewY + WALLPAPER_PREVIEW_H + 6, PhoneUi.COLOR_SUBTEXT);

        int homeX = previewStartX + WALLPAPER_PREVIEW_W + previewGap;
        guiGraphics.fill(homeX - 2, previewY - 2, homeX + WALLPAPER_PREVIEW_W + 2, previewY + WALLPAPER_PREVIEW_H + 2, WALLPAPER_PANEL_COLOR);
        PhoneWallpaperRenderer.renderPreviewHome(guiGraphics, stack, homeX, previewY, WALLPAPER_PREVIEW_W, WALLPAPER_PREVIEW_H);
        guiGraphics.drawCenteredString(screen.getPhoneFont(), "Inicio", homeX + WALLPAPER_PREVIEW_W / 2, previewY + WALLPAPER_PREVIEW_H + 6, PhoneUi.COLOR_SUBTEXT);

        int optionsY = previewY + WALLPAPER_PREVIEW_H + 22;
        guiGraphics.drawString(screen.getPhoneFont(), "Fondos", left, optionsY, PhoneUi.COLOR_TEXT, false);

        int optionStartY = optionsY + 10;
        int optionCount = PhoneWallpaperCatalog.selectable().size();
        int optionTotalWidth = optionCount * WALLPAPER_OPTION_W + (optionCount - 1) * WALLPAPER_OPTION_GAP;
        int optionStartX = screen.getPhoneCenterX() - optionTotalWidth / 2;

        renderWallpaperOptions(screen, guiGraphics, stack, mouseX, mouseY, optionStartX, optionStartY);

        int customY = optionsY + 80;
        renderCustomWallpaperPlaceholder(screen, guiGraphics, mouseX, mouseY, left, customY);
    }

    private void renderWallpaperOptions(
            PhoneScreen screen,
            GuiGraphics guiGraphics,
            ItemStack stack,
            int mouseX,
            int mouseY,
            int startX,
            int startY
    ) {
        List<PhoneWallpaperCatalog.WallpaperOption> options = PhoneWallpaperCatalog.selectable();
        String current = PhoneData.getWallpaper(stack);

        for (int i = 0; i < options.size(); i++) {
            PhoneWallpaperCatalog.WallpaperOption option = options.get(i);

            int x = startX + i * (WALLPAPER_OPTION_W + WALLPAPER_OPTION_GAP);
            int y = startY;

            boolean hover = screen.isInside(mouseX, mouseY, x, y, WALLPAPER_OPTION_W, WALLPAPER_OPTION_H);
            boolean selected = option.id().equals(current);

            int borderColor = selected ? WALLPAPER_SELECTED_COLOR : (hover ? 0x88FFFFFF : WALLPAPER_PANEL_COLOR);
            guiGraphics.fill(x - 2, y - 2, x + WALLPAPER_OPTION_W + 2, y + WALLPAPER_OPTION_H + 2, borderColor);

            PhoneWallpaperRenderer.renderPreviewWallpaperOnly(
                    guiGraphics,
                    stackForPreview(stack, option.id()),
                    x,
                    y,
                    WALLPAPER_OPTION_W,
                    WALLPAPER_OPTION_H - 12
            );

            guiGraphics.drawCenteredString(
                    screen.getPhoneFont(),
                    option.displayName(),
                    x + WALLPAPER_OPTION_W / 2,
                    y + WALLPAPER_OPTION_H - 8,
                    PhoneUi.COLOR_SUBTEXT
            );
        }
    }

    private void renderCustomWallpaperPlaceholder(
            PhoneScreen screen,
            GuiGraphics guiGraphics,
            int mouseX,
            int mouseY,
            int x,
            int y
    ) {
        int w = screen.getPhoneWidth() - 20;
        int h = 22;

        boolean hover = screen.isInside(mouseX, mouseY, x, y, w, h);

        guiGraphics.fill(x, y, x + w, y + h, hover ? 0x66444444 : WALLPAPER_DISABLED_COLOR);

        guiGraphics.drawString(
                screen.getPhoneFont(),
                "Agrega tu fondo",
                x + 8,
                y + 7,
                0xFFB8B8B8,
                false
        );
    }

    private void renderDisplay(PhoneScreen screen, GuiGraphics guiGraphics, int mouseX, int mouseY, ItemStack stack) {
        PhoneUi.drawPanel(screen, guiGraphics);

        int left = screen.getPhoneX() + PhoneUi.CONTENT_LEFT_X;
        int y = screen.getPhoneY() + PhoneUi.CONTENT_TOP_Y;

        PhoneUi.drawField(screen, guiGraphics, "Tema:", PhoneData.getThemeMode(stack), left, y);
        PhoneUi.drawField(screen, guiGraphics, "Tamano de letra:", PhoneData.getTextSize(stack), left, y + 38);
        PhoneUi.drawField(screen, guiGraphics, "Tamano de iconos:", PhoneData.getIconSize(stack), left, y + 76);

        PhoneUi.drawActionButton(screen, guiGraphics, mouseX, mouseY, left, y + 112, "Cambiar tema");
        PhoneUi.drawActionButton(screen, guiGraphics, mouseX, mouseY, left, y + 134, "Tamano letra");
        PhoneUi.drawActionButton(screen, guiGraphics, mouseX, mouseY, left, y + 156, "Tamano iconos");
    }

    private void renderNotifications(PhoneScreen screen, GuiGraphics guiGraphics) {
        PhoneUi.drawCenteredLines(
                screen,
                guiGraphics,
                screen.getPhoneY() + 62,
                14,
                "Sin contenido por ahora",
                "Aca luego vas a elegir",
                "que apps muestran avisos"
        );
    }

    private void renderSound(PhoneScreen screen, GuiGraphics guiGraphics, int mouseX, int mouseY, ItemStack stack) {
        PhoneUi.drawPanel(screen, guiGraphics);

        int left = screen.getPhoneX() + PhoneUi.CONTENT_LEFT_X;
        int y = screen.getPhoneY() + PhoneUi.CONTENT_TOP_Y;

        PhoneUi.drawField(screen, guiGraphics, "Volumen llamadas:", PhoneData.getCallVolume(stack) + "%", left, y);
        PhoneUi.drawField(screen, guiGraphics, "Volumen notificaciones:", PhoneData.getNotificationVolume(stack) + "%", left, y + 38);
        PhoneUi.drawField(screen, guiGraphics, "Modo silencio:", PhoneData.isSilentMode(stack) ? "Activado" : "Desactivado", left, y + 76);

        PhoneUi.drawActionButton(screen, guiGraphics, mouseX, mouseY, left, y + 112, "Subir llamadas");
        PhoneUi.drawActionButton(screen, guiGraphics, mouseX, mouseY, left, y + 134, "Subir avisos");
        PhoneUi.drawActionButton(screen, guiGraphics, mouseX, mouseY, left, y + 156, "Modo silencio");
    }

    private void renderSecurity(PhoneScreen screen, GuiGraphics guiGraphics, int mouseX, int mouseY, ItemStack stack) {
        if (isSecurityPadOpen()) {
            renderSecurityPad(screen, guiGraphics, mouseX, mouseY, stack);
            return;
        }

        PhoneUi.drawPanel(screen, guiGraphics);

        int left = screen.getPhoneX() + PhoneUi.CONTENT_LEFT_X;
        int y = screen.getPhoneY() + PhoneUi.CONTENT_TOP_Y;

        PhoneUi.drawField(screen, guiGraphics, "Seguridad:", PhoneData.getSecuritySummary(stack), left, y);
        PhoneUi.drawField(screen, guiGraphics, "Face ID:", PhoneData.getFaceIdName(stack), left, y + 34);

        String passLabel = PhoneData.hasPassword(stack) ? "Desactivar codigo" : "Activar codigo";
        PhoneUi.drawActionButton(screen, guiGraphics, mouseX, mouseY, left, y + 72, passLabel);

        if (PhoneData.hasPassword(stack)) {
            PhoneUi.drawActionButton(screen, guiGraphics, mouseX, mouseY, left, y + 94, "Cambiar codigo");

            String faceIdLabel = PhoneData.hasFaceId(stack)
                    ? "Restablecer Face ID"
                    : "Activar Face ID";
            PhoneUi.drawActionButton(screen, guiGraphics, mouseX, mouseY, left, y + 116, faceIdLabel);
        }
}

    private void renderSecurityPad(PhoneScreen screen, GuiGraphics guiGraphics, int mouseX, int mouseY, ItemStack stack) {
        PhoneNumericKeypad keypad = PhoneNumericKeypad.defaultPinPad(screen);

        guiGraphics.drawCenteredString(
                screen.getPhoneFont(),
                getSecurityTitle(),
                screen.getPhoneCenterX(),
                screen.getPhoneY() + 34,
                PhoneUi.COLOR_TEXT
        );

        guiGraphics.drawCenteredString(
                screen.getPhoneFont(),
                getSecuritySubtitle(stack),
                screen.getPhoneCenterX(),
                screen.getPhoneY() + 48,
                PhoneUi.COLOR_SUBTEXT
        );

        guiGraphics.drawCenteredString(
                screen.getPhoneFont(),
                PhoneUi.buildDots(enteredCode, PIN_LENGTH),
                screen.getPhoneCenterX(),
                screen.getPhoneY() + 68,
                PhoneUi.COLOR_TEXT
        );

        if (securityErrorTicks > 0) {
            guiGraphics.drawCenteredString(
                    screen.getPhoneFont(),
                    "Codigo invalido",
                    screen.getPhoneCenterX(),
                    screen.getPhoneY() + 82,
                    PhoneUi.COLOR_ERROR
            );
            securityErrorTicks--;
        }

        keypad.render(screen, guiGraphics, mouseX, mouseY);
    }

    private void renderEditor(PhoneScreen screen, GuiGraphics guiGraphics, int mouseX, int mouseY) {
        PhoneUi.drawDarkPanel(screen, guiGraphics);

        guiGraphics.drawCenteredString(
                screen.getPhoneFont(),
                getEditorSubtitle(),
                screen.getPhoneCenterX(),
                screen.getPhoneY() + 52,
                PhoneUi.COLOR_SUBTEXT
        );

        PhoneUi.drawTextInput(
                screen,
                guiGraphics,
                screen.getPhoneX() + 14,
                screen.getPhoneY() + 66,
                screen.getPhoneX() + screen.getPhoneWidth() - 14,
                screen.getPhoneY() + 82,
                editorBuffer
        );

        if (editorErrorTicks > 0) {
            guiGraphics.drawCenteredString(
                    screen.getPhoneFont(),
                    getEditorErrorMessage(),
                    screen.getPhoneCenterX(),
                    screen.getPhoneY() + 88,
                    PhoneUi.COLOR_ERROR
            );
            editorErrorTicks--;
        }

        if (editorMode == EditorMode.PROFILE_BIRTHDATE) {
            PhoneBirthdateKeyboard.defaultKeyboard(screen).render(screen, guiGraphics, mouseX, mouseY);
        } else {
            PhoneTextKeyboard.defaultKeyboard(screen).render(screen, guiGraphics, mouseX, mouseY);
        }
    }

    private ItemStack stackForPreview(ItemStack original, String wallpaperId) {
        ItemStack preview = original.copy();
        PhoneData.setWallpaper(preview, wallpaperId);
        return preview;
    }

    @Override
    public boolean mouseClicked(PhoneScreen screen, double mouseX, double mouseY, int button) {
        if (button != 0) {
            return false;
        }

        if (canGoBack() && PhoneUi.isBackButtonClicked(screen, mouseX, mouseY)) {
            handleBack(screen);
            return true;
        }

        ItemStack stack = screen.getPhoneStack();
        if (stack.isEmpty()) {
            return false;
        }

        if (isEditorOpen()) {
            return handleEditorClick(screen, mouseX, mouseY, stack);
        }

        return switch (currentCategory) {
            case HOME -> handleHomeClick(screen, mouseX, mouseY);
            case PROFILE -> handleProfileClick(screen, mouseX, mouseY, stack);
            case WALLPAPER -> handleWallpaperClick(screen, mouseX, mouseY, stack);
            case DISPLAY -> handleDisplayClick(screen, mouseX, mouseY, stack);
            case NOTIFICATIONS -> false;
            case SOUND -> handleSoundClick(screen, mouseX, mouseY, stack);
            case SECURITY -> handleSecurityClick(screen, mouseX, mouseY, stack);
        };
    }

    @Override
    public boolean keyPressed(PhoneScreen screen, int keyCode, int scanCode, int modifiers) {
        if (isSecurityPadOpen()) {
            if (keyCode >= GLFW.GLFW_KEY_0 && keyCode <= GLFW.GLFW_KEY_9) {
                handleSecurityInput(screen, String.valueOf((char) ('0' + (keyCode - GLFW.GLFW_KEY_0))));
                return true;
            }

            if (keyCode >= GLFW.GLFW_KEY_KP_0 && keyCode <= GLFW.GLFW_KEY_KP_9) {
                handleSecurityInput(screen, String.valueOf(keyCode - GLFW.GLFW_KEY_KP_0));
                return true;
            }

            if (keyCode == GLFW.GLFW_KEY_BACKSPACE) {
                if (!enteredCode.isEmpty()) {
                    enteredCode = enteredCode.substring(0, enteredCode.length() - 1);
                }
                return true;
            }

            if (keyCode == GLFW.GLFW_KEY_ENTER || keyCode == GLFW.GLFW_KEY_KP_ENTER) {
                submitSecurityCode(screen);
                return true;
            }

            if (keyCode == GLFW.GLFW_KEY_ESCAPE) {
                handleBack(screen);
                return true;
            }
        }

        if (isEditorOpen()) {
            if (keyCode == GLFW.GLFW_KEY_BACKSPACE) {
                if (!editorBuffer.isEmpty()) {
                    editorBuffer = editorBuffer.substring(0, editorBuffer.length() - 1);
                }
                return true;
            }

            if (keyCode == GLFW.GLFW_KEY_ENTER || keyCode == GLFW.GLFW_KEY_KP_ENTER) {
                submitEditor(screen.getPhoneStack());
                return true;
            }

            if (keyCode == GLFW.GLFW_KEY_ESCAPE) {
                handleBack(screen);
                return true;
            }
        }

        return false;
    }

    private boolean handleWallpaperClick(PhoneScreen screen, double mouseX, double mouseY, ItemStack stack) {
        int startX = screen.getPhoneX() + 10;
        int startY = screen.getPhoneY() + 48 + 10 + WALLPAPER_PREVIEW_H + 22 + 10;

        List<PhoneWallpaperCatalog.WallpaperOption> options = PhoneWallpaperCatalog.selectable();

        for (int i = 0; i < options.size(); i++) {
            PhoneWallpaperCatalog.WallpaperOption option = options.get(i);

            int x = startX + i * (WALLPAPER_OPTION_W + WALLPAPER_OPTION_GAP);
            int y = startY;

            if (screen.isInside(mouseX, mouseY, x, y, WALLPAPER_OPTION_W, WALLPAPER_OPTION_H)) {
                PhoneData.setWallpaper(stack, option.id());
                return true;
            }
        }

        return false;
    }

    private boolean handleHomeClick(PhoneScreen screen, double mouseX, double mouseY) {
        List<SettingsCategory> categories = List.of(
                SettingsCategory.PROFILE,
                SettingsCategory.WALLPAPER,
                SettingsCategory.DISPLAY,
                SettingsCategory.NOTIFICATIONS,
                SettingsCategory.SOUND,
                SettingsCategory.SECURITY
        );

        int x = screen.getPhoneX() + 12;
        int y = screen.getPhoneY() + 52;
        int w = screen.getPhoneWidth() - 24;

        for (int i = 0; i < categories.size(); i++) {
            int buttonY = y + i * (PhoneUi.LIST_BUTTON_H + PhoneUi.LIST_BUTTON_GAP);

            if (screen.isInside(mouseX, mouseY, x, buttonY, w, PhoneUi.LIST_BUTTON_H)) {
                currentCategory = categories.get(i);

                if (currentCategory == SettingsCategory.SECURITY && PhoneData.hasPassword(screen.getPhoneStack())) {
                    beginSecurityState(SecurityFlowState.VERIFY_TO_ENTER);
                } else {
                    beginSecurityState(SecurityFlowState.NONE);
                }

                return true;
            }
        }

        return false;
    }

    private boolean handleProfileClick(PhoneScreen screen, double mouseX, double mouseY, ItemStack stack) {
        int left = screen.getPhoneX() + PhoneUi.CONTENT_LEFT_X;
        int y = screen.getPhoneY() + PhoneUi.CONTENT_TOP_Y;

        if (screen.isInside(mouseX, mouseY, left, y + 132, PhoneUi.ACTION_BUTTON_W, PhoneUi.ACTION_BUTTON_H)) {
            openEditor(EditorMode.PROFILE_NAME, PhoneData.getProfileName(stack));
            return true;
        }

        if (screen.isInside(mouseX, mouseY, left, y + 154, PhoneUi.ACTION_BUTTON_W, PhoneUi.ACTION_BUTTON_H)) {
            openEditor(EditorMode.PROFILE_SURNAME, PhoneData.getProfileSurname(stack));
            return true;
        }

        if (screen.isInside(mouseX, mouseY, left, y + 176, PhoneUi.ACTION_BUTTON_W, PhoneUi.ACTION_BUTTON_H)) {
            openEditor(EditorMode.PROFILE_BIRTHDATE, PhoneData.getProfileBirthdate(stack));
            return true;
        }

        if (screen.isInside(mouseX, mouseY, left, y + 198, PhoneUi.ACTION_BUTTON_W, PhoneUi.ACTION_BUTTON_H)) {
            PhoneData.cycleProfilePhoto(stack);
            return true;
        }

        return false;
    }

    private boolean handleDisplayClick(PhoneScreen screen, double mouseX, double mouseY, ItemStack stack) {
        int left = screen.getPhoneX() + PhoneUi.CONTENT_LEFT_X;
        int y = screen.getPhoneY() + PhoneUi.CONTENT_TOP_Y;

        if (screen.isInside(mouseX, mouseY, left, y + 112, PhoneUi.ACTION_BUTTON_W, PhoneUi.ACTION_BUTTON_H)) {
            PhoneData.toggleThemeMode(stack);
            return true;
        }

        if (screen.isInside(mouseX, mouseY, left, y + 134, PhoneUi.ACTION_BUTTON_W, PhoneUi.ACTION_BUTTON_H)) {
            PhoneData.cycleTextSize(stack);
            return true;
        }

        if (screen.isInside(mouseX, mouseY, left, y + 156, PhoneUi.ACTION_BUTTON_W, PhoneUi.ACTION_BUTTON_H)) {
            PhoneData.cycleIconSize(stack);
            return true;
        }

        return false;
    }

    private boolean handleSoundClick(PhoneScreen screen, double mouseX, double mouseY, ItemStack stack) {
        int left = screen.getPhoneX() + PhoneUi.CONTENT_LEFT_X;
        int y = screen.getPhoneY() + PhoneUi.CONTENT_TOP_Y;

        if (screen.isInside(mouseX, mouseY, left, y + 112, PhoneUi.ACTION_BUTTON_W, PhoneUi.ACTION_BUTTON_H)) {
            PhoneData.cycleCallVolume(stack);
            return true;
        }

        if (screen.isInside(mouseX, mouseY, left, y + 134, PhoneUi.ACTION_BUTTON_W, PhoneUi.ACTION_BUTTON_H)) {
            PhoneData.cycleNotificationVolume(stack);
            return true;
        }

        if (screen.isInside(mouseX, mouseY, left, y + 156, PhoneUi.ACTION_BUTTON_W, PhoneUi.ACTION_BUTTON_H)) {
            PhoneData.toggleSilentMode(stack);
            return true;
        }

        return false;
    }

    private boolean handleSecurityClick(PhoneScreen screen, double mouseX, double mouseY, ItemStack stack) {
        if (isSecurityPadOpen()) {
            String key = PhoneNumericKeypad.defaultPinPad(screen).getKeyAt(screen, mouseX, mouseY);
            if (key != null) {
                handleSecurityInput(screen, key);
                return true;
            }
            return false;
        }

        int left = screen.getPhoneX() + PhoneUi.CONTENT_LEFT_X;
        int y = screen.getPhoneY() + PhoneUi.CONTENT_TOP_Y;

        if (screen.isInside(mouseX, mouseY, left, y + 72, PhoneUi.ACTION_BUTTON_W, PhoneUi.ACTION_BUTTON_H)) {
            if (PhoneData.hasPassword(stack)) {
                beginSecurityState(SecurityFlowState.VERIFY_TO_DISABLE);
            } else {
                beginSecurityState(SecurityFlowState.CREATE_NEW_CODE);
            }
            return true;
        }

        if (PhoneData.hasPassword(stack)
                && screen.isInside(mouseX, mouseY, left, y + 94, PhoneUi.ACTION_BUTTON_W, PhoneUi.ACTION_BUTTON_H)) {
            beginSecurityState(SecurityFlowState.VERIFY_TO_CHANGE);
            return true;
        }

        if (PhoneData.hasPassword(stack)
                && screen.isInside(mouseX, mouseY, left, y + 116, PhoneUi.ACTION_BUTTON_W, PhoneUi.ACTION_BUTTON_H)) {
            beginSecurityState(SecurityFlowState.VERIFY_FOR_FACE_ID);
            return true;
        }

        return false;
    }

    private void handleSecurityInput(PhoneScreen screen, String key) {
        if (key == null || key.isEmpty()) {
            return;
        }

        if (enteredCode.length() < PIN_LENGTH) {
            enteredCode += key;
        }

        if (enteredCode.length() == PIN_LENGTH) {
            submitSecurityCode(screen);
        }
    }

    private void submitSecurityCode(PhoneScreen screen) {
        ItemStack stack = screen.getPhoneStack();
        Player player = Minecraft.getInstance().player;

        if (enteredCode.length() != PIN_LENGTH) {
            return;
        }

        switch (securityState) {
            case VERIFY_TO_ENTER -> {
                if (PhoneData.validatePasscode(stack, enteredCode)) {
                    beginSecurityState(SecurityFlowState.NONE);
                } else {
                    onSecurityFail();
                }
            }

            case CREATE_NEW_CODE -> {
                pendingNewCode = enteredCode;
                enteredCode = "";
                securityErrorTicks = 0;
                securityState = SecurityFlowState.CONFIRM_NEW_CODE;
            }

            case CONFIRM_NEW_CODE -> {
                if (pendingNewCode.equals(enteredCode)) {
                    PhoneData.setPasscode(stack, enteredCode);
                    PhoneData.setHasPassword(stack, true);
                    PhoneData.setLocked(stack, true);
                    beginSecurityState(SecurityFlowState.NONE);
                } else {
                    pendingNewCode = "";
                    enteredCode = "";
                    securityErrorTicks = SECURITY_ERROR_TICKS;
                    securityState = SecurityFlowState.CREATE_NEW_CODE;
                }
            }

            case VERIFY_TO_DISABLE -> {
                if (PhoneData.validatePasscode(stack, enteredCode)) {
                    PhoneData.setHasPassword(stack, false);
                    PhoneData.setLocked(stack, false);
                    PhoneData.clearFaceId(stack);
                    beginSecurityState(SecurityFlowState.NONE);
                } else {
                    onSecurityFail();
                }
            }

            case VERIFY_TO_CHANGE -> {
                if (PhoneData.validatePasscode(stack, enteredCode)) {
                    beginSecurityState(SecurityFlowState.ENTER_CHANGED_CODE);
                } else {
                    onSecurityFail();
                }
            }

            case ENTER_CHANGED_CODE -> {
                pendingNewCode = enteredCode;
                enteredCode = "";
                securityErrorTicks = 0;
                securityState = SecurityFlowState.CONFIRM_CHANGED_CODE;
            }

            case CONFIRM_CHANGED_CODE -> {
                if (pendingNewCode.equals(enteredCode)) {
                    PhoneData.setPasscode(stack, enteredCode);
                    beginSecurityState(SecurityFlowState.NONE);
                } else {
                    pendingNewCode = "";
                    enteredCode = "";
                    securityErrorTicks = SECURITY_ERROR_TICKS;
                    securityState = SecurityFlowState.ENTER_CHANGED_CODE;
                }
            }

            case VERIFY_FOR_FACE_ID -> {
                if (PhoneData.validatePasscode(stack, enteredCode)) {
                    if (PhoneData.hasFaceId(stack)) {
                        PhoneData.clearFaceId(stack);
                    } else if (player != null) {
                        PhoneData.registerFaceId(stack, player);
                        PhoneData.setLocked(stack, true);
                    }
                    beginSecurityState(SecurityFlowState.NONE);
                } else {
                    onSecurityFail();
                }
            }

            case NONE -> {
            }
        }
    }

    private void beginSecurityState(SecurityFlowState newState) {
        securityState = newState;
        enteredCode = "";
        pendingNewCode = "";
        securityErrorTicks = 0;
    }

    private void onSecurityFail() {
        enteredCode = "";
        securityErrorTicks = SECURITY_ERROR_TICKS;
    }

    private boolean isSecurityPadOpen() {
        return securityState != SecurityFlowState.NONE;
    }

    private String getSecurityTitle() {
        return switch (securityState) {
            case VERIFY_TO_ENTER -> "Verificar codigo";
            case CREATE_NEW_CODE -> "Nuevo codigo";
            case CONFIRM_NEW_CODE -> "Confirmar codigo";
            case VERIFY_TO_DISABLE -> "Desactivar codigo";
            case VERIFY_TO_CHANGE -> "Codigo actual";
            case ENTER_CHANGED_CODE -> "Nuevo codigo";
            case CONFIRM_CHANGED_CODE -> "Confirmar codigo";
            case VERIFY_FOR_FACE_ID -> "Verificar codigo";
            case NONE -> "Codigo";
        };
    }

    private String getSecuritySubtitle(ItemStack stack) {
        return switch (securityState) {
            case VERIFY_TO_ENTER -> "Acceso a Face ID y codigo";
            case CREATE_NEW_CODE -> "Crea el codigo del iPhone";
            case CONFIRM_NEW_CODE -> "Repite el nuevo codigo";
            case VERIFY_TO_DISABLE -> "Ingresa el codigo actual";
            case VERIFY_TO_CHANGE -> "Ingresa el codigo actual";
            case ENTER_CHANGED_CODE -> "Escribe el nuevo codigo";
            case CONFIRM_CHANGED_CODE -> "Repite el nuevo codigo";
            case VERIFY_FOR_FACE_ID -> PhoneData.hasFaceId(stack)
                    ? "Para restablecer Face ID"
                    : "Para activar Face ID";
            case NONE -> "";
        };
    }

    private void openEditor(EditorMode mode, String initialValue) {
        editorMode = mode;
        editorBuffer = initialValue == null ? "" : initialValue;
        editorErrorTicks = 0;
    }

    private boolean isEditorOpen() {
        return editorMode != EditorMode.NONE;
    }

    private String getEditorSubtitle() {
        return switch (editorMode) {
            case PROFILE_NAME -> "Escribe el nombre";
            case PROFILE_SURNAME -> "Escribe el apellido";
            case PROFILE_BIRTHDATE -> "Formato DD/MM/AAAA";
            case NONE -> "";
        };
    }

    private String getEditorErrorMessage() {
        return switch (editorMode) {
            case PROFILE_NAME, PROFILE_SURNAME -> "Texto demasiado largo";
            case PROFILE_BIRTHDATE -> "Fecha invalida";
            case NONE -> "Error";
        };
    }

    private boolean handleEditorClick(PhoneScreen screen, double mouseX, double mouseY, ItemStack stack) {
        String key = editorMode == EditorMode.PROFILE_BIRTHDATE
                ? PhoneBirthdateKeyboard.defaultKeyboard(screen).getKeyAt(screen, mouseX, mouseY)
                : PhoneTextKeyboard.defaultKeyboard(screen).getKeyAt(screen, mouseX, mouseY);

        if (key == null) {
            return false;
        }

        handleEditorInput(key, stack);
        return true;
    }

    private void handleEditorInput(String value, ItemStack stack) {
        if (PhoneTextKeyboard.KEY_CLEAR.equals(value) || PhoneBirthdateKeyboard.KEY_CLEAR.equals(value)) {
            editorBuffer = "";
            return;
        }

        if (PhoneTextKeyboard.KEY_BACKSPACE.equals(value) || PhoneBirthdateKeyboard.KEY_BACKSPACE.equals(value)) {
            if (!editorBuffer.isEmpty()) {
                editorBuffer = editorBuffer.substring(0, editorBuffer.length() - 1);
            }
            return;
        }

        if (PhoneTextKeyboard.KEY_SPACE.equals(value)) {
            if (editorBuffer.length() < EDITOR_MAX_TEXT_LENGTH) {
                editorBuffer += " ";
            } else {
                editorErrorTicks = EDITOR_ERROR_TICKS;
            }
            return;
        }

        if (PhoneTextKeyboard.KEY_OK.equals(value) || PhoneBirthdateKeyboard.KEY_OK.equals(value)) {
            submitEditor(stack);
            return;
        }

        int limit = editorMode == EditorMode.PROFILE_BIRTHDATE
                ? EDITOR_BIRTHDATE_LENGTH
                : EDITOR_MAX_TEXT_LENGTH;

        if (editorBuffer.length() >= limit) {
            editorErrorTicks = EDITOR_ERROR_TICKS;
            return;
        }

        if (editorMode == EditorMode.PROFILE_BIRTHDATE) {
            if (isBirthdateCharAllowed(value)) {
                editorBuffer += value;
            }
            return;
        }

        if (value.length() == 1) {
            editorBuffer += value;
        }
    }

    private boolean isBirthdateCharAllowed(String value) {
        if (value == null || value.isEmpty()) {
            return false;
        }

        char c = value.charAt(0);
        return Character.isDigit(c) || c == '/';
    }

    private void submitEditor(ItemStack stack) {
        switch (editorMode) {
            case PROFILE_NAME -> {
                PhoneData.setProfileName(stack, editorBuffer.trim());
                closeEditor();
            }
            case PROFILE_SURNAME -> {
                PhoneData.setProfileSurname(stack, editorBuffer.trim());
                closeEditor();
            }
            case PROFILE_BIRTHDATE -> {
                if (isValidBirthdate(editorBuffer)) {
                    PhoneData.setProfileBirthdate(stack, editorBuffer);
                    closeEditor();
                } else {
                    editorErrorTicks = SECURITY_ERROR_TICKS;
                }
            }
            case NONE -> {
            }
        }
    }

    private boolean isValidBirthdate(String value) {
        if (value == null || value.length() != 10) {
            return false;
        }

        if (value.charAt(2) != '/' || value.charAt(5) != '/') {
            return false;
        }

        for (int i = 0; i < value.length(); i++) {
            if (i == 2 || i == 5) {
                continue;
            }
            if (!Character.isDigit(value.charAt(i))) {
                return false;
            }
        }

        try {
            int day = Integer.parseInt(value.substring(0, 2));
            int month = Integer.parseInt(value.substring(3, 5));
            int year = Integer.parseInt(value.substring(6, 10));

            LocalDate.of(year, month, day);
            return year >= 1900 && year <= 2100;
        } catch (DateTimeException | NumberFormatException e) {
            return false;
        }
    }

    private void closeEditor() {
        editorMode = EditorMode.NONE;
        editorBuffer = "";
        editorErrorTicks = 0;
    }

    private void handleBack(PhoneScreen screen) {
        if (isEditorOpen()) {
            closeEditor();
            return;
        }

        if (currentCategory == SettingsCategory.SECURITY && isSecurityPadOpen()) {
            if (PhoneData.hasPassword(screen.getPhoneStack()) && securityState == SecurityFlowState.VERIFY_TO_ENTER) {
                currentCategory = SettingsCategory.HOME;
                beginSecurityState(SecurityFlowState.NONE);
                return;
            }

            beginSecurityState(SecurityFlowState.NONE);
            return;
        }

        currentCategory = SettingsCategory.HOME;
        beginSecurityState(SecurityFlowState.NONE);
    }

    private boolean canGoBack() {
        return currentCategory != SettingsCategory.HOME || isEditorOpen();
    }

    private String getCurrentTitle() {
        if (isEditorOpen()) {
            return switch (editorMode) {
                case PROFILE_NAME -> "Editar nombre";
                case PROFILE_SURNAME -> "Editar apellido";
                case PROFILE_BIRTHDATE -> "Editar fecha";
                case NONE -> "Editor";
            };
        }

        return currentCategory.title;
    }

    private enum SettingsCategory {
        HOME("Configuracion"),
        PROFILE("Perfil"),
        WALLPAPER("Fondo de pantalla"),
        DISPLAY("Pantalla y brillo"),
        NOTIFICATIONS("Notificaciones"),
        SOUND("Sonido"),
        SECURITY("Face ID y codigo");

        private final String title;

        SettingsCategory(String title) {
            this.title = title;
        }
    }

    private enum SecurityFlowState {
        NONE,
        VERIFY_TO_ENTER,
        CREATE_NEW_CODE,
        CONFIRM_NEW_CODE,
        VERIFY_TO_DISABLE,
        VERIFY_TO_CHANGE,
        ENTER_CHANGED_CODE,
        CONFIRM_CHANGED_CODE,
        VERIFY_FOR_FACE_ID
    }

    private enum EditorMode {
        NONE,
        PROFILE_NAME,
        PROFILE_SURNAME,
        PROFILE_BIRTHDATE
    }

    private record ListEntry(String label, SettingsCategory category) {
    }
}