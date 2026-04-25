package santi_moder.roleplaymod.client.phone.app.whatsapp;

import com.mojang.blaze3d.systems.RenderSystem;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import santi_moder.roleplaymod.client.phone.ui.PhoneUi;
import santi_moder.roleplaymod.client.screen.PhoneScreen;

import java.util.ArrayList;
import java.util.List;

public final class WhatsappConversationView {

    private static final int HEADER_HEIGHT = 18;

    private static final int MESSAGES_TOP = 56;
    private static final int MESSAGES_BOTTOM_OFFSET = 42;
    private static final int MESSAGE_SIDE_PADDING = 10;
    private static final int MESSAGE_MAX_WIDTH = 72;
    private static final int MESSAGE_GAP = 4;
    private static final int MESSAGE_TEXT_PADDING_X = 5;
    private static final int MESSAGE_TEXT_PADDING_Y = 3;
    private static final int MESSAGE_LINE_HEIGHT = 7;
    private static final float MESSAGE_TEXT_SCALE = 0.78F;
    private static final int MESSAGE_META_HEIGHT = 8;
    private static final int MESSAGE_META_BOTTOM_PADDING = 2;
    private static final float MESSAGE_META_SCALE = 0.70F;

    private static final int INPUT_HEIGHT = 22;
    private static final int INPUT_SIDE_PADDING = 8;
    private static final int INPUT_BOTTOM_OFFSET = 12;
    private static final int ACTION_BUTTON_WIDTH = 20;
    private static final int ACTION_BUTTON_GAP = 4;
    private static final int INPUT_TEXT_PADDING_X = 4;
    private static final int INPUT_LINE_HEIGHT = 7;
    private static final float INPUT_TEXT_SCALE = 0.78F;
    private static final int INPUT_SCROLL_VISIBLE_LINES = 2;

    private static final int HEADER_INSET_X = 8;
    private static final int HEADER_TOP_Y = 24;

    private static final int HEADER_AVATAR_SIZE = 12;
    private static final int HEADER_AVATAR_X_OFFSET = 32;
    private static final int HEADER_NAME_X_OFFSET = 48;

    private static final int HEADER_ACTION_SIZE = 14;
    private static final int HEADER_CALL_RIGHT_OFFSET = 10;
    private static final int HEADER_VIDEO_GAP = 16;

    private static final int COLOR_HEADER_ICON = 0xFFFFFFFF;
    private static final int COLOR_HEADER_AVATAR = 0xFF25D366;
    private static final int COLOR_HEADER_AVATAR_TEXT = 0xFF081C15;

    private static final int SCROLL_STEP = 12;

    private static final int COLOR_HEADER = 0xAA111111;
    private static final int COLOR_INPUT = 0xCC1A1A1A;
    private static final int COLOR_INPUT_DISABLED = 0x99202020;
    private static final int COLOR_SENT = 0xFF25D366;
    private static final int COLOR_RECEIVED = 0xFF202C33;
    private static final int COLOR_TEXT_DARK = 0xFF081C15;
    private static final int COLOR_TEXT_LIGHT = 0xFFFFFFFF;
    private static final int COLOR_TIME_SENT = 0xCC103B2A;
    private static final int COLOR_TIME_RECEIVED = 0xCCB8C8CC;

    private static final int COLOR_TICK_GRAY = 0xFF98A6AD;
    private static final int COLOR_TICK_BLUE = 0xFF53BDEB;

    private int scrollOffset = 0;
    private int inputScrollOffset = 0;

    public void render(PhoneScreen screen, GuiGraphics guiGraphics, int mouseX, int mouseY, WhatsappState state) {
        WhatsappChat currentChat = state.getSelectedChat();
        WhatsappContact currentContact = state.getSelectedContact();
        if (currentChat == null || currentContact == null) {
            return;
        }

        renderMessages(screen, guiGraphics, state);
        renderHeader(screen, guiGraphics, mouseX, mouseY, state, currentChat, currentContact);
        renderInput(screen, guiGraphics, mouseX, mouseY, state, currentContact);
    }

