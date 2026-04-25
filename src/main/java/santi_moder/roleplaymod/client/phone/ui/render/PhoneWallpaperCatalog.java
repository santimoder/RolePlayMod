package santi_moder.roleplaymod.client.phone.ui.render;

import net.minecraft.resources.ResourceLocation;
import santi_moder.roleplaymod.RolePlayMod;
import santi_moder.roleplaymod.common.phone.PhoneData;

import java.util.List;

public final class PhoneWallpaperCatalog {

    public static final WallpaperOption DEFAULT = new WallpaperOption(
            PhoneData.WALLPAPER_DEFAULT,
            "Clásico",
            new ResourceLocation(RolePlayMod.MOD_ID, "textures/gui/phone/wallpapers/wallpaper_default.png"),
            false
    );

    public static final WallpaperOption BLUE = new WallpaperOption(
            PhoneData.WALLPAPER_BLUE,
            "Azul",
            new ResourceLocation(RolePlayMod.MOD_ID, "textures/gui/phone/wallpapers/wallpaper_blue.png"),
            false
    );

    public static final WallpaperOption DARK = new WallpaperOption(
            PhoneData.WALLPAPER_DARK,
            "Oscuro",
            new ResourceLocation(RolePlayMod.MOD_ID, "textures/gui/phone/wallpapers/wallpaper_dark.png"),
            false
    );

    public static final WallpaperOption CUSTOM_PLACEHOLDER = new WallpaperOption(
            "custom",
            "Agregar tu fondo",
            null,
            true
    );

    private static final List<WallpaperOption> SELECTABLE = List.of(DEFAULT, BLUE, DARK);
    private static final List<WallpaperOption> ALL = List.of(DEFAULT, BLUE, DARK, CUSTOM_PLACEHOLDER);

    private PhoneWallpaperCatalog() {
    }

    public static List<WallpaperOption> selectable() {
        return SELECTABLE;
    }

    public static List<WallpaperOption> all() {
        return ALL;
    }

    public static WallpaperOption byId(String id) {
        for (WallpaperOption option : ALL) {
            if (option.id().equals(id)) {
                return option;
            }
        }
        return DEFAULT;
    }

    public record WallpaperOption(
            String id,
            String displayName,
            ResourceLocation texture,
            boolean placeholder
    ) {
    }
}