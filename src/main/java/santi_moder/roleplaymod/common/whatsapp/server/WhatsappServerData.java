package santi_moder.roleplaymod.common.whatsapp.server;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.Tag;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.saveddata.SavedData;
import santi_moder.roleplaymod.common.whatsapp.model.WhatsappChat;
import santi_moder.roleplaymod.common.whatsapp.model.WhatsappContact;
import santi_moder.roleplaymod.common.whatsapp.model.WhatsappPresence;
import santi_moder.roleplaymod.common.whatsapp.model.WhatsappProfile;

import java.util.*;

public final class WhatsappServerData extends SavedData {

    public static final String DATA_NAME = "roleplaymod_whatsapp_server_data";

    private static final String TAG_ACCOUNTS = "accounts";
    private static final String TAG_PROFILES = "profiles";
    private static final String TAG_CONTACTS = "contacts";
    private static final String TAG_CHATS = "chats";
    private static final String TAG_PRESENCES = "presences";

    private static final String TAG_ACCOUNT_ID = "accountId";
    private static final String TAG_VALUE = "value";
    private static final String TAG_VALUES = "values";

    private final Map<UUID, WhatsappAccount> accountsById = new HashMap<>();
    private final Map<String, UUID> accountIdBySimId = new HashMap<>();
    private final Map<String, UUID> accountIdByPhoneNumber = new HashMap<>();
    private final Map<UUID, UUID> accountIdByLastKnownPlayer = new HashMap<>();

    private final Map<UUID, WhatsappProfile> profilesByAccount = new HashMap<>();
    private final Map<UUID, List<WhatsappContact>> contactsByAccount = new HashMap<>();
    private final Map<UUID, List<WhatsappChat>> chatsByAccount = new HashMap<>();
    private final Map<UUID, List<WhatsappPresence>> presencesByAccount = new HashMap<>();

    public static WhatsappServerData get(ServerLevel level) {
        return level.getDataStorage().computeIfAbsent(
                WhatsappServerData::load,
                WhatsappServerData::new,
                DATA_NAME
        );
    }

    public static WhatsappServerData load(CompoundTag tag) {
        WhatsappServerData data = new WhatsappServerData();

        data.loadAccounts(tag);
        data.loadProfiles(tag);
        data.loadContacts(tag);
        data.loadChats(tag);
        data.loadPresences(tag);
        data.rebuildIndexes();

        return data;
    }

    @Override
    public CompoundTag save(CompoundTag tag) {
        tag.put(TAG_ACCOUNTS, saveAccounts());
        tag.put(TAG_PROFILES, saveProfiles());
        tag.put(TAG_CONTACTS, saveContacts());
        tag.put(TAG_CHATS, saveChats());
        tag.put(TAG_PRESENCES, savePresences());
        return tag;
    }

    public WhatsappAccount getOrCreateAccountBySim(
            String simId,
            String phoneNumber,
            String fallbackName,
            UUID currentPlayerUuid
    ) {
        if (isBlank(simId) || isBlank(phoneNumber)) {
            return null;
        }

        WhatsappAccount existing = findAccountBySimId(simId);
        if (existing == null) {
            existing = findAccountByPhone(phoneNumber);
        }

        if (existing != null) {
            existing.setSimId(simId);
            existing.setPhoneNumber(phoneNumber);
            existing.setLastKnownPlayerUuid(currentPlayerUuid);

            if (isBlank(existing.displayName())) {
                existing.setDisplayName(fallbackName == null ? "Usuario" : fallbackName);
            }

            registerIndexes(existing);
            ensureContainers(existing.accountId(), existing.displayName(), existing.phoneNumber());
            setDirty();
            return existing;
        }

        WhatsappAccount created = new WhatsappAccount(
                UUID.randomUUID(),
                simId,
                phoneNumber,
                fallbackName == null ? "Usuario" : fallbackName,
                "",
                "default",
                true,
                currentPlayerUuid
        );

        accountsById.put(created.accountId(), created);
        registerIndexes(created);
        ensureContainers(created.accountId(), created.displayName(), created.phoneNumber());
        setDirty();

        return created;
    }

