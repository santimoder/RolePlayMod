package santi_moder.roleplaymod.common.player.hitbox;

import net.minecraft.world.entity.player.Player;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import santi_moder.roleplaymod.common.player.BodyPart;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Optional;

public final class BodyHitboxResolver {

    private BodyHitboxResolver() {
    }

    public static Optional<BodyHitResult> resolve(Player target, Vec3 rayStart, Vec3 rayEnd) {
        List<BodyHitbox> hitboxes = createHitboxes(target);

        return hitboxes.stream()
                .map(hitbox -> hitbox.box().clip(rayStart, rayEnd)
                        .map(hitPos -> new BodyHitResult(
                                hitbox.part(),
                                hitPos,
                                rayStart.distanceToSqr(hitPos)
                        )))
                .flatMap(Optional::stream)
                .min(Comparator.comparingDouble(BodyHitResult::distance));
    }

    public static List<BodyHitbox> createHitboxes(Player player) {
        AABB base = player.getBoundingBox();

        double minX = base.minX;
        double maxX = base.maxX;
        double minY = base.minY;
        double maxY = base.maxY;
        double minZ = base.minZ;
        double maxZ = base.maxZ;

        double width = maxX - minX;
        double height = maxY - minY;
        double depth = maxZ - minZ;

        double centerX = (minX + maxX) * 0.5;
        double centerZ = (minZ + maxZ) * 0.5;

        List<BodyHitbox> boxes = new ArrayList<>();

        boxes.add(new BodyHitbox(
                BodyPart.HEAD,
                new AABB(
                        centerX - width * 0.28,
                        minY + height * 0.78,
                        centerZ - depth * 0.28,
                        centerX + width * 0.28,
                        maxY,
                        centerZ + depth * 0.28
                )
        ));

        boxes.add(new BodyHitbox(
                BodyPart.TORSO,
                new AABB(
                        centerX - width * 0.32,
                        minY + height * 0.35,
                        centerZ - depth * 0.22,
                        centerX + width * 0.32,
                        minY + height * 0.78,
                        centerZ + depth * 0.22
                )
        ));

        boxes.add(new BodyHitbox(
                BodyPart.LEFT_ARM,
                new AABB(
                        minX,
                        minY + height * 0.35,
                        centerZ - depth * 0.20,
                        centerX - width * 0.32,
                        minY + height * 0.76,
                        centerZ + depth * 0.20
                )
        ));

        boxes.add(new BodyHitbox(
                BodyPart.RIGHT_ARM,
                new AABB(
                        centerX + width * 0.32,
                        minY + height * 0.35,
                        centerZ - depth * 0.20,
                        maxX,
                        minY + height * 0.76,
                        centerZ + depth * 0.20
                )
        ));

        boxes.add(new BodyHitbox(
                BodyPart.LEFT_LEG,
                new AABB(
                        minX,
                        minY,
                        centerZ - depth * 0.18,
                        centerX,
                        minY + height * 0.35,
                        centerZ + depth * 0.18
                )
        ));

        boxes.add(new BodyHitbox(
                BodyPart.RIGHT_LEG,
                new AABB(
                        centerX,
                        minY,
                        centerZ - depth * 0.18,
                        maxX,
                        minY + height * 0.35,
                        centerZ + depth * 0.18
                )
        ));

        return boxes;
    }
}