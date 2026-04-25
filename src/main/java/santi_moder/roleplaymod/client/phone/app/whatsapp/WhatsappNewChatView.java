package santi_moder.roleplaymod.client.phone.app.whatsapp;

import com.mojang.blaze3d.systems.RenderSystem;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import santi_moder.roleplaymod.client.phone.ui.PhoneUi;
import santi_moder.roleplaymod.client.screen.PhoneScreen;
import santi_moder.roleplaymod.network.ModNetwork;
import santi_moder.roleplaymod.network.phone.whatsapp.WhatsappOpenOrCreateChatC2SPacket;

import java.util.List;

public final class WhatsappNewChatView {

    private static final int TITLE_Y = 30;

    private static final int CONTENT_TOP = 44;
    private static final int CONTENT_BOTTOM = 28;
    private static final int SIDE_PADDING = 10;
    private static final int ROW_HEIGHT = 18;
    private static final int ROW_GAP = 2;
    private static final int SCROLL_STEP = 12;

    private static final int AVATAR_SIZE = 16;
    private static final int COLOR_ROW = 0x22111111;
    private static final int COLOR_ROW_HOVER = 0x44303030;
    private static final int COLOR_DIVIDER = 0x22FFFFFF;
    private static final int COLOR_AVATAR = 0xFF25D366;
    private static final int COLOR_AVATAR_TEXT = 0xFF081C15;
    private static final int COLOR_TEXT = 0xFFFFFFFF;
    private static final int COLOR_SUBTEXT = 0xFFBEBEBE;

    private int scrollOffset = 0;

    public void render(PhoneScreen screen, GuiGraphics guiGraphics, int mouseX, int mouseY, WhatsappState state) {
        PhoneUi.drawBackButton(screen, guiGraphics, mouseX, mouseY);

        guiGraphics.drawCenteredString(
                screen.getPhoneFont(),
                "Nuevo chat",
                screen.getPhoneCenterX(),
                screen.getPhoneY() + TITLE_Y,
                PhoneUi.COLOR_TEXT
        );

        clampScroll(screen, state);

        int x = screen.getPhoneX() + SIDE_PADDING;
        int y = screen.getPhoneY() + CONTENT_TOP;
        int w = screen.getPhoneWidth() - SIDE_PADDING * 2;
        int h = getVisibleHeight(screen);

        enableScissor(screen, x, y, w, h);

        int drawY = y - scrollOffset;

        renderOptionRow(screen, guiGraphics, mouseX, mouseY, x, drawY, w, "Nuevo grupo", "Próximamente");
        drawY += ROW_HEIGHT + ROW_GAP;

        renderOptionRow(screen, guiGraphics, mouseX, mouseY, x, drawY, w, "Nuevo contacto", "");
        drawY += ROW_HEIGHT + ROW_GAP + 4;

        List<WhatsappContact> contacts = state.getContacts();
        for (WhatsappContact contact : contacts) {
            if (drawY + ROW_HEIGHT >= y && drawY <= y + h) {
                renderContactRow(screen, guiGraphics, mouseX, mouseY, x, drawY, w, contact);
            }
            drawY += ROW_HEIGHT + ROW_GAP;
        }

        disableScissor();
    }

    public boolean mouseClicked(PhoneScreen screen, double mouseX, double mouseY, int button, WhatsappState state) {
        if (button != 0) {
            return false;
        }

        if (PhoneUi.isBackButtonClicked(screen, mouseX, mouseY)) {
            state.backFromChatSubscreen();
            return true;
        }

        int x = screen.getPhoneX() + SIDE_PADDING;
        int y = screen.getPhoneY() + CONTENT_TOP - scrollOffset;
        int w = screen.getPhoneWidth() - SIDE_PADDING * 2;

        if (screen.isInside(mouseX, mouseY, x, y + ROW_HEIGHT + ROW_GAP, w, ROW_HEIGHT)) {
            state.clearNewContactDraft();
            state.openNewContactScreen();
            return true;
        }

        int drawY = y + (ROW_HEIGHT + ROW_GAP) * 2 + 4;
        for (WhatsappContact contact : state.getContacts()) {
            if (screen.isInside(mouseX, mouseY, x, drawY, w, ROW_HEIGHT)) {
                state.setWaitingOpenConversationFromServer(true);
                ModNetwork.sendWhatsappToServer(new WhatsappOpenOrCreateChatC2SPacket(contact.id()));
                return true;
            }
            drawY += ROW_HEIGHT + ROW_GAP;
        }

        return true;
    }