    public boolean mouseClicked(PhoneScreen screen, double mouseX, double mouseY, int button, WhatsappState state) {
        WhatsappChat currentChat = state.getSelectedChat();
        WhatsappContact currentContact = state.getSelectedContact();

        if (currentChat == null || currentContact == null || button != 0) {
            return false;
        }

        if (PhoneUi.isBackButtonClicked(screen, mouseX, mouseY)) {
            state.backFromChatSubscreen();
            resetViewState();
            return true;
        }

        if (isAvatarClicked(screen, mouseX, mouseY)) {
            state.openSelectedContactPhoto();
            return true;
        }

        if (isNameClicked(screen, mouseX, mouseY, currentContact.displayName())) {
            state.openSelectedContactInfo();
            return true;
        }

        if (!currentContact.blocked() && !state.getDraftMessage().trim().isEmpty() && isSendButton(screen, mouseX, mouseY, state)) {
            sendCurrentDraft(screen, state);
            return true;
        }

        return true;
    }

    public boolean charTyped(char codePoint, int modifiers, WhatsappState state) {
        WhatsappContact currentContact = state.getSelectedContact();
        if (state.getSelectedChat() == null || currentContact == null || currentContact.blocked()) {
            return false;
        }

        if (Character.isISOControl(codePoint)) {
            return false;
        }

        String draft = state.getDraftMessage();
        if (draft.length() >= 200) {
            return true;
        }

        state.setDraftMessage(draft + codePoint);
        inputScrollOffset = 0;
        return true;
    }

    public boolean keyPressed(PhoneScreen screen, int keyCode, WhatsappState state) {
        WhatsappContact currentContact = state.getSelectedContact();
        if (state.getSelectedChat() == null || currentContact == null) {
            return false;
        }

        if (keyCode == 259) {
            if (!currentContact.blocked()) {
                String draft = state.getDraftMessage();
                if (!draft.isEmpty()) {
                    state.setDraftMessage(draft.substring(0, draft.length() - 1));
                }
                inputScrollOffset = 0;
            }
            return true;
        }

        if ((keyCode == 257 || keyCode == 335) && !currentContact.blocked()) {
            sendCurrentDraft(screen, state);
            return true;
        }

        return false;
    }

    public boolean mouseScrolled(PhoneScreen screen, double mouseX, double mouseY, double scrollDelta, WhatsappState state) {
        WhatsappChat currentChat = state.getSelectedChat();
        WhatsappContact currentContact = state.getSelectedContact();
        if (currentChat == null || currentContact == null || scrollDelta == 0.0D) {
            return false;
        }

        int inputX = screen.getPhoneX() + INPUT_SIDE_PADDING;
        int inputY = screen.getPhoneY() + screen.getPhoneHeight() - INPUT_BOTTOM_OFFSET - INPUT_HEIGHT;
        int inputW = screen.getPhoneWidth() - INPUT_SIDE_PADDING * 2;

        if (!currentContact.blocked() && screen.isInside(mouseX, mouseY, inputX, inputY, inputW, INPUT_HEIGHT)) {
            int draftMaxWidth = getDraftMaxWidth(screen, state);
            List<String> wrappedDraft = wrapText(screen, state.getDraftMessage(), draftMaxWidth, INPUT_TEXT_SCALE);
            int maxOffset = Math.max(0, wrappedDraft.size() - INPUT_SCROLL_VISIBLE_LINES);

            if (scrollDelta < 0) {
                inputScrollOffset = Math.min(maxOffset, inputScrollOffset + 1);
            } else {
                inputScrollOffset = Math.max(0, inputScrollOffset - 1);
            }
            return true;
        }

        int maxScroll = Math.max(0, getConversationContentHeight(screen, currentChat) - getVisibleMessagesHeight(screen));
        if (scrollDelta < 0.0D) {
            scrollOffset = Math.min(maxScroll, scrollOffset + SCROLL_STEP);
        } else {
            scrollOffset = Math.max(0, scrollOffset - SCROLL_STEP);
        }

        return true;
    }

