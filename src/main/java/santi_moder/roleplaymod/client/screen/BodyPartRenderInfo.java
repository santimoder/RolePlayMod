package santi_moder.roleplaymod.client.screen;

import net.minecraft.resources.ResourceLocation;
import santi_moder.roleplaymod.common.player.BodyPart;

public record BodyPartRenderInfo(
        BodyPart part,
        ResourceLocation texture
) {}
