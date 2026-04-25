package santi_moder.roleplaymod.common.inventory.rules;

import net.minecraft.world.item.ItemStack;
import santi_moder.roleplaymod.item.RadioItem;

public class ItemMetadataResolver {

    public static ItemMetadata resolve(ItemStack stack) {
        if (stack.isEmpty()) {
            return ItemMetadata.of(ItemCategory.NONE, ItemSize.SMALL, 0.0f);
        }

        if (isPistol(stack)) {
            return ItemMetadata.of(ItemCategory.PISTOL, ItemSize.SMALL, 2.0f);
        }

        if (isRadio(stack)) {
            return ItemMetadata.of(ItemCategory.RADIO, ItemSize.SMALL, 1.0f);
        }

        if (isTaser(stack)) {
            return ItemMetadata.of(ItemCategory.TASER, ItemSize.SMALL, 1.2f);
        }

        if (isBaton(stack)) {
            return ItemMetadata.of(ItemCategory.BATON, ItemSize.SMALL, 1.5f);
        }

        if (isHandcuffs(stack)) {
            return ItemMetadata.of(ItemCategory.HANDCUFFS, ItemSize.SMALL, 0.7f);
        }

        if (isMagazine(stack)) {
            return ItemMetadata.of(ItemCategory.AMMO, ItemSize.SMALL, 0.5f);
        }

        if (isPhone(stack)) {
            return ItemMetadata.of(ItemCategory.PHONE, ItemSize.SMALL, 0.4f);
        }

        return ItemMetadata.defaultGeneric();
    }

    public static ItemCategory getCategory(ItemStack stack) {
        return resolve(stack).getCategory();
    }

    public static ItemSize getSize(ItemStack stack) {
        return resolve(stack).getSize();
    }

    public static float getWeight(ItemStack stack) {
        return resolve(stack).getWeight();
    }

    private static boolean isPistol(ItemStack stack) {
        if (!stack.hasTag()) return false;

        var tag = stack.getTag();
        if (tag == null || !tag.contains("GunId")) return false;

        String gunId = tag.getString("GunId");

        return gunId.equals("tacz:deagle")
                || gunId.equals("tacz:deagle_golden")
                || gunId.equals("tacz:glock_17")
                || gunId.equals("tacz:cz75")
                || gunId.equals("tacz:p320")
                || gunId.equals("tacz:b93r")
                || gunId.equals("tacz:m1911");
    }

    private static boolean isRadio(ItemStack stack) {
        return stack.getItem() instanceof RadioItem;
    }

    private static boolean isTaser(ItemStack stack) {
        return false;
    }

    private static boolean isBaton(ItemStack stack) {
        return false;
    }

    private static boolean isHandcuffs(ItemStack stack) {
        return false;
    }

    private static boolean isMagazine(ItemStack stack) {
        return isFromNamespaceAndContains(stack, "tacz", "ammo");
    }

    private static boolean isPhone(ItemStack stack) {
        return false;
    }

    private static boolean isItem(ItemStack stack, String namespace, String path) {
        var key = stack.getItem().builtInRegistryHolder().key();
        return key.location().getNamespace().equals(namespace)
                && key.location().getPath().equals(path);
    }

    private static boolean isFromNamespaceAndContains(ItemStack stack, String namespace, String text) {
        var key = stack.getItem().builtInRegistryHolder().key();
        return key.location().getNamespace().equals(namespace)
                && key.location().getPath().contains(text);
    }
}