    public void onConversationOpened(PhoneScreen screen, WhatsappState state) {
        inputScrollOffset = 0;
        scrollToBottomIfNeeded(screen, state);

        WhatsappChat chat = state.getSelectedChat();
        if (chat != null) {
            santi_moder.roleplaymod.network.ModNetwork.sendWhatsappToServer(
                    new santi_moder.roleplaymod.network.phone.whatsapp.WhatsappMarkChatReadC2SPacket(chat.id())
            );
        }
    }

    public void resetViewState() {
        scrollOffset = 0;
        inputScrollOffset = 0;
    }

    private void renderHeader(
            PhoneScreen screen,
            GuiGraphics guiGraphics,
            int mouseX,
            int mouseY,
            WhatsappState state,
            WhatsappChat currentChat,
            WhatsappContact currentContact
    ) {
        int x1 = screen.getPhoneX() + HEADER_INSET_X;
        int y1 = screen.getPhoneY() + HEADER_TOP_Y;
        int x2 = screen.getPhoneX() + screen.getPhoneWidth() - HEADER_INSET_X;
        int y2 = y1 + HEADER_HEIGHT;

        guiGraphics.fill(x1, y1, x2, y2, COLOR_HEADER);

        PhoneUi.drawBackButton(screen, guiGraphics, mouseX, mouseY);

        int avatarX = screen.getPhoneX() + HEADER_AVATAR_X_OFFSET;
        int avatarY = screen.getPhoneY() + 27;

        guiGraphics.fill(avatarX, avatarY, avatarX + HEADER_AVATAR_SIZE, avatarY + HEADER_AVATAR_SIZE, COLOR_HEADER_AVATAR);
        guiGraphics.drawCenteredString(
                screen.getPhoneFont(),
                currentContact.getInitials(),
                avatarX + HEADER_AVATAR_SIZE / 2,
                avatarY + 3,
                COLOR_HEADER_AVATAR_TEXT
        );

        int nameX = screen.getPhoneX() + HEADER_NAME_X_OFFSET;
        guiGraphics.drawString(screen.getPhoneFont(), state.getChatDisplayName(currentChat), nameX, screen.getPhoneY() + 31, PhoneUi.COLOR_TEXT, false);

        int callX = screen.getPhoneX() + screen.getPhoneWidth() - HEADER_CALL_RIGHT_OFFSET - HEADER_ACTION_SIZE;
        int videoX = callX - HEADER_VIDEO_GAP;

        guiGraphics.drawString(screen.getPhoneFont(), "L", callX, screen.getPhoneY() + 31, COLOR_HEADER_ICON, false);
        guiGraphics.drawString(screen.getPhoneFont(), "V", videoX, screen.getPhoneY() + 31, COLOR_HEADER_ICON, false);
    }

