package santi_moder.roleplaymod.client.phone.app;

import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.world.item.ItemStack;
import santi_moder.roleplaymod.client.phone.app.whatsapp.*;
import santi_moder.roleplaymod.client.phone.ui.PhoneUi;
import santi_moder.roleplaymod.client.screen.PhoneScreen;
import santi_moder.roleplaymod.common.phone.PhoneAppId;
import santi_moder.roleplaymod.common.phone.PhoneWhatsappData;
import santi_moder.roleplaymod.network.ModNetwork;
import santi_moder.roleplaymod.network.phone.whatsapp.*;

public class WhatsappPhoneApp extends AbstractPhoneApp {

    private boolean requestedInitialSync = false;
    private static final int TITLE_Y = 34;
    private static final int CONTENT_Y = 70;
    private static final int SEARCH_MAX_LENGTH = 40;

    private final WhatsappBottomNav bottomNav = new WhatsappBottomNav();
    private final WhatsappChatsView chatsView = new WhatsappChatsView();
    private final WhatsappConversationView conversationView = new WhatsappConversationView();
    private final WhatsappContactInfoView contactInfoView = new WhatsappContactInfoView();
    private final WhatsappContactPhotoView contactPhotoView = new WhatsappContactPhotoView();
    private final WhatsappNewChatView newChatView = new WhatsappNewChatView();
    private final WhatsappNewContactView newContactView = new WhatsappNewContactView();
    private final WhatsappProfileView profileView = new WhatsappProfileView();
    private final WhatsappState state = new WhatsappState();

    private WhatsappTab activeTab = WhatsappTab.CHATS;
    private String chatsSearchQuery = "";
    private boolean waitingOpenConversationFromServer = false;

    public WhatsappPhoneApp() {
        super(PhoneAppId.WHATSAPP);
    }

    @Override
    public void onOpen(PhoneScreen screen, ItemStack phoneStack) {
        WhatsappState loaded = PhoneWhatsappData.loadState(phoneStack);

        state.replaceChats(loaded.getChats());
        state.replaceContacts(loaded.getContacts());
        state.replacePresences(loaded.getPresences());
        state.setSelectedChatId(null);
        state.setDraftMessage("");
        state.setProfile(loaded.getProfile());
        state.setChatScreen(WhatsappChatScreen.LIST);
        state.clearNewContactDraft();

        state.setLocalUserInServer(loaded.isLocalUserInServer());
        state.setLocalUserHasInternet(loaded.isLocalUserHasInternet());
        state.setLocalUserHasBattery(loaded.isLocalUserHasBattery());

        activeTab = WhatsappTab.CHATS;
        chatsSearchQuery = "";

        conversationView.resetViewState();
        contactInfoView.resetViewState();
        newChatView.resetViewState();
        newContactView.resetViewState();
        chatsView.resetScroll();
        chatsView.closeSwipe();
        chatsView.closeMoreMenu();
        profileView.resetTransientState();
        requestedInitialSync = false;
        waitingOpenConversationFromServer = false;
    }

    @Override
    public void onClose(PhoneScreen screen, ItemStack phoneStack) {
        state.closeChat();
        state.setDraftMessage("");
        state.clearNewContactDraft();
        chatsSearchQuery = "";
        profileView.resetTransientState();
        PhoneWhatsappData.saveState(phoneStack, state);
        waitingOpenConversationFromServer = false;
    }

