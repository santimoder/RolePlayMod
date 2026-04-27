package santi_moder.roleplaymod.client.screen;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.item.ItemStack;
import org.lwjgl.glfw.GLFW;
import santi_moder.roleplaymod.client.phone.animation.PhoneAnimator;
import santi_moder.roleplaymod.client.phone.animation.PhoneNavigationAction;
import santi_moder.roleplaymod.client.phone.animation.PhoneTransition;
import santi_moder.roleplaymod.client.phone.animation.PhoneTransitionContext;
import santi_moder.roleplaymod.client.phone.animation.PhoneTransitionRenderer;
import santi_moder.roleplaymod.client.phone.app.AbstractPhoneApp;
import santi_moder.roleplaymod.client.phone.navigation.PhoneRecentAppsManager;
import santi_moder.roleplaymod.client.phone.overlay.PhoneAppSwitcherOverlay;
import santi_moder.roleplaymod.client.phone.registry.PhoneAppRegistry;
import santi_moder.roleplaymod.client.phone.ui.layout.PhoneHomeIconLayoutResolver;
import santi_moder.roleplaymod.client.phone.ui.render.PhoneLockedHudRenderer;
import santi_moder.roleplaymod.client.phone.ui.render.PhoneUnlockedHudRenderer;
import santi_moder.roleplaymod.client.phone.ui.render.PhoneWallpaperRenderer;
import santi_moder.roleplaymod.common.phone.PhoneAppId;
import santi_moder.roleplaymod.common.phone.PhoneData;
import santi_moder.roleplaymod.item.PhoneItem;

public class PhoneScreen extends Screen {

    private static final int PHONE_WIDTH = 150;
    private static final int PHONE_HEIGHT = 260;

    private static final int MARGIN_RIGHT = 20;
    private static final int MARGIN_BOTTOM = 20;

    private static final int PHONE_INNER_PADDING = 4;

    private static final int NOTCH_WIDTH = 54;
    private static final int NOTCH_HEIGHT = 10;

    private static final int HOME_INDICATOR_WIDTH = 42;
    private static final int HOME_INDICATOR_HEIGHT = 4;
    private static final int HOME_INDICATOR_Y_OFFSET = 12;
    private static final int HOME_INDICATOR_EXTRA_HITBOX_X = 6;
    private static final int HOME_INDICATOR_EXTRA_HITBOX_Y = 6;
    private static final int HOME_INDICATOR_HITBOX_HEIGHT = 16;

    private static final int SAFE_TOP = 26;
    private static final int SAFE_BOTTOM = 20;
    private static final int SAFE_LEFT = 8;
    private static final int SAFE_RIGHT = 8;

    private static final int FRAME_COLOR = 0xFF111111;
    private static final int NOTCH_COLOR = 0xFF000000;
    private static final int HOME_INDICATOR_COLOR = 0xDDFFFFFF;
    private static final int HOME_INDICATOR_HOVER_COLOR = 0xFFFFFFFF;

    private static final int SWIPE_HOME_THRESHOLD = 18;
    private static final int SWIPE_SWITCHER_THRESHOLD = 62;
    private static final int SWIPE_HORIZONTAL_THRESHOLD = 22;

    private static final double SWIPE_LERP_FACTOR = 0.35D;
    private static final double SWIPE_SNAP_EPSILON = 0.5D;
    private static final double MAX_SWIPE_OFFSET = 38.0D;

    private final InteractionHand hand;
    private final PhoneAppRegistry appRegistry;
    private final PhoneAnimator animator;
    private final PhoneRecentAppsManager recentAppsManager;
    private final PhoneAppSwitcherOverlay appSwitcherOverlay;

    private PhoneAppId currentApp = PhoneAppId.HOME;
    private PhoneAppId previousApp = PhoneAppId.HOME;

    private int phoneX;
    private int phoneY;

    private boolean sessionAuthenticated;
    private boolean faceIdReady;

    private boolean gestureActive;
    private double gestureStartX = -1.0D;
    private double gestureStartY = -1.0D;

    private double currentSwipeOffset = 0.0D;
    private double targetSwipeOffset = 0.0D;

    public PhoneScreen(InteractionHand hand) {
        super(Component.literal("Phone"));
        this.hand = hand;
        this.appRegistry = new PhoneAppRegistry();
        this.animator = new PhoneAnimator();
        this.recentAppsManager = new PhoneRecentAppsManager();
        this.appSwitcherOverlay = new PhoneAppSwitcherOverlay();
    }

