package santi_moder.roleplaymod.client.phone.app.whatsapp;

import com.mojang.blaze3d.systems.RenderSystem;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.resources.ResourceLocation;
import santi_moder.roleplaymod.RolePlayMod;
import santi_moder.roleplaymod.client.phone.ui.PhoneThemeColors;
import santi_moder.roleplaymod.client.phone.ui.PhoneUi;
import santi_moder.roleplaymod.client.screen.PhoneScreen;
import santi_moder.roleplaymod.common.whatsapp.model.WhatsappChat;
import santi_moder.roleplaymod.common.whatsapp.model.WhatsappContact;
import santi_moder.roleplaymod.common.whatsapp.model.WhatsappMessage;
import santi_moder.roleplaymod.common.whatsapp.model.WhatsappMessageStatus;

import java.util.ArrayList;
import java.util.List;

public final class WhatsappConversationView {

    private static final int HEADER_HEIGHT = 18;

    private static final int MESSAGES_TOP = 44;
    private static final int MESSAGES_BOTTOM_OFFSET = 34;
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

    private static final int INPUT_HEIGHT = 18;
    private static final int INPUT_SIDE_PADDING = 8;
    private static final int INPUT_BOTTOM_OFFSET = 12;
    private static final int ACTION_BUTTON_WIDTH = 18;
    private static final int INPUT_AREA_PADDING_TOP = 4;
    private static final int ACTION_BUTTON_GAP = 4;
    private static final int INPUT_TEXT_PADDING_X = 4;
    private static final int INPUT_LINE_HEIGHT = 7;
    private static final float INPUT_TEXT_SCALE = 0.78F;
    private static final int INPUT_SCROLL_VISIBLE_LINES = 2;

    private static final int HEADER_INSET_X = 8;
    private static final int HEADER_TOP_Y = 22;

    private static final int HEADER_AVATAR_SIZE = 12;
    private static final int HEADER_AVATAR_X_OFFSET = 32;
    private static final int HEADER_NAME_X_OFFSET = 48;

    private static final int HEADER_ACTION_SIZE = 14;
    private static final int HEADER_CALL_RIGHT_OFFSET = 10;
    private static final int HEADER_VIDEO_GAP = 16;

    private static final int SCROLL_STEP = 12;

    private int scrollOffset = 0;
    private int inputScrollOffset = 0;

    private static final ResourceLocation CHAT_BG_LIGHT =
            new ResourceLocation("roleplaymod", "textures/gui/phone/chat_bg.png");

    private static final ResourceLocation CHAT_BG_DARK =
            new ResourceLocation("roleplaymod", "textures/gui/phone/chat_bg_dark.png");

    private static final ResourceLocation TICK_PENDING_TEXTURE =
            new ResourceLocation(RolePlayMod.MOD_ID, "textures/gui/phone/tick_pending.png");

    private static final ResourceLocation TICK_SENT_TEXTURE =
            new ResourceLocation(RolePlayMod.MOD_ID, "textures/gui/phone/tick_sent.png");

    private static final ResourceLocation TICK_READ_TEXTURE =
            new ResourceLocation(RolePlayMod.MOD_ID, "textures/gui/phone/tick_read.png");

    private static final ResourceLocation ICON_MIC =
            new ResourceLocation(RolePlayMod.MOD_ID, "textures/gui/phone/icon_mic.png");

    private static final ResourceLocation ICON_CAM =
            new ResourceLocation(RolePlayMod.MOD_ID, "textures/gui/phone/icon_cam.png");

    private static final ResourceLocation ICON_CALL =
            new ResourceLocation(RolePlayMod.MOD_ID, "textures/gui/phone/icon_call.png");

    private static final ResourceLocation ICON_VIDEO =
            new ResourceLocation(RolePlayMod.MOD_ID, "textures/gui/phone/icon_video.png");

    private static final ResourceLocation ICON_SEND =
            new ResourceLocation(RolePlayMod.MOD_ID, "textures/gui/phone/icon_send.png");

    private static final ResourceLocation ICON_MIC_DARK =
            new ResourceLocation(RolePlayMod.MOD_ID, "textures/gui/phone/icon_mic_dark.png");

    private static final ResourceLocation ICON_CAM_DARK =
            new ResourceLocation(RolePlayMod.MOD_ID, "textures/gui/phone/icon_cam_dark.png");

    private static final ResourceLocation ICON_CALL_DARK =
            new ResourceLocation(RolePlayMod.MOD_ID, "textures/gui/phone/icon_call_dark.png");

    private static final ResourceLocation ICON_VIDEO_DARK =
            new ResourceLocation(RolePlayMod.MOD_ID, "textures/gui/phone/icon_video_dark.png");

    private static final ResourceLocation ICON_SEND_DARK =
            new ResourceLocation(RolePlayMod.MOD_ID, "textures/gui/phone/icon_send_dark.png");

