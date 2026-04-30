package santi_moder.roleplaymod.client.events;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.RenderGuiOverlayEvent;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import santi_moder.roleplaymod.RolePlayMod;
import santi_moder.roleplaymod.client.data.ClientDamageFeedback;

@Mod.EventBusSubscriber(modid = RolePlayMod.MOD_ID, value = Dist.CLIENT)
public final class MedicalVisualOverlayHandler {

    private MedicalVisualOverlayHandler() {
    }

    @SubscribeEvent
    public static void onClientTick(TickEvent.ClientTickEvent event) {
        if (event.phase != TickEvent.Phase.END) return;

        Minecraft mc = Minecraft.getInstance();
        if (mc.player == null || mc.level == null) return;

        ClientDamageFeedback.tick();
    }

    @SubscribeEvent
    public static void onRenderOverlay(RenderGuiOverlayEvent.Post event) {
        Minecraft mc = Minecraft.getInstance();
        if (mc.player == null || mc.options.hideGui) return;

        GuiGraphics g = event.getGuiGraphics();

        renderDamageFlash(g);
        renderBleedingBorder(g);
    }

    private static void renderDamageFlash(GuiGraphics g) {
        if (!ClientDamageFeedback.hasDamageEffect()) return;

        int ticks = ClientDamageFeedback.getDamageTicks();
        int alpha = Math.min(90, ticks * 7);

        int color = (alpha << 24) | 0x551111;

        int w = Minecraft.getInstance().getWindow().getGuiScaledWidth();
        int h = Minecraft.getInstance().getWindow().getGuiScaledHeight();

        int shakeX = ticks % 2 == 0 ? 2 : -2;
        int shakeY = ticks % 3 == 0 ? 1 : -1;

        g.pose().pushPose();
        g.pose().translate(shakeX, shakeY, 0);
        g.fill(0, 0, w, h, color);
        g.pose().popPose();
    }

    private static void renderBleedingBorder(GuiGraphics g) {
        if (!ClientDamageFeedback.hasBleedingEffect()) return;

        int ticks = ClientDamageFeedback.getBleedingTicks();

        int pulse = (int) (Math.sin(ticks * 0.35D) * 25.0D) + 55;
        int alpha = Math.max(25, Math.min(90, pulse));
        int color = (alpha << 24) | 0x880000;

        int w = Minecraft.getInstance().getWindow().getGuiScaledWidth();
        int h = Minecraft.getInstance().getWindow().getGuiScaledHeight();

        int border = 18;

        g.fill(0, 0, w, border, color);
        g.fill(0, h - border, w, h, color);
        g.fill(0, 0, border, h, color);
        g.fill(w - border, 0, w, h, color);
    }
}