    @Override
    protected void init() {
        super.init();
        recalculatePhonePosition();
        resolveInitialState();
        notifyCurrentAppOpened();
        recentAppsManager.onAppBecameForeground(currentApp);
    }

    private void resolveInitialState() {
        ItemStack stack = getPhoneStack();

        sessionAuthenticated = false;
        faceIdReady = false;
        resetGestureState();

        currentApp = PhoneAppId.LOCK_SCREEN;
        previousApp = currentApp;
        faceIdReady = canCurrentViewerAutoUnlock();
    }

    @Override
    public boolean charTyped(char codePoint, int modifiers) {
        if (animator.isTransitionRunning()) {
            return true;
        }

        if (appSwitcherOverlay.isActive()) {
            return true;
        }

        return getCurrentAppInstance().charTyped(this, codePoint, modifiers)
                || super.charTyped(codePoint, modifiers);
    }

    private void notifyCurrentAppOpened() {
        getAppInstance(currentApp).onOpen(this, getPhoneStack());
    }

    private void notifyCurrentAppClosed(PhoneAppId appId) {
        getAppInstance(appId).onClose(this, getPhoneStack());
    }

    private void recalculatePhonePosition() {
        phoneX = width - PHONE_WIDTH - MARGIN_RIGHT;
        phoneY = height - PHONE_HEIGHT - MARGIN_BOTTOM;
    }

    private void resetGestureState() {
        gestureActive = false;
        gestureStartX = -1.0D;
        gestureStartY = -1.0D;
        currentSwipeOffset = 0.0D;
        targetSwipeOffset = 0.0D;
    }

    public void openApp(PhoneAppId requestedApp) {
        navigateTo(requestedApp, PhoneNavigationAction.OPEN_FROM_ICON, PhoneTransitionContext.empty());
    }

    public void openAppFromIcon(PhoneAppId requestedApp, PhoneTransitionContext context) {
        navigateTo(requestedApp, PhoneNavigationAction.OPEN_FROM_ICON, context);
    }

    public void navigateBackTo(PhoneAppId targetApp) {
        navigateTo(targetApp, PhoneNavigationAction.BACK, PhoneTransitionContext.empty());
    }

    public void goHomeToAppIcon() {
        PhoneTransitionContext context = PhoneHomeIconLayoutResolver.resolveIconContext(this, currentApp);
        navigateTo(PhoneAppId.HOME, PhoneNavigationAction.CLOSE_TO_HOME_ICON, context);
    }

    public void unlockPhone() {
        sessionAuthenticated = true;
        navigateTo(PhoneAppId.HOME, PhoneNavigationAction.UNLOCK, PhoneTransitionContext.empty());
    }

    public void goToLockScreen() {
        sessionAuthenticated = false;
        appSwitcherOverlay.close();
        navigateTo(PhoneAppId.LOCK_SCREEN, PhoneNavigationAction.LOCK, PhoneTransitionContext.empty());
    }

    public void openPasscodeScreen() {
        resetGestureState();
        navigateTo(PhoneAppId.PASSCODE_SCREEN, PhoneNavigationAction.OPEN_PASSCODE, PhoneTransitionContext.empty());
    }

    public void openAppSwitcher() {
        if (animator.isTransitionRunning()) {
            return;
        }

        appSwitcherOverlay.open(recentAppsManager.getRecentApps(), currentApp);
    }

    public void closeAppSwitcher() {
        appSwitcherOverlay.close();
    }

    public boolean isAppSwitcherOpen() {
        return appSwitcherOverlay.isActive();
    }

    public void switchToPreviousRecentApp() {
        PhoneAppId target = recentAppsManager.getPreviousRecent(currentApp);
        if (target == null) {
            return;
        }

        closeAppSwitcher();
        navigateTo(target, PhoneNavigationAction.SWITCH_TO_PREVIOUS, PhoneTransitionContext.empty());
    }

    public void switchToNextRecentApp() {
        PhoneAppId target = recentAppsManager.getNextRecent(currentApp);
        if (target == null) {
            return;
        }

        closeAppSwitcher();
        navigateTo(target, PhoneNavigationAction.SWITCH_TO_NEXT, PhoneTransitionContext.empty());
    }

