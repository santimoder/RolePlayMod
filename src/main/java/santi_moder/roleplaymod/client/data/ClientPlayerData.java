package santi_moder.roleplaymod.client.data;

import net.minecraft.nbt.CompoundTag;
import santi_moder.roleplaymod.common.player.BleedingType;
import santi_moder.roleplaymod.common.player.BodyPart;

import java.util.EnumMap;
import java.util.Map;

public final class ClientPlayerData {

    private static final Map<BodyPart, Integer> bodyHp = new EnumMap<>(BodyPart.class);
    private static final Map<BodyPart, BleedingType> bleedings = new EnumMap<>(BodyPart.class);

    private static int sangre = 100;
    private static int stamina = 100;
    private static int sed = 100;
    private static int shock = 0;

    private static boolean inconsciente = false;

    static {
        for (BodyPart part : BodyPart.values()) {
            bodyHp.put(part, getMaxBodyHp(part));
            bleedings.put(part, BleedingType.NONE);
        }
    }

    private ClientPlayerData() {
    }

    public static int getSangre() {
        return sangre;
    }

    public static void setSangre(int value) {
        sangre = clampPercent(value);
    }

    public static int getStamina() {
        return stamina;
    }

    public static void setStamina(int value) {
        stamina = clampPercent(value);
    }

    public static int getSed() {
        return sed;
    }

    public static void setSed(int value) {
        sed = clampPercent(value);
    }

    public static int getShock() {
        return shock;
    }

    public static void setShock(int value) {
        shock = clampPercent(value);
    }

    public static boolean isInconsciente() {
        return inconsciente;
    }

    public static void setInconsciente(boolean value) {
        inconsciente = value;
    }

    public static int getBodyHp(BodyPart part) {
        if (part == null) return 0;
        return bodyHp.getOrDefault(part, getMaxBodyHp(part));
    }

    public static void setBodyHp(BodyPart part, int hp) {
        if (part == null) return;
        bodyHp.put(part, clampBodyHp(part, hp));
    }

    public static BleedingType getBleeding(BodyPart part) {
        if (part == null) return BleedingType.NONE;
        return bleedings.getOrDefault(part, BleedingType.NONE);
    }

    public static void setBleeding(BodyPart part, BleedingType type) {
        if (part == null) return;
        bleedings.put(part, type == null ? BleedingType.NONE : type);
    }

    public static void applyBodyParts(CompoundTag tag) {
        if (tag == null) return;

        for (BodyPart part : BodyPart.values()) {
            if (tag.contains(part.name())) {
                setBodyHp(part, tag.getInt(part.name()));
            }

            if (tag.contains(part.name() + "_bleeding")) {
                int ordinal = tag.getInt(part.name() + "_bleeding");
                BleedingType[] values = BleedingType.values();

                if (ordinal >= 0 && ordinal < values.length) {
                    setBleeding(part, values[ordinal]);
                } else {
                    setBleeding(part, BleedingType.NONE);
                }
            }
        }
    }

    public static void reset() {
        sangre = 100;
        stamina = 100;
        sed = 100;
        shock = 0;
        inconsciente = false;

        for (BodyPart part : BodyPart.values()) {
            bodyHp.put(part, getMaxBodyHp(part));
            bleedings.put(part, BleedingType.NONE);
        }
    }

    public static int getMaxBodyHp(BodyPart part) {
        if (part == null) return 0;

        return switch (part) {
            case HEAD -> 20;
            case TORSO -> 60;
            case LEFT_ARM, RIGHT_ARM -> 25;
            case LEFT_LEG, RIGHT_LEG -> 30;
        };
    }

    private static int clampPercent(int value) {
        return Math.max(0, Math.min(100, value));
    }

    private static int clampBodyHp(BodyPart part, int value) {
        return Math.max(0, Math.min(getMaxBodyHp(part), value));
    }
}