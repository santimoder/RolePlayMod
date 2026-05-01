package santi_moder.roleplaymod.server.combat.weapon;

import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.Entity;

public final class WeaponDamageResolver {

    private WeaponDamageResolver() {
    }

    public static WeaponDamageProfile resolve(DamageSource source, float rawDamage) {
        String sourceId = source.typeHolder()
                .unwrapKey()
                .map(key -> key.location().toString().toLowerCase())
                .orElse("");

        String msgId = source.getMsgId().toLowerCase();

        Entity direct = source.getDirectEntity();
        Entity attacker = source.getEntity();

        String directClass = direct == null ? "" : direct.getClass().getName().toLowerCase();
        String attackerClass = attacker == null ? "" : attacker.getClass().getName().toLowerCase();

        String all = sourceId + " " + msgId + " " + directClass + " " + attackerClass;

        WeaponCategory category = resolveCategory(all, rawDamage);

        return WeaponDamageProfile.forCategory(category);
    }

    private static WeaponCategory resolveCategory(String text, float rawDamage) {
        if (containsAny(text, "grenade", "explosive", "explosion", "rocket", "rpg")) {
            return WeaponCategory.EXPLOSIVE;
        }

        if (containsAny(text, "shotgun", "buckshot", "shell")) {
            return WeaponCategory.SHOTGUN;
        }

        if (containsAny(text, "sniper", "marksman", "dmr", "anti_material", "50bmg")) {
            return WeaponCategory.SNIPER;
        }

        if (containsAny(text, "rifle", "ak", "m4", "scar", "g36", "aug", "556", "762")) {
            return WeaponCategory.RIFLE;
        }

        if (containsAny(text, "smg", "submachine", "mp5", "ump", "p90", "vector")) {
            return WeaponCategory.SMG;
        }

        if (containsAny(text, "pistol", "handgun", "revolver", "glock", "m1911", "deagle")) {
            return WeaponCategory.PISTOL;
        }

        if (rawDamage >= 18.0F) return WeaponCategory.SNIPER;
        if (rawDamage >= 11.0F) return WeaponCategory.RIFLE;
        if (rawDamage >= 7.0F) return WeaponCategory.PISTOL;
        if (rawDamage >= 3.0F) return WeaponCategory.SMG;

        return WeaponCategory.GENERIC;
    }

    private static boolean containsAny(String text, String... values) {
        for (String value : values) {
            if (text.contains(value)) {
                return true;
            }
        }

        return false;
    }
}