    public WhatsappAccount getOrCreateAccountByPhone(String phoneNumber, String fallbackName) {
        if (isBlank(phoneNumber)) {
            return null;
        }

        WhatsappAccount existing = findAccountByPhone(phoneNumber);
        if (existing != null) {
            ensureContainers(existing.accountId(), existing.displayName(), existing.phoneNumber());
            return existing;
        }

        WhatsappAccount created = new WhatsappAccount(
                UUID.randomUUID(),
                "",
                phoneNumber,
                fallbackName == null ? "Usuario" : fallbackName,
                "",
                "default",
                true,
                null
        );

        accountsById.put(created.accountId(), created);
        registerIndexes(created);
        ensureContainers(created.accountId(), created.displayName(), created.phoneNumber());
        setDirty();

        return created;
    }

    /**
     * Compatibilidad temporal con código viejo.
     * Crea una cuenta ligada al jugador si todavía algún flujo viejo llama esto.
     */
    public WhatsappAccount getOrCreateAccount(UUID playerUuid, String fallbackName) {
        if (playerUuid == null) {
            return null;
        }

        WhatsappAccount existing = findAccountByPlayer(playerUuid);
        if (existing != null) {
            ensureContainers(existing.accountId(), existing.displayName(), existing.phoneNumber());
            return existing;
        }

        String generatedPhone = generatePhoneNumber(playerUuid);

        WhatsappAccount created = new WhatsappAccount(
                UUID.randomUUID(),
                "",
                generatedPhone,
                fallbackName == null ? "Jugador" : fallbackName,
                "",
                "default",
                true,
                playerUuid
        );

        accountsById.put(created.accountId(), created);
        registerIndexes(created);
        ensureContainers(created.accountId(), created.displayName(), created.phoneNumber());
        setDirty();

        return created;
    }

    public WhatsappProfile getOrCreateProfile(UUID accountId, String fallbackName) {
        if (accountId == null) {
            return WhatsappProfile.createDefault(fallbackName == null ? "Jugador" : fallbackName, "");
        }

        WhatsappProfile profile = profilesByAccount.get(accountId);
        if (profile != null) {
            return profile;
        }

        WhatsappAccount account = accountsById.get(accountId);
        String displayName = account != null ? account.displayName() : fallbackName;
        String phoneNumber = account != null ? account.phoneNumber() : "";

        WhatsappProfile created = WhatsappProfile.createDefault(
                displayName == null ? "Jugador" : displayName,
                phoneNumber
        );

        profilesByAccount.put(accountId, created);
        setDirty();
        return created;
    }

    public List<WhatsappContact> getOrCreateContacts(UUID accountId) {
        return contactsByAccount.computeIfAbsent(accountId, ignored -> new ArrayList<>());
    }

    public List<WhatsappChat> getOrCreateChats(UUID accountId) {
        return chatsByAccount.computeIfAbsent(accountId, ignored -> new ArrayList<>());
    }

    public List<WhatsappPresence> getOrCreatePresences(UUID accountId) {
        return presencesByAccount.computeIfAbsent(accountId, ignored -> new ArrayList<>());
    }

    public WhatsappAccount findAccountById(UUID accountId) {
        return accountId == null ? null : accountsById.get(accountId);
    }

    public WhatsappAccount findAccountBySimId(String simId) {
        if (isBlank(simId)) {
            return null;
        }

        UUID accountId = accountIdBySimId.get(simId);
        return accountId == null ? null : accountsById.get(accountId);
    }

    public WhatsappAccount findAccountByPhone(String phoneNumber) {
        if (isBlank(phoneNumber)) {
            return null;
        }

        UUID accountId = accountIdByPhoneNumber.get(phoneNumber);
        return accountId == null ? null : accountsById.get(accountId);
    }

    public WhatsappAccount findAccountByPlayer(UUID playerUuid) {
        if (playerUuid == null) {
            return null;
        }

        UUID accountId = accountIdByLastKnownPlayer.get(playerUuid);
        return accountId == null ? null : accountsById.get(accountId);
    }

