package santi_moder.roleplaymod.client.events;

import com.mojang.blaze3d.systems.RenderSystem;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.renderer.GameRenderer;
import net.minecraft.resources.ResourceLocation;
import net.minecraftforge.client.event.RenderGuiOverlayEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import santi_moder.roleplaymod.client.data.ClientPlayerData;

@Mod.EventBusSubscriber(modid = "roleplaymod", bus = Mod.EventBusSubscriber.Bus.FORGE)
public class HudHandler {

    private static final Minecraft mc = Minecraft.getInstance();
    private static final int SIZE = 32; // Tamaño de cada ícono
    private static final int GAP  = 5;  // Espacio entre íconos

    // Arrays de texturas, 11 texturas por stat (0% a 100%)
    private static final ResourceLocation[] BLOOD_TEXTURES   = new ResourceLocation[11];
    private static final ResourceLocation[] STAMINA_TEXTURES = new ResourceLocation[11];
    private static final ResourceLocation[] HUNGER_TEXTURES  = new ResourceLocation[11];
    private static final ResourceLocation[] THIRST_TEXTURES  = new ResourceLocation[11];

    static {
        for (int i = 0; i <= 10; i++) {
            BLOOD_TEXTURES[i]   = new ResourceLocation("roleplaymod", "textures/gui/blood_" + i*10 + ".png");
            STAMINA_TEXTURES[i] = new ResourceLocation("roleplaymod", "textures/gui/stamina_" + i*10 + ".png");
            HUNGER_TEXTURES[i]  = new ResourceLocation("roleplaymod", "textures/gui/hunger_" + i*10 + ".png");
            THIRST_TEXTURES[i]  = new ResourceLocation("roleplaymod", "textures/gui/thirst_" + i*10 + ".png");
        }
    }

    // ===================== BLOQUEAR HUD VANILLA =====================
    @SubscribeEvent
    public static void onRenderPre(RenderGuiOverlayEvent.Pre event) {
        event.setCanceled(true); // Cancelamos el HUD vanilla
        renderHud(event.getGuiGraphics());
    }

    // ===================== RENDER HUD PERSONALIZADO =====================
    @SubscribeEvent
    public static void onRenderPost(RenderGuiOverlayEvent.Post event) {
        renderHud(event.getGuiGraphics());
    }

    private static void renderHud(GuiGraphics graphics) {
        if (mc.player == null) return;

        int xBase = 10; // margen desde la esquina izquierda
        int y     = mc.getWindow().getGuiScaledHeight() - SIZE - 10; // margen desde la esquina inferior
        int x = xBase;

        // === Sangre ===
        int bloodIndex = Math.min(10, Math.max(0, Math.round(ClientPlayerData.getSangre() / 10f)));
        RenderSystem.setShader(GameRenderer::getPositionTexShader);
        RenderSystem.setShaderTexture(0, BLOOD_TEXTURES[bloodIndex]);
        graphics.blit(BLOOD_TEXTURES[bloodIndex], x, y, 0, 0, SIZE, SIZE, SIZE, SIZE);
        x += SIZE + GAP;

        // === Stamina ===
        int staminaIndex = Math.min(10, Math.max(0, Math.round(ClientPlayerData.getStamina() / 10f)));
        RenderSystem.setShaderTexture(0, STAMINA_TEXTURES[staminaIndex]);
        graphics.blit(STAMINA_TEXTURES[staminaIndex], x, y, 0, 0, SIZE, SIZE, SIZE, SIZE);
        x += SIZE + GAP;

        // === Hambre ===
        int hungerIndex = Math.min(10, Math.max(0, Math.round(mc.player.getFoodData().getFoodLevel() / 2f))); // 0-20 → 0-10
        RenderSystem.setShaderTexture(0, HUNGER_TEXTURES[hungerIndex]);
        graphics.blit(HUNGER_TEXTURES[hungerIndex], x, y, 0, 0, SIZE, SIZE, SIZE, SIZE);
        x += SIZE + GAP;

        // === Sed ===
        int thirstIndex = Math.min(10, Math.max(0, Math.round(ClientPlayerData.getSed() / 10f)));
        RenderSystem.setShaderTexture(0, THIRST_TEXTURES[thirstIndex]);
        graphics.blit(THIRST_TEXTURES[thirstIndex], x, y, 0, 0, SIZE, SIZE, SIZE, SIZE);
    }
}
