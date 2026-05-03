package santi_moder.roleplaymod.server.combat.weapon;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.registries.ForgeRegistries;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Locale;
import java.util.Map;
import java.util.Set;

public final class TaCZWeaponResolver {

    private static final boolean DEBUG_GUN_ID = false;

    private static final Map<String, WeaponDamageProfile> WEAPONS = new HashMap<>();
    private static final Set<String> DISCOVERED_GUNS = new HashSet<>();

    static {
        // Pistolas
        register("tacz:deagle", "Desert Eagle", WeaponCategory.PISTOL, 9.0F, 1.25F, 1.20F, 1.25F, true, true);
        register("tacz:glock_17", "Glock 17", WeaponCategory.PISTOL, 6.0F, 1.00F, 1.00F, 1.00F, false, false);
        register("tacz:deagle_golden", "Golden Desert Eagle", WeaponCategory.PISTOL, 9.5F, 1.30F, 1.25F, 1.30F, true, true);
        register("mw19:x16", "X16", WeaponCategory.PISTOL, 6.0F, 1.00F, 1.00F, 1.00F, false, false);
        register("tacz:m1911", "M1911", WeaponCategory.PISTOL, 6.5F, 1.05F, 1.05F, 1.05F, false, false);
        register("tacz:p320", "P320", WeaponCategory.PISTOL, 6.2F, 1.00F, 1.00F, 1.00F, false, false);
        register("tacz:cz75", "CZ75", WeaponCategory.PISTOL, 6.2F, 1.00F, 1.00F, 1.00F, false, false);
        register("tacz:b93r", "B93R", WeaponCategory.PISTOL, 5.8F, 0.95F, 0.95F, 0.95F, false, false);

        // Escopetas
        register("tacz:db_short", "Double Barrel Short", WeaponCategory.SHOTGUN, 12.5F, 1.65F, 1.55F, 1.50F, true, false);
        register("tacz:db_long", "Double Barrel Long", WeaponCategory.SHOTGUN, 13.5F, 1.75F, 1.60F, 1.55F, true, false);
        register("tacz:m870", "M870", WeaponCategory.SHOTGUN, 12.0F, 1.60F, 1.50F, 1.45F, true, false);
        register("tacz:aa12", "AA-12", WeaponCategory.SHOTGUN, 10.5F, 1.40F, 1.35F, 1.30F, true, false);
        register("tacz:spas_12", "SPAS-12", WeaponCategory.SHOTGUN, 13.0F, 1.70F, 1.55F, 1.50F, true, false);
        register("tacz:m1014", "M1014", WeaponCategory.SHOTGUN, 12.5F, 1.65F, 1.50F, 1.45F, true, false);
        register("mw19:725", "725", WeaponCategory.SHOTGUN, 13.5F, 1.75F, 1.60F, 1.55F, true, false);

        // Subfusiles
        register("tacz:ump45", "UMP45", WeaponCategory.SMG, 6.5F, 1.00F, 1.00F, 1.00F, false, false);
        register("tacz:hk_mp5a5", "HK MP5A5", WeaponCategory.SMG, 5.5F, 0.90F, 0.90F, 0.90F, false, false);
        register("tacz:uzi", "UZI", WeaponCategory.SMG, 5.2F, 0.85F, 0.85F, 0.85F, false, false);
        register("tacz:vector45", "Vector .45", WeaponCategory.SMG, 5.8F, 0.90F, 0.90F, 0.90F, false, false);
        register("tacz:p90", "P90", WeaponCategory.SMG, 5.0F, 0.85F, 0.85F, 0.85F, false, false);
        register("mw19:smg45", "SMG45", WeaponCategory.SMG, 6.2F, 0.95F, 0.95F, 0.95F, false, false);
        register("mw19:iso", "ISO", WeaponCategory.SMG, 5.4F, 0.88F, 0.88F, 0.88F, false, false);
        register("mw19:mp7", "MP7", WeaponCategory.SMG, 5.2F, 0.85F, 0.85F, 0.85F, false, false);

        // Fusiles
        register("tacz:sks_tactical", "SKS Tactical", WeaponCategory.RIFLE, 9.5F, 1.25F, 1.20F, 1.20F, true, true);
        register("tacz:ak47", "AK-47", WeaponCategory.RIFLE, 10.0F, 1.35F, 1.25F, 1.30F, true, true);
        register("tacz:type_81", "Type 81", WeaponCategory.RIFLE, 9.8F, 1.30F, 1.22F, 1.25F, true, true);
        register("tacz:qbz_95", "QBZ-95", WeaponCategory.RIFLE, 9.0F, 1.25F, 1.18F, 1.20F, true, true);
        register("tacz:hk416d", "HK416D", WeaponCategory.RIFLE, 9.0F, 1.25F, 1.20F, 1.20F, true, true);
        register("tacz:m4a1", "M4A1", WeaponCategory.RIFLE, 9.0F, 1.25F, 1.20F, 1.20F, true, true);
        register("tacz:m16a1", "M16A1", WeaponCategory.RIFLE, 9.2F, 1.25F, 1.20F, 1.20F, true, true);
        register("tacz:hk_g3", "HK G3", WeaponCategory.RIFLE, 11.5F, 1.45F, 1.35F, 1.40F, true, true);
        register("tacz:m16a4", "M16A4", WeaponCategory.RIFLE, 9.2F, 1.25F, 1.20F, 1.20F, true, true);
        register("tacz:spr15hb", "SPR-15 HB", WeaponCategory.RIFLE, 10.0F, 1.35F, 1.25F, 1.30F, true, true);
        register("tacz:mk14", "MK14", WeaponCategory.RIFLE, 11.0F, 1.40F, 1.30F, 1.35F, true, true);
        register("tacz:scar_l", "SCAR-L", WeaponCategory.RIFLE, 9.0F, 1.25F, 1.20F, 1.20F, true, true);
        register("tacz:scar_h", "SCAR-H", WeaponCategory.RIFLE, 11.5F, 1.45F, 1.35F, 1.40F, true, true);
        register("tacz:aug", "AUG", WeaponCategory.RIFLE, 9.0F, 1.25F, 1.18F, 1.20F, true, true);
        register("tacz:g36k", "G36K", WeaponCategory.RIFLE, 8.8F, 1.20F, 1.15F, 1.15F, true, true);
        register("tacz:fn_fal", "FN FAL", WeaponCategory.RIFLE, 11.5F, 1.45F, 1.35F, 1.40F, true, true);
        register("tacz:qbz_191", "QBZ-191", WeaponCategory.RIFLE, 9.0F, 1.25F, 1.18F, 1.20F, true, true);
        register("mw19:aug", "MW AUG", WeaponCategory.RIFLE, 9.0F, 1.25F, 1.18F, 1.20F, true, true);
        register("mw19:mk18", "MK18", WeaponCategory.RIFLE, 8.8F, 1.20F, 1.15F, 1.15F, true, true);
        register("mw19:asval", "AS VAL", WeaponCategory.RIFLE, 8.7F, 1.20F, 1.15F, 1.15F, true, true);
        register("mw19:141", "Kilo 141", WeaponCategory.RIFLE, 9.0F, 1.25F, 1.18F, 1.20F, true, true);
        register("mw19:ak48", "AK Variant", WeaponCategory.RIFLE, 10.0F, 1.35F, 1.25F, 1.30F, true, true);
        register("mw19:cr56", "CR-56", WeaponCategory.RIFLE, 9.8F, 1.30F, 1.22F, 1.25F, true, true);

        // Ametralladoras
        register("tacz:m249", "M249", WeaponCategory.RIFLE, 8.8F, 1.20F, 1.15F, 1.15F, true, true);
        register("tacz:minigun", "Minigun", WeaponCategory.RIFLE, 8.0F, 1.10F, 1.10F, 1.10F, true, true);
        register("tacz:rpk", "RPK", WeaponCategory.RIFLE, 9.8F, 1.30F, 1.22F, 1.25F, true, true);
        register("tacz:fn_evolys", "FN Evolys", WeaponCategory.RIFLE, 8.8F, 1.20F, 1.15F, 1.15F, true, true);

        // Francotiradores
        register("tacz:m700", "M700", WeaponCategory.SNIPER, 17.0F, 2.00F, 1.70F, 1.85F, true, true);
        register("tacz:m107", "M107", WeaponCategory.SNIPER, 23.0F, 2.55F, 2.00F, 2.25F, true, true);
        register("tacz:springfield1873", "Springfield 1873", WeaponCategory.SNIPER, 15.0F, 1.80F, 1.55F, 1.70F, true, true);
        register("tacz:m95", "M95", WeaponCategory.SNIPER, 24.0F, 2.60F, 2.00F, 2.30F, true, true);
        register("tacz:ai_awp", "AI AWP", WeaponCategory.SNIPER, 18.0F, 2.20F, 1.80F, 2.00F, true, true);
        register("mw19:amr", "AMR", WeaponCategory.SNIPER, 23.0F, 2.55F, 2.00F, 2.25F, true, true);
        register("mw19:hdr", "HDR", WeaponCategory.SNIPER, 21.0F, 2.45F, 1.95F, 2.15F, true, true);

        // Armas pesadas
        register("tacz:rpg7", "RPG-7", WeaponCategory.EXPLOSIVE, 22.0F, 2.00F, 1.60F, 2.20F, true, false);
        register("tacz:m320", "M320", WeaponCategory.EXPLOSIVE, 16.0F, 1.70F, 1.45F, 1.90F, true, false);
    }




