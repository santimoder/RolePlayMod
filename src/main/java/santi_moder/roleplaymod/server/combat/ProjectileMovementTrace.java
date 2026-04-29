package santi_moder.roleplaymod.server.combat;

import net.minecraft.world.phys.Vec3;

public record ProjectileMovementTrace(
        Vec3 previousPosition,
        Vec3 currentPosition
) {
}