    public void openSelectedSwitcherApp() {
        PhoneAppId target = appSwitcherOverlay.getSelectedApp();
        if (target == null) {
            return;
        }

        closeAppSwitcher();

        if (target == currentApp) {
            return;
        }

        navigateTo(target, PhoneNavigationAction.SWITCH_TO_PREVIOUS, PhoneTransitionContext.empty());
    }

    private void navigateTo(PhoneAppId requestedApp, PhoneNavigationAction action, PhoneTransitionContext context) {
        PhoneAppId targetApp = normalizeTargetApp(requestedApp);

        if (targetApp == null) {
            return;
        }

        if (targetApp == currentApp) {
            resetGestureState();
            faceIdReady = targetApp == PhoneAppId.LOCK_SCREEN && canCurrentViewerAutoUnlock();
            return;
        }

        if (animator.isTransitionRunning()) {
            return;
        }

        PhoneAppId oldApp = currentApp;

        notifyCurrentAppClosed(oldApp);

        previousApp = oldApp;
        currentApp = targetApp;

        faceIdReady = targetApp == PhoneAppId.LOCK_SCREEN && canCurrentViewerAutoUnlock();
        resetGestureState();

        animator.startTransition(oldApp, targetApp, action, context);
        notifyCurrentAppOpened();
        recentAppsManager.onAppBecameForeground(targetApp);
    }

    private PhoneAppId normalizeTargetApp(PhoneAppId requestedApp) {
        if (requestedApp == null) {
            return PhoneAppId.HOME;
        }

        if (!sessionAuthenticated
                && requestedApp != PhoneAppId.LOCK_SCREEN
                && requestedApp != PhoneAppId.PASSCODE_SCREEN) {
            return PhoneAppId.LOCK_SCREEN;
        }

        if (requestedApp.isVisibleOnHome()
                && !PhoneData.isAppInstalled(getPhoneStack(), requestedApp)) {
            return PhoneAppId.HOME;
        }

        return requestedApp;
    }

    public boolean isSessionAuthenticated() {
        return sessionAuthenticated;
    }

    public boolean isFaceIdReady() {
        return faceIdReady;
    }

    public PhoneAppId getCurrentAppId() {
        return currentApp;
    }

    public PhoneAppId getPreviousAppId() {
        return previousApp;
    }

    public boolean isTransitionRunning() {
        return animator.isTransitionRunning();
    }

    public boolean canCurrentViewerAutoUnlock() {
        if (minecraft == null || minecraft.player == null) {
            return false;
        }

        return PhoneData.canUnlockWithFaceId(getPhoneStack(), minecraft.player);
    }

    private AbstractPhoneApp getCurrentAppInstance() {
        return getAppInstance(currentApp);
    }

    private AbstractPhoneApp getPreviousAppInstance() {
        return getAppInstance(previousApp);
    }

    private AbstractPhoneApp getAppInstance(PhoneAppId appId) {
        AbstractPhoneApp app = appRegistry.get(appId);
        if (app != null) {
            return app;
        }

        AbstractPhoneApp fallback = appRegistry.get(PhoneAppId.HOME);
        if (fallback == null) {
            throw new IllegalStateException("No se encontro la app HOME en el registry.");
        }

        return fallback;
    }

    @Override
    public void tick() {
        super.tick();

        if (!isPhoneStillValid()) {
            closePhone();
            return;
        }

        if (currentApp == PhoneAppId.LOCK_SCREEN) {
            faceIdReady = canCurrentViewerAutoUnlock();
        }

        animateSwipeOffset();
        animator.tick();

        if (appSwitcherOverlay.isActive()) {
            appSwitcherOverlay.tick();
        }

        getCurrentAppInstance().tick(this, getPhoneStack());
    }

    private boolean isPhoneStillValid() {
        if (minecraft == null || minecraft.player == null) {
            return false;
        }

        ItemStack stack = minecraft.player.getItemInHand(hand);
        return stack.getItem() instanceof PhoneItem;
    }

    private void animateSwipeOffset() {
        currentSwipeOffset += (targetSwipeOffset - currentSwipeOffset) * SWIPE_LERP_FACTOR;

        if (!gestureActive
                && Math.abs(targetSwipeOffset) < SWIPE_SNAP_EPSILON
                && Math.abs(currentSwipeOffset) < SWIPE_SNAP_EPSILON) {
            currentSwipeOffset = 0.0D;
            targetSwipeOffset = 0.0D;
        }
    }

