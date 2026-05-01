package santi_moder.roleplaymod.client.events;

import com.mojang.blaze3d.systems.RenderSystem;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.renderer.GameRenderer;
import net.minecraft.resources.ResourceLocation;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.RenderGuiOverlayEvent;
import net.minecraftforge.client.event.ViewportEvent;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import santi_moder.roleplaymod.RolePlayMod;
import santi_moder.roleplaymod.client.data.ClientDamageFeedback;
import santi_moder.roleplaymod.client.effects.DamageShaderHandler;
import santi_moder.roleplaymod.server.data.PlayerDataProvider;

@Mod.EventBusSubscriber(modid = RolePlayMod.MOD_ID, value = Dist.CLIENT)
public final class MedicalVisualOverlayHandler {

    private MedicalVisualOverlayHandler() {
    }

    private static final ResourceLocation BLOOD_VIGNETTE =
            new ResourceLocation(RolePlayMod.MOD_ID, "textures/gui/blood_vignette.png");

    @SubscribeEvent
    public static void onClientTick(TickEvent.ClientTickEvent event) {
        if (event.phase != TickEvent.Phase.END) return;

        Minecraft mc = Minecraft.getInstance();
        if (mc.player == null || mc.level == null) return;

        DamageShaderHandler.tick();
        ClientDamageFeedback.tick();
    }

    @SubscribeEvent
    public static void onRenderOverlay(RenderGuiOverlayEvent.Post event) {
        Minecraft mc = Minecraft.getInstance();
        if (mc.player == null || mc.options.hideGui) return;

        GuiGraphics g = event.getGuiGraphics();

        renderBleedingBorder(g);
        renderUnconsciousBlur(g);
    }

    private static void renderUnconsciousBlur(GuiGraphics g) {
        Minecraft mc = Minecraft.getInstance();
        if (mc.player == null) return;

        mc.player.getCapability(PlayerDataProvider.PLAYER_DATA).ifPresent(data -> {
            if (!data.isInconsciente() && !data.isVisionBlurred()) return;

            int w = mc.getWindow().getGuiScaledWidth();
            int h = mc.getWindow().getGuiScaledHeight();

            int alpha = data.isInconsciente() ? 105 : 45;
            int color = (alpha << 24) | 0x000000;

            g.fill(0, 0, w, h, color);
        });
    }

    @SubscribeEvent
    public static void onCameraSetup(ViewportEvent.ComputeCameraAngles event) {
        if (!ClientDamageFeedback.hasDamageEffect()) return;

        int ticks = ClientDamageFeedback.getDamageTicks();
        float intensity = ClientDamageFeedback.getDamageIntensity();

        float progress = ticks / 40.0F;
        progress = Math.max(0.0F, Math.min(1.0F, progress));

        double time = ticks * 0.35D;

        double yawShake = Math.sin(time * 4.0D) * 0.65D * progress * intensity;
        double pitchShake = Math.cos(time * 3.2D) * 0.45D * progress * intensity;
        double rollShake = Math.sin(time * 5.0D) * 1.10D * progress * intensity;

        event.setYaw((float) (event.getYaw() + yawShake));
        event.setPitch((float) (event.getPitch() + pitchShake));
        event.setRoll((float) (event.getRoll() + rollShake));
    }

    @SubscribeEvent
    public static void onFov(ViewportEvent.ComputeFov event) {
        if (!ClientDamageFeedback.hasDamageEffect()) return;

        int ticks = ClientDamageFeedback.getDamageTicks();
        float intensity = ClientDamageFeedback.getDamageIntensity();

        float progress = ticks / 40.0F;
        progress = Math.max(0.0F, Math.min(1.0F, progress));

        double wave = Math.sin(ticks * 0.45D) * 2.0D * progress * intensity;

        event.setFOV(event.getFOV() + wave);
    }

    private static void renderBleedingBorder(GuiGraphics g) {
        if (!ClientDamageFeedback.hasBleedingEffect()) return;

        int ticks = ClientDamageFeedback.getBleedingTicks();
        float intensity = ClientDamageFeedback.getBleedingIntensity();

        float progress = ticks / 60.0F;
        double pulse = (Math.sin(ticks * 0.18D) + 1.0D) * 0.5D;

        float alpha = (float) ((0.18F + 0.32F * pulse) * intensity * progress);

        int w = Minecraft.getInstance().getWindow().getGuiScaledWidth();
        int h = Minecraft.getInstance().getWindow().getGuiScaledHeight();

        RenderSystem.enableBlend();
        RenderSystem.defaultBlendFunc();
        RenderSystem.setShader(GameRenderer::getPositionTexShader);
        RenderSystem.setShaderTexture(0, BLOOD_VIGNETTE);
        RenderSystem.setShaderColor(1.0F, 1.0F, 1.0F, alpha);

        g.blit(BLOOD_VIGNETTE, 0, 0, 0, 0, w, h, w, h);

        RenderSystem.setShaderColor(1.0F, 1.0F, 1.0F, 1.0F);
        RenderSystem.disableBlend();
    }
}