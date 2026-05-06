package santi_moder.roleplaymod.client.phone.app.whatsapp;

import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.resources.ResourceLocation;
import santi_moder.roleplaymod.RolePlayMod;

public final class WhatsappTextureResolver {

    private static final int PROFILE_TEXTURE_WIDTH = 200;
    private static final int PROFILE_TEXTURE_HEIGHT = 170;
    private static final int PROFILE_CROP_SIZE = 170;
    private static final int PROFILE_CROP_X = 15;
    private static final int PROFILE_CROP_Y = 0;

    private WhatsappTextureResolver() {
    }

    public static ResourceLocation getProfileTexture(String photoId) {
        String safePhotoId = normalizeProfilePhotoId(photoId);

        return new ResourceLocation(
                RolePlayMod.MOD_ID,
                "textures/gui/phone/" + safePhotoId + ".png"
        );
    }

    public static void drawProfilePhoto(GuiGraphics guiGraphics, String photoId, int x, int y, int size) {
        ResourceLocation texture = getProfileTexture(photoId);

        guiGraphics.blit(
                texture,
                x,
                y,
                size,
                size,
                PROFILE_CROP_X,
                PROFILE_CROP_Y,
                PROFILE_CROP_SIZE,
                PROFILE_CROP_SIZE,
                PROFILE_TEXTURE_WIDTH,
                PROFILE_TEXTURE_HEIGHT
        );
    }

    private static String normalizeProfilePhotoId(String photoId) {
        if (photoId == null || photoId.isBlank() || "default".equals(photoId)) {
            return "default_1";
        }

        return photoId;
    }
}