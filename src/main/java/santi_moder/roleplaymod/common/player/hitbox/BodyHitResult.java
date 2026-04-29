package santi_moder.roleplaymod.common.player.hitbox;

import net.minecraft.world.phys.Vec3;
import santi_moder.roleplaymod.common.player.BodyPart;

public record BodyHitResult(
        BodyPart bodyPart,
        Vec3 hitPosition,
        double distance
) {
}