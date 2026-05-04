package santi_moder.roleplaymod.common.inventory.rules;

public record ItemMetadata(ItemCategory category, ItemSize size, float weight) {

    public static ItemMetadata of(ItemCategory category, ItemSize size, float weight) {
        return new ItemMetadata(category, size, weight);
    }

    public static ItemMetadata defaultGeneric() {
        return new ItemMetadata(ItemCategory.GENERIC, ItemSize.SMALL, 1.0f);
    }
}