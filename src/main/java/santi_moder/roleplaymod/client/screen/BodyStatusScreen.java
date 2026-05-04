package santi_moder.roleplaymod.client.screen;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;
import santi_moder.roleplaymod.RolePlayMod;
import santi_moder.roleplaymod.client.data.ClientMedicalBackpackData;
import santi_moder.roleplaymod.client.data.ClientPatientMedicalData;
import santi_moder.roleplaymod.client.data.ClientPlayerData;
import santi_moder.roleplaymod.common.player.BleedingType;
import santi_moder.roleplaymod.common.player.BodyPart;
import santi_moder.roleplaymod.network.ModNetwork;
import santi_moder.roleplaymod.network.RequestMedicalBackpackC2SPacket;
import santi_moder.roleplaymod.network.StartTreatmentC2SPacket;

public class BodyStatusScreen extends Screen {

    private static final ResourceLocation BASE = new ResourceLocation(RolePlayMod.MOD_ID, "textures/gui/body/base.png");

    private static final ResourceLocation HEAD = new ResourceLocation(RolePlayMod.MOD_ID, "textures/gui/body/head.png");
    private static final ResourceLocation TORSO = new ResourceLocation(RolePlayMod.MOD_ID, "textures/gui/body/torso.png");
    private static final ResourceLocation LEFT_ARM = new ResourceLocation(RolePlayMod.MOD_ID, "textures/gui/body/left_arm.png");
    private static final ResourceLocation RIGHT_ARM = new ResourceLocation(RolePlayMod.MOD_ID, "textures/gui/body/right_arm.png");
    private static final ResourceLocation LEFT_LEG = new ResourceLocation(RolePlayMod.MOD_ID, "textures/gui/body/left_leg.png");
    private static final ResourceLocation RIGHT_LEG = new ResourceLocation(RolePlayMod.MOD_ID, "textures/gui/body/right_leg.png");

    private static final ResourceLocation BLEED_LIGHT = new ResourceLocation(RolePlayMod.MOD_ID, "textures/gui/body/bleed_light.png");
    private static final ResourceLocation BLEED_MEDIUM = new ResourceLocation(RolePlayMod.MOD_ID, "textures/gui/body/bleed_medium.png");
    private static final ResourceLocation BLEED_HEAVY = new ResourceLocation(RolePlayMod.MOD_ID, "textures/gui/body/bleed_heavy.png");

    private static final int TEXTURE_WIDTH = 256;
    private static final int TEXTURE_HEIGHT = 512;

    private static final int PANEL_WIDTH = 170;
    private static final int PANEL_HEIGHT = 285;

    private static final float BODY_SCALE = 0.30F;


    private static final int DIAGNOSIS_TICKS_REQUIRED = 100;
    private final boolean targetMode;
    private final java.util.UUID targetUuid;
    private final String targetName;

    private int panelX;
    private int panelY;
    private int bodyX;
    private int bodyY;

    private boolean diagnosing = false;
    private boolean diagnosed = false;
    private int diagnosisTicks = 0;

    private Button diagnoseButton;

    private int selectedBackpackSlot = -1;
    private BodyPart selectedBodyPart = null;

    private boolean treating = false;
    private int treatmentTicks = 0;


    private static final int TREATMENT_TICKS_REQUIRED = 60;

    private Button treatButton;

    private BodyStatusScreen(boolean targetMode, java.util.UUID targetUuid, String targetName) {
        super(Component.literal(targetMode ? "Paciente: " + targetName : "Estado médico"));
        this.targetMode = targetMode;
        this.targetUuid = targetUuid;
        this.targetName = targetName;
    }

    public static BodyStatusScreen self() {
        return new BodyStatusScreen(false, null, null);
    }

    public static BodyStatusScreen forTarget(java.util.UUID uuid, String name) {
        return new BodyStatusScreen(true, uuid, name);
    }

