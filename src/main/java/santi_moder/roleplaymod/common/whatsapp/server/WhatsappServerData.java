package santi_moder.roleplaymod.common.whatsapp.server;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.saveddata.SavedData;
import santi_moder.roleplaymod.client.phone.app.whatsapp.WhatsappChat;
import santi_moder.roleplaymod.client.phone.app.whatsapp.WhatsappContact;
import santi_moder.roleplaymod.client.phone.app.whatsapp.WhatsappMessage;
import santi_moder.roleplaymod.client.phone.app.whatsapp.WhatsappPresence;
import santi_moder.roleplaymod.client.phone.app.whatsapp.WhatsappProfile;

import java.util.*;

public final class WhatsappServerData extends SavedData {

    public static final String DATA_NAME = "roleplaymod_whatsapp_server_data";

    private final Map<UUID, WhatsappAccount> accountsByPlayer = new HashMap<>();
    private final Map<UUID, WhatsappProfile> profilesByPlayer = new HashMap<>();
    private final Map<UUID, List<WhatsappContact>> contactsByPlayer = new HashMap<>();
    private final Map<UUID, List<WhatsappChat>> chatsByPlayer = new HashMap<>();
    private final Map<UUID, List<WhatsappPresence>> presencesByPlayer = new HashMap<>();

    public static WhatsappServerData get(ServerLevel level) {
        return level.getDataStorage().computeIfAbsent(
                WhatsappServerData::load,
                WhatsappServerData::new,
                DATA_NAME
        );
    }

    public static WhatsappServerData load(CompoundTag tag) {
        return new WhatsappServerData();
    }

    @Override
    public CompoundTag save(CompoundTag tag) {
        return tag;
    }

    public WhatsappAccount getOrCreateAccount(UUID playerUuid, String fallbackName) {
        WhatsappAccount account = accountsByPlayer.get(playerUuid);
        if (account != null) {
            return account;
        }

        String generatedPhone = generatePhoneNumber(playerUuid);
        WhatsappAccount created = new WhatsappAccount(
                playerUuid,
                generatedPhone,
                fallbackName == null ? "Jugador" : fallbackName,
                "",
                "default",
                true
        );

        accountsByPlayer.put(playerUuid, created);

        profilesByPlayer.put(
                playerUuid,
                WhatsappProfile.createDefault(created.displayName(), created.phoneNumber())
        );

        contactsByPlayer.putIfAbsent(playerUuid, new ArrayList<>());
        chatsByPlayer.putIfAbsent(playerUuid, new ArrayList<>());
        presencesByPlayer.putIfAbsent(playerUuid, new ArrayList<>());

        setDirty();
        return created;
    }

    public WhatsappProfile getOrCreateProfile(UUID playerUuid, String fallbackName) {
        WhatsappProfile profile = profilesByPlayer.get(playerUuid);
        if (profile != null) {
            return profile;
        }

        WhatsappAccount account = getOrCreateAccount(playerUuid, fallbackName);
        WhatsappProfile created = WhatsappProfile.createDefault(account.displayName(), account.phoneNumber());
        profilesByPlayer.put(playerUuid, created);
        setDirty();
        return created;
    }

    public List<WhatsappContact> getOrCreateContacts(UUID playerUuid) {
        return contactsByPlayer.computeIfAbsent(playerUuid, ignored -> new ArrayList<>());
    }

    public List<WhatsappChat> getOrCreateChats(UUID playerUuid) {
        return chatsByPlayer.computeIfAbsent(playerUuid, ignored -> new ArrayList<>());
    }

    public List<WhatsappPresence> getOrCreatePresences(UUID playerUuid) {
        return presencesByPlayer.computeIfAbsent(playerUuid, ignored -> new ArrayList<>());
    }

    public WhatsappAccount findAccountByPhone(String phoneNumber) {
        if (phoneNumber == null || phoneNumber.isBlank()) {
            return null;
        }

        for (WhatsappAccount account : accountsByPlayer.values()) {
            if (phoneNumber.equals(account.phoneNumber())) {
                return account;
            }
        }

        return null;
    }

    public WhatsappAccount findAccountByPlayer(UUID playerUuid) {
        return accountsByPlayer.get(playerUuid);
    }

    public WhatsappContact findContactForOwner(UUID ownerUuid, String contactId) {
        if (contactId == null || contactId.isBlank()) {
            return null;
        }

        for (WhatsappContact contact : getOrCreateContacts(ownerUuid)) {
            if (contact.id().equals(contactId)) {
                return contact;
            }
        }

        return null;
    }

    public WhatsappChat findChatByContactId(UUID ownerUuid, String contactId) {
        if (contactId == null || contactId.isBlank()) {
            return null;
        }

        for (WhatsappChat chat : getOrCreateChats(ownerUuid)) {
            if (contactId.equals(chat.contactId())) {
                return chat;
            }
        }

        return null;
    }

    public WhatsappPresence findPresenceByContactId(UUID ownerUuid, String contactId) {
        if (contactId == null || contactId.isBlank()) {
            return null;
        }

        for (WhatsappPresence presence : getOrCreatePresences(ownerUuid)) {
            if (contactId.equals(presence.contactId())) {
                return presence;
            }
        }

        return null;
    }

    public WhatsappChat createChat(UUID ownerUuid, String contactId) {
        WhatsappChat chat = new WhatsappChat(
                UUID.randomUUID().toString(),
                contactId,
                false,
                false,
                0,
                new ArrayList<>()
        );

        getOrCreateChats(ownerUuid).add(chat);
        sortChats(ownerUuid);
        setDirty();
        return chat;
    }

    public WhatsappPresence getOrCreatePresence(UUID ownerUuid, String contactId) {
        WhatsappPresence existing = findPresenceByContactId(ownerUuid, contactId);
        if (existing != null) {
            return existing;
        }

        WhatsappPresence created = new WhatsappPresence(
                contactId,
                false,
                false,
                false,
                System.currentTimeMillis()
        );

        getOrCreatePresences(ownerUuid).add(created);
        setDirty();
        return created;
    }

    public void sortChats(UUID ownerUuid) {
        getOrCreateChats(ownerUuid).sort((a, b) -> {
            if (a.archived() != b.archived()) {
                return a.archived() ? 1 : -1;
            }

            if (a.pinned() != b.pinned()) {
                return a.pinned() ? -1 : 1;
            }

            return Long.compare(b.getLastMessageTimestamp(), a.getLastMessageTimestamp());
        });
    }

    private String generatePhoneNumber(UUID playerUuid) {
        long seed = Math.abs(playerUuid.getLeastSignificantBits());
        String digits = String.format("%08d", seed % 100_000_000L);
        return "+598 " + digits;
    }
}