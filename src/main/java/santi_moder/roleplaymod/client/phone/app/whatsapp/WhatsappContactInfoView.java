package santi_moder.roleplaymod.client.phone.app.whatsapp;

import com.mojang.blaze3d.systems.RenderSystem;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import santi_moder.roleplaymod.client.phone.ui.PhoneUi;
import santi_moder.roleplaymod.client.screen.PhoneScreen;
import santi_moder.roleplaymod.common.whatsapp.model.WhatsappChat;
import santi_moder.roleplaymod.common.whatsapp.model.WhatsappContact;

import java.util.ArrayList;
import java.util.List;

public final class WhatsappContactInfoView {

    private static final int TITLE_Y = 30;

    private static final int SCROLL_TOP = 44;
    private static final int SCROLL_BOTTOM = 30;
    private static final int SCROLL_STEP = 12;

    private static final int AVATAR_RADIUS = 26;
    private static final int AVATAR_COLOR = 0xFF25D366;
    private static final int AVATAR_TEXT_COLOR = 0xFF081C15;

    private static final int SECTION_X = 12;
    private static final int SECTION_W_MARGIN = 24;
    private static final int ROW_H = 18;
    private static final int ROW_GAP = 2;

    private static final int HEADER_AVATAR_TOP = 12;
    private static final int HEADER_NAME_GAP = 10;
    private static final int HEADER_PHONE_GAP = 12;
    private static final int HEADER_BOTTOM_GAP = 12;

    private static final int COLOR_ROW = 0x22111111;
    private static final int COLOR_ROW_HOVER = 0x44303030;
    private static final int COLOR_DIVIDER = 0x22FFFFFF;
    private static final int COLOR_TEXT = 0xFFFFFFFF;
    private static final int COLOR_SUBTEXT = 0xFFBEBEBE;
    private static final int COLOR_DANGER = 0xFFFF8080;
    private static final int COLOR_SUCCESS = 0xFF7CFF9C;

    private int scrollOffset = 0;

    public void render(PhoneScreen screen, GuiGraphics guiGraphics, int mouseX, int mouseY, WhatsappState state) {
        WhatsappContact contact = state.getSelectedContact();
        if (contact == null) {
            return;
        }

        PhoneUi.drawBackButton(screen, guiGraphics, mouseX, mouseY);

        guiGraphics.drawCenteredString(
                screen.getPhoneFont(),
                "Info",
                screen.getPhoneCenterX(),
                screen.getPhoneY() + TITLE_Y,
                PhoneUi.COLOR_TEXT
        );

        int clipX = screen.getPhoneX() + 8;
        int clipY = screen.getPhoneY() + SCROLL_TOP;
        int clipW = screen.getPhoneWidth() - 16;
        int clipH = getVisibleHeight(screen);

        clampScroll(screen, state);

        enableScissor(screen, clipX, clipY, clipW, clipH);

        int contentTopY = clipY - scrollOffset;

        renderScrollableHeader(screen, guiGraphics, contact, contentTopY);
        renderOptions(screen, guiGraphics, mouseX, mouseY, state, contentTopY + getHeaderHeight());

        disableScissor();
    }

