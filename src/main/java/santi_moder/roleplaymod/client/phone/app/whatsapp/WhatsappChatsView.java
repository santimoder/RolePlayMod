package santi_moder.roleplaymod.client.phone.app.whatsapp;

import com.mojang.blaze3d.systems.RenderSystem;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.resources.ResourceLocation;
import santi_moder.roleplaymod.client.phone.ui.PhoneThemeColors;
import santi_moder.roleplaymod.client.phone.ui.PhoneUi;
import santi_moder.roleplaymod.client.screen.PhoneScreen;
import santi_moder.roleplaymod.common.whatsapp.model.WhatsappChat;
import santi_moder.roleplaymod.common.whatsapp.model.WhatsappContact;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public final class WhatsappChatsView {

    private static final int TITLE_Y = 34;

    private static final int LIST_SIDE_PADDING = 10;
    private static final int LIST_BOTTOM_OFFSET = 28;

    private static final int SEARCH_BAR_X_PADDING = 10;
    private static final int SEARCH_BAR_WIDTH_PADDING = 20;
    private static final int SEARCH_BAR_HEIGHT = 16;
    private static final int SEARCH_BAR_TOP_Y = 48;
    private static final int SEARCH_BAR_TEXT_X = 6;
    private static final int SEARCH_BAR_TEXT_Y = 4;

    private static final int LIST_TOP_WITH_SEARCH = 68;
    private static final int LIST_TOP_NO_SEARCH = 48;

    private static final int ROW_HEIGHT = 28;
    private static final int ROW_GAP = 2;
    private static final int SCROLL_STEP = 12;

    private static final int AVATAR_SIZE = 18;

    private static final int ADD_BUTTON_SIZE = 14;
    private static final int ADD_BUTTON_RIGHT_MARGIN = 12;
    private static final int ADD_BUTTON_Y = 31;

    private static final int ACTION_WIDTH = 34;
    private static final int MAX_RIGHT_REVEAL = ACTION_WIDTH * 2;
    private static final int MAX_LEFT_REVEAL = ACTION_WIDTH * 2;
    private static final int SWIPE_THRESHOLD = 6;
    private static final int SNAP_OPEN_THRESHOLD = 26;

    private static final int MORE_SHEET_PADDING = 10;
    private static final int MORE_SHEET_ROW_H = 18;
    private static final int MORE_SHEET_ROW_GAP = 2;
    private static final int MORE_SHEET_BOTTOM_MARGIN = 34;
    private static final int COLOR_MESSAGE_BLOCKED = 0xFF7A7A7A;

    private static final int COLOR_SWIPE_PIN = 0xFF25D366;
    private static final int COLOR_SWIPE_READ = 0xFF3B82F6;
    private static final int COLOR_SWIPE_ARCHIVE = 0xFF64748B;
    private static final int COLOR_SWIPE_MORE = 0xFF6D28D9;
    private static final int COLOR_SWIPE_TEXT = 0xFFFFFFFF;

    private int scrollOffset = 0;

    private String swipedChatId;
    private int swipeOffset;
    private boolean dragging;
    private String draggingChatId;
    private int draggingBaseOffset;
    private int pressStartX;
    private int pressStartY;
    private boolean suppressClick;
    private boolean swipeActionConsumed;
    private boolean suppressNextRelease;

    private String moreMenuChatId;

    public void render(
            PhoneScreen screen,
            GuiGraphics guiGraphics,
            int mouseX,
            int mouseY,
            WhatsappState state,
            String searchQuery
    ) {
        int x = screen.getPhoneX() + LIST_SIDE_PADDING;
        int w = screen.getPhoneWidth() - LIST_SIDE_PADDING * 2;

        guiGraphics.fill(
                screen.getPhoneX() + 4,
                screen.getPhoneY() + 4,
                screen.getPhoneX() + screen.getPhoneWidth() - 4,
                screen.getPhoneY() + screen.getPhoneHeight() - 4,
                PhoneThemeColors.appBackground(screen.getPhoneStack())
        );

        guiGraphics.drawString(
                screen.getPhoneFont(),
                "Chats",
                x,
                screen.getPhoneY() + TITLE_Y,
                PhoneThemeColors.text(screen.getPhoneStack()),
                false
        );

        renderAddButton(screen, guiGraphics, mouseX, mouseY);
        clampScroll(screen, state, searchQuery);

        boolean showSearchBar = shouldShowSearchBar();
        if (showSearchBar) {
            renderSearchBar(screen, guiGraphics, searchQuery);
        }

        int listTop = getListTop(screen);
        int visibleHeight = getVisibleHeight(screen);
        List<WhatsappChat> filteredChats = getFilteredChats(state, searchQuery);

        if (filteredChats.isEmpty()) {
            renderEmptyState(screen, guiGraphics, searchQuery, listTop, visibleHeight);
            renderMoreSheetIfOpen(screen, guiGraphics, mouseX, mouseY, state);
            return;
        }

        enableScissor(screen, x, listTop, w, visibleHeight);

        for (int i = 0; i < filteredChats.size(); i++) {
            WhatsappChat chat = filteredChats.get(i);

            int rowY = listTop + i * (ROW_HEIGHT + ROW_GAP) - scrollOffset;
            if (rowY + ROW_HEIGHT < listTop || rowY > listTop + visibleHeight) {
                continue;
            }

            int currentOffset = getRenderedOffset(chat.id());
            renderSwipeBackground(screen, guiGraphics, mouseX, mouseY, state, chat, x, rowY, w, currentOffset);

            boolean hover = screen.isInside(mouseX, mouseY, x, rowY, w, ROW_HEIGHT);

            guiGraphics.fill(
                    x + currentOffset,
                    rowY,
                    x + currentOffset + w,
                    rowY + ROW_HEIGHT,
                    hover ? PhoneThemeColors.cardHover(screen.getPhoneStack()) : PhoneThemeColors.card(screen.getPhoneStack())
            );
            guiGraphics.fill(
                    x + currentOffset,
                    rowY + ROW_HEIGHT - 1,
                    x + currentOffset + w,
                    rowY + ROW_HEIGHT,
                    PhoneThemeColors.divider(screen.getPhoneStack())
            );

            renderAvatar(screen, guiGraphics, state, chat, x + 5 + currentOffset, rowY + 5);
            renderRowText(screen, guiGraphics, state, chat, x + currentOffset, rowY, w);
        }

        disableScissor();
        renderMoreSheetIfOpen(screen, guiGraphics, mouseX, mouseY, state);
    }

    public WhatsappChat getClickedChat(
            PhoneScreen screen,
            double mouseX,
            double mouseY,
            WhatsappState state,
            String searchQuery
    ) {

        if (suppressClick) {
            suppressClick = false;
            return null;
        }

        if (dragging) {
            return null;
        }

        if (swipedChatId != null || moreMenuChatId != null) {
            return null;
        }

        int x = screen.getPhoneX() + LIST_SIDE_PADDING;
        int y = getListTop(screen);
        int w = screen.getPhoneWidth() - LIST_SIDE_PADDING * 2;
        int visibleHeight = getVisibleHeight(screen);

        if (!screen.isInside(mouseX, mouseY, x, y, w, visibleHeight)) {
            return null;
        }

        List<WhatsappChat> filteredChats = getFilteredChats(state, searchQuery);
        for (int i = 0; i < filteredChats.size(); i++) {
            int rowY = y + i * (ROW_HEIGHT + ROW_GAP) - scrollOffset;
            if (screen.isInside(mouseX, mouseY, x, rowY, w, ROW_HEIGHT)) {
                WhatsappChat chat = filteredChats.get(i);
                if (getRenderedOffset(chat.id()) != 0) {
                    return null;
                }
                return chat;
            }
        }

        return null;
    }

    public boolean isAddButtonClicked(PhoneScreen screen, double mouseX, double mouseY) {
        int x = getAddButtonX(screen);
        int y = screen.getPhoneY() + ADD_BUTTON_Y;
        return screen.isInside(mouseX, mouseY, x, y, ADD_BUTTON_SIZE, ADD_BUTTON_SIZE);
    }

    public boolean mouseScrolled(
            PhoneScreen screen,
            double mouseX,
            double mouseY,
            double scrollDelta,
            WhatsappState state,
            String searchQuery
    ) {
        if (moreMenuChatId != null) {
            return false;
        }

        int x = screen.getPhoneX() + LIST_SIDE_PADDING;
        int y = getListTop(screen);
        int w = screen.getPhoneWidth() - LIST_SIDE_PADDING * 2;
        int visibleHeight = getVisibleHeight(screen);

        if (!screen.isInside(mouseX, mouseY, x, y, w, visibleHeight)) {
            return false;
        }

        int maxScroll = getMaxScroll(screen, state, searchQuery);

        if (scrollDelta < 0.0D) {
            scrollOffset = Math.min(maxScroll, scrollOffset + SCROLL_STEP);
        } else if (scrollDelta > 0.0D) {
            scrollOffset = Math.max(0, scrollOffset - SCROLL_STEP);
        }

        return true;
    }

    public boolean mousePressed(
            PhoneScreen screen,
            double mouseX,
            double mouseY,
            int button,
            WhatsappState state,
            String searchQuery
    ) {
        if (button != 0 || moreMenuChatId != null) {
            return false;
        }

        WhatsappChat chat = findRowAt(screen, mouseX, mouseY, state, searchQuery);
        if (chat == null) {
            return false;
        }

        dragging = true;
        draggingChatId = chat.id();
        draggingBaseOffset = getRenderedOffset(chat.id());
        pressStartX = (int) mouseX;
        pressStartY = (int) mouseY;
        suppressClick = false;
        return true;
    }

    public boolean mouseDragged(
            PhoneScreen screen,
            double mouseX,
            double mouseY,
            int button,
            double dragX,
            double dragY,
            WhatsappState state,
            String searchQuery
    ) {
        if (button != 0 || !dragging || draggingChatId == null) {
            return false;
        }

        int deltaX = (int) mouseX - pressStartX;
        int deltaY = (int) mouseY - pressStartY;

        if (Math.abs(deltaY) > Math.abs(deltaX) && Math.abs(deltaY) > SWIPE_THRESHOLD) {
            return false;
        }

        int candidate = draggingBaseOffset + deltaX;
        if (candidate > MAX_RIGHT_REVEAL) {
            candidate = MAX_RIGHT_REVEAL;
        }
        if (candidate < -MAX_LEFT_REVEAL) {
            candidate = -MAX_LEFT_REVEAL;
        }

        swipedChatId = draggingChatId;
        swipeOffset = candidate;

        boolean moved = Math.abs(deltaX) > 6;

        if (moved) {
            suppressClick = true;
        }

        return true;
    }

    public boolean mouseReleased(
            PhoneScreen screen,
            double mouseX,
            double mouseY,
            int button,
            WhatsappState state,
            String searchQuery
    ) {
        if (button != 0) {
            return false;
        }

        if (suppressNextRelease) {
            dragging = false;
            draggingChatId = null;
            draggingBaseOffset = 0;
            suppressClick = false;
            return false;
        }

        if (dragging) {
            dragging = false;
            draggingChatId = null;
            draggingBaseOffset = 0;

            if (swipedChatId != null) {
                if (swipeOffset >= SNAP_OPEN_THRESHOLD) {
                    swipeOffset = MAX_RIGHT_REVEAL;
                } else if (swipeOffset <= -SNAP_OPEN_THRESHOLD) {
                    swipeOffset = -MAX_LEFT_REVEAL;
                } else {
                    closeSwipe();
                }
            }

            boolean wasDrag = suppressClick;
            suppressClick = false;
            return wasDrag;
        }

        return false;
    }

    public SwipeActionResult getSwipeActionClicked(
            PhoneScreen screen,
            double mouseX,
            double mouseY,
            WhatsappState state,
            String searchQuery
    ) {
        if (swipedChatId == null || swipeOffset == 0) {
            return null;
        }

        WhatsappChat chat = state.findChatById(swipedChatId);
        if (chat == null) {
            closeSwipe();
            return null;
        }

        int x = screen.getPhoneX() + LIST_SIDE_PADDING;
        int w = screen.getPhoneWidth() - LIST_SIDE_PADDING * 2;
        int rowY = getRowYForChat(screen, state, searchQuery, chat.id());
        if (rowY == Integer.MIN_VALUE) {
            return null;
        }

        if (swipeOffset > 0) {
            int actionX = x;

            if (screen.isInside(mouseX, mouseY, actionX, rowY, ACTION_WIDTH, ROW_HEIGHT)) {
                ChatRowAction action = chat.pinned() ? ChatRowAction.UNPIN : ChatRowAction.PIN;
                markSwipeActionConsumed();
                suppressNextRelease = true;
                closeSwipe();
                return new SwipeActionResult(chat.id(), action);
            }

            if (screen.isInside(mouseX, mouseY, actionX + ACTION_WIDTH, rowY, ACTION_WIDTH, ROW_HEIGHT)) {
                ChatRowAction action = chat.unreadCount() > 0 ? ChatRowAction.MARK_READ : ChatRowAction.MARK_UNREAD;
                markSwipeActionConsumed();
                suppressNextRelease = true;
                closeSwipe();
                return new SwipeActionResult(chat.id(), action);
            }
        } else {
            int actionsX = x + w - MAX_LEFT_REVEAL;

            if (screen.isInside(mouseX, mouseY, actionsX, rowY, ACTION_WIDTH, ROW_HEIGHT)) {
                markSwipeActionConsumed();
                suppressNextRelease = true;
                closeSwipe();
                return new SwipeActionResult(chat.id(), ChatRowAction.ARCHIVE);
            }

            if (screen.isInside(mouseX, mouseY, actionsX + ACTION_WIDTH, rowY, ACTION_WIDTH, ROW_HEIGHT)) {
                moreMenuChatId = chat.id();
                markSwipeActionConsumed();
                suppressNextRelease = true;
                closeSwipe();
                return new SwipeActionResult(chat.id(), ChatRowAction.OPEN_MORE);
            }
        }

        return null;
    }

    public MoreMenuAction getMoreMenuActionClicked(
            PhoneScreen screen,
            double mouseX,
            double mouseY,
            WhatsappState state
    ) {
        if (moreMenuChatId == null) {
            return null;
        }

        int x = screen.getPhoneX() + MORE_SHEET_PADDING;
        int w = screen.getPhoneWidth() - MORE_SHEET_PADDING * 2;
        int y = getMoreSheetTop(screen);

        WhatsappChat chat = state.findChatById(moreMenuChatId);
        if (chat == null) {
            moreMenuChatId = null;
            return null;
        }

        List<SheetRow> rows = buildMoreRows(screen, chat, state);

        for (SheetRow row : rows) {
            if (screen.isInside(mouseX, mouseY, x, y, w, MORE_SHEET_ROW_H)) {
                moreMenuChatId = null;
                return row.action;
            }
            y += MORE_SHEET_ROW_H + MORE_SHEET_ROW_GAP;
        }

        if (!screen.isInside(mouseX, mouseY, x, getMoreSheetTop(screen), w, getMoreSheetHeight(chat, state))) {
            moreMenuChatId = null;
            return MoreMenuAction.NONE;
        }

        return null;
    }

    public boolean isMoreMenuOpen() {
        return moreMenuChatId != null;
    }

    public String getMoreMenuChatId() {
        return moreMenuChatId;
    }

    public void closeMoreMenu() {
        moreMenuChatId = null;
    }

    public void resetScroll() {
        scrollOffset = 0;
    }

    public void closeSwipe() {
        swipedChatId = null;
        swipeOffset = 0;
        dragging = false;
        draggingChatId = null;
        draggingBaseOffset = 0;
    }

    public void markSwipeActionConsumed() {
        swipeActionConsumed = true;
    }

    public boolean consumeSwipeActionConsumed() {
        boolean value = swipeActionConsumed;
        swipeActionConsumed = false;
        return value;
    }

    private void renderAddButton(PhoneScreen screen, GuiGraphics guiGraphics, int mouseX, int mouseY) {
        int x = getAddButtonX(screen);
        int y = screen.getPhoneY() + ADD_BUTTON_Y;
        boolean hover = screen.isInside(mouseX, mouseY, x, y, ADD_BUTTON_SIZE, ADD_BUTTON_SIZE);

        guiGraphics.fill(
                x,
                y,
                x + ADD_BUTTON_SIZE,
                y + ADD_BUTTON_SIZE,
                hover ? PhoneThemeColors.successHover(screen.getPhoneStack()) : PhoneThemeColors.success(screen.getPhoneStack())
        );

        PhoneUi.drawCenteredText(
                screen,
                guiGraphics,
                "+",
                x + ADD_BUTTON_SIZE / 2,
                y + 3,
                PhoneThemeColors.onSuccess(screen.getPhoneStack())
        );
    }

    private int getAddButtonX(PhoneScreen screen) {
        return screen.getPhoneX() + screen.getPhoneWidth() - ADD_BUTTON_RIGHT_MARGIN - ADD_BUTTON_SIZE;
    }

    private void renderSearchBar(PhoneScreen screen, GuiGraphics guiGraphics, String searchQuery) {
        int x = screen.getPhoneX() + SEARCH_BAR_X_PADDING;
        int y = screen.getPhoneY() + SEARCH_BAR_TOP_Y;
        int w = screen.getPhoneWidth() - SEARCH_BAR_WIDTH_PADDING;

        guiGraphics.fill(x, y, x + w, y + SEARCH_BAR_HEIGHT, PhoneThemeColors.card(screen.getPhoneStack()));

        String renderedText;
        int renderedColor;

        if (searchQuery == null || searchQuery.isEmpty()) {
            renderedText = "Buscar chats";
            renderedColor = PhoneThemeColors.hint(screen.getPhoneStack());
        } else {
            renderedText = "Buscar: " + searchQuery;
            renderedColor = PhoneThemeColors.text(screen.getPhoneStack());
        }

        guiGraphics.drawString(
                screen.getPhoneFont(),
                renderedText,
                x + SEARCH_BAR_TEXT_X,
                y + SEARCH_BAR_TEXT_Y,
                renderedColor,
                false
        );
    }

    private void renderEmptyState(
            PhoneScreen screen,
            GuiGraphics guiGraphics,
            String searchQuery,
            int listTop,
            int visibleHeight
    ) {
        int centerX = screen.getPhoneCenterX();
        int centerY = listTop + visibleHeight / 2 - 8;

        String title = (searchQuery == null || searchQuery.isBlank())
                ? "No hay chats"
                : "Sin resultados";

        String subtitle = (searchQuery == null || searchQuery.isBlank())
                ? "Todavía no tenés conversaciones"
                : "No se encontraron chats";

        PhoneUi.drawCenteredText(screen, guiGraphics, title, centerX, centerY, PhoneThemeColors.text(screen.getPhoneStack()));
        PhoneUi.drawCenteredText(screen, guiGraphics, subtitle, centerX, centerY + 12, PhoneThemeColors.subtext(screen.getPhoneStack()));
    }

    private void renderAvatar(PhoneScreen screen, GuiGraphics guiGraphics, WhatsappState state, WhatsappChat chat, int x, int y) {
        WhatsappContact contact = state.findContactById(chat.contactId());

        if (contact == null) {
            return;
        }

        WhatsappTextureResolver.drawProfilePhoto(
                guiGraphics,
                contact.photoId(),
                x,
                y,
                AVATAR_SIZE
        );
    }

    private void renderRowText(
            PhoneScreen screen,
            GuiGraphics guiGraphics,
            WhatsappState state,
            WhatsappChat chat,
            int rowX,
            int rowY,
            int rowWidth
    ) {
        WhatsappContact contact = state.findContactById(chat.contactId());

        int textX = rowX + 28;
        int nameY = rowY + 5;
        int messageY = rowY + 15;

        String name = chat.pinned() ? "📌 " + state.getChatDisplayName(chat) : state.getChatDisplayName(chat);
        String message = contact != null && contact.blocked()
                ? "Contacto bloqueado"
                : shorten(chat.getLastMessageText(), 18);

        guiGraphics.drawString(screen.getPhoneFont(), name, textX, nameY, PhoneThemeColors.text(screen.getPhoneStack()), false);
        guiGraphics.drawString(
                screen.getPhoneFont(),
                message,
                textX,
                messageY,
                contact != null && contact.blocked() ? COLOR_MESSAGE_BLOCKED : PhoneThemeColors.subtext(screen.getPhoneStack()),
                false
        );

        int timeWidth = screen.getPhoneFont().width(chat.getLastMessageTimeText());
        int timeX = rowX + rowWidth - timeWidth - 6;

        guiGraphics.drawString(screen.getPhoneFont(), chat.getLastMessageTimeText(), timeX, nameY, PhoneThemeColors.hint(screen.getPhoneStack()), false);

        if (chat.unreadCount() > 0) {
            renderUnreadBadge(screen, guiGraphics, chat.unreadCount(), rowX + rowWidth - 18, messageY - 1);
        }
    }

    private void renderUnreadBadge(PhoneScreen screen, GuiGraphics guiGraphics, int unreadCount, int x, int y) {
        String text = unreadCount > 9 ? "9+" : String.valueOf(unreadCount);
        int width = Math.max(10, screen.getPhoneFont().width(text) + 6);

        guiGraphics.fill(x - width, y, x, y + 10, PhoneThemeColors.success(screen.getPhoneStack()));
        PhoneUi.drawCenteredText(screen, guiGraphics, text, x - width / 2, y + 1, PhoneThemeColors.onSuccess(screen.getPhoneStack()));
    }

    private void renderSwipeBackground(
            PhoneScreen screen,
            GuiGraphics guiGraphics,
            int mouseX,
            int mouseY,
            WhatsappState state,
            WhatsappChat chat,
            int x,
            int rowY,
            int w,
            int offset
    ) {
        if (offset > 0) {
            int reveal = Math.min(offset, MAX_RIGHT_REVEAL);

            int firstWidth = Math.min(ACTION_WIDTH, reveal);
            int secondWidth = Math.min(ACTION_WIDTH, Math.max(0, reveal - ACTION_WIDTH));

            if (firstWidth > 0) {
                guiGraphics.fill(x, rowY, x + firstWidth, rowY + ROW_HEIGHT, COLOR_SWIPE_PIN);
            }

            if (secondWidth > 0) {
                guiGraphics.fill(x + ACTION_WIDTH, rowY, x + ACTION_WIDTH + secondWidth, rowY + ROW_HEIGHT, COLOR_SWIPE_READ);
            }

            if (reveal >= ACTION_WIDTH) {
                guiGraphics.drawCenteredString(
                        screen.getPhoneFont(),
                        chat.pinned() ? "Desfijar" : "Fijar",
                        x + ACTION_WIDTH / 2,
                        rowY + 10,
                        COLOR_SWIPE_TEXT
                );
            }

            if (reveal >= ACTION_WIDTH * 2) {
                guiGraphics.drawCenteredString(
                        screen.getPhoneFont(),
                        chat.unreadCount() > 0 ? "Leído" : "No leído",
                        x + ACTION_WIDTH + ACTION_WIDTH / 2,
                        rowY + 10,
                        COLOR_SWIPE_TEXT
                );
            }
        } else if (offset < 0) {
            int reveal = Math.min(-offset, MAX_LEFT_REVEAL);

            int firstWidth = Math.min(ACTION_WIDTH, reveal);
            int secondWidth = Math.min(ACTION_WIDTH, Math.max(0, reveal - ACTION_WIDTH));

            int firstX = x + w - firstWidth;
            int secondX = x + w - ACTION_WIDTH - secondWidth;

            if (firstWidth > 0) {
                guiGraphics.fill(firstX, rowY, x + w, rowY + ROW_HEIGHT, COLOR_SWIPE_MORE);
            }

            if (secondWidth > 0) {
                guiGraphics.fill(secondX, rowY, x + w - ACTION_WIDTH, rowY + ROW_HEIGHT, COLOR_SWIPE_ARCHIVE);
            }

            if (reveal >= ACTION_WIDTH) {
                guiGraphics.drawCenteredString(
                        screen.getPhoneFont(),
                        "Más",
                        x + w - ACTION_WIDTH / 2,
                        rowY + 10,
                        COLOR_SWIPE_TEXT
                );
            }

            if (reveal >= ACTION_WIDTH * 2) {
                guiGraphics.drawCenteredString(
                        screen.getPhoneFont(),
                        "Archivar",
                        x + w - ACTION_WIDTH - ACTION_WIDTH / 2,
                        rowY + 10,
                        COLOR_SWIPE_TEXT
                );
            }
        }
    }

    public boolean consumeSuppressNextRelease() {
        boolean value = suppressNextRelease;
        suppressNextRelease = false;
        return value;
    }

    private void renderLeftSwipeActions(
            PhoneScreen screen,
            GuiGraphics guiGraphics,
            int mouseX,
            int mouseY,
            WhatsappChat chat,
            int x,
            int rowY
    ) {
        int x1 = x;
        int x2 = x + ACTION_WIDTH;

        guiGraphics.fill(x1, rowY, x1 + ACTION_WIDTH, rowY + ROW_HEIGHT, COLOR_SWIPE_PIN);
        guiGraphics.fill(x2, rowY, x2 + ACTION_WIDTH, rowY + ROW_HEIGHT, COLOR_SWIPE_READ);

        guiGraphics.drawCenteredString(
                screen.getPhoneFont(),
                chat.pinned() ? "Desfijar" : "Fijar",
                x1 + ACTION_WIDTH / 2,
                rowY + 10,
                COLOR_SWIPE_TEXT
        );

        guiGraphics.drawCenteredString(
                screen.getPhoneFont(),
                chat.unreadCount() > 0 ? "Leído" : "No leído",
                x2 + ACTION_WIDTH / 2,
                rowY + 10,
                COLOR_SWIPE_TEXT
        );
    }

    private void renderRightSwipeActions(
            PhoneScreen screen,
            GuiGraphics guiGraphics,
            int mouseX,
            int mouseY,
            int x,
            int rowY
    ) {
        int x1 = x;
        int x2 = x + ACTION_WIDTH;

        guiGraphics.fill(x1, rowY, x1 + ACTION_WIDTH, rowY + ROW_HEIGHT, COLOR_SWIPE_ARCHIVE);
        guiGraphics.fill(x2, rowY, x2 + ACTION_WIDTH, rowY + ROW_HEIGHT, COLOR_SWIPE_MORE);

        guiGraphics.drawCenteredString(
                screen.getPhoneFont(),
                "Archivar",
                x1 + ACTION_WIDTH / 2,
                rowY + 10,
                COLOR_SWIPE_TEXT
        );

        guiGraphics.drawCenteredString(
                screen.getPhoneFont(),
                "Más",
                x2 + ACTION_WIDTH / 2,
                rowY + 10,
                COLOR_SWIPE_TEXT
        );
    }

    private void renderMoreSheetIfOpen(
            PhoneScreen screen,
            GuiGraphics guiGraphics,
            int mouseX,
            int mouseY,
            WhatsappState state
    ) {
        if (moreMenuChatId == null) {
            return;
        }

        WhatsappChat chat = state.findChatById(moreMenuChatId);
        if (chat == null) {
            moreMenuChatId = null;
            return;
        }

        int x = screen.getPhoneX() + MORE_SHEET_PADDING;
        int y = getMoreSheetTop(screen);
        int w = screen.getPhoneWidth() - MORE_SHEET_PADDING * 2;
        int h = getMoreSheetHeight(chat, state);

        guiGraphics.fill(x, y, x + w, y + h, PhoneThemeColors.sheet(screen.getPhoneStack()));

        List<SheetRow> rows = buildMoreRows(screen, chat, state);
        int rowY = y;

        for (SheetRow row : rows) {
            boolean hover = screen.isInside(mouseX, mouseY, x, rowY, w, MORE_SHEET_ROW_H);

            guiGraphics.fill(x, rowY, x + w, rowY + MORE_SHEET_ROW_H, hover ? PhoneThemeColors.cardHover(screen.getPhoneStack()) : PhoneThemeColors.card(screen.getPhoneStack()));
            guiGraphics.fill(x, rowY + MORE_SHEET_ROW_H - 1, x + w, rowY + MORE_SHEET_ROW_H, PhoneThemeColors.divider(screen.getPhoneStack()));

            guiGraphics.drawString(screen.getPhoneFont(), row.label, x + 6, rowY + 5, row.color, false);
            rowY += MORE_SHEET_ROW_H + MORE_SHEET_ROW_GAP;
        }
    }

    private List<SheetRow> buildMoreRows(PhoneScreen screen, WhatsappChat chat, WhatsappState state) {
        List<SheetRow> rows = new ArrayList<>();
        WhatsappContact contact = state.findContactById(chat.contactId());

        rows.add(new SheetRow("Silenciar", PhoneThemeColors.text(screen.getPhoneStack()), MoreMenuAction.MUTE));
        rows.add(new SheetRow("Info del contacto", PhoneThemeColors.text(screen.getPhoneStack()), MoreMenuAction.CONTACT_INFO));
        rows.add(new SheetRow("Vaciar chat", PhoneThemeColors.danger(screen.getPhoneStack()), MoreMenuAction.CLEAR_CHAT));
        rows.add(new SheetRow(
                contact != null && contact.blocked() ? "Desbloquear chat" : "Bloquear chat",
                contact != null && contact.blocked()
                        ? PhoneThemeColors.success(screen.getPhoneStack())
                        : PhoneThemeColors.danger(screen.getPhoneStack()),
                MoreMenuAction.TOGGLE_BLOCK
        ));
        rows.add(new SheetRow("Eliminar chat", PhoneThemeColors.danger(screen.getPhoneStack()), MoreMenuAction.DELETE_CHAT));
        return rows;
    }

    private int getMoreSheetTop(PhoneScreen screen) {
        int h = getMoreSheetHeight(null, null);
        return screen.getPhoneY() + screen.getPhoneHeight() - MORE_SHEET_BOTTOM_MARGIN - h;
    }

    private int getMoreSheetHeight(WhatsappChat chat, WhatsappState state) {
        int rows = 5;
        return rows * MORE_SHEET_ROW_H + Math.max(0, rows - 1) * MORE_SHEET_ROW_GAP;
    }

    private List<WhatsappChat> getFilteredChats(WhatsappState state, String searchQuery) {
        List<WhatsappChat> source = state.getChats();
        List<WhatsappChat> visible = new ArrayList<>();

        for (WhatsappChat chat : source) {
            if (!chat.archived()) {
                visible.add(chat);
            }
        }

        if (searchQuery == null || searchQuery.isBlank()) {
            return visible;
        }

        String query = normalize(searchQuery);
        List<WhatsappChat> filtered = new ArrayList<>();

        for (WhatsappChat chat : visible) {
            String name = normalize(state.getChatDisplayName(chat));
            String lastMessage = normalize(chat.getLastMessageText());

            if (name.contains(query) || lastMessage.contains(query)) {
                filtered.add(chat);
            }
        }

        return filtered;
    }

    private WhatsappChat findRowAt(
            PhoneScreen screen,
            double mouseX,
            double mouseY,
            WhatsappState state,
            String searchQuery
    ) {
        int x = screen.getPhoneX() + LIST_SIDE_PADDING;
        int y = getListTop(screen);
        int w = screen.getPhoneWidth() - LIST_SIDE_PADDING * 2;
        int visibleHeight = getVisibleHeight(screen);

        if (!screen.isInside(mouseX, mouseY, x, y, w, visibleHeight)) {
            return null;
        }

        List<WhatsappChat> filteredChats = getFilteredChats(state, searchQuery);
        for (int i = 0; i < filteredChats.size(); i++) {
            int rowY = y + i * (ROW_HEIGHT + ROW_GAP) - scrollOffset;
            if (screen.isInside(mouseX, mouseY, x, rowY, w, ROW_HEIGHT)) {
                return filteredChats.get(i);
            }
        }

        return null;
    }

    private int getRowYForChat(
            PhoneScreen screen,
            WhatsappState state,
            String searchQuery,
            String chatId
    ) {
        List<WhatsappChat> filteredChats = getFilteredChats(state, searchQuery);
        int y = getListTop(screen);

        for (int i = 0; i < filteredChats.size(); i++) {
            WhatsappChat chat = filteredChats.get(i);
            if (chat.id().equals(chatId)) {
                return y + i * (ROW_HEIGHT + ROW_GAP) - scrollOffset;
            }
        }

        return Integer.MIN_VALUE;
    }

    private int getRenderedOffset(String chatId) {
        if (chatId == null || swipedChatId == null) {
            return 0;
        }
        return swipedChatId.equals(chatId) ? swipeOffset : 0;
    }

    private String normalize(String value) {
        return value == null ? "" : value.toLowerCase(Locale.ROOT).trim();
    }

    private boolean shouldShowSearchBar() {
        return scrollOffset <= 0;
    }

    private int getListTop(PhoneScreen screen) {
        return screen.getPhoneY() + (shouldShowSearchBar() ? LIST_TOP_WITH_SEARCH : LIST_TOP_NO_SEARCH);
    }

    private int getVisibleHeight(PhoneScreen screen) {
        return screen.getPhoneHeight() - (getListTop(screen) - screen.getPhoneY()) - LIST_BOTTOM_OFFSET;
    }

    private int getContentHeight(WhatsappState state, String searchQuery) {
        int rowCount = getFilteredChats(state, searchQuery).size();
        if (rowCount <= 0) {
            return 0;
        }

        return rowCount * ROW_HEIGHT + Math.max(0, rowCount - 1) * ROW_GAP;
    }

    private int getMaxScroll(PhoneScreen screen, WhatsappState state, String searchQuery) {
        return Math.max(0, getContentHeight(state, searchQuery) - getVisibleHeight(screen));
    }

    private void clampScroll(PhoneScreen screen, WhatsappState state, String searchQuery) {
        int maxScroll = getMaxScroll(screen, state, searchQuery);
        if (scrollOffset < 0) scrollOffset = 0;
        if (scrollOffset > maxScroll) scrollOffset = maxScroll;
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

    private String shorten(String value, int maxLength) {
        if (value == null || value.isEmpty()) {
            return "";
        }
        if (value.length() <= maxLength) {
            return value;
        }
        return value.substring(0, maxLength - 1) + "…";
    }

    public enum ChatRowAction {
        PIN,
        UNPIN,
        MARK_READ,
        MARK_UNREAD,
        ARCHIVE,
        OPEN_MORE
    }

    public enum MoreMenuAction {
        NONE,
        MUTE,
        CONTACT_INFO,
        CLEAR_CHAT,
        TOGGLE_BLOCK,
        DELETE_CHAT
    }

    public record SwipeActionResult(String chatId, ChatRowAction action) {
    }

    private record SheetRow(String label, int color, MoreMenuAction action) {
    }
}