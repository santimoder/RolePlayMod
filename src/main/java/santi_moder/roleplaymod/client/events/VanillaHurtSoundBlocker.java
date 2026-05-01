package santi_moder.roleplaymod.client.events;

import net.minecraft.client.resources.sounds.SoundInstance;
import net.minecraft.resources.ResourceLocation;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.sound.PlaySoundEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import santi_moder.roleplaymod.RolePlayMod;

@Mod.EventBusSubscriber(modid = RolePlayMod.MOD_ID, value = Dist.CLIENT)
public final class VanillaHurtSoundBlocker {

    private VanillaHurtSoundBlocker() {
    }

    @SubscribeEvent
    public static void onPlaySound(PlaySoundEvent event) {
        SoundInstance sound = event.getSound();
        if (sound == null) return;

        ResourceLocation id = sound.getLocation();
        if (id == null) return;

        String path = id.getPath();

        if (
                path.equals("entity.player.hurt") ||
                        path.equals("entity.player.hurt_drown") ||
                        path.equals("entity.player.hurt_on_fire") ||
                        path.equals("entity.generic.hurt")
        ) {
            event.setSound(null);
        }
    }
}