    public ItemStack getPhoneStack() {
        if (minecraft == null || minecraft.player == null) {
            return ItemStack.EMPTY;
        }

        return minecraft.player.getItemInHand(hand);
    }

    public void closePhone() {
        sessionAuthenticated = false;
        appSwitcherOverlay.close();
        if (minecraft != null) {
            minecraft.setScreen(null);
        }
    }

    @Override
    public boolean isPauseScreen() {
        return false;
    }

    @Override
    public void resize(Minecraft minecraft, int width, int height) {
        super.resize(minecraft, width, height);
        recalculatePhonePosition();
        getCurrentAppInstance().onResize(this, width, height);
    }

    @Override
    public void renderBackground(GuiGraphics guiGraphics) {
    }

    @Override
    public void render(GuiGraphics guiGraphics, int mouseX, int mouseY, float partialTick) {
        renderPhoneFrame(guiGraphics);
        if (currentApp == PhoneAppId.LOCK_SCREEN || currentApp == PhoneAppId.HOME) {
            PhoneWallpaperRenderer.render(this, guiGraphics, getPhoneStack());
        }

        if (currentApp == PhoneAppId.LOCK_SCREEN) {
            PhoneLockedHudRenderer.render(this, guiGraphics);
        } else {
            PhoneUnlockedHudRenderer.render(this, guiGraphics);
        }

        PhoneTransition transition = animator.getActiveTransition();

        if (appSwitcherOverlay.isActive()) {
            appSwitcherOverlay.render(this, guiGraphics, mouseX, mouseY, partialTick);
        } else if (transition != null) {
            renderTransition(guiGraphics, mouseX, mouseY, partialTick, transition);
        } else {
            getCurrentAppInstance().render(this, guiGraphics, mouseX, mouseY, partialTick);
        }

        renderHomeIndicator(guiGraphics, mouseX, mouseY);
        super.render(guiGraphics, mouseX, mouseY, partialTick);
    }

    private void renderTransition(GuiGraphics guiGraphics, int mouseX, int mouseY, float partialTick, PhoneTransition transition) {
        if (transition.outgoingAnimation() != null && transition.fromApp() != transition.toApp()) {
            PhoneTransitionRenderer.beginOutgoing(guiGraphics, this, transition);
            getPreviousAppInstance().render(this, guiGraphics, mouseX, mouseY, partialTick);
            PhoneTransitionRenderer.renderOutgoingFadeOverlay(guiGraphics, this, transition);
            PhoneTransitionRenderer.endOutgoing(guiGraphics);
        }

        PhoneTransitionRenderer.beginIncoming(guiGraphics, this, transition);
        getCurrentAppInstance().render(this, guiGraphics, mouseX, mouseY, partialTick);
        PhoneTransitionRenderer.renderIncomingFadeOverlay(guiGraphics, this, transition);
        PhoneTransitionRenderer.endIncoming(guiGraphics);
    }

    private void renderPhoneFrame(GuiGraphics guiGraphics) {
        guiGraphics.fill(phoneX, phoneY, phoneX + PHONE_WIDTH, phoneY + PHONE_HEIGHT, FRAME_COLOR);

        guiGraphics.fill(
                phoneX + PHONE_INNER_PADDING,
                phoneY + PHONE_INNER_PADDING,
                phoneX + PHONE_WIDTH - PHONE_INNER_PADDING,
                phoneY + PHONE_HEIGHT - PHONE_INNER_PADDING,
                0xFF000000
        );

        int notchX = phoneX + (PHONE_WIDTH - NOTCH_WIDTH) / 2;
        int notchY = phoneY + PHONE_INNER_PADDING;

        guiGraphics.fill(notchX, notchY, notchX + NOTCH_WIDTH, notchY + NOTCH_HEIGHT, NOTCH_COLOR);
    }

    private void renderHomeIndicator(GuiGraphics guiGraphics, int mouseX, int mouseY) {
        if (currentApp == PhoneAppId.PASSCODE_SCREEN) {
            return;
        }

        int x = getHomeIndicatorX();
        int y = getHomeIndicatorY() - (int) currentSwipeOffset;

        boolean hover = isInside(
                mouseX,
                mouseY,
                x - HOME_INDICATOR_EXTRA_HITBOX_X,
                y - HOME_INDICATOR_EXTRA_HITBOX_Y,
                HOME_INDICATOR_WIDTH + HOME_INDICATOR_EXTRA_HITBOX_X * 2,
                HOME_INDICATOR_HITBOX_HEIGHT
        );

        int color = hover || gestureActive ? HOME_INDICATOR_HOVER_COLOR : HOME_INDICATOR_COLOR;
        guiGraphics.fill(x, y, x + HOME_INDICATOR_WIDTH, y + HOME_INDICATOR_HEIGHT, color);
    }