    @Override
    public void tick(PhoneScreen screen, ItemStack phoneStack) {
        if (!requestedInitialSync) {
            requestedInitialSync = true;
            ModNetwork.sendWhatsappToServer(new WhatsappRequestInitialStateC2SPacket());
        }

        var pending = WhatsappClientSyncApplier.consumePendingInitialSnapshot();
        if (pending != null) {
            WhatsappClientSyncApplier.applySnapshotToState(state, pending);
        }

        var chatPayloads = WhatsappClientSyncApplier.consumePendingChatPayloads();
        for (var payload : chatPayloads) {
            WhatsappClientSyncApplier.applyChatPayloadToState(state, payload);
        }

        var statusPayloads = WhatsappClientSyncApplier.consumePendingStatusPayloads();
        for (var payload : statusPayloads) {
            WhatsappClientSyncApplier.applyMessageStatusPayloadToState(state, payload);
        }

        var presencePayloads = WhatsappClientSyncApplier.consumePendingPresencePayloads();
        for (var payload : presencePayloads) {
            WhatsappClientSyncApplier.applyPresencePayloadToState(state, payload);
        }

        var contactPayloads = WhatsappClientSyncApplier.consumePendingContactPayloads();
        for (var payload : contactPayloads) {
            WhatsappClientSyncApplier.applyContactPayloadToState(state, payload);
        }

        var clearedChatPayloads = WhatsappClientSyncApplier.consumePendingClearedChatPayloads();
        for (var payload : clearedChatPayloads) {
            WhatsappClientSyncApplier.applyClearedChatPayloadToState(state, payload);
        }

        var createdContactPayloads = WhatsappClientSyncApplier.consumePendingCreatedContactPayloads();
        for (var payload : createdContactPayloads) {
            WhatsappClientSyncApplier.applyCreatedContactPayloadToState(state, payload);
        }

        var openedChatPayloads = WhatsappClientSyncApplier.consumePendingOpenedChatPayloads();
        for (var payload : openedChatPayloads) {
            WhatsappClientSyncApplier.applyOpenedChatPayloadToState(state, payload);

            if (waitingOpenConversationFromServer) {
                state.openChat(payload.id());
                conversationView.onConversationOpened(screen, state);
                activeTab = WhatsappTab.CHATS;
                chatsSearchQuery = "";
                chatsView.resetScroll();
                waitingOpenConversationFromServer = false;
            }
        }

        var deletedChatIds = WhatsappClientSyncApplier.consumePendingDeletedChatIds();
        for (var chatId : deletedChatIds) {
            WhatsappClientSyncApplier.applyDeletedChatIdToState(state, chatId);
        }

        PhoneWhatsappData.saveState(phoneStack, state);
    }

    @Override
    public void render(PhoneScreen screen, GuiGraphics guiGraphics, int mouseX, int mouseY, float partialTick) {
        switch (activeTab) {
            case CHATS -> renderChatsTab(screen, guiGraphics, mouseX, mouseY);
            case YOU -> profileView.render(screen, guiGraphics, mouseX, mouseY, state);
            case UPDATES, CALLS -> renderPlaceholderTab(screen, guiGraphics);
        }

        if (!(activeTab == WhatsappTab.CHATS && state.getChatScreen() != WhatsappChatScreen.LIST)) {
            bottomNav.render(screen, guiGraphics, mouseX, mouseY, activeTab);
        }
    }

    private void renderChatsTab(PhoneScreen screen, GuiGraphics guiGraphics, int mouseX, int mouseY) {
        switch (state.getChatScreen()) {
            case LIST -> chatsView.render(screen, guiGraphics, mouseX, mouseY, state, chatsSearchQuery);
            case CONVERSATION -> conversationView.render(screen, guiGraphics, mouseX, mouseY, state);
            case CONTACT_INFO -> contactInfoView.render(screen, guiGraphics, mouseX, mouseY, state);
            case CONTACT_PHOTO -> contactPhotoView.render(screen, guiGraphics, mouseX, mouseY, state);
            case NEW_CHAT -> newChatView.render(screen, guiGraphics, mouseX, mouseY, state);
            case NEW_CONTACT -> newContactView.render(screen, guiGraphics, mouseX, mouseY, state);
        }
    }

