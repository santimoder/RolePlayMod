package santi_moder.roleplaymod.common.phone;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.Tag;
import net.minecraft.world.item.ItemStack;
import santi_moder.roleplaymod.client.phone.app.whatsapp.WhatsappState;
import santi_moder.roleplaymod.common.whatsapp.model.WhatsappChatScreen;
import santi_moder.roleplaymod.common.whatsapp.model.WhatsappProfile;

import java.util.ArrayList;

public final class PhoneWhatsappData {

    private static final String ROOT_TAG = "rp_whatsapp_ui";

    private static final String SELECTED_CHAT_ID_TAG = "selected_chat_id";
    private static final String DRAFT_MESSAGE_TAG = "draft_message";
    private static final String CHAT_SCREEN_TAG = "chat_screen";

    private PhoneWhatsappData() {
    }

    public static WhatsappState loadState(ItemStack stack) {
        if (stack.isEmpty()) {
            return new WhatsappState();
        }

        CompoundTag root = stack.getOrCreateTag();
        CompoundTag uiTag = root.contains(ROOT_TAG, Tag.TAG_COMPOUND)
                ? root.getCompound(ROOT_TAG)
                : new CompoundTag();

        String selectedChatId = uiTag.getString(SELECTED_CHAT_ID_TAG);
        String draftMessage = uiTag.getString(DRAFT_MESSAGE_TAG);

        WhatsappChatScreen screen = WhatsappChatScreen.LIST;
        String rawScreen = uiTag.getString(CHAT_SCREEN_TAG);

        if (!rawScreen.isBlank()) {
            try {
                screen = WhatsappChatScreen.valueOf(rawScreen);
            } catch (IllegalArgumentException ignored) {
                screen = WhatsappChatScreen.LIST;
            }
        }

        /*
         * IMPORTANTE:
         * Los chats/contactos/mensajes reales ya NO se cargan del celular.
         * Vienen del servidor mediante WhatsappInitialStateSnapshot.
         *
         * Esta clase ahora solo conserva estado local de UI:
         * - pantalla actual
         * - chat seleccionado
         * - borrador del mensaje
         */
        return new WhatsappState(
                new ArrayList<>(),
                new ArrayList<>(),
                new ArrayList<>(),
                selectedChatId,
                draftMessage,
                WhatsappProfile.createDefault("Jugador", ""),
                screen
        );
    }

    public static void saveState(ItemStack stack, WhatsappState state) {
        if (stack.isEmpty() || state == null) {
            return;
        }

        CompoundTag root = stack.getOrCreateTag();
        CompoundTag uiTag = new CompoundTag();

        if (state.getChatScreen() != null) {
            uiTag.putString(CHAT_SCREEN_TAG, state.getChatScreen().name());
        }

        if (state.getSelectedChatId() != null && !state.getSelectedChatId().isBlank()) {
            uiTag.putString(SELECTED_CHAT_ID_TAG, state.getSelectedChatId());
        }

        if (state.getDraftMessage() != null && !state.getDraftMessage().isEmpty()) {
            uiTag.putString(DRAFT_MESSAGE_TAG, state.getDraftMessage());
        }

        root.put(ROOT_TAG, uiTag);
    }

    public static void clearUiState(ItemStack stack) {
        if (stack.isEmpty()) {
            return;
        }

        CompoundTag root = stack.getOrCreateTag();
        root.remove(ROOT_TAG);
    }
}