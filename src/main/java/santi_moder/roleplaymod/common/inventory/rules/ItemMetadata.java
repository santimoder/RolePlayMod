package santi_moder.roleplaymod.common.inventory.rules;

public class ItemMetadata {

    private final ItemCategory category;
    private final ItemSize size;
    private final float weight;

    public ItemMetadata(ItemCategory category, ItemSize size, float weight) {
        this.category = category;
        this.size = size;
        this.weight = weight;
    }

    public ItemCategory getCategory() {
        return category;
    }

    public ItemSize getSize() {
        return size;
    }

    public float getWeight() {
        return weight;
    }

    public static ItemMetadata of(ItemCategory category, ItemSize size, float weight) {
        return new ItemMetadata(category, size, weight);
    }

    public static ItemMetadata defaultGeneric() {
        return new ItemMetadata(ItemCategory.GENERIC, ItemSize.SMALL, 1.0f);
    }
}