package santi_moder.roleplaymod.common.whatsapp.server;

import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraftforge.network.PacketDistributor;
import santi_moder.roleplaymod.common.phone.PhoneItemResolver;
import santi_moder.roleplaymod.common.whatsapp.model.*;
import santi_moder.roleplaymod.common.whatsapp.sync.*;
import santi_moder.roleplaymod.network.ModNetwork;
import santi_moder.roleplaymod.network.phone.whatsapp.*;

import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public final class WhatsappServerService {

    private static final DateTimeFormatter TIME_FORMATTER = DateTimeFormatter.ofPattern("HH:mm");

    private WhatsappServerService() {
    }

    private static WhatsappAccount getActiveAccount(ServerPlayer player, WhatsappServerData data) {
        if (player == null || data == null) {
            return null;
        }

        String simId = PhoneItemResolver.getActiveSimId(player);
        String phoneNumber = PhoneItemResolver.getActivePhoneNumber(player);

        if (simId.isBlank() || phoneNumber.isBlank()) {
            return null;
        }

        return data.getOrCreateAccountBySim(
                simId,
                phoneNumber,
                player.getGameProfile().getName(),
                player.getUUID()
        );
    }

    private static UUID getActiveAccountId(ServerPlayer player, WhatsappServerData data) {
        WhatsappAccount account = getActiveAccount(player, data);
        return account == null ? null : account.accountId();
    }

    public static WhatsappInitialStateSnapshot buildInitialSnapshot(ServerPlayer player) {
        ServerLevel level = player.serverLevel();
        WhatsappServerData data = WhatsappServerData.get(level);

        WhatsappAccount account = getActiveAccount(player, data);

        if (account == null) {
            return new WhatsappInitialStateSnapshot(
                    new ArrayList<>(),
                    new ArrayList<>(),
                    new ArrayList<>(),
                    new WhatsappSyncProfile(
                            "default",
                            "",
                            player.getGameProfile().getName(),
                            ""
                    ),
                    WhatsappPresenceResolver.isPlayerOnlineInServer(player),
                    false,
                    false
            );
        }

        UUID accountId = account.accountId();
        WhatsappProfile profile = data.getOrCreateProfile(accountId, account.displayName());

        syncOwnedPresences(player);

        List<WhatsappSyncContact> syncContacts = new ArrayList<>();
        for (WhatsappContact contact : data.getOrCreateContacts(accountId)) {
            syncContacts.add(new WhatsappSyncContact(
                    contact.id(),
                    contact.displayName(),
                    contact.phoneNumber(),
                    contact.photoId(),
                    contact.about(),
                    contact.blocked(),
                    contact.commonGroupsCount(),
                    contact.mediaCount()
            ));
        }

        List<WhatsappSyncChat> syncChats = new ArrayList<>();
        for (WhatsappChat chat : data.getOrCreateChats(accountId)) {
            List<WhatsappSyncMessage> syncMessages = new ArrayList<>();

            for (WhatsappMessage message : chat.messages()) {
                syncMessages.add(new WhatsappSyncMessage(
                        message.id(),
                        message.text(),
                        message.sentByMe(),
                        message.timeText(),
                        message.sortTimestamp(),
                        message.status(),
                        message.lastStatusUpdateAt()
                ));
            }

            syncChats.add(new WhatsappSyncChat(
                    chat.id(),
                    chat.contactId(),
                    chat.pinned(),
                    chat.unreadCount(),
                    syncMessages
            ));
        }

        List<WhatsappSyncPresence> syncPresences = new ArrayList<>();
        for (WhatsappPresence presence : data.getOrCreatePresences(accountId)) {
            syncPresences.add(new WhatsappSyncPresence(
                    presence.contactId(),
                    presence.onlineInServer(),
                    presence.hasInternet(),
                    presence.hasBattery(),
                    presence.lastSeenTimestamp()
            ));
        }

        WhatsappSyncProfile syncProfile = new WhatsappSyncProfile(
                profile.photoId(),
                profile.about(),
                profile.displayName(),
                profile.phoneNumber()
        );

        return new WhatsappInitialStateSnapshot(
                syncChats,
                syncContacts,
                syncPresences,
                syncProfile,
                WhatsappPresenceResolver.isPlayerOnlineInServer(player),
                WhatsappPresenceResolver.hasInternet(player),
                WhatsappPresenceResolver.hasBattery(player)
        );
    }

    public static void handleSendMessage(ServerPlayer senderPlayer, String phoneNumber, String text) {
        if (senderPlayer == null || text == null || text.isBlank()) {
            return;
        }

        ServerLevel level = senderPlayer.serverLevel();
        WhatsappServerData data = WhatsappServerData.get(level);

        WhatsappAccount senderAccount = getActiveAccount(senderPlayer, data);
        if (senderAccount == null) {
            return;
        }

        UUID senderAccountId = senderAccount.accountId();

        data.getOrCreateProfile(senderAccountId, senderAccount.displayName());
        syncOwnedPresences(senderPlayer);

        WhatsappContact senderContact = data.getOrCreateContacts(senderAccountId)
                .stream()
                .filter(c -> samePhone(phoneNumber, c.phoneNumber()))
                .findFirst()
                .orElse(null);

        if (senderContact == null) {
            senderContact = WhatsappContact.of(
                    phoneNumber,
                    phoneNumber,
                    WhatsappContact.DEFAULT_PHOTO,
                    "",
                    false,
                    0,
                    0
            );
            data.getOrCreateContacts(senderAccountId).add(senderContact);
        }

        if (senderContact.blocked()) {
            return;
        }

        WhatsappAccount targetAccount = data.getOrCreateAccountByPhone(
                senderContact.phoneNumber(),
                senderContact.displayName()
        );

        if (targetAccount == null) {
            return;
        }

        UUID targetAccountId = targetAccount.accountId();

        data.getOrCreateProfile(targetAccountId, targetAccount.displayName());

        WhatsappContact reverseContact = findOrCreateMirrorContact(
                data,
                targetAccountId,
                senderAccount.phoneNumber(),
                senderAccount.phoneNumber()
        );

        if (reverseContact.blocked()) {
            return;
        }

        long now = System.currentTimeMillis();
        String timeText = LocalTime.now().format(TIME_FORMATTER);
        String cleanText = text.trim();

        WhatsappChat senderChat = data.findChatByContactId(senderAccountId, senderContact.id());
        if (senderChat == null) {
            senderChat = data.createChat(senderAccountId, senderContact.id());
        }

        WhatsappMessage senderMessage = WhatsappMessage.of(
                cleanText,
                true,
                timeText,
                now,
                WhatsappMessageStatus.SENT,
                now
        );

        senderChat.addMessage(senderMessage);
        senderChat.clearUnreadCount();
        data.sortChats(senderAccountId);

        WhatsappChat targetChat = data.findChatByContactId(targetAccountId, reverseContact.id());

        if (targetChat == null) {
            targetChat = data.createChat(targetAccountId, reverseContact.id());
        }

        WhatsappMessage incomingMessage = WhatsappMessage.of(
                cleanText,
                false,
                timeText,
                now,
                WhatsappMessageStatus.SENT,
                now
        );

        targetChat.addMessage(incomingMessage);
        targetChat.incrementUnreadCount();
        data.sortChats(targetAccountId);

        ServerPlayer targetPlayer = findOnlinePlayerByPhone(level, senderContact.phoneNumber());

        if (targetPlayer != null) {
            targetAccount.setLastKnownPlayerUuid(targetPlayer.getUUID());

            ModNetwork.sendWhatsappToClient(
                    new WhatsappContactUpdatedS2CPacket(buildContactPayload(reverseContact)),
                    PacketDistributor.PLAYER.with(() -> targetPlayer)
            );

            ModNetwork.sendWhatsappToClient(
                    new WhatsappMessageAddedS2CPacket(buildChatPayload(targetChat)),
                    PacketDistributor.PLAYER.with(() -> targetPlayer)
            );
        }

        data.setDirty();

        ModNetwork.sendWhatsappToClient(
                new WhatsappMessageAddedS2CPacket(buildChatPayload(senderChat)),
                PacketDistributor.PLAYER.with(() -> senderPlayer)
        );
    }

    public static void handleMarkChatRead(ServerPlayer readerPlayer, String chatId) {
        if (readerPlayer == null || chatId == null || chatId.isBlank()) {
            return;
        }

        ServerLevel level = readerPlayer.serverLevel();
        WhatsappServerData data = WhatsappServerData.get(level);

        WhatsappAccount readerAccount = getActiveAccount(readerPlayer, data);
        if (readerAccount == null) {
            return;
        }

        UUID readerAccountId = readerAccount.accountId();
        syncOwnedPresences(readerPlayer);

        WhatsappChat readerChat = findOwnedChat(data, readerAccountId, chatId);
        if (readerChat == null) {
            return;
        }

        readerChat.clearUnreadCount();

        long now = System.currentTimeMillis();

        for (WhatsappMessage message : readerChat.messages()) {
            if (!message.sentByMe() && message.status() != WhatsappMessageStatus.READ) {
                readerChat.updateMessageStatus(message.id(), WhatsappMessageStatus.READ, now);
            }
        }

        WhatsappContact readerContact = data.findContactForOwner(readerAccountId, readerChat.contactId());
        if (readerContact == null) {
            data.setDirty();
            return;
        }

        WhatsappAccount otherAccount = data.findAccountByPhone(readerContact.phoneNumber());
        if (otherAccount != null) {
            UUID otherAccountId = otherAccount.accountId();

            WhatsappContact mirrorContact = findMirrorContactByPhone(
                    data,
                    otherAccountId,
                    readerAccount.phoneNumber()
            );

            if (mirrorContact != null) {
                WhatsappChat otherChat = data.findChatByContactId(otherAccountId, mirrorContact.id());

                if (otherChat != null) {
                    for (WhatsappMessage message : otherChat.messages()) {
                        if (message.sentByMe() && message.status() == WhatsappMessageStatus.SENT) {
                            otherChat.updateMessageStatus(message.id(), WhatsappMessageStatus.READ, now);

                            ServerPlayer otherPlayer = getOnlinePlayerForAccount(level, otherAccount);
                            if (otherPlayer != null) {
                                ModNetwork.sendWhatsappToClient(
                                        new WhatsappMessageStatusUpdatedS2CPacket(
                                                new WhatsappMessageStatusPayload(
                                                        otherChat.id(),
                                                        message.id(),
                                                        WhatsappMessageStatus.READ,
                                                        now,
                                                        otherChat.unreadCount()
                                                )
                                        ),
                                        PacketDistributor.PLAYER.with(() -> otherPlayer)
                                );
                            }
                        }
                    }
                }
            }
        }

        for (WhatsappMessage message : readerChat.messages()) {
            if (!message.sentByMe()) {
                ModNetwork.sendWhatsappToClient(
                        new WhatsappMessageStatusUpdatedS2CPacket(
                                new WhatsappMessageStatusPayload(
                                        readerChat.id(),
                                        message.id(),
                                        WhatsappMessageStatus.READ,
                                        now,
                                        readerChat.unreadCount()
                                )
                        ),
                        PacketDistributor.PLAYER.with(() -> readerPlayer)
                );
            }
        }

        data.sortChats(readerAccountId);
        data.setDirty();
    }

    public static void handleToggleBlockContact(ServerPlayer ownerPlayer, String contactId) {
        if (ownerPlayer == null || contactId == null || contactId.isBlank()) {
            return;
        }

        ServerLevel level = ownerPlayer.serverLevel();
        WhatsappServerData data = WhatsappServerData.get(level);

        UUID accountId = getActiveAccountId(ownerPlayer, data);
        if (accountId == null) {
            return;
        }

        WhatsappContact contact = data.findContactForOwner(accountId, contactId);
        if (contact == null) {
            return;
        }

        contact.setBlocked(!contact.blocked());
        data.setDirty();

        ModNetwork.sendWhatsappToClient(
                new WhatsappContactUpdatedS2CPacket(buildContactPayload(contact)),
                PacketDistributor.PLAYER.with(() -> ownerPlayer)
        );
    }

    public static void handleClearChat(ServerPlayer ownerPlayer, String chatId) {
        if (ownerPlayer == null || chatId == null || chatId.isBlank()) {
            return;
        }

        ServerLevel level = ownerPlayer.serverLevel();
        WhatsappServerData data = WhatsappServerData.get(level);

        UUID accountId = getActiveAccountId(ownerPlayer, data);
        if (accountId == null) {
            return;
        }

        WhatsappChat targetChat = findOwnedChat(data, accountId, chatId);
        if (targetChat == null) {
            return;
        }

        targetChat.clearMessages();
        targetChat.clearUnreadCount();
        data.sortChats(accountId);
        data.setDirty();

        ModNetwork.sendWhatsappToClient(
                new WhatsappChatClearedS2CPacket(buildChatPayload(targetChat)),
                PacketDistributor.PLAYER.with(() -> ownerPlayer)
        );
    }

    public static void syncOwnedPresences(ServerPlayer ownerPlayer) {
        if (ownerPlayer == null) {
            return;
        }

        ServerLevel level = ownerPlayer.serverLevel();
        WhatsappServerData data = WhatsappServerData.get(level);

        WhatsappAccount ownerAccount = getActiveAccount(ownerPlayer, data);
        if (ownerAccount == null) {
            return;
        }

        UUID ownerAccountId = ownerAccount.accountId();

        for (WhatsappContact contact : data.getOrCreateContacts(ownerAccountId)) {
            WhatsappPresence presence = data.getOrCreatePresence(ownerAccountId, contact.id());

            WhatsappAccount remoteAccount = data.findAccountByPhone(contact.phoneNumber());
            if (remoteAccount == null) {
                presence.setOnlineInServer(false);
                presence.setHasInternet(false);
                presence.setHasBattery(false);
                continue;
            }

            ServerPlayer remotePlayer = getOnlinePlayerForAccount(level, remoteAccount);

            boolean online = WhatsappPresenceResolver.isPlayerOnlineInServer(remotePlayer);
            boolean internet = WhatsappPresenceResolver.hasInternet(remotePlayer);
            boolean battery = WhatsappPresenceResolver.hasBattery(remotePlayer);

            presence.setOnlineInServer(online);
            presence.setHasInternet(internet);
            presence.setHasBattery(battery);
            presence.setLastSeenTimestamp(System.currentTimeMillis());

            ModNetwork.sendWhatsappToClient(
                    new WhatsappPresenceUpdatedS2CPacket(
                            new WhatsappPresencePayload(
                                    contact.id(),
                                    online,
                                    internet,
                                    battery,
                                    presence.lastSeenTimestamp()
                            )
                    ),
                    PacketDistributor.PLAYER.with(() -> ownerPlayer)
            );
        }

        data.setDirty();
    }

    public static void handleOpenOrCreateChat(ServerPlayer ownerPlayer, String contactId) {
        if (ownerPlayer == null || contactId == null || contactId.isBlank()) {
            return;
        }

        ServerLevel level = ownerPlayer.serverLevel();
        WhatsappServerData data = WhatsappServerData.get(level);

        UUID accountId = getActiveAccountId(ownerPlayer, data);
        if (accountId == null) {
            return;
        }

        WhatsappContact contact = data.findContactForOwner(accountId, contactId);
        if (contact == null) {
            return;
        }

        WhatsappChat chat = data.findChatByContactId(accountId, contact.id());
        if (chat == null) {
            chat = data.createChat(accountId, contact.id());
            data.setDirty();
        }

        ModNetwork.sendWhatsappToClient(
                new WhatsappChatOpenedS2CPacket(buildChatPayload(chat)),
                PacketDistributor.PLAYER.with(() -> ownerPlayer)
        );
    }

    public static void handleCreateContact(ServerPlayer ownerPlayer, String displayName, String phoneNumber) {
        if (ownerPlayer == null) {
            return;
        }

        String cleanName = displayName == null ? "" : displayName.trim();
        String cleanPhone = phoneNumber == null ? "" : phoneNumber.trim();

        if (cleanName.isBlank() || cleanPhone.isBlank()) {
            return;
        }

        ServerLevel level = ownerPlayer.serverLevel();
        WhatsappServerData data = WhatsappServerData.get(level);

        UUID accountId = getActiveAccountId(ownerPlayer, data);
        if (accountId == null) {
            return;
        }

        for (WhatsappContact existing : data.getOrCreateContacts(accountId)) {
            if (samePhone(cleanPhone, existing.phoneNumber())) {
                ModNetwork.sendWhatsappToClient(
                        new WhatsappContactCreatedS2CPacket(
                                buildContactPayload(existing),
                                true
                        ),
                        PacketDistributor.PLAYER.with(() -> ownerPlayer)
                );
                return;
            }
        }

        WhatsappContact created = WhatsappContact.of(
                cleanName,
                cleanPhone,
                WhatsappContact.DEFAULT_PHOTO,
                "",
                false,
                0,
                0
        );

        data.getOrCreateContacts(accountId).add(created);
        data.getOrCreatePresence(accountId, created.id());
        data.setDirty();

        ModNetwork.sendWhatsappToClient(
                new WhatsappContactCreatedS2CPacket(
                        buildContactPayload(created),
                        true
                ),
                PacketDistributor.PLAYER.with(() -> ownerPlayer)
        );
    }

    public static void handleTogglePinChat(ServerPlayer ownerPlayer, String chatId) {
        ServerLevel level = ownerPlayer.serverLevel();
        WhatsappServerData data = WhatsappServerData.get(level);

        UUID accountId = getActiveAccountId(ownerPlayer, data);
        if (accountId == null) {
            return;
        }

        WhatsappChat chat = findOwnedChat(data, accountId, chatId);
        if (chat == null) {
            return;
        }

        chat.setPinned(!chat.pinned());
        data.sortChats(accountId);
        data.setDirty();

        ModNetwork.sendWhatsappToClient(
                new WhatsappChatOpenedS2CPacket(buildChatPayload(chat)),
                PacketDistributor.PLAYER.with(() -> ownerPlayer)
        );
    }

    public static void handleMarkChatUnread(ServerPlayer ownerPlayer, String chatId, boolean unread) {
        ServerLevel level = ownerPlayer.serverLevel();
        WhatsappServerData data = WhatsappServerData.get(level);

        UUID accountId = getActiveAccountId(ownerPlayer, data);
        if (accountId == null) {
            return;
        }

        WhatsappChat chat = findOwnedChat(data, accountId, chatId);
        if (chat == null) {
            return;
        }

        chat.setUnreadCount(unread ? Math.max(1, chat.unreadCount()) : 0);
        data.sortChats(accountId);
        data.setDirty();

        ModNetwork.sendWhatsappToClient(
                new WhatsappChatOpenedS2CPacket(buildChatPayload(chat)),
                PacketDistributor.PLAYER.with(() -> ownerPlayer)
        );
    }

    public static void handleArchiveChat(ServerPlayer ownerPlayer, String chatId) {
        ServerLevel level = ownerPlayer.serverLevel();
        WhatsappServerData data = WhatsappServerData.get(level);

        UUID accountId = getActiveAccountId(ownerPlayer, data);
        if (accountId == null) {
            return;
        }

        WhatsappChat chat = findOwnedChat(data, accountId, chatId);
        if (chat == null) {
            return;
        }

        chat.setArchived(true);
        data.sortChats(accountId);
        data.setDirty();

        ModNetwork.sendWhatsappToClient(
                new WhatsappChatOpenedS2CPacket(buildChatPayload(chat)),
                PacketDistributor.PLAYER.with(() -> ownerPlayer)
        );
    }

    public static void handleDeleteChat(ServerPlayer ownerPlayer, String chatId) {
        if (ownerPlayer == null || chatId == null || chatId.isBlank()) {
            return;
        }

        ServerLevel level = ownerPlayer.serverLevel();
        WhatsappServerData data = WhatsappServerData.get(level);

        UUID accountId = getActiveAccountId(ownerPlayer, data);
        if (accountId == null) {
            return;
        }

        List<WhatsappChat> chats = data.getOrCreateChats(accountId);
        chats.removeIf(chat -> chat.id().equals(chatId));
        data.setDirty();

        ModNetwork.sendWhatsappToClient(
                new WhatsappChatDeletedS2CPacket(chatId),
                PacketDistributor.PLAYER.with(() -> ownerPlayer)
        );
    }

    private static WhatsappContact findOrCreateMirrorContact(
            WhatsappServerData data,
            UUID accountId,
            String displayName,
            String phoneNumber
    ) {
        for (WhatsappContact contact : data.getOrCreateContacts(accountId)) {
            if (samePhone(phoneNumber, contact.phoneNumber())) {
                return contact;
            }
        }

        WhatsappContact created = WhatsappContact.of(
                displayName,
                phoneNumber,
                WhatsappContact.DEFAULT_PHOTO,
                "",
                false,
                0,
                0
        );

        data.getOrCreateContacts(accountId).add(created);
        data.setDirty();
        return created;
    }

    private static WhatsappContact findMirrorContactByPhone(
            WhatsappServerData data,
            UUID accountId,
            String phoneNumber
    ) {
        for (WhatsappContact contact : data.getOrCreateContacts(accountId)) {
            if (samePhone(phoneNumber, contact.phoneNumber())) {
                return contact;
            }
        }
        return null;
    }

    private static WhatsappChat findOwnedChat(WhatsappServerData data, UUID accountId, String chatId) {
        if (data == null || accountId == null || chatId == null || chatId.isBlank()) {
            return null;
        }

        for (WhatsappChat chat : data.getOrCreateChats(accountId)) {
            if (chat.id().equals(chatId)) {
                return chat;
            }
        }

        return null;
    }

    private static ServerPlayer getOnlinePlayerForAccount(ServerLevel level, WhatsappAccount account) {
        if (level == null || account == null || account.lastKnownPlayerUuid() == null) {
            return null;
        }

        return level.getServer().getPlayerList().getPlayer(account.lastKnownPlayerUuid());
    }

    private static ServerPlayer findOnlinePlayerByPhone(ServerLevel level, String phoneNumber) {
        if (level == null || phoneNumber == null || phoneNumber.isBlank()) {
            return null;
        }

        for (ServerPlayer player : level.getServer().getPlayerList().getPlayers()) {
            String activePhoneNumber = PhoneItemResolver.getActivePhoneNumber(player);

            if (samePhone(phoneNumber, activePhoneNumber)) {
                return player;
            }
        }

        return null;
    }

    private static boolean samePhone(String a, String b) {
        String normalizedA = normalizePhoneForCompare(a);
        String normalizedB = normalizePhoneForCompare(b);

        return !normalizedA.isBlank() && normalizedA.equals(normalizedB);
    }

    private static String normalizePhoneForCompare(String value) {
        if (value == null) return "";

        StringBuilder digits = new StringBuilder();

        for (int i = 0; i < value.length(); i++) {
            char c = value.charAt(i);
            if (Character.isDigit(c)) {
                digits.append(c);
            }
        }

        String result = digits.toString();

        // Uruguay: nos quedamos con los últimos 9 dígitos.
        if (result.length() > 9) {
            result = result.substring(result.length() - 9);
        }

        return result;
    }

    public static WhatsappChatPayload buildChatPayload(WhatsappChat chat) {
        List<WhatsappSyncMessage> syncMessages = new ArrayList<>();

        for (WhatsappMessage message : chat.messages()) {
            syncMessages.add(new WhatsappSyncMessage(
                    message.id(),
                    message.text(),
                    message.sentByMe(),
                    message.timeText(),
                    message.sortTimestamp(),
                    message.status(),
                    message.lastStatusUpdateAt()
            ));
        }

        return new WhatsappChatPayload(
                chat.id(),
                chat.contactId(),
                chat.pinned(),
                chat.archived(),
                chat.unreadCount(),
                syncMessages
        );
    }

    public static WhatsappContactPayload buildContactPayload(WhatsappContact contact) {
        return new WhatsappContactPayload(
                contact.id(),
                contact.displayName(),
                contact.phoneNumber(),
                contact.photoId(),
                contact.about(),
                contact.blocked(),
                contact.commonGroupsCount(),
                contact.mediaCount()
        );
    }
}