package santi_moder.roleplaymod.client.phone.overlay;

import net.minecraft.client.gui.GuiGraphics;
import santi_moder.roleplaymod.client.phone.ui.PhoneUi;
import santi_moder.roleplaymod.client.screen.PhoneScreen;
import santi_moder.roleplaymod.common.phone.PhoneAppId;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

public final class PhoneAppSwitcherOverlay {

    private static final int BACKDROP_COLOR = 0xEE000000;

    private static final int CARD_COLOR = 0xEE16161F;
    private static final int CARD_SELECTED_COLOR = 0xF0222230;
    private static final int CARD_PREVIEW_COLOR = 0x33000000;

    private static final int CARD_WIDTH = 102;
    private static final int CARD_HEIGHT = 154;

    /**
     * Stack hacia la izquierda:
     * - card enfocada al centro
     * - siguientes más viejas un poco a la izquierda
     */
    private static final int STACK_OFFSET_X = 24;
    private static final int STACK_OFFSET_Y = 7;

    private static final int CARD_TOP_MARGIN = 42;
    private static final int CARD_CORNER_RIGHT_WIDTH = 30;
    private static final int CARD_HEADER_HEIGHT = 16;

    private static final int ICON_BOX_SIZE = 10;
    private static final int ICON_BOX_COLOR = 0xFF4A90E2;

    private static final int DRAG_THRESHOLD = 6;
    private static final int MAX_VISIBLE_DEPTH = 3;

    private static final double DRAG_PIXELS_PER_CARD = 82.0D;
    private static final double WHEEL_STEP = 1.0D;

    /**
     * Qué tan lejos del foco permitimos renderizar.
     * - 0.0 = card centrada
     * - 1.0 = siguiente más vieja
     * - 2.0 = tercera visible
     */
    private static final double MIN_RENDER_RELATIVE = -0.45D;
    private static final double MAX_RENDER_RELATIVE = 2.45D;
    private final List<PhoneAppId> cachedApps = new ArrayList<>();
    private final PhoneAppSwitcherMotion motion = new PhoneAppSwitcherMotion();
    private boolean active;
    private int selectedIndex;
    private boolean pointerDown;
    private boolean dragging;
    private double pressStartX;
    private double pressStartPosition;

    public void open(List<PhoneAppId> recentApps, PhoneAppId currentApp) {
        cachedApps.clear();
        cachedApps.addAll(recentApps);

        active = !cachedApps.isEmpty();

        if (!active) {
            selectedIndex = 0;
            resetInteractionState();
            motion.resetTo(0.0D, 0.0D);
            return;
        }

        int currentIndex = cachedApps.indexOf(currentApp);
        selectedIndex = currentIndex >= 0 ? currentIndex : 0;
        selectedIndex = clampIndex(selectedIndex);

        motion.resetTo(selectedIndex, selectedIndex);
        resetInteractionState();
    }

    public void close() {
        active = false;
        cachedApps.clear();
        selectedIndex = 0;
        resetInteractionState();
        motion.resetTo(0.0D, 0.0D);
    }

    public void tick() {
        if (!active) {
            return;
        }

        if (!dragging) {
            motion.tick();
        }

        selectedIndex = clampIndex((int) Math.round(motion.getScrollPosition()));
    }

    public boolean isActive() {
        return active;
    }

    public PhoneAppId getSelectedApp() {
        if (!active || cachedApps.isEmpty()) {
            return null;
        }

        int index = clampIndex(selectedIndex);
        if (index < 0 || index >= cachedApps.size()) {
            return null;
        }

        return cachedApps.get(index);
    }

    public void selectPrevious() {
        if (!active || cachedApps.isEmpty()) {
            return;
        }

        motion.moveTargetBy(-1.0D, 0.0D, maxIndexDouble());
        selectedIndex = clampIndex((int) Math.round(motion.getTargetScrollPosition()));
    }

    public void selectNext() {
        if (!active || cachedApps.isEmpty()) {
            return;
        }

        motion.moveTargetBy(1.0D, 0.0D, maxIndexDouble());
        selectedIndex = clampIndex((int) Math.round(motion.getTargetScrollPosition()));
    }

