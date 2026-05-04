package santi_moder.roleplaymod.client.data;

import net.minecraft.nbt.CompoundTag;
import santi_moder.roleplaymod.common.player.BleedingType;
import santi_moder.roleplaymod.common.player.BodyPart;

import java.util.EnumMap;
import java.util.Map;
import java.util.UUID;

public final class ClientPatientMedicalData {

    private static UUID targetUuid;
    private static String targetName = "";

    private static int sangre;
    private static int shock;
    private static boolean inconsciente;

    private static final Map<BodyPart, Integer> bodyHp = new EnumMap<>(BodyPart.class);
    private static final Map<BodyPart, BleedingType> bleedings = new EnumMap<>(BodyPart.class);

    private ClientPatientMedicalData() {}

    public static void set(UUID uuid, String name, int blood, int shockValue, boolean unconscious, CompoundTag bodyTag) {
        targetUuid = uuid;
        targetName = name;
        sangre = blood;
        shock = shockValue;
        inconsciente = unconscious;
        applyBodyParts(bodyTag);
    }

    public static UUID getTargetUuid() { return targetUuid; }
    public static String getTargetName() { return targetName; }
    public static int getSangre() { return sangre; }
    public static int getShock() { return shock; }
    public static boolean isInconsciente() { return inconsciente; }

    public static int getBodyHp(BodyPart part) {
        return bodyHp.getOrDefault(part, 0);
    }

    public static BleedingType getBleeding(BodyPart part) {
        return bleedings.getOrDefault(part, BleedingType.NONE);
    }

    private static void applyBodyParts(CompoundTag tag) {
        if (tag == null) return;

        for (BodyPart part : BodyPart.values()) {
            if (tag.contains(part.name())) {
                bodyHp.put(part, tag.getInt(part.name()));
            }

            if (tag.contains(part.name() + "_bleeding")) {
                int ordinal = tag.getInt(part.name() + "_bleeding");
                BleedingType[] values = BleedingType.values();

                bleedings.put(
                        part,
                        ordinal >= 0 && ordinal < values.length ? values[ordinal] : BleedingType.NONE
                );
            }
        }
    }
}