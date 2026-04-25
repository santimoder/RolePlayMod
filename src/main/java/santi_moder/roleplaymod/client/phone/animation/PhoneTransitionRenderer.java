package santi_moder.roleplaymod.client.phone.animation;

import net.minecraft.client.gui.GuiGraphics;
import santi_moder.roleplaymod.client.screen.PhoneScreen;

public final class PhoneTransitionRenderer {

    private PhoneTransitionRenderer() {
    }

    public static void beginIncoming(GuiGraphics guiGraphics, PhoneScreen screen, PhoneTransition transition) {
        if (transition == null || transition.incomingAnimation() == null) {
            return;
        }

        guiGraphics.pose().pushPose();
        applyIncomingTransform(guiGraphics, screen, transition);
    }

    public static void endIncoming(GuiGraphics guiGraphics) {
        guiGraphics.pose().popPose();
    }

    public static void beginOutgoing(GuiGraphics guiGraphics, PhoneScreen screen, PhoneTransition transition) {
        if (transition == null || transition.outgoingAnimation() == null) {
            return;
        }

        guiGraphics.pose().pushPose();
        applyOutgoingTransform(guiGraphics, screen, transition);
    }

    public static void endOutgoing(GuiGraphics guiGraphics) {
        guiGraphics.pose().popPose();
    }

    public static void renderIncomingFadeOverlay(GuiGraphics guiGraphics, PhoneScreen screen, PhoneTransition transition) {
        if (transition == null || transition.incomingAnimation() == null) {
            return;
        }

        float progress = transition.incomingAnimation().easeInOut();
        int alpha = (int) ((1.0F - progress) * 170.0F);
        fillPhoneContent(guiGraphics, screen, alpha);
    }

    public static void renderOutgoingFadeOverlay(GuiGraphics guiGraphics, PhoneScreen screen, PhoneTransition transition) {
        if (transition == null || transition.outgoingAnimation() == null) {
            return;
        }

        float progress = transition.outgoingAnimation().easeInOut();
        int alpha = (int) (progress * 140.0F);
        fillPhoneContent(guiGraphics, screen, alpha);
    }

    private static void applyIncomingTransform(GuiGraphics guiGraphics, PhoneScreen screen, PhoneTransition transition) {
        PhoneAnimation animation = transition.incomingAnimation();
        float progress = animation.easeInOut();

        switch (animation.type()) {
            case ICON_EXPAND_IN -> applyExpandFromIcon(guiGraphics, screen, progress, transition.context());
            case APP_BACK_LEFT_IN -> {
                float x = lerp(-18.0F, 0.0F, progress);
                guiGraphics.pose().translate(x, 0.0F, 0.0F);
            }
            case PASSCODE_UP_IN -> {
                float y = lerp(18.0F, 0.0F, progress);
                guiGraphics.pose().translate(0.0F, y, 0.0F);
            }
            case HOME_FADE_IN -> {
                // solo fade
            }
            default -> {
            }
        }
    }

    private static void applyOutgoingTransform(GuiGraphics guiGraphics, PhoneScreen screen, PhoneTransition transition) {
        PhoneAnimation animation = transition.outgoingAnimation();
        float progress = animation.easeInOut();

        switch (animation.type()) {
            case ICON_COLLAPSE_OUT -> applyCollapseToIcon(guiGraphics, screen, progress, transition.context());
            case APP_BACK_RIGHT_OUT -> {
                float x = lerp(0.0F, 24.0F, progress);
                guiGraphics.pose().translate(x, 0.0F, 0.0F);
            }
            case LOCKSCREEN_FADE_OUT, PASSCODE_FADE_OUT -> {
                // solo fade
            }
            default -> {
            }
        }
    }

    private static void applyExpandFromIcon(
            GuiGraphics guiGraphics,
            PhoneScreen screen,
            float progress,
            PhoneTransitionContext context
    ) {
        if (!context.hasSourceRect()) {
            return;
        }

        float contentX = screen.getContentX();
        float contentY = screen.getContentY();
        float contentWidth = screen.getContentWidth();
        float contentHeight = screen.getContentHeight();

        float sourceCenterX = context.sourceX() + context.sourceWidth() / 2.0F;
        float sourceCenterY = context.sourceY() + context.sourceHeight() / 2.0F;

        float targetCenterX = contentX + contentWidth / 2.0F;
        float targetCenterY = contentY + contentHeight / 2.0F;

        float startScaleX = context.sourceWidth() / contentWidth;
        float startScaleY = context.sourceHeight() / contentHeight;

        float scaleX = lerp(startScaleX, 1.0F, progress);
        float scaleY = lerp(startScaleY, 1.0F, progress);

        float centerX = lerp(sourceCenterX, targetCenterX, progress);
        float centerY = lerp(sourceCenterY, targetCenterY, progress);

        guiGraphics.pose().translate(centerX, centerY, 0.0F);
        guiGraphics.pose().scale(scaleX, scaleY, 1.0F);
        guiGraphics.pose().translate(-targetCenterX, -targetCenterY, 0.0F);
    }

    private static void applyCollapseToIcon(
            GuiGraphics guiGraphics,
            PhoneScreen screen,
            float progress,
            PhoneTransitionContext context
    ) {
        if (!context.hasSourceRect()) {
            return;
        }

        float contentX = screen.getContentX();
        float contentY = screen.getContentY();
        float contentWidth = screen.getContentWidth();
        float contentHeight = screen.getContentHeight();

        float startCenterX = contentX + contentWidth / 2.0F;
        float startCenterY = contentY + contentHeight / 2.0F;

        float targetCenterX = context.sourceX() + context.sourceWidth() / 2.0F;
        float targetCenterY = context.sourceY() + context.sourceHeight() / 2.0F;

        float endScaleX = context.sourceWidth() / contentWidth;
        float endScaleY = context.sourceHeight() / contentHeight;

        float scaleX = lerp(1.0F, endScaleX, progress);
        float scaleY = lerp(1.0F, endScaleY, progress);

        float centerX = lerp(startCenterX, targetCenterX, progress);
        float centerY = lerp(startCenterY, targetCenterY, progress);

        guiGraphics.pose().translate(centerX, centerY, 0.0F);
        guiGraphics.pose().scale(scaleX, scaleY, 1.0F);
        guiGraphics.pose().translate(-startCenterX, -startCenterY, 0.0F);
    }

    private static void fillPhoneContent(GuiGraphics guiGraphics, PhoneScreen screen, int alpha) {
        if (alpha <= 0) {
            return;
        }

        int color = (alpha << 24);
        guiGraphics.fill(
                screen.getContentX(),
                screen.getContentY(),
                screen.getContentX() + screen.getContentWidth(),
                screen.getContentY() + screen.getContentHeight(),
                color
        );
    }

    private static float lerp(float start, float end, float progress) {
        return start + (end - start) * progress;
    }
}