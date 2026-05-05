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
        // ================= PRE-ALPHA ENABLED =================

        register("tacz:glock_17", "Glock 17", WeaponCategory.PISTOL, AmmoCaliber.NINE_MM,
                1.00F, 1.00F, 1.00F, 1.00F, 25F, 55F);

        register("tacz:p320", "P320", WeaponCategory.PISTOL, AmmoCaliber.FORTY_FIVE_ACP,
                1.00F, 1.00F, 1.00F, 1.00F, 25F, 55F);

        register("tacz:deagle", "Desert Eagle", WeaponCategory.PISTOL, AmmoCaliber.FIFTY_AE,
                1.00F, 1.05F, 1.00F, 1.05F, 35F, 75F);

        register("tacz:m870", "M870", WeaponCategory.SHOTGUN, AmmoCaliber.TWELVE_GAUGE,
                1.00F, 1.05F, 1.00F, 1.00F, 8F, 24F);

        register("tacz:m1014", "M1014", WeaponCategory.SHOTGUN, AmmoCaliber.TWELVE_GAUGE,
                0.92F, 1.00F, 0.95F, 0.95F, 7F, 22F);

        register("tacz:hk_mp5a5", "HK MP5A5", WeaponCategory.SMG, AmmoCaliber.NINE_MM,
                0.88F, 0.90F, 0.90F, 0.90F, 28F, 65F);

        register("tacz:uzi", "UZI", WeaponCategory.SMG, AmmoCaliber.NINE_MM,
                0.82F, 0.85F, 0.88F, 0.85F, 22F, 55F);

        register("tacz:m4a1", "M4A1", WeaponCategory.RIFLE, AmmoCaliber.FIVE_FIVE_SIX,
                1.00F, 1.00F, 1.00F, 1.00F, 85F, 170F);

        register("tacz:ak47", "AK-47", WeaponCategory.RIFLE, AmmoCaliber.SEVEN_SIX_TWO,
                1.00F, 1.05F, 1.00F, 1.05F, 90F, 180F);

        register("tacz:minigun", "Minigun", WeaponCategory.RIFLE, AmmoCaliber.THREE_ZERO_EIGHT,
                0.85F, 0.90F, 0.90F, 0.95F, 100F, 200F);

        register("tacz:m700", "M700", WeaponCategory.SNIPER, AmmoCaliber.THIRTY_ZERO_SIX,
                1.00F, 1.10F, 1.00F, 1.05F, 180F, 350F);

        register("tacz:m95", "M95", WeaponCategory.SNIPER, AmmoCaliber.FIFTY_BMG,
                1.00F, 1.15F, 1.00F, 1.10F, 220F, 450F);

        register("tacz:rpg7", "RPG-7", WeaponCategory.EXPLOSIVE, AmmoCaliber.RPG_ROCKET,
                1.00F, 1.00F, 1.00F, 1.00F, 80F, 180F);

        // ================= FUTURE WEAPONS DISABLED =================
        // register("tacz:deagle_golden", "Golden Desert Eagle", WeaponCategory.PISTOL, 9.5F, 1.30F, 1.25F, 1.30F, 35F, 75F, true, true);
        // register("tacz:m1911", "M1911", WeaponCategory.PISTOL, 6.5F, 1.05F, 1.05F, 1.05F, 25F, 55F, false, false);
        // register("tacz:cz75", "CZ75", WeaponCategory.PISTOL, 6.2F, 1.00F, 1.00F, 1.00F, 25F, 55F, false, false);
        // register("tacz:b93r", "B93R", WeaponCategory.PISTOL, 5.8F, 0.95F, 0.95F, 0.95F, 25F, 55F, false, false);

        // register("tacz:db_short", "Double Barrel Short", WeaponCategory.SHOTGUN, 12.5F, 1.65F, 1.55F, 1.50F, 14F, 35F, true, false);
        // register("tacz:db_long", "Double Barrel Long", WeaponCategory.SHOTGUN, 13.5F, 1.75F, 1.60F, 1.55F, 20F, 50F, true, false);
        // register("tacz:aa12", "AA-12", WeaponCategory.SHOTGUN, 10.5F, 1.40F, 1.35F, 1.30F, 14F, 35F, true, false);
        // register("tacz:spas_12", "SPAS-12", WeaponCategory.SHOTGUN, 13.0F, 1.70F, 1.55F, 1.50F, 18F, 45F, true, false);

        // register("tacz:ump45", "UMP45", WeaponCategory.SMG, 6.5F, 1.00F, 1.00F, 1.00F, 28F, 65F, false, false);
        // register("tacz:vector45", "Vector .45", WeaponCategory.SMG, 5.8F, 0.90F, 0.90F, 0.90F, 24F, 55F, false, false);
        // register("tacz:p90", "P90", WeaponCategory.SMG, 5.0F, 0.85F, 0.85F, 0.85F, 30F, 70F, false, false);

        // register("tacz:sks_tactical", "SKS Tactical", WeaponCategory.RIFLE, 9.5F, 1.25F, 1.20F, 1.20F, 100F, 190F, true, true);
        // register("tacz:type_81", "Type 81", WeaponCategory.RIFLE, 9.8F, 1.30F, 1.22F, 1.25F, 90F, 180F, true, true);
        // register("tacz:qbz_95", "QBZ-95", WeaponCategory.RIFLE, 9.0F, 1.25F, 1.18F, 1.20F, 85F, 170F, true, true);
        // register("tacz:hk416d", "HK416D", WeaponCategory.RIFLE, 9.0F, 1.25F, 1.20F, 1.20F, 85F, 170F, true, true);
        // register("tacz:m16a1", "M16A1", WeaponCategory.RIFLE, 9.2F, 1.25F, 1.20F, 1.20F, 90F, 180F, true, true);
        // register("tacz:hk_g3", "HK G3", WeaponCategory.RIFLE, 11.5F, 1.45F, 1.35F, 1.40F, 120F, 220F, true, true);
        // register("tacz:m16a4", "M16A4", WeaponCategory.RIFLE, 9.2F, 1.25F, 1.20F, 1.20F, 90F, 180F, true, true);
        // register("tacz:spr15hb", "SPR-15 HB", WeaponCategory.RIFLE, 10.0F, 1.35F, 1.25F, 1.30F, 95F, 190F, true, true);
        // register("tacz:mk14", "MK14", WeaponCategory.RIFLE, 11.0F, 1.40F, 1.30F, 1.35F, 130F, 240F, true, true);
        // register("tacz:scar_l", "SCAR-L", WeaponCategory.RIFLE, 9.0F, 1.25F, 1.20F, 1.20F, 90F, 180F, true, true);
        // register("tacz:scar_h", "SCAR-H", WeaponCategory.RIFLE, 11.5F, 1.45F, 1.35F, 1.40F, 120F, 220F, true, true);
        // register("tacz:aug", "AUG", WeaponCategory.RIFLE, 9.0F, 1.25F, 1.18F, 1.20F, 90F, 180F, true, true);
        // register("tacz:g36k", "G36K", WeaponCategory.RIFLE, 8.8F, 1.20F, 1.15F, 1.15F, 85F, 170F, true, true);
        // register("tacz:fn_fal", "FN FAL", WeaponCategory.RIFLE, 11.5F, 1.45F, 1.35F, 1.40F, 120F, 220F, true, true);
        // register("tacz:qbz_191", "QBZ-191", WeaponCategory.RIFLE, 9.0F, 1.25F, 1.18F, 1.20F, 90F, 180F, true, true);

        // register("tacz:m249", "M249", WeaponCategory.RIFLE, 8.8F, 1.20F, 1.15F, 1.15F, 100F, 200F, true, true);
        // register("tacz:rpk", "RPK", WeaponCategory.RIFLE, 9.8F, 1.30F, 1.22F, 1.25F, 100F, 200F, true, true);
        // register("tacz:fn_evolys", "FN Evolys", WeaponCategory.RIFLE, 8.8F, 1.20F, 1.15F, 1.15F, 100F, 200F, true, true);

        // register("tacz:m107", "M107", WeaponCategory.SNIPER, 23.0F, 2.55F, 2.00F, 2.25F, 220F, 450F, true, true);
        // register("tacz:springfield1873", "Springfield 1873", WeaponCategory.SNIPER, 15.0F, 1.80F, 1.55F, 1.70F, 160F, 320F, true, true);
        // register("tacz:ai_awp", "AI AWP", WeaponCategory.SNIPER, 18.0F, 2.20F, 1.80F, 2.00F, 180F, 350F, true, true);

        // register("tacz:m320", "M320", WeaponCategory.EXPLOSIVE, 16.0F, 1.70F, 1.45F, 1.90F, 80F, 180F, true, false);
    }

    private TaCZWeaponResolver() {
    }

    public static WeaponDamageProfile resolveFromPlayer(ServerPlayer player) {
        if (player == null) {
            return WeaponDamageProfile.disabled("unknown");
        }

        ItemStack stack = player.getMainHandItem();

        if (stack.isEmpty()) {
            return WeaponDamageProfile.disabled("empty_hand");
        }

        String gunId = readGunId(stack);

        if (gunId.isBlank()) {
            gunId = readRegistryId(stack);
        }

        if (DEBUG_GUN_ID) {
            System.out.println("[RolePlayMod][TaCZ] gunId=" + gunId + " stack=" + stack);
            System.out.println("[RolePlayMod][TaCZ] tag=" + stack.getTag());
        }

        return resolveFromGunId(gunId);
    }

    public static WeaponDamageProfile resolveFromGunId(String gunId) {
        if (gunId == null || gunId.isBlank()) {
            return WeaponDamageProfile.disabled("unknown");
        }

        String normalized = normalize(gunId);
        WeaponDamageProfile profile = WEAPONS.get(normalized);

        if (profile != null) {
            return profile;
        }

        if (DISCOVERED_GUNS.add("disabled:" + normalized)) {
            System.out.println("[RolePlayMod][TaCZ] Arma no permitida en pre-alpha: " + normalized);
        }

        return WeaponDamageProfile.disabled(normalized);
    }

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
            if (!tag.contains(key)) continue;

            String value = tag.getString(key);

            if (value != null && !value.isBlank()) {
                return value;
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

    private static void register(
            String weaponId,
            String displayName,
            WeaponCategory category,
            AmmoCaliber caliber,
            float weaponDamageMultiplier,
            float bodyDamageMultiplier,
            float bloodLossMultiplier,
            float shockMultiplier,
            float effectiveRange,
            float maxRange
    ) {
        WEAPONS.put(
                normalize(weaponId),
                new WeaponDamageProfile(
                        normalize(weaponId),
                        displayName,
                        category,
                        caliber,
                        weaponDamageMultiplier,
                        bodyDamageMultiplier,
                        bloodLossMultiplier,
                        shockMultiplier,
                        effectiveRange,
                        maxRange,
                        true
                )
        );
    }

    private static String normalize(String id) {
        return id == null ? "" : id.toLowerCase(Locale.ROOT).trim();
    }
}