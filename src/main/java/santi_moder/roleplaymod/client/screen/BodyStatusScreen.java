package santi_moder.roleplaymod.client.screen;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import santi_moder.roleplaymod.RolePlayMod;
import santi_moder.roleplaymod.client.data.ClientPlayerData;
import santi_moder.roleplaymod.common.player.BodyPart;

public class BodyStatusScreen extends Screen {

    private static final ResourceLocation BASE =
            new ResourceLocation(RolePlayMod.MOD_ID, "textures/gui/body/base.png");
    private static final ResourceLocation HEAD =
            new ResourceLocation(RolePlayMod.MOD_ID, "textures/gui/body/head.png");
    private static final ResourceLocation TORSO =
            new ResourceLocation(RolePlayMod.MOD_ID, "textures/gui/body/torso.png");
    private static final ResourceLocation LEFT_ARM =
            new ResourceLocation(RolePlayMod.MOD_ID, "textures/gui/body/left_arm.png");
    private static final ResourceLocation RIGHT_ARM =
            new ResourceLocation(RolePlayMod.MOD_ID, "textures/gui/body/right_arm.png");
    private static final ResourceLocation LEFT_LEG =
            new ResourceLocation(RolePlayMod.MOD_ID, "textures/gui/body/left_leg.png");
    private static final ResourceLocation RIGHT_LEG =
            new ResourceLocation(RolePlayMod.MOD_ID, "textures/gui/body/right_leg.png");
    private static final ResourceLocation BLEED_LIGHT =
            new ResourceLocation(RolePlayMod.MOD_ID, "textures/gui/body/bleed_light.png");
    private static final ResourceLocation BLEED_MEDIUM =
            new ResourceLocation(RolePlayMod.MOD_ID, "textures/gui/body/bleed_medium.png");
    private static final ResourceLocation BLEED_HEAVY =
            new ResourceLocation(RolePlayMod.MOD_ID, "textures/gui/body/bleed_heavy.png");


    private int x;
    private int y;

    public BodyStatusScreen() {
        super(Component.literal("Estado del Cuerpo"));
    }

    @Override
    protected void init() {
        this.x = (this.width - 256) / 2;
        this.y = (this.height - 512) / 2;
    }

    @Override
    public void render(GuiGraphics g, int mouseX, int mouseY, float partialTick) {
        this.renderBackground(g);

        var player = Minecraft.getInstance().player;
        if (player == null) return;

        float scale = 0.45f; // 🔹 AJUSTÁ ESTE VALOR (0.4 – 0.6)

        // Centramos el cuerpo escalado
        int scaledWidth = Math.round(256 * scale);
        int scaledHeight = Math.round(512 * scale);

        int drawX = (this.width - scaledWidth) / 2;
        int drawY = (this.height - scaledHeight) / 2;

        g.pose().pushPose();

        // Movemos el origen al punto donde queremos dibujar
        g.pose().translate(drawX, drawY, 0);

        // Escalamos todo
        g.pose().scale(scale, scale, 1);

        // Dibujamos como si fuera tamaño normal
        g.blit(BASE, 0, 0, 0, 0, 256, 512, 256, 512);

        renderPart(g, BodyPart.HEAD, ClientPlayerData.getBodyHp(BodyPart.HEAD), HEAD);
        renderPart(g, BodyPart.TORSO, ClientPlayerData.getBodyHp(BodyPart.TORSO), TORSO);
        renderPart(g, BodyPart.LEFT_ARM, ClientPlayerData.getBodyHp(BodyPart.LEFT_ARM), LEFT_ARM);
        renderPart(g, BodyPart.RIGHT_ARM, ClientPlayerData.getBodyHp(BodyPart.RIGHT_ARM), RIGHT_ARM);
        renderPart(g, BodyPart.LEFT_LEG, ClientPlayerData.getBodyHp(BodyPart.LEFT_LEG), LEFT_LEG);
        renderPart(g, BodyPart.RIGHT_LEG, ClientPlayerData.getBodyHp(BodyPart.RIGHT_LEG), RIGHT_LEG);

        g.pose().popPose();

        super.render(g, mouseX, mouseY, partialTick);
    }

    private void renderPart(GuiGraphics g, BodyPart part, int hp, ResourceLocation texture) {
        int color = getColorByHp(hp);

        float a = ((color >> 24) & 0xFF) / 255f;
        float r = ((color >> 16) & 0xFF) / 255f;
        float gg = ((color >> 8) & 0xFF) / 255f;
        float b = (color & 0xFF) / 255f;

        // Parte del cuerpo (con color por HP)
        g.setColor(r, gg, b, a);
        g.blit(texture, 0, 0, 0, 0, 256, 512, 256, 512);
        g.setColor(1, 1, 1, 1);

        // ===== SANGRADO =====
        ResourceLocation bleedTex = getBleedTexture(part);
        if (bleedTex != null) {
            int size = 16; // tamaño de la gota
            int[] offset = getBleedOffset(part);
            int bleedX = offset[0] - size / 2;
            int bleedY = offset[1] - size / 2;

            g.blit(bleedTex, bleedX, bleedY, 0, 0, size, size, 16, 16);
        }
    }

    private int[] getBleedOffset(BodyPart part) {
        return switch (part) {
            case HEAD      -> new int[]{128, 60};
            case TORSO     -> new int[]{128, 170};
            case LEFT_ARM  -> new int[]{70, 170};
            case RIGHT_ARM -> new int[]{186, 170};
            case LEFT_LEG  -> new int[]{115, 300};
            case RIGHT_LEG -> new int[]{141, 300};
        };
    }

    private int getColorByHp(int hp) {
        return switch (hp) {
            case 5 -> 0xFF00FF00;
            case 4, 3 -> 0xFFFFFF00;
            case 2, 1 -> 0xFFFFA500;
            default -> 0xFFFF0000;
        };
    }

    private ResourceLocation getBleedTexture(BodyPart part) {
        var bleeding = ClientPlayerData.getBleeding(part);

        return switch (bleeding) {
            case LIGHT -> BLEED_LIGHT;
            case MEDIUM -> BLEED_MEDIUM;
            case HEAVY -> BLEED_HEAVY;
            default -> null;
        };
    }

    @Override
    public boolean isPauseScreen() {
        return false;
    }
}