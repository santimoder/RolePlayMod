package santi_moder.roleplaymod.client.effects;

import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.PostChain;
import net.minecraft.resources.ResourceLocation;

public class DamageShaderHandler {

    private static PostChain shader;
    private static int ticks = 0;
    private static float intensity = 0f;

    public static void trigger(float strength) {
        ticks = 40;
        intensity = Math.max(intensity, strength);

        if (shader == null) {
            try {
                shader = new PostChain(
                        Minecraft.getInstance().getTextureManager(),
                        Minecraft.getInstance().getResourceManager(),
                        Minecraft.getInstance().getMainRenderTarget(),
                        new ResourceLocation("roleplaymod", "shaders/post/damage_distortion.json")
                );

                shader.resize(
                        Minecraft.getInstance().getWindow().getWidth(),
                        Minecraft.getInstance().getWindow().getHeight()
                );

            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    public static void tick() {
        if (shader == null) return;

        if (ticks > 0) {
            ticks--;
            intensity *= 0.95f;

            shader.process(1.0f);

        } else {
            shader.close();
            shader = null;
            intensity = 0f;
        }
    }
}