    public UUID getAccountIdByPhone(String phoneNumber) {
        if (isBlank(phoneNumber)) {
            return null;
        }
        return accountIdByPhoneNumber.get(phoneNumber);
    }

    public UUID getAccountUuidByPhone(String phoneNumber) {
        return getAccountIdByPhone(phoneNumber);
    }

    public UUID getAccountIdBySimId(String simId) {
        if (isBlank(simId)) {
            return null;
        }
        return accountIdBySimId.get(simId);
    }

    public WhatsappContact findContactForOwner(UUID accountId, String contactId) {
        if (accountId == null || isBlank(contactId)) {
            return null;
        }

        for (WhatsappContact contact : getOrCreateContacts(accountId)) {
            if (contact.id().equals(contactId)) {
                return contact;
            }
        }

        return null;
    }

    public WhatsappChat findChatByContactId(UUID accountId, String contactId) {
        if (accountId == null || isBlank(contactId)) {
            return null;
        }

        for (WhatsappChat chat : getOrCreateChats(accountId)) {
            if (contactId.equals(chat.contactId())) {
                return chat;
            }
        }

        return null;
    }

    public WhatsappPresence findPresenceByContactId(UUID accountId, String contactId) {
        if (accountId == null || isBlank(contactId)) {
            return null;
        }

        for (WhatsappPresence presence : getOrCreatePresences(accountId)) {
            if (contactId.equals(presence.contactId())) {
                return presence;
            }
        }

        return null;
    }

    public WhatsappChat createChat(UUID accountId, String contactId) {
        WhatsappChat chat = new WhatsappChat(
                UUID.randomUUID().toString(),
                contactId,
                false,
                false,
                0,
                new ArrayList<>()
        );

        getOrCreateChats(accountId).add(chat);
        sortChats(accountId);
        setDirty();
        return chat;
    }

    public WhatsappPresence getOrCreatePresence(UUID accountId, String contactId) {
        WhatsappPresence existing = findPresenceByContactId(accountId, contactId);
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

        getOrCreatePresences(accountId).add(created);
        setDirty();
        return created;
    }

    public void sortChats(UUID accountId) {
        getOrCreateChats(accountId).sort((a, b) -> {
            if (a.archived() != b.archived()) {
                return a.archived() ? 1 : -1;
            }

            if (a.pinned() != b.pinned()) {
                return a.pinned() ? -1 : 1;
            }

            return Long.compare(b.getLastMessageTimestamp(), a.getLastMessageTimestamp());
        });
    }

    private void ensureContainers(UUID accountId, String displayName, String phoneNumber) {
        if (accountId == null) {
            return;
        }

        profilesByAccount.computeIfAbsent(
                accountId,
                ignored -> WhatsappProfile.createDefault(
                        displayName == null ? "Usuario" : displayName,
                        phoneNumber == null ? "" : phoneNumber
                )
        );

        contactsByAccount.computeIfAbsent(accountId, ignored -> new ArrayList<>());
        chatsByAccount.computeIfAbsent(accountId, ignored -> new ArrayList<>());
        presencesByAccount.computeIfAbsent(accountId, ignored -> new ArrayList<>());
    }

    private void registerIndexes(WhatsappAccount account) {
        if (account == null) {
            return;
        }

        accountsById.put(account.accountId(), account);

        if (!isBlank(account.simId())) {
            accountIdBySimId.put(account.simId(), account.accountId());
        }

        if (!isBlank(account.phoneNumber())) {
            accountIdByPhoneNumber.put(account.phoneNumber(), account.accountId());
        }

        if (account.lastKnownPlayerUuid() != null) {
            accountIdByLastKnownPlayer.put(account.lastKnownPlayerUuid(), account.accountId());
        }
    }

    private void rebuildIndexes() {
        accountIdBySimId.clear();
        accountIdByPhoneNumber.clear();
        accountIdByLastKnownPlayer.clear();

        for (WhatsappAccount account : accountsById.values()) {
            registerIndexes(account);
            ensureContainers(account.accountId(), account.displayName(), account.phoneNumber());
        }
    }