    private static final int TICK_PENDING_WIDTH = 6;
    private static final int TICK_SENT_WIDTH = 10;
    private static final int TICK_READ_WIDTH = 10;
    private static final int TICK_HEIGHT = 6;

    public void render(PhoneScreen screen, GuiGraphics guiGraphics, int mouseX, int mouseY, WhatsappState state) {
        WhatsappChat currentChat = state.getSelectedChat();
        WhatsappContact currentContact = state.getSelectedContact();
        if (currentChat == null || currentContact == null) {
            return;
        }

        renderBaseBackground(screen, guiGraphics);
        renderChatWallpaper(screen, guiGraphics);
        renderMessages(screen, guiGraphics, state);
        renderInputBackground(screen, guiGraphics);
        renderHeader(screen, guiGraphics, mouseX, mouseY, state, currentChat, currentContact);
        renderInput(screen, guiGraphics, mouseX, mouseY, state, currentContact);
    }

    private void renderBaseBackground(PhoneScreen screen, GuiGraphics guiGraphics) {
        guiGraphics.fill(
                screen.getPhoneX() + 4,
                screen.getPhoneY() + 4,
                screen.getPhoneX() + screen.getPhoneWidth() - 4,
                screen.getPhoneY() + screen.getPhoneHeight() - 4,
                PhoneThemeColors.appBackground(screen.getPhoneStack())
        );
    }

    private void renderChatWallpaper(PhoneScreen screen, GuiGraphics guiGraphics) {
        int x = screen.getPhoneX() + 8;
        int y = screen.getPhoneY() + 42;
        int w = screen.getPhoneWidth() - 16;
        int h = screen.getPhoneHeight() - 64;

        ResourceLocation wallpaper = PhoneThemeColors.isDark(screen.getPhoneStack())
                ? CHAT_BG_DARK
                : CHAT_BG_LIGHT;

        guiGraphics.blit(
                wallpaper,
                x,
                y,
                0,
                0,
                w,
                h,
                w,
                h
        );
    }

    private void renderInputBackground(PhoneScreen screen, GuiGraphics guiGraphics) {
        int x1 = screen.getPhoneX() + 4;
        int y1 = screen.getPhoneY() + screen.getPhoneHeight() - INPUT_BOTTOM_OFFSET - INPUT_HEIGHT - INPUT_AREA_PADDING_TOP;
        int x2 = screen.getPhoneX() + screen.getPhoneWidth() - 4;
        int y2 = screen.getPhoneY() + screen.getPhoneHeight() - 4;

        guiGraphics.fill(
                x1,
                y1,
                x2,
                y2,
                PhoneThemeColors.appBackground(screen.getPhoneStack())
        );
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

        guiGraphics.fill(x1, y1, x2, y2, PhoneThemeColors.whatsappHeader(screen.getPhoneStack()));

        PhoneUi.drawBackButton(screen, guiGraphics, mouseX, mouseY);

        int avatarX = screen.getPhoneX() + HEADER_AVATAR_X_OFFSET;
        int avatarY = screen.getPhoneY() + 25;

        WhatsappTextureResolver.drawProfilePhoto(
                guiGraphics,
                currentContact.photoId(),
                avatarX,
                avatarY,
                HEADER_AVATAR_SIZE
        );

        int nameX = screen.getPhoneX() + HEADER_NAME_X_OFFSET;
        guiGraphics.drawString(
        screen.getPhoneFont(),
        state.getChatDisplayName(currentChat),
        nameX,
                screen.getPhoneY() + 29,
                PhoneThemeColors.text(screen.getPhoneStack()),
        false
);

        int callX = screen.getPhoneX() + screen.getPhoneWidth() - HEADER_CALL_RIGHT_OFFSET - HEADER_ACTION_SIZE;
        int videoX = callX - HEADER_VIDEO_GAP;

        drawIcon(guiGraphics, themedIcon(screen, ICON_CALL, ICON_CALL_DARK), callX - ACTION_BUTTON_WIDTH / 2, screen.getPhoneY() + 24);
        drawIcon(guiGraphics, themedIcon(screen, ICON_VIDEO, ICON_VIDEO_DARK), videoX - ACTION_BUTTON_WIDTH / 2, screen.getPhoneY() + 24);

    }

    private void drawIcon(GuiGraphics guiGraphics, ResourceLocation texture, int x, int y) {
        int size = 12;

        guiGraphics.blit(
                texture,
                x + (ACTION_BUTTON_WIDTH - size) / 2,
                y + (INPUT_HEIGHT - size) / 2,
                0,
                0,
                size,
                size,
                size,
                size
        );
    }

