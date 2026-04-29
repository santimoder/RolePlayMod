package santi_moder.roleplaymod.common.player.hitbox;

import net.minecraft.world.phys.AABB;
import santi_moder.roleplaymod.common.player.BodyPart;

public record BodyHitbox(
        BodyPart part,
        AABB box
) {
}