    private void renderMessages(PhoneScreen screen, GuiGraphics guiGraphics, WhatsappState state) {
        WhatsappChat currentChat = state.getSelectedChat();
        if (currentChat == null) {
            return;
        }

        int visibleTop = screen.getPhoneY() + MESSAGES_TOP;
        int visibleBottom = screen.getPhoneY() + screen.getPhoneHeight() - MESSAGES_BOTTOM_OFFSET;
        int visibleHeight = visibleBottom - visibleTop;

        clampConversationScroll(screen, currentChat);

        enableScissor(screen, screen.getPhoneX() + 8, visibleTop, screen.getPhoneWidth() - 16, visibleHeight);

        int currentY = visibleTop - scrollOffset;

        for (WhatsappMessage message : currentChat.messages()) {
            List<String> wrappedLines = wrapMessage(screen, message.text(), MESSAGE_MAX_WIDTH - MESSAGE_TEXT_PADDING_X * 2);

            int longestLineWidth = 0;
            for (String line : wrappedLines) {
                longestLineWidth = Math.max(longestLineWidth, scaledTextWidth(screen, line, MESSAGE_TEXT_SCALE));
            }

            String timeText = message.timeText();
            int timeWidth = scaledTextWidth(screen, timeText, MESSAGE_META_SCALE);
            int tickWidth = message.sentByMe() ? 10 : 0;
            int metaWidth = timeWidth + (message.sentByMe() ? tickWidth + 2 : 0);

            int bubbleWidth = Math.min(
                    MESSAGE_MAX_WIDTH,
                    Math.max(longestLineWidth + MESSAGE_TEXT_PADDING_X * 2, metaWidth + MESSAGE_TEXT_PADDING_X * 2 + 2)
            );

            int textHeight = wrappedLines.size() * MESSAGE_LINE_HEIGHT;
            int bubbleHeight = MESSAGE_TEXT_PADDING_Y * 2 + textHeight + MESSAGE_META_HEIGHT + MESSAGE_META_BOTTOM_PADDING;
            int totalBlockHeight = bubbleHeight + MESSAGE_GAP;

            int x = message.sentByMe()
                    ? screen.getPhoneX() + screen.getPhoneWidth() - MESSAGE_SIDE_PADDING - bubbleWidth
                    : screen.getPhoneX() + MESSAGE_SIDE_PADDING;

            int color = message.sentByMe() ? COLOR_SENT : COLOR_RECEIVED;
            int textColor = message.sentByMe() ? COLOR_TEXT_DARK : COLOR_TEXT_LIGHT;
            int timeColor = message.sentByMe() ? COLOR_TIME_SENT : COLOR_TIME_RECEIVED;

            guiGraphics.fill(x, currentY, x + bubbleWidth, currentY + bubbleHeight, color);

            int lineY = currentY + MESSAGE_TEXT_PADDING_Y;
            for (String line : wrappedLines) {
                drawScaledText(guiGraphics, screen, line, x + MESSAGE_TEXT_PADDING_X, lineY, textColor, MESSAGE_TEXT_SCALE);
                lineY += MESSAGE_LINE_HEIGHT;
            }

            renderMessageMetaInsideBubble(screen, guiGraphics, message, x, currentY, bubbleWidth, bubbleHeight, timeColor);
            currentY += totalBlockHeight;
        }

        disableScissor();
    }

    private void renderMessageMetaInsideBubble(
            PhoneScreen screen,
            GuiGraphics guiGraphics,
            WhatsappMessage message,
            int bubbleX,
            int bubbleY,
            int bubbleWidth,
            int bubbleHeight,
            int timeColor
    ) {
        String timeText = message.timeText();
        int timeWidth = scaledTextWidth(screen, timeText, MESSAGE_META_SCALE);
        int tickWidth = message.sentByMe() ? 10 : 0;
        int totalMetaWidth = timeWidth + (message.sentByMe() ? tickWidth + 2 : 0);

        int metaX = bubbleX + bubbleWidth - totalMetaWidth - 4;
        int metaY = bubbleY + bubbleHeight - MESSAGE_META_HEIGHT - 1;

        drawScaledText(guiGraphics, screen, timeText, metaX, metaY + 1, timeColor, MESSAGE_META_SCALE);

        if (message.sentByMe()) {
            renderTicks(guiGraphics, metaX + timeWidth + 2, metaY, message.status());
        }
    }

    private void renderTicks(GuiGraphics guiGraphics, int x, int y, WhatsappMessageStatus status) {
        switch (status) {
            case PENDING -> guiGraphics.drawString(Minecraft.getInstance().font, "✓", x, y, COLOR_TICK_GRAY, false);
            case SENT -> guiGraphics.drawString(Minecraft.getInstance().font, "✓✓", x, y, COLOR_TICK_GRAY, false);
            case READ -> guiGraphics.drawString(Minecraft.getInstance().font, "✓✓", x, y, COLOR_TICK_BLUE, false);
        }
    }