    private ListTag saveAccounts() {
        ListTag list = new ListTag();

        for (WhatsappAccount account : accountsById.values()) {
            if (account != null) {
                list.add(account.save());
            }
        }

        return list;
    }

    private void loadAccounts(CompoundTag root) {
        if (!root.contains(TAG_ACCOUNTS, Tag.TAG_LIST)) {
            return;
        }

        ListTag list = root.getList(TAG_ACCOUNTS, Tag.TAG_COMPOUND);

        for (int i = 0; i < list.size(); i++) {
            WhatsappAccount account = WhatsappAccount.load(list.getCompound(i));
            if (account != null && account.accountId() != null) {
                accountsById.put(account.accountId(), account);
            }
        }
    }

    private ListTag saveProfiles() {
        ListTag list = new ListTag();

        for (Map.Entry<UUID, WhatsappProfile> entry : profilesByAccount.entrySet()) {
            if (entry.getKey() == null || entry.getValue() == null) {
                continue;
            }

            CompoundTag wrapper = new CompoundTag();
            wrapper.putUUID(TAG_ACCOUNT_ID, entry.getKey());
            wrapper.put(TAG_VALUE, entry.getValue().save());
            list.add(wrapper);
        }

        return list;
    }

    private void loadProfiles(CompoundTag root) {
        if (!root.contains(TAG_PROFILES, Tag.TAG_LIST)) {
            return;
        }

        ListTag list = root.getList(TAG_PROFILES, Tag.TAG_COMPOUND);

        for (int i = 0; i < list.size(); i++) {
            CompoundTag wrapper = list.getCompound(i);

            UUID accountId = readAccountIdFromWrapper(wrapper);
            if (accountId == null || !wrapper.contains(TAG_VALUE, Tag.TAG_COMPOUND)) {
                continue;
            }

            profilesByAccount.put(accountId, WhatsappProfile.load(wrapper.getCompound(TAG_VALUE)));
        }
    }

    private ListTag saveContacts() {
        ListTag list = new ListTag();

        for (Map.Entry<UUID, List<WhatsappContact>> entry : contactsByAccount.entrySet()) {
            if (entry.getKey() == null || entry.getValue() == null) {
                continue;
            }

            CompoundTag wrapper = new CompoundTag();
            wrapper.putUUID(TAG_ACCOUNT_ID, entry.getKey());

            ListTag values = new ListTag();
            for (WhatsappContact contact : entry.getValue()) {
                if (contact != null) {
                    values.add(contact.save());
                }
            }

            wrapper.put(TAG_VALUES, values);
            list.add(wrapper);
        }

        return list;
    }

    private void loadContacts(CompoundTag root) {
        if (!root.contains(TAG_CONTACTS, Tag.TAG_LIST)) {
            return;
        }

        ListTag list = root.getList(TAG_CONTACTS, Tag.TAG_COMPOUND);

        for (int i = 0; i < list.size(); i++) {
            CompoundTag wrapper = list.getCompound(i);

            UUID accountId = readAccountIdFromWrapper(wrapper);
            if (accountId == null || !wrapper.contains(TAG_VALUES, Tag.TAG_LIST)) {
                continue;
            }

            ListTag values = wrapper.getList(TAG_VALUES, Tag.TAG_COMPOUND);
            List<WhatsappContact> contacts = new ArrayList<>();

            for (int j = 0; j < values.size(); j++) {
                WhatsappContact contact = WhatsappContact.load(values.getCompound(j));
                if (contact != null) {
                    contacts.add(contact);
                }
            }

            contactsByAccount.put(accountId, contacts);
        }
    }

    private ListTag saveChats() {
        ListTag list = new ListTag();

        for (Map.Entry<UUID, List<WhatsappChat>> entry : chatsByAccount.entrySet()) {
            if (entry.getKey() == null || entry.getValue() == null) {
                continue;
            }

            CompoundTag wrapper = new CompoundTag();
            wrapper.putUUID(TAG_ACCOUNT_ID, entry.getKey());

            ListTag values = new ListTag();
            for (WhatsappChat chat : entry.getValue()) {
                if (chat != null) {
                    values.add(chat.save());
                }
            }

            wrapper.put(TAG_VALUES, values);
            list.add(wrapper);
        }

        return list;
    }

