package santi_moder.roleplaymod.client.phone.overlay;

import net.minecraft.client.gui.GuiGraphics;
import santi_moder.roleplaymod.client.phone.app.whatsapp.WhatsappTextureResolver;
import santi_moder.roleplaymod.client.phone.ui.PhoneThemeColors;
import santi_moder.roleplaymod.client.screen.PhoneScreen;
import santi_moder.roleplaymod.common.phone.PhoneAppId;

public final class PhoneNotificationOverlay {

    private static final long DURATION_MS = 4000L;

    private String title;
    private String message;
    private String photoId;

    private long createdAt;
    private boolean active;

    public void show(String title, String message, String photoId) {
        this.title = title;
        this.message = message;
        this.photoId = photoId;

        this.createdAt = System.currentTimeMillis();
        this.active = true;
    }

    public void tick() {
        if (!active) {
            return;
        }

        if (System.currentTimeMillis() - createdAt >= DURATION_MS) {
            active = false;
        }
    }

    public void render(PhoneScreen screen, GuiGraphics guiGraphics) {
        if (!active) {
            return;
        }

        if (screen.getCurrentAppId() == PhoneAppId.LOCK_SCREEN) {
            renderLockscreen(screen, guiGraphics);
        } else {
            renderBanner(screen, guiGraphics);
        }
    }

    private void renderBanner(PhoneScreen screen, GuiGraphics guiGraphics) {
        int x = screen.getPhoneX() + 10;
        int y = screen.getPhoneY() + 26;

        int width = screen.getPhoneWidth() - 20;
        int height = 34;

        guiGraphics.fill(
                x,
                y,
                x + width,
                y + height,
                PhoneThemeColors.sheet(screen.getPhoneStack())
        );

        WhatsappTextureResolver.drawProfilePhoto(
                guiGraphics,
                photoId,
                x + 4,
                y + 4,
                26
        );

        guiGraphics.drawString(
                screen.getPhoneFont(),
                title,
                x + 36,
                y + 6,
                PhoneThemeColors.text(screen.getPhoneStack()),
                false
        );

        guiGraphics.drawString(
                screen.getPhoneFont(),
                message,
                x + 36,
                y + 18,
                PhoneThemeColors.subtext(screen.getPhoneStack()),
                false
        );
    }

    private void renderLockscreen(PhoneScreen screen, GuiGraphics guiGraphics) {
        int width = screen.getPhoneWidth() - 26;
        int height = 46;

        int x = screen.getPhoneCenterX() - width / 2;
        int y = screen.getPhoneCenterY() - 24;

        guiGraphics.fill(
                x,
                y,
                x + width,
                y + height,
                PhoneThemeColors.sheet(screen.getPhoneStack())
        );

        WhatsappTextureResolver.drawProfilePhoto(
                guiGraphics,
                photoId,
                x + 6,
                y + 10,
                26
        );

        guiGraphics.drawString(
                screen.getPhoneFont(),
                title,
                x + 40,
                y + 12,
                PhoneThemeColors.text(screen.getPhoneStack()),
                false
        );

        guiGraphics.drawString(
                screen.getPhoneFont(),
                message,
                x + 40,
                y + 24,
                PhoneThemeColors.subtext(screen.getPhoneStack()),
                false
        );
    }
}