    private void renderInput(PhoneScreen screen, GuiGraphics guiGraphics, int mouseX, int mouseY, WhatsappState state, WhatsappContact contact) {
        int x = screen.getPhoneX() + INPUT_SIDE_PADDING;
        int y = screen.getPhoneY() + screen.getPhoneHeight() - INPUT_BOTTOM_OFFSET - INPUT_HEIGHT;
        int totalWidth = screen.getPhoneWidth() - INPUT_SIDE_PADDING * 2;

        String draftMessage = state.getDraftMessage();
        boolean hasText = !draftMessage.trim().isEmpty();
        boolean blocked = contact.blocked();

        int rightButtonsWidth = hasText && !blocked
                ? ACTION_BUTTON_WIDTH
                : ACTION_BUTTON_WIDTH * 2 + ACTION_BUTTON_GAP;

        int inputWidth = totalWidth - rightButtonsWidth - 4;

        guiGraphics.fill(x, y, x + inputWidth, y + INPUT_HEIGHT, blocked ? COLOR_INPUT_DISABLED : COLOR_INPUT);

        if (blocked) {
            drawScaledText(guiGraphics, screen, "No podés escribir a este contacto", x + INPUT_TEXT_PADDING_X, y + 7, PhoneUi.COLOR_HINT, INPUT_TEXT_SCALE);
        } else {
            int draftMaxWidth = inputWidth - INPUT_TEXT_PADDING_X * 2;
            List<String> wrappedDraft = wrapText(screen, draftMessage, draftMaxWidth, INPUT_TEXT_SCALE);

            if (draftMessage.isEmpty()) {
                drawScaledText(guiGraphics, screen, "Mensaje...", x + INPUT_TEXT_PADDING_X, y + 7, PhoneUi.COLOR_HINT, INPUT_TEXT_SCALE);
            } else {
                int visibleLines = INPUT_SCROLL_VISIBLE_LINES;
                int maxOffset = Math.max(0, wrappedDraft.size() - visibleLines);

                if (inputScrollOffset > maxOffset) inputScrollOffset = maxOffset;
                if (inputScrollOffset < 0) inputScrollOffset = 0;

                int firstVisibleLine = Math.max(0, wrappedDraft.size() - visibleLines - inputScrollOffset);
                int lastExclusive = Math.min(wrappedDraft.size(), firstVisibleLine + visibleLines);

                enableScissor(screen, x, y, inputWidth, INPUT_HEIGHT);

                int totalTextHeight = (lastExclusive - firstVisibleLine) * INPUT_LINE_HEIGHT;
                int lineY = y + (INPUT_HEIGHT - totalTextHeight) / 2;

                for (int i = firstVisibleLine; i < lastExclusive; i++) {
                    drawScaledText(guiGraphics, screen, wrappedDraft.get(i), x + INPUT_TEXT_PADDING_X, lineY, PhoneUi.COLOR_TEXT, INPUT_TEXT_SCALE);
                    lineY += INPUT_LINE_HEIGHT;
                }

                disableScissor();
            }
        }

        if (blocked || !hasText) {
            int camX = x + inputWidth + 4;
            int audioX = camX + ACTION_BUTTON_WIDTH + ACTION_BUTTON_GAP;

            guiGraphics.fill(camX, y, camX + ACTION_BUTTON_WIDTH, y + INPUT_HEIGHT, 0xFF2A3942);
            guiGraphics.fill(audioX, y, audioX + ACTION_BUTTON_WIDTH, y + INPUT_HEIGHT, 0xFF2A3942);

            guiGraphics.drawCenteredString(screen.getPhoneFont(), blocked ? "-" : "Cam", camX + ACTION_BUTTON_WIDTH / 2, y + 7, PhoneUi.COLOR_TEXT);
            guiGraphics.drawCenteredString(screen.getPhoneFont(), blocked ? "-" : "Mic", audioX + ACTION_BUTTON_WIDTH / 2, y + 7, PhoneUi.COLOR_TEXT);
        } else {
            int sendX = x + inputWidth + 4;
            boolean hoverSend = screen.isInside(mouseX, mouseY, sendX, y, ACTION_BUTTON_WIDTH, INPUT_HEIGHT);

            guiGraphics.fill(sendX, y, sendX + ACTION_BUTTON_WIDTH, y + INPUT_HEIGHT, 0xFF25D366);
            guiGraphics.drawCenteredString(screen.getPhoneFont(), ">", sendX + ACTION_BUTTON_WIDTH / 2, y + 7, hoverSend ? 0xFFFFFFFF : 0xFF081C15);
        }
    }