    public void scrollBy(double scrollDelta) {
        if (!active || cachedApps.isEmpty() || scrollDelta == 0.0D) {
            return;
        }

        // Ajustado para que avanzar en el switcher te lleve a apps más viejas.
        if (scrollDelta < 0.0D) {
            motion.moveTargetBy(WHEEL_STEP, 0.0D, maxIndexDouble());
        } else {
            motion.moveTargetBy(-WHEEL_STEP, 0.0D, maxIndexDouble());
        }

        selectedIndex = clampIndex((int) Math.round(motion.getTargetScrollPosition()));
    }

    public boolean beginPointer(double mouseX, double mouseY, PhoneScreen screen) {
        if (!active || !isInsideCards(screen, mouseX, mouseY)) {
            return false;
        }

        pointerDown = true;
        dragging = false;
        pressStartX = mouseX;
        pressStartPosition = motion.getScrollPosition();
        return true;
    }

    public void dragTo(double mouseX) {
        if (!active || !pointerDown) {
            return;
        }

        double dragDistance = mouseX - pressStartX;

        if (Math.abs(dragDistance) >= DRAG_THRESHOLD) {
            dragging = true;
        }

        if (!dragging) {
            return;
        }

        double deltaCards = dragDistance / DRAG_PIXELS_PER_CARD;
        double newPosition = pressStartPosition + deltaCards;

        motion.setImmediatePosition(clampDouble(newPosition, 0.0D, maxIndexDouble()));
        selectedIndex = clampIndex((int) Math.round(motion.getScrollPosition()));
    }

    public boolean isDragging() {
        return dragging;
    }

    public boolean wasPointerDown() {
        return pointerDown;
    }

    public PhoneAppId releasePointer(PhoneScreen screen, double mouseX, double mouseY) {
        if (!active || !pointerDown) {
            return null;
        }

        boolean releasedInsideCards = isInsideCards(screen, mouseX, mouseY);
        PhoneAppId clickedApp = null;

        if (dragging) {
            int snappedIndex = clampIndex((int) Math.round(motion.getScrollPosition()));
            motion.setTargetPosition(snappedIndex, 0.0D, maxIndexDouble());
            selectedIndex = snappedIndex;
        } else if (releasedInsideCards) {
            clickedApp = getClickedApp(screen, mouseX, mouseY);
        }

        resetInteractionState();
        return clickedApp;
    }

    public void render(PhoneScreen screen, GuiGraphics guiGraphics, int mouseX, int mouseY, float partialTick) {
        if (!active) {
            return;
        }

        guiGraphics.fill(
                screen.getPhoneX() + 4,
                screen.getPhoneY() + 4,
                screen.getPhoneX() + screen.getPhoneWidth() - 4,
                screen.getPhoneY() + screen.getPhoneHeight() - 4,
                BACKDROP_COLOR
        );

        List<CardLayout> cards = buildCardLayouts(screen);

        // Fondo -> frente
        cards.sort(Comparator.comparingDouble((CardLayout card) -> card.relative()).reversed());

        for (CardLayout card : cards) {
            PhoneAppId appId = cachedApps.get(card.appIndex());

            boolean selected = Math.abs(card.relative()) < 0.5D;
            boolean hover = !dragging && screen.isInside(mouseX, mouseY, card.x(), card.y(), card.width(), card.height());

            int color = selected ? CARD_SELECTED_COLOR : CARD_COLOR;
            if (hover && !selected) {
                color = 0xEE1D1D29;
            }

            guiGraphics.fill(card.x(), card.y(), card.x() + card.width(), card.y() + card.height(), color);

            renderCardHeader(screen, guiGraphics, card, appId);
            renderCardPreview(guiGraphics, card);
        }
    }

    public PhoneAppId getClickedApp(PhoneScreen screen, double mouseX, double mouseY) {
        if (!active || cachedApps.isEmpty()) {
            return null;
        }

        List<CardLayout> cards = buildCardLayouts(screen);

        // Frente -> fondo
        cards.sort(Comparator.comparingDouble(CardLayout::relative));

        for (CardLayout card : cards) {
            if (screen.isInside(mouseX, mouseY, card.x(), card.y(), card.width(), card.height())) {
                selectedIndex = clampIndex(card.appIndex());
                motion.setTargetPosition(selectedIndex, 0.0D, maxIndexDouble());
                return cachedApps.get(card.appIndex());
            }
        }

        return null;
    }