    @Override
    protected void init() {
        this.panelX = 12;
        this.panelY = (this.height - PANEL_HEIGHT) / 2;

        int scaledBodyWidth = Math.round(TEXTURE_WIDTH * BODY_SCALE);

        this.bodyX = panelX + (PANEL_WIDTH - scaledBodyWidth) / 2;
        this.bodyY = panelY + 62;

        this.diagnoseButton = Button.builder(
                Component.literal("Diagnosticar"),
                button -> startDiagnosis()
        ).bounds(
                panelX + 20,
                panelY + PANEL_HEIGHT - 42,
                PANEL_WIDTH - 40,
                20
        ).build();

        this.treatButton = Button.builder(
                Component.literal("Tratar"),
                button -> startTreatment()
        ).bounds(
                panelX + 20,
                panelY + PANEL_HEIGHT - 20,
                PANEL_WIDTH - 40,
                20
        ).build();

        this.diagnoseButton.visible = true;
        this.diagnoseButton.active = true;

        this.treatButton.visible = false;
        this.treatButton.active = false;
        addRenderableWidget(treatButton);

        addRenderableWidget(diagnoseButton);
    }

    @Override
    public void tick() {
        super.tick();

        if (diagnosing && !diagnosed) {
            diagnosisTicks++;

            if (diagnosisTicks >= DIAGNOSIS_TICKS_REQUIRED) {
                diagnosing = false;
                diagnosed = true;
                diagnosisTicks = DIAGNOSIS_TICKS_REQUIRED;

                if (diagnoseButton != null) {
                    diagnoseButton.visible = false;
                    diagnoseButton.active = false;
                }

                if (treatButton != null) {
                    treatButton.visible = true;
                    treatButton.active = false;
                    treatButton.setMessage(Component.literal("Tratar"));
                }

                ModNetwork.STATS_CHANNEL.sendToServer(new RequestMedicalBackpackC2SPacket());
            }
        }

        if (treating) {
            treatmentTicks++;

            if (treatmentTicks >= TREATMENT_TICKS_REQUIRED) {
                resetDiagnosisState();
            }
        }

        updateTreatButton();
    }

    private void startTreatment() {
        if (!diagnosed) return;
        if (selectedBackpackSlot < 0) return;
        if (selectedBodyPart == null) return;
        if (getBleeding(selectedBodyPart) == BleedingType.NONE) return;

        ModNetwork.STATS_CHANNEL.sendToServer(
                new StartTreatmentC2SPacket(
                        selectedBackpackSlot,
                        selectedBodyPart,
                        targetMode ? targetUuid : null
                )
        );

        treating = true;
        treatmentTicks = 0;

        if (treatButton != null) {
            treatButton.active = false;
            treatButton.setMessage(Component.literal("Tratando..."));
        }
    }

    private void updateTreatButton() {

        if (diagnoseButton != null && diagnosed) {
            diagnoseButton.visible = false;
            diagnoseButton.active = false;
        }

        if (treatButton == null) return;

        boolean canTreat = diagnosed
                && !treating
                && selectedBackpackSlot >= 0
                && selectedBodyPart != null
                && getBleeding(selectedBodyPart) != BleedingType.NONE;

        treatButton.visible = diagnosed;
        treatButton.active = canTreat;
        treatButton.setMessage(Component.literal(treating ? "Tratando..." : "Tratar"));
    }

    private void renderBackpack(GuiGraphics g, int mouseX, int mouseY) {
        int startX = panelX + PANEL_WIDTH + 16;
        int startY = panelY + 20;

        if (!ClientMedicalBackpackData.hasBackpack()) {
            g.drawString(this.font, "Sin mochila", startX, startY, 0xFFFF5555, false);
            return;
        }

        g.drawString(this.font, "Mochila", startX, startY - 12, 0xFFFFFFFF, false);

        for (int i = 0; i < ClientMedicalBackpackData.size(); i++) {
            int col = i % 4;
            int row = i / 4;

            int x = startX + col * 20;
            int y = startY + row * 20;

            g.fill(x, y, x + 18, y + 18, 0xAA222222);
            g.renderOutline(x, y, 18, 18, selectedBackpackSlot == i ? 0xFFFFFF00 : 0xAAFFFFFF);

            ItemStack stack = ClientMedicalBackpackData.getItem(i);

            if (!stack.isEmpty()) {
                g.renderItem(stack, x + 1, y + 1);
                g.renderItemDecorations(this.font, stack, x + 1, y + 1);
            }
        }
    }