    private TaCZWeaponResolver() {}

    // ==========================
    // ENTRY POINT PRINCIPAL
    // ==========================
    public static WeaponDamageProfile resolveFromPlayer(ServerPlayer player) {
        if (player == null) {
            return WeaponDamageProfile.generic();
        }

        ItemStack stack = player.getMainHandItem();

        if (stack.isEmpty()) {
            return WeaponDamageProfile.generic();
        }

        String gunId = readGunId(stack);

        if (gunId.isBlank()) {
            gunId = readRegistryId(stack);
        }

        if (DEBUG_GUN_ID) {
            System.out.println("[RolePlayMod][TaCZ] gunId=" + gunId + " stack=" + stack);
            System.out.println("[RolePlayMod][TaCZ] tag=" + stack.getTag());

            if (!gunId.isBlank() && DISCOVERED_GUNS.add(normalize(gunId))) {
                System.out.println("[RolePlayMod][TaCZ] NUEVA ARMA DETECTADA: " + gunId);
            }
        }

        return resolveFromGunId(gunId);
    }

    // ==========================
    // RESOLVER POR ID
    // ==========================
    public static WeaponDamageProfile resolveFromGunId(String gunId) {
        if (gunId == null || gunId.isBlank()) {
            return WeaponDamageProfile.generic();
        }

        WeaponDamageProfile profile = WEAPONS.get(normalize(gunId));

        if (profile != null) {
            return profile;
        }

        if (DISCOVERED_GUNS.add("unregistered:" + normalize(gunId))) {
            System.out.println("[RolePlayMod][TaCZ] Arma no registrada: " + gunId);
        }

        return WeaponDamageProfile.generic();
    }

