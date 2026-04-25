package santi_moder.roleplaymod.client.data;

import net.minecraft.nbt.CompoundTag;
import santi_moder.roleplaymod.common.player.BodyPart;
import santi_moder.roleplaymod.common.player.BleedingType;
import java.util.EnumMap;
import java.util.Map;

public class ClientPlayerData {
    private static final Map<BodyPart, Integer> bodyHp = new EnumMap<>(BodyPart.class);
    private static final Map<BodyPart, BleedingType> bleedings = new EnumMap<>(BodyPart.class);
    static {
        for (BodyPart part : BodyPart.values()) {
            bodyHp.put(part, 5);
            bleedings.put(part, BleedingType.NONE);
        }
    }

    private static int sangre = 100;
    private static int stamina = 100;
    private static int sed = 100;

    public static void setSangre(int value) { sangre = value; }
    public static void setStamina(int value) { stamina = value; }
    public static void setSed(int value) { sed = value; }

    public static int getSangre() { return sangre; }
    public static int getStamina() { return stamina; }
    public static int getSed() { return sed; }

    // ================= BODY PARTS (CLIENTE) =================

    public static int getBodyHp(BodyPart part) {
        return bodyHp.getOrDefault(part, 5);
    }

    public static void setBodyHp(BodyPart part, int hp) {
        bodyHp.put(part, Math.max(0, Math.min(5, hp)));
    }

    public static BleedingType getBleeding(BodyPart part) {
        return bleedings.getOrDefault(part, BleedingType.NONE);
    }

    public static void setBleeding(BodyPart part, BleedingType type) {
        bleedings.put(part, type);
    }

    public static void applyBodyParts(CompoundTag tag) {
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
}