    private void renderPlaceholderTab(PhoneScreen screen, GuiGraphics guiGraphics) {
        String content = switch (activeTab) {
            case UPDATES -> "Novedades";
            case CALLS -> "Llamadas";
            case CHATS -> "Chats";
            case YOU -> "Perfil";
        };

        guiGraphics.drawCenteredString(screen.getPhoneFont(), "WhatsApp", screen.getPhoneCenterX(), screen.getPhoneY() + TITLE_Y, PhoneUi.COLOR_TEXT);
        guiGraphics.drawCenteredString(screen.getPhoneFont(), content, screen.getPhoneCenterX(), screen.getPhoneY() + CONTENT_Y, PhoneUi.COLOR_SUBTEXT);
    }

    @Override
    public boolean mouseClicked(PhoneScreen screen, double mouseX, double mouseY, int button) {
        if (button != 0) {
            return false;
        }

        if (activeTab == WhatsappTab.YOU) {
            if (PhoneUi.isBackButtonClicked(screen, mouseX, mouseY) && !profileView.isPhotoSheetOpen()) {
                activeTab = WhatsappTab.CHATS;
                profileView.resetTransientState();
                return true;
            }

            if (profileView.mouseClicked(screen, mouseX, mouseY, button, state)) {
                return true;
            }
        }

        if (activeTab == WhatsappTab.CHATS) {
            switch (state.getChatScreen()) {
                case CONVERSATION -> {
                    return conversationView.mouseClicked(screen, mouseX, mouseY, button, state);
                }
                case CONTACT_INFO -> {
                    return contactInfoView.mouseClicked(screen, mouseX, mouseY, button, state);
                }
                case CONTACT_PHOTO -> {
                    return contactPhotoView.mouseClicked(screen, mouseX, mouseY, button, state);
                }
                case NEW_CHAT -> {
                    return newChatView.mouseClicked(screen, mouseX, mouseY, button, state);
                }
                case NEW_CONTACT -> {
                    return newContactView.mouseClicked(screen, mouseX, mouseY, button, state);
                }
                case LIST -> {
                    if (chatsView.isMoreMenuOpen()) {
                        WhatsappChatsView.MoreMenuAction action = chatsView.getMoreMenuActionClicked(screen, mouseX, mouseY, state);
                        if (action != null) {
                            handleMoreMenuAction(action);
                            return true;
                        }
                    }

                    WhatsappChatsView.SwipeActionResult rowAction = chatsView.getSwipeActionClicked(screen, mouseX, mouseY, state, chatsSearchQuery);
                    if (rowAction != null) {
                        handleRowAction(rowAction);
                        return true;
                    }

                    WhatsappTab clickedTab = bottomNav.getClickedTab(screen, mouseX, mouseY);
                    if (clickedTab != null) {
                        activeTab = clickedTab;

                        if (clickedTab != WhatsappTab.CHATS) {
                            state.closeChat();
                            state.clearNewContactDraft();
                            chatsSearchQuery = "";
                            chatsView.resetScroll();
                            chatsView.closeSwipe();
                            chatsView.closeMoreMenu();
                        }

                        if (clickedTab != WhatsappTab.YOU) {
                            profileView.resetTransientState();
                        }

                        return true;
                    }

                    if (chatsView.isAddButtonClicked(screen, mouseX, mouseY)) {
                        state.openNewChatScreen();
                        newChatView.resetViewState();
                        return true;
                    }

                    // IMPORTANTE:
                    // En la lista NO abrir chats en mouseClicked.
                    // Solo iniciar posible swipe/tap.
                    return chatsView.mousePressed(screen, mouseX, mouseY, button, state, chatsSearchQuery);
                }
            }
            return true;
        }

        WhatsappTab clickedTab = bottomNav.getClickedTab(screen, mouseX, mouseY);
        if (clickedTab != null) {
            activeTab = clickedTab;

            if (clickedTab != WhatsappTab.CHATS) {
                state.closeChat();
                state.clearNewContactDraft();
                chatsSearchQuery = "";
                chatsView.resetScroll();
                chatsView.closeSwipe();
                chatsView.closeMoreMenu();
            }

            if (clickedTab != WhatsappTab.YOU) {
                profileView.resetTransientState();
            }

            return true;
        }

        return false;
    }