    @Override
    public boolean mouseClicked(double mouseX, double mouseY, int button) {
        if (diagnosed && button == 0) {
            if (handleBackpackClick(mouseX, mouseY)) return true;
            if (handleBodyPartClick(mouseX, mouseY)) return true;
        }

        return super.mouseClicked(mouseX, mouseY, button);
    }

    private boolean handleBackpackClick(double mouseX, double mouseY) {
        int startX = panelX + PANEL_WIDTH + 16;
        int startY = panelY + 20;

        for (int i = 0; i < ClientMedicalBackpackData.size(); i++) {
            int col = i % 4;
            int row = i / 4;

            int x = startX + col * 20;
            int y = startY + row * 20;

            if (mouseX >= x && mouseX < x + 18 && mouseY >= y && mouseY < y + 18) {
                selectedBackpackSlot = i;
                return true;
            }
        }

        return false;
    }

    private boolean handleBodyPartClick(double mouseX, double mouseY) {
        BodyPart part = getClickedBleedingMarker(mouseX, mouseY);

        if (part == null) return false;

        selectedBodyPart = part;
        return true;
    }

    private BodyPart getClickedBleedingMarker(double mouseX, double mouseY) {
        double localX = (mouseX - bodyX) / BODY_SCALE;
        double localY = (mouseY - bodyY) / BODY_SCALE;

        if (localX < 0 || localX > TEXTURE_WIDTH || localY < 0 || localY > TEXTURE_HEIGHT) {
            return null;
        }

        for (BodyPart part : BodyPart.values()) {
            if (getBleeding(part) == BleedingType.NONE) {
                continue;
            }

            BleedMarker marker = getBleedMarker(part);

            int radius = Math.max(10, marker.size());

            double dx = localX - marker.x();
            double dy = localY - marker.y();

            if ((dx * dx) + (dy * dy) <= radius * radius) {
                return part;
            }
        }

        return null;
    }

    private BodyPart getClickedBodyPart(double mouseX, double mouseY) {
        double localX = (mouseX - bodyX) / BODY_SCALE;
        double localY = (mouseY - bodyY) / BODY_SCALE;

        if (localX < 0 || localX > TEXTURE_WIDTH || localY < 0 || localY > TEXTURE_HEIGHT) {
            return null;
        }

        if (localY >= 45 && localY <= 115 && localX >= 95 && localX <= 160) {
            return BodyPart.HEAD;
        }

        if (localY >= 120 && localY <= 260 && localX >= 90 && localX <= 165) {
            return BodyPart.TORSO;
        }

        if (localY >= 130 && localY <= 285 && localX >= 45 && localX < 90) {
            return BodyPart.LEFT_ARM;
        }

        if (localY >= 130 && localY <= 285 && localX > 165 && localX <= 210) {
            return BodyPart.RIGHT_ARM;
        }

        if (localY >= 265 && localY <= 470 && localX >= 90 && localX < 128) {
            return BodyPart.LEFT_LEG;
        }

        if (localY >= 265 && localY <= 470 && localX >= 128 && localX <= 166) {
            return BodyPart.RIGHT_LEG;
        }

        return null;
    }

    private void renderTreatmentProgress(GuiGraphics g) {
        if (!treating) return;

        int x = panelX + 20;
        int y = panelY + PANEL_HEIGHT - 46;
        int w = PANEL_WIDTH - 40;
        int h = 7;

        float progress = treatmentTicks / (float) TREATMENT_TICKS_REQUIRED;
        int fill = Math.round(w * progress);

        g.fill(x, y, x + w, y + h, 0xFF333333);
        g.fill(x, y, x + fill, y + h, 0xFFAA2222);
        g.renderOutline(x, y, w, h, 0xAAFFFFFF);
    }