    public boolean mouseClicked(PhoneScreen screen, double mouseX, double mouseY, int button, WhatsappState state) {
        WhatsappContact contact = state.getSelectedContact();
        if (contact == null || button != 0) {
            return false;
        }

        if (PhoneUi.isBackButtonClicked(screen, mouseX, mouseY)) {
            state.backFromChatSubscreen();
            return true;
        }

        int contentTopY = screen.getPhoneY() + SCROLL_TOP - scrollOffset;

        if (isAvatarClicked(screen, mouseX, mouseY, contentTopY)) {
            state.openSelectedContactPhoto();
            return true;
        }

        List<RowAction> rows = buildRows(screen, state, contentTopY + getHeaderHeight());
        for (RowAction row : rows) {
            if (screen.isInside(mouseX, mouseY, row.x, row.y, row.w, ROW_H)) {
                switch (row.action) {
                    case EMPTY_CHAT -> {
                        WhatsappChat chat = state.getSelectedChat();
                        if (chat != null) {
                            santi_moder.roleplaymod.network.ModNetwork.sendWhatsappToServer(
                                    new santi_moder.roleplaymod.network.phone.whatsapp.WhatsappClearChatC2SPacket(chat.id())
                            );
                        }
                    }
                    case TOGGLE_BLOCK -> {
                        if (contact != null) {
                            santi_moder.roleplaymod.network.ModNetwork.sendWhatsappToServer(
                                    new santi_moder.roleplaymod.network.phone.whatsapp.WhatsappToggleBlockContactC2SPacket(contact.id())
                            );
                        }
                    }
                    case NONE -> {
                    }
                }
                return true;
            }
        }

        return true;
    }