    public int getHomeIndicatorX() {
        return phoneX + (PHONE_WIDTH - HOME_INDICATOR_WIDTH) / 2;
    }

    public int getHomeIndicatorY() {
        return phoneY + PHONE_HEIGHT - HOME_INDICATOR_Y_OFFSET;
    }

    public boolean isHomeIndicatorArea(double mouseX, double mouseY) {
        if (currentApp == PhoneAppId.PASSCODE_SCREEN || animator.isTransitionRunning()) {
            return false;
        }

        int x = getHomeIndicatorX();
        int y = getHomeIndicatorY() - (int) currentSwipeOffset;

        return isInside(
                mouseX,
                mouseY,
                x - HOME_INDICATOR_EXTRA_HITBOX_X,
                y - HOME_INDICATOR_EXTRA_HITBOX_Y,
                HOME_INDICATOR_WIDTH + HOME_INDICATOR_EXTRA_HITBOX_X * 2,
                HOME_INDICATOR_HITBOX_HEIGHT
        );
    }

    @Override
    public boolean mouseClicked(double mouseX, double mouseY, int button) {
        if (animator.isTransitionRunning()) {
            return true;
        }

        if (appSwitcherOverlay.isActive()) {
            if (button == 0) {
                if (appSwitcherOverlay.beginPointer(mouseX, mouseY, this)) {
                    return true;
                }

                closeAppSwitcher();
                return true;
            }
            return true;
        }

        if (button == 0 && isHomeIndicatorArea(mouseX, mouseY)) {
            gestureActive = true;
            gestureStartX = mouseX;
            gestureStartY = mouseY;
            targetSwipeOffset = 0.0D;
            return true;
        }

        return getCurrentAppInstance().mouseClicked(this, mouseX, mouseY, button)
                || super.mouseClicked(mouseX, mouseY, button);
    }

    @Override
    public boolean mouseDragged(double mouseX, double mouseY, int button, double dragX, double dragY) {
        if (animator.isTransitionRunning()) {
            return true;
        }

        if (appSwitcherOverlay.isActive()) {
            if (button == 0 && appSwitcherOverlay.wasPointerDown()) {
                appSwitcherOverlay.dragTo(mouseX);
            }
            return true;
        }

        if (button == 0 && gestureActive) {
            double deltaY = Math.max(0.0D, gestureStartY - mouseY);
            targetSwipeOffset = Math.min(MAX_SWIPE_OFFSET, deltaY);
            return true;
        }

        return getCurrentAppInstance().mouseDragged(this, mouseX, mouseY, button, dragX, dragY)
                || super.mouseDragged(mouseX, mouseY, button, dragX, dragY);
    }

    @Override
    public boolean mouseReleased(double mouseX, double mouseY, int button) {
        if (animator.isTransitionRunning()) {
            return true;
        }

        if (appSwitcherOverlay.isActive()) {
            if (button == 0) {
                PhoneAppId clickedApp = appSwitcherOverlay.releasePointer(this, mouseX, mouseY);

                if (clickedApp != null) {
                    closeAppSwitcher();

                    if (clickedApp != currentApp) {
                        navigateTo(clickedApp, PhoneNavigationAction.SWITCH_TO_PREVIOUS, PhoneTransitionContext.empty());
                    }
                    return true;
                }

                return true;
            }
            return true;
        }

        if (button == 0 && gestureActive) {
            double deltaX = mouseX - gestureStartX;
            double deltaY = gestureStartY - mouseY;

            gestureActive = false;
            gestureStartX = -1.0D;
            gestureStartY = -1.0D;

            if (Math.abs(deltaX) >= SWIPE_HORIZONTAL_THRESHOLD && Math.abs(deltaX) > Math.abs(deltaY)) {
                currentSwipeOffset = 0.0D;
                targetSwipeOffset = 0.0D;

                if (deltaX < 0) {
                    switchToPreviousRecentApp();
                } else {
                    switchToNextRecentApp();
                }
                return true;
            }

            if (deltaY >= SWIPE_SWITCHER_THRESHOLD) {
                currentSwipeOffset = 0.0D;
                targetSwipeOffset = 0.0D;
                openAppSwitcher();
                return true;
            }

            if (deltaY >= SWIPE_HOME_THRESHOLD) {
                currentSwipeOffset = 0.0D;
                targetSwipeOffset = 0.0D;
                handleSwipeUpShort();
                return true;
            }

            targetSwipeOffset = 0.0D;
            return true;
        }

        return getCurrentAppInstance().mouseReleased(this, mouseX, mouseY, button)
                || super.mouseReleased(mouseX, mouseY, button);
    }