    private int getDraftMaxWidth(PhoneScreen screen, WhatsappState state) {
        String draft = state.getDraftMessage();
        int totalWidth = screen.getPhoneWidth() - INPUT_SIDE_PADDING * 2;

        int rightButtonsWidth = draft.trim().isEmpty()
                ? ACTION_BUTTON_WIDTH * 2 + ACTION_BUTTON_GAP
                : ACTION_BUTTON_WIDTH;

        int inputWidth = totalWidth - rightButtonsWidth - 4;
        return inputWidth - INPUT_TEXT_PADDING_X * 2;
    }

    private int getVisibleMessagesHeight(PhoneScreen screen) {
        int visibleTop = screen.getPhoneY() + MESSAGES_TOP;
        int visibleBottom = screen.getPhoneY() + screen.getPhoneHeight() - MESSAGES_BOTTOM_OFFSET;
        return visibleBottom - visibleTop;
    }

    private int getConversationContentHeight(PhoneScreen screen, WhatsappChat chat) {
        int total = 0;

        for (WhatsappMessage message : chat.messages()) {
            List<String> wrappedLines = wrapMessage(screen, message.text(), MESSAGE_MAX_WIDTH - MESSAGE_TEXT_PADDING_X * 2);
            int textHeight = wrappedLines.size() * MESSAGE_LINE_HEIGHT;
            int bubbleHeight = MESSAGE_TEXT_PADDING_Y * 2 + textHeight + MESSAGE_META_HEIGHT + MESSAGE_META_BOTTOM_PADDING;
            total += bubbleHeight + MESSAGE_GAP;
        }

        return total;
    }

    private void clampConversationScroll(PhoneScreen screen, WhatsappChat chat) {
        int maxScroll = Math.max(0, getConversationContentHeight(screen, chat) - getVisibleMessagesHeight(screen));
        if (scrollOffset < 0) scrollOffset = 0;
        if (scrollOffset > maxScroll) scrollOffset = maxScroll;
    }

    private void scrollToBottomIfNeeded(PhoneScreen screen, WhatsappState state) {
        WhatsappChat currentChat = state.getSelectedChat();
        if (currentChat == null) {
            scrollOffset = 0;
            return;
        }

        int maxScroll = Math.max(0, getConversationContentHeight(screen, currentChat) - getVisibleMessagesHeight(screen));
        scrollOffset = maxScroll;
    }

    private void sendCurrentDraft(PhoneScreen screen, WhatsappState state) {
        String trimmed = state.getDraftMessage().trim();
        if (trimmed.isEmpty()) {
            return;
        }

        WhatsappContact contact = state.getSelectedContact();
        if (contact == null) {
            return;
        }

        santi_moder.roleplaymod.network.ModNetwork.sendWhatsappToServer(
                new santi_moder.roleplaymod.network.phone.whatsapp.WhatsappSendMessageC2SPacket(
                        contact.id(),
                        trimmed
                )
        );

        state.clearDraftMessage();
        inputScrollOffset = 0;
    }