    public boolean isInsideCards(PhoneScreen screen, double mouseX, double mouseY) {
        if (!active || cachedApps.isEmpty()) {
            return false;
        }

        List<CardLayout> cards = buildCardLayouts(screen);

        for (CardLayout card : cards) {
            if (screen.isInside(mouseX, mouseY, card.x(), card.y(), card.width(), card.height())) {
                return true;
            }
        }

        return false;
    }

    private List<CardLayout> buildCardLayouts(PhoneScreen screen) {
        List<CardLayout> layouts = new ArrayList<>();

        if (cachedApps.isEmpty()) {
            return layouts;
        }

        int baseX = screen.getPhoneCenterX() - (CARD_WIDTH / 2);
        int baseY = screen.getPhoneY() + CARD_TOP_MARGIN;

        int visibleCount = 0;
        double scrollPosition = motion.getScrollPosition();

        for (int appIndex = 0; appIndex < cachedApps.size(); appIndex++) {
            double relative = appIndex - scrollPosition;

            if (relative < MIN_RENDER_RELATIVE || relative > MAX_RENDER_RELATIVE) {
                continue;
            }

            if (relative >= 0.0D && visibleCount >= MAX_VISIBLE_DEPTH) {
                continue;
            }

            int x = baseX - (int) Math.round(relative * STACK_OFFSET_X * 2.3D);
            int y = baseY + (int) Math.round(Math.max(0.0D, relative) * STACK_OFFSET_Y * 1.25D);

            x = clampInt(
                    x,
                    screen.getContentX() + 4,
                    screen.getContentX() + screen.getContentWidth() - CARD_WIDTH - 4
            );

            layouts.add(new CardLayout(x, y, CARD_WIDTH, CARD_HEIGHT, appIndex, relative));

            if (relative >= 0.0D) {
                visibleCount++;
            }
        }

        return layouts;
    }

    private void renderCardHeader(PhoneScreen screen, GuiGraphics guiGraphics, CardLayout card, PhoneAppId appId) {
        int rightX = card.x() + card.width() - CARD_CORNER_RIGHT_WIDTH;
        int topY = card.y() + 6;

        guiGraphics.fill(
                rightX - 4,
                topY - 2,
                card.x() + card.width() - 6,
                topY + CARD_HEADER_HEIGHT,
                0x22000000
        );

        int iconX = rightX;
        int iconY = topY + 1;

        guiGraphics.fill(
                iconX,
                iconY,
                iconX + ICON_BOX_SIZE,
                iconY + ICON_BOX_SIZE,
                ICON_BOX_COLOR
        );

        guiGraphics.drawString(
                screen.getPhoneFont(),
                shortenName(appId.getDisplayName(), 8),
                iconX + ICON_BOX_SIZE + 4,
                topY,
                PhoneUi.COLOR_TEXT,
                false
        );
    }

    private void renderCardPreview(GuiGraphics guiGraphics, CardLayout card) {
        int x1 = card.x() + 8;
        int y1 = card.y() + 26;
        int x2 = card.x() + card.width() - 8;
        int y2 = card.y() + card.height() - 10;

        guiGraphics.fill(x1, y1, x2, y2, CARD_PREVIEW_COLOR);
    }

    private void resetInteractionState() {
        pointerDown = false;
        dragging = false;
        pressStartX = 0.0D;
        pressStartPosition = 0.0D;
    }

    private int clampIndex(int value) {
        if (cachedApps.isEmpty()) {
            return 0;
        }
        return Math.max(0, Math.min(cachedApps.size() - 1, value));
    }

    private int clampInt(int value, int min, int max) {
        return Math.max(min, Math.min(max, value));
    }

    private double clampDouble(double value, double min, double max) {
        return Math.max(min, Math.min(max, value));
    }

    private double maxIndexDouble() {
        return Math.max(0.0D, cachedApps.size() - 1.0D);
    }

    private String shortenName(String value, int maxLength) {
        if (value == null || value.isEmpty()) {
            return "";
        }

        if (value.length() <= maxLength) {
            return value;
        }

        return value.substring(0, maxLength);
    }

    private record CardLayout(int x, int y, int width, int height, int appIndex, double relative) {
    }
}