    @Override
    public boolean mouseReleased(PhoneScreen screen, double mouseX, double mouseY, int button) {
        if (activeTab == WhatsappTab.CHATS && state.getChatScreen() == WhatsappChatScreen.LIST) {
            boolean handledRelease = chatsView.mouseReleased(screen, mouseX, mouseY, button, state, chatsSearchQuery);

            boolean consumedSwipeAction = chatsView.consumeSwipeActionConsumed();
            if (consumedSwipeAction) {
                return true;
            }

            boolean suppressRelease = chatsView.consumeSuppressNextRelease();
            if (suppressRelease) {
                return true;
            }

            if (handledRelease) {
                return true;
            }

            if (button == 0 && !chatsView.isMoreMenuOpen()) {
                WhatsappChat clickedChat = chatsView.getClickedChat(screen, mouseX, mouseY, state, chatsSearchQuery);
                if (clickedChat != null) {
                    state.openChat(clickedChat);
                    conversationView.onConversationOpened(screen, state);
                    chatsSearchQuery = "";
                    chatsView.resetScroll();
                    chatsView.closeSwipe();
                    chatsView.closeMoreMenu();
                    return true;
                }
            }

            return false;
        }
        return false;
    }

    @Override
    public boolean mouseDragged(
            PhoneScreen screen,
            double mouseX,
            double mouseY,
            int button,
            double dragX,
            double dragY
    ) {
        if (activeTab == WhatsappTab.CHATS && state.getChatScreen() == WhatsappChatScreen.LIST) {
            return chatsView.mouseDragged(screen, mouseX, mouseY, button, dragX, dragY, state, chatsSearchQuery);
        }
        return false;
    }

    @Override
    public boolean charTyped(PhoneScreen screen, char codePoint, int modifiers) {
        if (activeTab == WhatsappTab.CHATS) {
            switch (state.getChatScreen()) {
                case CONVERSATION -> {
                    return conversationView.charTyped(codePoint, modifiers, state);
                }
                case LIST -> {
                    if (Character.isISOControl(codePoint)) {
                        return false;
                    }

                    if (chatsSearchQuery.length() >= SEARCH_MAX_LENGTH) {
                        return true;
                    }

                    chatsSearchQuery += codePoint;
                    chatsView.resetScroll();
                    return true;
                }
                case NEW_CONTACT -> {
                    return newContactView.charTyped(codePoint, modifiers, state);
                }
                case CONTACT_INFO, CONTACT_PHOTO, NEW_CHAT -> {
                    return false;
                }
            }
        }

        if (activeTab == WhatsappTab.YOU) {
            return profileView.charTyped(codePoint, modifiers, state);
        }

        return false;
    }

    @Override
    public boolean mouseScrolled(PhoneScreen screen, double mouseX, double mouseY, double scrollDelta) {
        if (activeTab != WhatsappTab.CHATS) {
            return false;
        }

        return switch (state.getChatScreen()) {
            case LIST -> chatsView.mouseScrolled(screen, mouseX, mouseY, scrollDelta, state, chatsSearchQuery);
            case CONVERSATION -> conversationView.mouseScrolled(screen, mouseX, mouseY, scrollDelta, state);
            case CONTACT_INFO -> contactInfoView.mouseScrolled(screen, mouseX, mouseY, scrollDelta, state);
            case CONTACT_PHOTO -> false;
            case NEW_CHAT -> newChatView.mouseScrolled(screen, mouseX, mouseY, scrollDelta, state);
            case NEW_CONTACT -> false;
        };
    }

