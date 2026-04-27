package santi_moder.roleplaymod.common.phone.sim;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.Tag;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.saveddata.SavedData;

import java.util.HashSet;
import java.util.Random;
import java.util.Set;
import java.util.UUID;

public final class PhoneNumberRegistry extends SavedData {

    public static final String DATA_NAME = "roleplaymod_phone_number_registry";

    private static final String TAG_USED_NUMBERS = "used_numbers";
    private static final String TAG_NUMBER = "number";

    private static final int MAX_GENERATION_ATTEMPTS = 10_000;

    private final Set<String> usedNumbers = new HashSet<>();
    private final Random random = new Random();

    public static PhoneNumberRegistry get(ServerLevel level) {
        return level.getDataStorage().computeIfAbsent(
                PhoneNumberRegistry::load,
                PhoneNumberRegistry::new,
                DATA_NAME
        );
    }

    public static PhoneNumberRegistry load(CompoundTag tag) {
        PhoneNumberRegistry registry = new PhoneNumberRegistry();

        if (!tag.contains(TAG_USED_NUMBERS, Tag.TAG_LIST)) {
            return registry;
        }

        ListTag list = tag.getList(TAG_USED_NUMBERS, Tag.TAG_COMPOUND);

        for (int i = 0; i < list.size(); i++) {
            CompoundTag entry = list.getCompound(i);
            String number = PhoneNumberFormatter.normalize(entry.getString(TAG_NUMBER));

            if (PhoneNumberFormatter.isValidNineDigitMobile(number)) {
                registry.usedNumbers.add(number);
            }
        }

        return registry;
    }

    @Override
    public CompoundTag save(CompoundTag tag) {
        ListTag list = new ListTag();

        for (String number : usedNumbers) {
            CompoundTag entry = new CompoundTag();
            entry.putString(TAG_NUMBER, number);
            list.add(entry);
        }

        tag.put(TAG_USED_NUMBERS, list);
        return tag;
    }

    public String reserveNewNumber(PhoneCarrier carrier) {
        for (int attempt = 0; attempt < MAX_GENERATION_ATTEMPTS; attempt++) {
            String number = PhoneNumberGenerator.generateRawNumber(carrier, random);

            if (reserveNumber(number)) {
                return number;
            }
        }

        throw new IllegalStateException("No se pudo generar un numero unico para " + carrier.displayName());
    }

    public boolean reserveNumber(String rawNumber) {
        String normalized = PhoneNumberFormatter.normalize(rawNumber);

        if (!PhoneNumberFormatter.isValidNineDigitMobile(normalized)) {
            return false;
        }

        if (usedNumbers.contains(normalized)) {
            return false;
        }

        usedNumbers.add(normalized);
        setDirty();
        return true;
    }

    public boolean isNumberUsed(String rawNumber) {
        String normalized = PhoneNumberFormatter.normalize(rawNumber);
        return usedNumbers.contains(normalized);
    }

    public void releaseNumber(String rawNumber) {
        String normalized = PhoneNumberFormatter.normalize(rawNumber);

        if (usedNumbers.remove(normalized)) {
            setDirty();
        }
    }

    public int usedCount() {
        return usedNumbers.size();
    }

    public String generateSimId(PhoneCarrier carrier) {
        String carrierPart = carrier == null ? "ANTEL" : carrier.name();
        return carrierPart + "-" + UUID.randomUUID();
    }
}