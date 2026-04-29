package santi_moder.roleplaymod.mixin.voicechat;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import santi_moder.roleplaymod.client.radio.voice.RadioVoiceChatPttState;

@Mixin(targets = "de.maxhenkel.voicechat.voice.client.PTTKeyHandler", remap = false)
public abstract class PTTKeyHandlerMixin {

    @Inject(method = "isPTTDown", at = @At("HEAD"), cancellable = true)
    private void roleplaymod$radioPttIsVoiceChatPtt(CallbackInfoReturnable<Boolean> cir) {
        if (RadioVoiceChatPttState.isRadioPttDown()) {
            cir.setReturnValue(true);
        }
    }
}