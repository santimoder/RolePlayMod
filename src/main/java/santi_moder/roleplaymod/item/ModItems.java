package santi_moder.roleplaymod.item;

import net.minecraft.world.item.Item;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;
import santi_moder.roleplaymod.RolePlayMod;

public class ModItems {

    public static final DeferredRegister<Item> ITEMS =
            DeferredRegister.create(ForgeRegistries.ITEMS, RolePlayMod.MOD_ID);

    public static final RegistryObject<Item> JACKET =
            ITEMS.register("jacket",
                    () -> new ItemJacket(new Item.Properties()));

    public static final RegistryObject<Item> PANTS =
            ITEMS.register("pants",
                    () -> new ItemPants(new Item.Properties()));

    public static final RegistryObject<Item> VEST =
            ITEMS.register("vest",
                    () -> new ItemVest(new Item.Properties()));

    public static final RegistryObject<Item> BELT =
            ITEMS.register("belt",
                    () -> new ItemBelt(new Item.Properties()));

    public static final RegistryObject<Item> BACKPACK_SMALL =
            ITEMS.register("backpack_small",
                    () -> new ItemBackpackSmall(new Item.Properties().stacksTo(1)));

    public static final RegistryObject<Item> BACKPACK_MEDIUM =
            ITEMS.register("backpack_medium",
                    () -> new ItemBackpackMedium(new Item.Properties().stacksTo(1)));

    public static final RegistryObject<Item> BACKPACK_LARGE =
            ITEMS.register("backpack_large",
                    () -> new ItemBackpackLarge(new Item.Properties().stacksTo(1)));

    public static final RegistryObject<Item> BACKPACK_HUGE =
            ITEMS.register("backpack_huge",
                    () -> new ItemBackpackHuge(new Item.Properties().stacksTo(1)));

    public static final RegistryObject<Item> RADIO =
            ITEMS.register("radio",
                    () -> new RadioItem(new Item.Properties()));

    public static final RegistryObject<Item> PHONE =
            ITEMS.register("phone",
                    () -> new PhoneItem(new Item.Properties().stacksTo(1)));

    public static void register() {
        ITEMS.register(FMLJavaModLoadingContext.get().getModEventBus());
    }
}