    private ResourceLocation themedIcon(PhoneScreen screen, ResourceLocation lightIcon, ResourceLocation darkIcon) {
        return PhoneThemeColors.isDark(screen.getPhoneStack()) ? darkIcon : lightIcon;
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
            int tickWidth = message.sentByMe() ? getTickWidth(message.status()) : 0;
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

            int color = message.sentByMe()
                    ? PhoneThemeColors.whatsappBubbleSent(screen.getPhoneStack())
                    : PhoneThemeColors.whatsappBubbleReceived(screen.getPhoneStack());

            int textColor = message.sentByMe()
                    ? PhoneThemeColors.whatsappMessageTextSent(screen.getPhoneStack())
                    : PhoneThemeColors.whatsappMessageTextReceived(screen.getPhoneStack());

            int timeColor = message.sentByMe()
                    ? PhoneThemeColors.whatsappMetaSent(screen.getPhoneStack())
                    : PhoneThemeColors.whatsappMetaReceived(screen.getPhoneStack());

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
        int tickWidth = message.sentByMe() ? getTickWidth(message.status()) : 0;
        int totalMetaWidth = timeWidth + (message.sentByMe() ? tickWidth + 2 : 0);

        int metaX = bubbleX + bubbleWidth - totalMetaWidth - 4;
        int metaY = bubbleY + bubbleHeight - MESSAGE_META_HEIGHT - 1;

        drawScaledText(guiGraphics, screen, timeText, metaX, metaY + 1, timeColor, MESSAGE_META_SCALE);

        if (message.sentByMe()) {
            renderTicks(guiGraphics, metaX + timeWidth + 2, metaY, message.status());
        }
    }

    private int getTickWidth(WhatsappMessageStatus status) {
        return switch (status) {
            case PENDING -> TICK_PENDING_WIDTH;
            case SENT -> TICK_SENT_WIDTH;
            case READ -> TICK_READ_WIDTH;
        };
    }

    private void renderTicks(GuiGraphics guiGraphics, int x, int y, WhatsappMessageStatus status) {
        ResourceLocation texture = switch (status) {
            case PENDING -> TICK_PENDING_TEXTURE;
            case SENT -> TICK_SENT_TEXTURE;
            case READ -> TICK_READ_TEXTURE;
        };

        int width = getTickWidth(status);

        guiGraphics.blit(
                texture,
                x,
                y + 2,
                0,
                0,
                width,
                TICK_HEIGHT,
                width,
                TICK_HEIGHT
        );
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

        guiGraphics.fill(
        x,
        y,
        x + inputWidth,
        y + INPUT_HEIGHT,
        blocked ? PhoneThemeColors.disabledInput(screen.getPhoneStack()) : PhoneThemeColors.input(screen.getPhoneStack())
);

        if (blocked) {
            drawScaledText(guiGraphics, screen, "No podés escribir a este contacto", x + INPUT_TEXT_PADDING_X, y + 7, PhoneThemeColors.hint(screen.getPhoneStack()), INPUT_TEXT_SCALE);
        } else {
            int draftMaxWidth = inputWidth - INPUT_TEXT_PADDING_X * 2;
            List<String> wrappedDraft = wrapText(screen, draftMessage, draftMaxWidth, INPUT_TEXT_SCALE);

            if (draftMessage.isEmpty()) {
                drawScaledText(guiGraphics, screen, "Mensaje...", x + INPUT_TEXT_PADDING_X, y + 7, PhoneThemeColors.hint(screen.getPhoneStack()), INPUT_TEXT_SCALE);
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
                    drawScaledText(guiGraphics, screen, wrappedDraft.get(i), x + INPUT_TEXT_PADDING_X, lineY, PhoneThemeColors.text(screen.getPhoneStack()), INPUT_TEXT_SCALE);
                    lineY += INPUT_LINE_HEIGHT;
                }

                disableScissor();
            }
        }

        if (blocked || !hasText) {
            int camX = x + inputWidth + 4;
            int audioX = camX + ACTION_BUTTON_WIDTH + ACTION_BUTTON_GAP;

            if (!blocked) {
                drawIcon(guiGraphics, themedIcon(screen, ICON_CAM, ICON_CAM_DARK), camX, y);
                drawIcon(guiGraphics, themedIcon(screen, ICON_MIC, ICON_MIC_DARK), audioX, y);
            }

        } else {
            int sendX = x + inputWidth + 4;
            drawIcon(guiGraphics, themedIcon(screen, ICON_SEND, ICON_SEND_DARK), sendX, y);
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
                        contact.phoneNumber(),
                        trimmed
                )
        );

        state.clearDraftMessage();
        inputScrollOffset = 0;
        scrollToBottomIfNeeded(screen, state);
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
        int avatarY = screen.getPhoneY() + 25;
        return screen.isInside(mouseX, mouseY, avatarX, avatarY, HEADER_AVATAR_SIZE, HEADER_AVATAR_SIZE);
    }

    private boolean isNameClicked(PhoneScreen screen, double mouseX, double mouseY, String name) {
        int x = screen.getPhoneX() + HEADER_NAME_X_OFFSET;
        int y = screen.getPhoneY() + 29;
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