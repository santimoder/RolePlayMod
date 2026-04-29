package santi_moder.roleplaymod.server.events;

import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.projectile.Projectile;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import santi_moder.roleplaymod.server.combat.ProjectilePositionTracker;

@Mod.EventBusSubscriber(modid = "roleplaymod")
public final class ProjectileTrackEventHandler {

    private ProjectileTrackEventHandler() {
    }

    @SubscribeEvent
    public static void onLevelTickStart(TickEvent.LevelTickEvent event) {
        if (event.phase != TickEvent.Phase.START) return;
        if (!(event.level instanceof ServerLevel serverLevel)) return;

        for (Entity entity : serverLevel.getAllEntities()) {
            if (shouldTrack(entity)) {
                ProjectilePositionTracker.remember(entity);
            }
        }
    }

    @SubscribeEvent
    public static void onLevelTickEnd(TickEvent.LevelTickEvent event) {
        if (event.phase != TickEvent.Phase.END) return;
        if (!(event.level instanceof ServerLevel serverLevel)) return;

        ProjectilePositionTracker.cleanup(serverLevel.getGameTime());
    }

    private static boolean shouldTrack(Entity entity) {
        if (entity instanceof Projectile) return true;

        String className = entity.getClass().getName().toLowerCase();

        return className.contains("bullet")
                || className.contains("projectile")
                || className.contains("ammo")
                || className.contains("shot")
                || className.contains("shell")
                || className.contains("pellet");
    }
}