    @Override
    public void render(GuiGraphics g, int mouseX, int mouseY, float partialTick) {
        this.renderBackground(g);

        if (Minecraft.getInstance().player == null) return;

        renderPanel(g);

        if (diagnosed) {
            renderBody(g, true);
            renderMedicalText(g);

            renderBackpack(g, mouseX, mouseY);
            renderTreatmentProgress(g);

        } else {
            renderBody(g, false);
            renderDiagnosisProgress(g);
        }

        super.render(g, mouseX, mouseY, partialTick);
    }

    private void startDiagnosis() {
        if (diagnosing || diagnosed) return;

        diagnosing = true;
        diagnosisTicks = 0;

        if (diagnoseButton != null) {
            diagnoseButton.setMessage(Component.literal("Diagnosticando..."));
            diagnoseButton.active = false;
        }
    }

    private int getSangre() {
        return targetMode
                ? ClientPatientMedicalData.getSangre()
                : ClientPlayerData.getSangre();
    }

    private int getBodyHp(BodyPart part) {
        return targetMode
                ? ClientPatientMedicalData.getBodyHp(part)
                : ClientPlayerData.getBodyHp(part);
    }

    private BleedingType getBleeding(BodyPart part) {
        return targetMode
                ? ClientPatientMedicalData.getBleeding(part)
                : ClientPlayerData.getBleeding(part);
    }

    private void renderPanel(GuiGraphics g) {
        g.fill(panelX, panelY, panelX + PANEL_WIDTH, panelY + PANEL_HEIGHT, 0xDD101010);
        g.fill(panelX, panelY, panelX + PANEL_WIDTH, panelY + 26, 0xEE1D1D1D);
        g.renderOutline(panelX, panelY, PANEL_WIDTH, PANEL_HEIGHT, 0xAAFFFFFF);

        g.drawString(
                this.font,
                targetMode ? "Paciente: " + targetName : "Estado médico",
                panelX + 10,
                panelY + 9,
                0xFFFFFFFF,
                false
        );
    }

    private void renderDiagnosisProgress(GuiGraphics g) {
        if (!diagnosing) return;

        int barX = panelX + 20;
        int barY = panelY + PANEL_HEIGHT - 68;
        int barW = PANEL_WIDTH - 40;
        int barH = 8;

        float progress = diagnosisTicks / (float) DIAGNOSIS_TICKS_REQUIRED;
        int fillW = Math.round(barW * progress);

        g.fill(barX, barY, barX + barW, barY + barH, 0xFF333333);
        g.fill(barX, barY, barX + fillW, barY + barH, 0xFFAA2222);
        g.renderOutline(barX, barY, barW, barH, 0xAAFFFFFF);

        int secondsLeft = Math.max(0, 5 - diagnosisTicks / 20);

        g.drawString(
                this.font,
                "Tiempo: " + secondsLeft + "s",
                barX,
                barY - 12,
                0xFFFFFFFF,
                false
        );
    }

    private void resetDiagnosisState() {
        diagnosing = false;
        diagnosed = false;
        diagnosisTicks = 0;

        selectedBackpackSlot = -1;
        selectedBodyPart = null;

        treating = false;
        treatmentTicks = 0;

        if (diagnoseButton != null) {
            diagnoseButton.visible = true;
            diagnoseButton.active = true;
            diagnoseButton.setMessage(Component.literal("Diagnosticar"));
        }

        if (treatButton != null) {
            treatButton.visible = false;
            treatButton.active = false;
            treatButton.setMessage(Component.literal("Tratar"));
        }
    }

    private void renderMedicalText(GuiGraphics g) {
        int y = panelY + PANEL_HEIGHT - 68;
        int sangre = getSangre();

        g.drawString(
                this.font,
                "Sangre: " + sangre + "%",
                panelX + 12,
                y,
                getBloodTextColor(sangre),
                false
        );
    }