    @Override
    public boolean keyPressed(PhoneScreen screen, int keyCode, int scanCode, int modifiers) {
        if (activeTab == WhatsappTab.CHATS) {
            switch (state.getChatScreen()) {
                case LIST -> {
                    if (keyCode == 259) {
                        if (!chatsSearchQuery.isEmpty()) {
                            chatsSearchQuery = chatsSearchQuery.substring(0, chatsSearchQuery.length() - 1);
                            chatsView.resetScroll();
                        }
                        return true;
                    }

                    if (keyCode == 256) {
                        if (chatsView.isMoreMenuOpen()) {
                            chatsView.closeMoreMenu();
                            return true;
                        }

                        if (!chatsSearchQuery.isEmpty()) {
                            chatsSearchQuery = "";
                            chatsView.resetScroll();
                            return true;
                        }
                    }

                    return false;
                }

                case CONVERSATION -> {
                    if (conversationView.keyPressed(screen, keyCode, state)) {
                        return true;
                    }

                    if (keyCode == 256) {
                        state.backFromChatSubscreen();
                        conversationView.resetViewState();
                        return true;
                    }

                    return false;
                }

                case CONTACT_INFO, CONTACT_PHOTO, NEW_CHAT -> {
                    if (keyCode == 256) {
                        state.backFromChatSubscreen();
                        return true;
                    }
                    return false;
                }

                case NEW_CONTACT -> {
                    return newContactView.keyPressed(keyCode, state);
                }
            }
        }

        if (activeTab == WhatsappTab.YOU) {
            if (profileView.keyPressed(keyCode, state)) {
                return true;
            }

            if (keyCode == 256 && !profileView.isPhotoSheetOpen()) {
                activeTab = WhatsappTab.CHATS;
                profileView.resetTransientState();
                return true;
            }
        }

        return false;
    }

    private void handleRowAction(WhatsappChatsView.SwipeActionResult result) {
        if (result == null) {
            return;
        }

        WhatsappChat chat = state.findChatById(result.chatId());
        if (chat == null) {
            return;
        }

        switch (result.action()) {
            case PIN, UNPIN -> ModNetwork.sendWhatsappToServer(new WhatsappTogglePinChatC2SPacket(chat.id()));
            case MARK_READ -> ModNetwork.sendWhatsappToServer(new WhatsappMarkChatUnreadC2SPacket(chat.id(), false));
            case MARK_UNREAD -> ModNetwork.sendWhatsappToServer(new WhatsappMarkChatUnreadC2SPacket(chat.id(), true));
            case ARCHIVE -> ModNetwork.sendWhatsappToServer(new WhatsappArchiveChatC2SPacket(chat.id()));
            case OPEN_MORE -> {
            }
        }
    }

    public void requestOpenConversationFromServer() {
        waitingOpenConversationFromServer = true;
    }

    @Override
    public boolean mousePressed(PhoneScreen screen, double mouseX, double mouseY, int button) {
        if (activeTab == WhatsappTab.CHATS && state.getChatScreen() == WhatsappChatScreen.LIST) {
            return chatsView.mousePressed(screen, mouseX, mouseY, button, state, chatsSearchQuery);
        }
        return false;
    }

    private void handleMoreMenuAction(WhatsappChatsView.MoreMenuAction action) {
        String chatId = chatsView.getMoreMenuChatId();
        WhatsappChat chat = chatId == null ? null : state.findChatById(chatId);
        if (chat == null && action != WhatsappChatsView.MoreMenuAction.NONE) {
            return;
        }

        switch (action) {
            case MUTE -> {
                // preparado para bloque posterior
            }
            case CONTACT_INFO -> {
                if (chat != null) {
                    state.openChat(chat);
                    state.openSelectedContactInfo();
                }
            }
            case CLEAR_CHAT -> {
                if (chat != null) {
                    ModNetwork.sendWhatsappToServer(new WhatsappClearChatC2SPacket(chat.id()));
                }
            }
            case TOGGLE_BLOCK -> {
                WhatsappContact contact = state.findContactById(chat.contactId());
                if (contact != null) {
                    ModNetwork.sendWhatsappToServer(new WhatsappToggleBlockContactC2SPacket(contact.id()));
                }
            }
            case DELETE_CHAT -> {
                if (chat != null) {
                    ModNetwork.sendWhatsappToServer(new WhatsappDeleteChatC2SPacket(chat.id()));
                }
            }
            case NONE -> {
            }
        }
    }
}