    private void loadChats(CompoundTag root) {
        if (!root.contains(TAG_CHATS, Tag.TAG_LIST)) {
            return;
        }

        ListTag list = root.getList(TAG_CHATS, Tag.TAG_COMPOUND);

        for (int i = 0; i < list.size(); i++) {
            CompoundTag wrapper = list.getCompound(i);

            UUID accountId = readAccountIdFromWrapper(wrapper);
            if (accountId == null || !wrapper.contains(TAG_VALUES, Tag.TAG_LIST)) {
                continue;
            }

            ListTag values = wrapper.getList(TAG_VALUES, Tag.TAG_COMPOUND);
            List<WhatsappChat> chats = new ArrayList<>();

            for (int j = 0; j < values.size(); j++) {
                WhatsappChat chat = WhatsappChat.load(values.getCompound(j));
                if (chat != null) {
                    chats.add(chat);
                }
            }

            chats.sort((a, b) -> {
                if (a.archived() != b.archived()) {
                    return a.archived() ? 1 : -1;
                }

                if (a.pinned() != b.pinned()) {
                    return a.pinned() ? -1 : 1;
                }

                return Long.compare(b.getLastMessageTimestamp(), a.getLastMessageTimestamp());
            });

            chatsByAccount.put(accountId, chats);
        }
    }

    private ListTag savePresences() {
        ListTag list = new ListTag();

        for (Map.Entry<UUID, List<WhatsappPresence>> entry : presencesByAccount.entrySet()) {
            if (entry.getKey() == null || entry.getValue() == null) {
                continue;
            }

            CompoundTag wrapper = new CompoundTag();
            wrapper.putUUID(TAG_ACCOUNT_ID, entry.getKey());

            ListTag values = new ListTag();
            for (WhatsappPresence presence : entry.getValue()) {
                if (presence != null) {
                    values.add(presence.save());
                }
            }

            wrapper.put(TAG_VALUES, values);
            list.add(wrapper);
        }

        return list;
    }

    private void loadPresences(CompoundTag root) {
        if (!root.contains(TAG_PRESENCES, Tag.TAG_LIST)) {
            return;
        }

        ListTag list = root.getList(TAG_PRESENCES, Tag.TAG_COMPOUND);

        for (int i = 0; i < list.size(); i++) {
            CompoundTag wrapper = list.getCompound(i);

            UUID accountId = readAccountIdFromWrapper(wrapper);
            if (accountId == null || !wrapper.contains(TAG_VALUES, Tag.TAG_LIST)) {
                continue;
            }

            ListTag values = wrapper.getList(TAG_VALUES, Tag.TAG_COMPOUND);
            List<WhatsappPresence> presences = new ArrayList<>();

            for (int j = 0; j < values.size(); j++) {
                WhatsappPresence presence = WhatsappPresence.load(values.getCompound(j));
                if (presence != null) {
                    presences.add(presence);
                }
            }

            presencesByAccount.put(accountId, presences);
        }
    }

    private UUID readAccountIdFromWrapper(CompoundTag wrapper) {
        if (wrapper.hasUUID(TAG_ACCOUNT_ID)) {
            return wrapper.getUUID(TAG_ACCOUNT_ID);
        }

        // Compatibilidad con saves viejos del sistema playerUuid.
        if (wrapper.hasUUID("playerUuid")) {
            return wrapper.getUUID("playerUuid");
        }

        return null;
    }

    private String generatePhoneNumber(UUID playerUuid) {
        long seed = Math.abs(playerUuid.getLeastSignificantBits());
        String digits = String.format("%08d", seed % 100_000_000L);
        return "+598 " + digits;
    }

    private static boolean isBlank(String value) {
        return value == null || value.isBlank();
    }
}