    @Override
    public boolean mouseScrolled(double mouseX, double mouseY, double scrollDelta) {
        if (animator.isTransitionRunning()) {
            return true;
        }

        if (appSwitcherOverlay.isActive()) {
            appSwitcherOverlay.scrollBy(scrollDelta);
            return true;
        }

        return getCurrentAppInstance().mouseScrolled(this, mouseX, mouseY, scrollDelta)
                || super.mouseScrolled(mouseX, mouseY, scrollDelta);
    }

    private void handleSwipeUpShort() {
        ItemStack stack = getPhoneStack();

        if (currentApp == PhoneAppId.PASSCODE_SCREEN) {
            return;
        }

        if (currentApp == PhoneAppId.LOCK_SCREEN) {
            if (PhoneData.hasPassword(stack)) {
                if (minecraft != null
                        && minecraft.player != null
                        && PhoneData.canUnlockWithFaceId(stack, minecraft.player)) {
                    unlockPhone();
                } else {
                    openPasscodeScreen();
                }
                return;
            }

            unlockPhone();
            return;
        }

        if (sessionAuthenticated && currentApp != PhoneAppId.HOME) {
            goHomeToAppIcon();
        }
    }

    @Override
    public boolean keyPressed(int keyCode, int scanCode, int modifiers) {
        if (animator.isTransitionRunning()) {
            return true;
        }

        if (appSwitcherOverlay.isActive()) {
            if (keyCode == GLFW.GLFW_KEY_LEFT) {
                appSwitcherOverlay.selectPrevious();
                return true;
            }

            if (keyCode == GLFW.GLFW_KEY_RIGHT) {
                appSwitcherOverlay.selectNext();
                return true;
            }

            if (keyCode == GLFW.GLFW_KEY_ENTER || keyCode == GLFW.GLFW_KEY_KP_ENTER) {
                openSelectedSwitcherApp();
                return true;
            }

            if (keyCode == GLFW.GLFW_KEY_ESCAPE) {
                closeAppSwitcher();
                return true;
            }

            return true;
        }

        return getCurrentAppInstance().keyPressed(this, keyCode, scanCode, modifiers)
                || super.keyPressed(keyCode, scanCode, modifiers);
    }

    public boolean isInside(double mouseX, double mouseY, int x, int y, int width, int height) {
        return mouseX >= x && mouseX < x + width && mouseY >= y && mouseY < y + height;
    }

    public int getPhoneX() {
        return phoneX;
    }

    public int getPhoneY() {
        return phoneY;
    }

    public int getPhoneWidth() {
        return PHONE_WIDTH;
    }

    public int getPhoneHeight() {
        return PHONE_HEIGHT;
    }

    public int getPhoneCenterX() {
        return phoneX + PHONE_WIDTH / 2;
    }

    public int getPhoneCenterY() {
        return phoneY + PHONE_HEIGHT / 2;
    }

    public int getContentX() {
        return phoneX + PHONE_INNER_PADDING;
    }

    public int getContentY() {
        return phoneY + PHONE_INNER_PADDING;
    }

    public int getContentWidth() {
        return PHONE_WIDTH - PHONE_INNER_PADDING * 2;
    }

    public int getContentHeight() {
        return PHONE_HEIGHT - PHONE_INNER_PADDING * 2;
    }

    public int getSafeLeft() {
        return phoneX + SAFE_LEFT;
    }

    public int getSafeRight() {
        return phoneX + PHONE_WIDTH - SAFE_RIGHT;
    }

    public int getSafeTop() {
        return phoneY + SAFE_TOP;
    }

    public int getSafeBottom() {
        return phoneY + PHONE_HEIGHT - SAFE_BOTTOM;
    }

    public int getSafeWidth() {
        return getSafeRight() - getSafeLeft();
    }

    public int getSafeHeight() {
        return getSafeBottom() - getSafeTop();
    }

    public Font getPhoneFont() {
        return font;
    }
}