    private boolean isSendButton(PhoneScreen screen, double mouseX, double mouseY, WhatsappState state) {
        if (state.getDraftMessage().trim().isEmpty()) {
            return false;
        }

        int x = screen.getPhoneX() + INPUT_SIDE_PADDING;
        int y = screen.getPhoneY() + screen.getPhoneHeight() - INPUT_BOTTOM_OFFSET - INPUT_HEIGHT;
        int totalWidth = screen.getPhoneWidth() - INPUT_SIDE_PADDING * 2;
        int inputWidth = totalWidth - ACTION_BUTTON_WIDTH - 4;

        return screen.isInside(mouseX, mouseY, x + inputWidth + 4, y, ACTION_BUTTON_WIDTH, INPUT_HEIGHT);
    }

    private boolean isAvatarClicked(PhoneScreen screen, double mouseX, double mouseY) {
        int avatarX = screen.getPhoneX() + HEADER_AVATAR_X_OFFSET;
        int avatarY = screen.getPhoneY() + 27;
        return screen.isInside(mouseX, mouseY, avatarX, avatarY, HEADER_AVATAR_SIZE, HEADER_AVATAR_SIZE);
    }

    private boolean isNameClicked(PhoneScreen screen, double mouseX, double mouseY, String name) {
        int x = screen.getPhoneX() + HEADER_NAME_X_OFFSET;
        int y = screen.getPhoneY() + 31;
        int width = screen.getPhoneFont().width(name);
        return screen.isInside(mouseX, mouseY, x, y, width, 10);
    }

    private List<String> wrapMessage(PhoneScreen screen, String text, int maxWidth) {
        return wrapText(screen, text, maxWidth, MESSAGE_TEXT_SCALE);
    }

    private List<String> wrapText(PhoneScreen screen, String text, int maxWidth, float scale) {
        List<String> lines = new ArrayList<>();

        if (text == null || text.isEmpty()) {
            return lines;
        }

        String[] words = text.split(" ");
        StringBuilder currentLine = new StringBuilder();

        for (String word : words) {
            String candidate = currentLine.isEmpty() ? word : currentLine + " " + word;

            if (scaledTextWidth(screen, candidate, scale) <= maxWidth) {
                currentLine.setLength(0);
                currentLine.append(candidate);
            } else {
                if (!currentLine.isEmpty()) {
                    lines.add(currentLine.toString());
                    currentLine.setLength(0);
                }

                if (scaledTextWidth(screen, word, scale) <= maxWidth) {
                    currentLine.append(word);
                } else {
                    lines.addAll(breakLongWord(screen, word, maxWidth, scale));
                }
            }
        }

        if (!currentLine.isEmpty()) {
            lines.add(currentLine.toString());
        }

        return lines;
    }

    private List<String> breakLongWord(PhoneScreen screen, String word, int maxWidth, float scale) {
        List<String> parts = new ArrayList<>();
        StringBuilder current = new StringBuilder();

        for (int i = 0; i < word.length(); i++) {
            char c = word.charAt(i);
            String candidate = current + String.valueOf(c);

            if (scaledTextWidth(screen, candidate, scale) <= maxWidth) {
                current.append(c);
            } else {
                if (!current.isEmpty()) {
                    parts.add(current.toString());
                    current.setLength(0);
                }
                current.append(c);
            }
        }

        if (!current.isEmpty()) {
            parts.add(current.toString());
        }

        return parts;
    }

    private int scaledTextWidth(PhoneScreen screen, String text, float scale) {
        return Math.round(screen.getPhoneFont().width(text) * scale);
    }

    private void drawScaledText(
            GuiGraphics guiGraphics,
            PhoneScreen screen,
            String text,
            int x,
            int y,
            int color,
            float scale
    ) {
        guiGraphics.pose().pushPose();
        guiGraphics.pose().scale(scale, scale, 1.0F);
        guiGraphics.drawString(screen.getPhoneFont(), text, (int) (x / scale), (int) (y / scale), color, false);
        guiGraphics.pose().popPose();
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