    public boolean mouseScrolled(PhoneScreen screen, double mouseX, double mouseY, double scrollDelta, WhatsappState state) {
        if (scrollDelta == 0.0D) {
            return false;
        }

        int maxScroll = Math.max(0, getContentHeight(state) - getVisibleHeight(screen));

        if (scrollDelta < 0.0D) {
            scrollOffset = Math.min(maxScroll, scrollOffset + SCROLL_STEP);
        } else {
            scrollOffset = Math.max(0, scrollOffset - SCROLL_STEP);
        }

        return true;
    }

    public void resetViewState() {
        scrollOffset = 0;
    }

    private void renderOptionRow(
            PhoneScreen screen,
            GuiGraphics guiGraphics,
            int mouseX,
            int mouseY,
            int x,
            int y,
            int w,
            String title,
            String subtitle
    ) {
        boolean hover = screen.isInside(mouseX, mouseY, x, y, w, ROW_HEIGHT);

        guiGraphics.fill(x, y, x + w, y + ROW_HEIGHT, hover ? COLOR_ROW_HOVER : COLOR_ROW);
        guiGraphics.fill(x, y + ROW_HEIGHT - 1, x + w, y + ROW_HEIGHT, COLOR_DIVIDER);

        guiGraphics.drawString(screen.getPhoneFont(), title, x + 6, y + 5, COLOR_TEXT, false);

        if (!subtitle.isBlank()) {
            guiGraphics.drawString(
                    screen.getPhoneFont(),
                    subtitle,
                    x + w - screen.getPhoneFont().width(subtitle) - 6,
                    y + 5,
                    COLOR_SUBTEXT,
                    false
            );
        }
    }

    private void renderContactRow(
            PhoneScreen screen,
            GuiGraphics guiGraphics,
            int mouseX,
            int mouseY,
            int x,
            int y,
            int w,
            WhatsappContact contact
    ) {
        boolean hover = screen.isInside(mouseX, mouseY, x, y, w, ROW_HEIGHT);

        guiGraphics.fill(x, y, x + w, y + ROW_HEIGHT, hover ? COLOR_ROW_HOVER : COLOR_ROW);
        guiGraphics.fill(x, y + ROW_HEIGHT - 1, x + w, y + ROW_HEIGHT, COLOR_DIVIDER);

        guiGraphics.fill(x + 4, y + 1, x + 4 + AVATAR_SIZE, y + 1 + AVATAR_SIZE, COLOR_AVATAR);
        guiGraphics.drawCenteredString(
                screen.getPhoneFont(),
                contact.getInitials(),
                x + 4 + AVATAR_SIZE / 2,
                y + 5,
                COLOR_AVATAR_TEXT
        );

        guiGraphics.drawString(screen.getPhoneFont(), contact.displayName(), x + 24, y + 5, COLOR_TEXT, false);
    }

    private int getVisibleHeight(PhoneScreen screen) {
        return screen.getPhoneHeight() - CONTENT_TOP - CONTENT_BOTTOM;
    }

    private int getContentHeight(WhatsappState state) {
        int baseRows = 2;
        int contactRows = state.getContacts().size();
        int rowCount = baseRows + contactRows;
        return rowCount * ROW_HEIGHT + Math.max(0, rowCount - 1) * ROW_GAP + 4;
    }

    private void clampScroll(PhoneScreen screen, WhatsappState state) {
        int maxScroll = Math.max(0, getContentHeight(state) - getVisibleHeight(screen));

        if (scrollOffset < 0) {
            scrollOffset = 0;
        }
        if (scrollOffset > maxScroll) {
            scrollOffset = maxScroll;
        }
    }

    private void enableScissor(PhoneScreen screen, int x, int y, int width, int height) {
        Minecraft mc = Minecraft.getInstance();
        double scale = mc.getWindow().getGuiScale();

        int scissorX = (int) (x * scale);
        int scissorY = (int) ((mc.getWindow().getGuiScaledHeight() - (y + height)) * scale);
        int scissorW = (int) (width * scale);
        int scissorH = (int) (height * scale);

        RenderSystem.enableScissor(scissorX, scissorY, scissorW, scissorH);
    }

    private void disableScissor() {
        RenderSystem.disableScissor();
    }
}