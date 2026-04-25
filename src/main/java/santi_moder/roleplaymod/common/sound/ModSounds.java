package santi_moder.roleplaymod.common.sound;

import net.minecraft.resources.ResourceLocation;
import net.minecraft.sounds.SoundEvent;
import santi_moder.roleplaymod.RolePlayMod;

public class ModSounds {

    public static final SoundEvent RADIO_PTT_START =
            SoundEvent.createVariableRangeEvent(new ResourceLocation(RolePlayMod.MOD_ID, "radio_ptt_start"));

    public static final SoundEvent RADIO_PTT_END =
            SoundEvent.createVariableRangeEvent(new ResourceLocation(RolePlayMod.MOD_ID, "radio_ptt_end"));

    private ModSounds() {
    }
}