    private void renderBody(GuiGraphics g, boolean showDiagnosis) {
        g.pose().pushPose();

        g.pose().translate(bodyX, bodyY, 0);
        g.pose().scale(BODY_SCALE, BODY_SCALE, 1.0F);

        g.blit(BASE, 0, 0, 0, 0, TEXTURE_WIDTH, TEXTURE_HEIGHT, TEXTURE_WIDTH, TEXTURE_HEIGHT);

        if (showDiagnosis) {
            renderPart(g, BodyPart.HEAD, HEAD);
            renderPart(g, BodyPart.TORSO, TORSO);
            renderPart(g, BodyPart.LEFT_ARM, LEFT_ARM);
            renderPart(g, BodyPart.RIGHT_ARM, RIGHT_ARM);
            renderPart(g, BodyPart.LEFT_LEG, LEFT_LEG);
            renderPart(g, BodyPart.RIGHT_LEG, RIGHT_LEG);

            renderBleeding(g, BodyPart.HEAD);
            renderBleeding(g, BodyPart.TORSO);
            renderBleeding(g, BodyPart.LEFT_ARM);
            renderBleeding(g, BodyPart.RIGHT_ARM);
            renderBleeding(g, BodyPart.LEFT_LEG);
            renderBleeding(g, BodyPart.RIGHT_LEG);
        }

        g.pose().popPose();
    }

    private void renderPart(GuiGraphics g, BodyPart part, ResourceLocation texture) {
        int hp = getBodyHp(part);
        int color = getColorByHp(part, hp);

        float a = ((color >> 24) & 0xFF) / 255.0F;
        float r = ((color >> 16) & 0xFF) / 255.0F;
        float gg = ((color >> 8) & 0xFF) / 255.0F;
        float b = (color & 0xFF) / 255.0F;

        g.setColor(r, gg, b, a);
        g.blit(texture, 0, 0, 0, 0, TEXTURE_WIDTH, TEXTURE_HEIGHT, TEXTURE_WIDTH, TEXTURE_HEIGHT);
        g.setColor(1.0F, 1.0F, 1.0F, 1.0F);
    }

    private void renderBleeding(GuiGraphics g, BodyPart part) {
        ResourceLocation bleedTexture = getBleedTexture(getBleeding(part));
        if (bleedTexture == null) return;

        BleedMarker marker = getBleedMarker(part);

        int size = marker.size();
        int x = marker.x() - size / 2;
        int y = marker.y() - size / 2;

        if (selectedBodyPart == part) {
            g.renderOutline(x - 2, y - 2, size + 4, size + 4, 0xFFFFFF00);
        }

        g.blit(bleedTexture, x, y, 0, 0, size, size, 16, 16);
    }

    private BleedMarker getBleedMarker(BodyPart part) {
        return switch (part) {
            case HEAD -> new BleedMarker(128, 90, 14);
            case TORSO -> new BleedMarker(128, 230, 16);
            case LEFT_ARM -> new BleedMarker(72, 230, 13);
            case RIGHT_ARM -> new BleedMarker(184, 230, 13);
            case LEFT_LEG -> new BleedMarker(108, 405, 13);
            case RIGHT_LEG -> new BleedMarker(148, 405, 13);
        };
    }

    private ResourceLocation getBleedTexture(BleedingType bleeding) {
        return switch (bleeding) {
            case LIGHT -> BLEED_LIGHT;
            case MEDIUM -> BLEED_MEDIUM;
            case HEAVY -> BLEED_HEAVY;
            case NONE -> null;
        };
    }

    private int getColorByHp(BodyPart part, int hp) {
        int max = switch (part) {
            case HEAD -> 20;
            case TORSO -> 60;
            case LEFT_ARM, RIGHT_ARM -> 25;
            case LEFT_LEG, RIGHT_LEG -> 30;
        };

        float ratio = hp / (float) max;

        if (ratio >= 0.70F) return 0xFF00FF00;
        if (ratio >= 0.40F) return 0xFFFFFF00;
        if (ratio > 0.0F) return 0xFFFFA500;
        return 0xFFFF0000;
    }

    private int getBloodTextColor(int sangre) {
        if (sangre <= 20) return 0xFFFF3333;
        if (sangre <= 50) return 0xFFFFAA00;
        return 0xFFFFFFFF;
    }

    @Override
    public boolean isPauseScreen() {
        return false;
    }

    private record BleedMarker(int x, int y, int size) {
    }
}