    // ==========================
    // LEER GUN ID DESDE NBT
    // ==========================
    private static String readGunId(ItemStack stack) {
        CompoundTag tag = stack.getTag();

        if (tag == null) {
            return "";
        }

        String direct = readGunIdFromTag(tag);
        if (!direct.isBlank()) {
            return direct;
        }

        for (String key : tag.getAllKeys()) {
            if (!tag.contains(key)) continue;

            try {
                if (tag.get(key) instanceof CompoundTag nestedTag) {
                    String nested = readGunIdFromTag(nestedTag);

                    if (!nested.isBlank()) {
                        return nested;
                    }
                }
            } catch (Exception ignored) {
            }
        }

        return "";
    }

    private static String readGunIdFromTag(CompoundTag tag) {
        if (tag == null) return "";

        String[] possibleKeys = {
                "GunId",
                "gun_id",
                "Gun",
                "gun",
                "Id",
                "id",
                "WeaponId",
                "weapon_id",
                "tacz:gun_id"
        };

        for (String key : possibleKeys) {
            if (tag.contains(key)) {
                String value = tag.getString(key);

                if (value != null && !value.isBlank()) {
                    return value;
                }
            }
        }

        return "";
    }

    private static String readRegistryId(ItemStack stack) {
        if (stack == null || stack.isEmpty()) return "";

        if (ForgeRegistries.ITEMS.getKey(stack.getItem()) == null) {
            return "";
        }

        return ForgeRegistries.ITEMS.getKey(stack.getItem()).toString();
    }

    // ==========================
    // REGISTRO
    // ==========================
    private static void register(
            String weaponId,
            String displayName,
            WeaponCategory category,
            float baseDamage,
            float bodyDamageMultiplier,
            float bloodLossMultiplier,
            float shockMultiplier,
            boolean causesHeavyBleeding,
            boolean isHighPenetration
    ) {
        WEAPONS.put(
                normalize(weaponId),
                new WeaponDamageProfile(
                        normalize(weaponId),
                        displayName,
                        category,
                        baseDamage,
                        bodyDamageMultiplier,
                        bloodLossMultiplier,
                        shockMultiplier,
                        causesHeavyBleeding,
                        isHighPenetration
                )
        );
    }

    private static String normalize(String id) {
        return id.toLowerCase(Locale.ROOT).trim();
    }
}