    public boolean mouseScrolled(PhoneScreen screen, double mouseX, double mouseY, double scrollDelta, WhatsappState state) {
        if (scrollDelta == 0.0D) {
            return false;
        }

        int contentHeight = getContentHeight(screen, state);
        int visibleHeight = getVisibleHeight(screen);
        int maxScroll = Math.max(0, contentHeight - visibleHeight);

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

    private void renderScrollableHeader(PhoneScreen screen, GuiGraphics guiGraphics, WhatsappContact contact, int topY) {
        int centerX = screen.getPhoneCenterX();
        int avatarCenterY = topY + HEADER_AVATAR_TOP + AVATAR_RADIUS;

        guiGraphics.fill(
                centerX - AVATAR_RADIUS,
                avatarCenterY - AVATAR_RADIUS,
                centerX + AVATAR_RADIUS,
                avatarCenterY + AVATAR_RADIUS,
                AVATAR_COLOR
        );

        guiGraphics.drawCenteredString(
                screen.getPhoneFont(),
                contact.getInitials(),
                centerX,
                avatarCenterY - 4,
                AVATAR_TEXT_COLOR
        );

        int nameY = avatarCenterY + AVATAR_RADIUS + HEADER_NAME_GAP;
        int phoneY = nameY + HEADER_PHONE_GAP;

        guiGraphics.drawCenteredString(
                screen.getPhoneFont(),
                contact.displayName(),
                centerX,
                nameY,
                COLOR_TEXT
        );

        guiGraphics.drawCenteredString(
                screen.getPhoneFont(),
                contact.phoneNumber().isBlank() ? "Sin número" : contact.phoneNumber(),
                centerX,
                phoneY,
                COLOR_SUBTEXT
        );
    }

    private void renderOptions(PhoneScreen screen, GuiGraphics guiGraphics, int mouseX, int mouseY, WhatsappState state, int startY) {
        for (RowAction row : buildRows(screen, state, startY)) {
            boolean hover = screen.isInside(mouseX, mouseY, row.x, row.y, row.w, ROW_H);

            guiGraphics.fill(
                    row.x,
                    row.y,
                    row.x + row.w,
                    row.y + ROW_H,
                    hover ? COLOR_ROW_HOVER : COLOR_ROW
            );

            guiGraphics.fill(
                    row.x,
                    row.y + ROW_H - 1,
                    row.x + row.w,
                    row.y + ROW_H,
                    COLOR_DIVIDER
            );

            guiGraphics.drawString(
                    screen.getPhoneFont(),
                    row.title,
                    row.x + 6,
                    row.y + 5,
                    row.color,
                    false
            );

            if (!row.subtitle.isBlank()) {
                guiGraphics.drawString(
                        screen.getPhoneFont(),
                        row.subtitle,
                        row.x + row.w - screen.getPhoneFont().width(row.subtitle) - 6,
                        row.y + 5,
                        COLOR_SUBTEXT,
                        false
                );
            }
        }
    }

    private List<RowAction> buildRows(PhoneScreen screen, WhatsappState state, int startY) {
        WhatsappContact contact = state.getSelectedContact();
        List<RowAction> rows = new ArrayList<>();

        int x = screen.getPhoneX() + SECTION_X;
        int w = screen.getPhoneWidth() - SECTION_W_MARGIN;
        int y = startY;

        rows.add(new RowAction(x, y, w, "Llamar", "", COLOR_TEXT, Action.NONE));
        y += ROW_H + ROW_GAP;

        rows.add(new RowAction(x, y, w, "Videollamar", "", COLOR_TEXT, Action.NONE));
        y += ROW_H + ROW_GAP;

        rows.add(new RowAction(x, y, w, "Buscar", "", COLOR_TEXT, Action.NONE));
        y += ROW_H + ROW_GAP + 4;

        rows.add(new RowAction(x, y, w, "Archivos", String.valueOf(contact.mediaCount()), COLOR_TEXT, Action.NONE));
        y += ROW_H + ROW_GAP;

        rows.add(new RowAction(x, y, w, "Grupos en común", String.valueOf(contact.commonGroupsCount()), COLOR_TEXT, Action.NONE));
        y += ROW_H + ROW_GAP;

        rows.add(new RowAction(x, y, w, "Compartir contacto", "", COLOR_TEXT, Action.NONE));
        y += ROW_H + ROW_GAP + 4;

        rows.add(new RowAction(x, y, w, "Vaciar chat", "", COLOR_DANGER, Action.EMPTY_CHAT));
        y += ROW_H + ROW_GAP;

        rows.add(new RowAction(
                x,
                y,
                w,
                contact.blocked() ? "Desbloquear usuario" : "Bloquear usuario",
                "",
                contact.blocked() ? COLOR_SUCCESS : COLOR_DANGER,
                Action.TOGGLE_BLOCK
        ));

        return rows;
    }

    private int getHeaderHeight() {
        int avatarBlock = HEADER_AVATAR_TOP + AVATAR_RADIUS * 2;
        int nameY = avatarBlock + HEADER_NAME_GAP;
        int phoneY = nameY + HEADER_PHONE_GAP;

        return phoneY + HEADER_BOTTOM_GAP;
    }

    private int getContentHeight(PhoneScreen screen, WhatsappState state) {
        int startY = screen.getPhoneY() + SCROLL_TOP + getHeaderHeight();
        List<RowAction> rows = buildRows(screen, state, startY);

        if (rows.isEmpty()) {
            return getHeaderHeight();
        }

        RowAction last = rows.get(rows.size() - 1);
        int rowsBottom = (last.y - screen.getPhoneY()) + ROW_H + 10;

        return Math.max(getHeaderHeight(), rowsBottom);
    }

    private int getVisibleHeight(PhoneScreen screen) {
        return screen.getPhoneHeight() - SCROLL_TOP - SCROLL_BOTTOM;
    }

    private void clampScroll(PhoneScreen screen, WhatsappState state) {
        int maxScroll = Math.max(0, getContentHeight(screen, state) - getVisibleHeight(screen));

        if (scrollOffset < 0) {
            scrollOffset = 0;
        }
        if (scrollOffset > maxScroll) {
            scrollOffset = maxScroll;
        }
    }

    private boolean isAvatarClicked(PhoneScreen screen, double mouseX, double mouseY, int topY) {
        int centerX = screen.getPhoneCenterX();
        int avatarCenterY = topY + HEADER_AVATAR_TOP + AVATAR_RADIUS;

        return screen.isInside(
                mouseX,
                mouseY,
                centerX - AVATAR_RADIUS,
                avatarCenterY - AVATAR_RADIUS,
                AVATAR_RADIUS * 2,
                AVATAR_RADIUS * 2
        );
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

    private enum Action {
        NONE,
        EMPTY_CHAT,
        TOGGLE_BLOCK
    }

    private record RowAction(int x, int y, int w, String title, String subtitle, int color, Action action) {
    }
}