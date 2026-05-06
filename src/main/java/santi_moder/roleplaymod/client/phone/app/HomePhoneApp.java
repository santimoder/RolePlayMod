package santi_moder.roleplaymod.client.phone.app;

import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.world.item.ItemStack;
import santi_moder.roleplaymod.client.phone.animation.PhoneTransitionContext;
import santi_moder.roleplaymod.client.phone.ui.component.PhoneIconGrid;
import santi_moder.roleplaymod.client.screen.PhoneScreen;
import santi_moder.roleplaymod.common.phone.PhoneAppId;
import santi_moder.roleplaymod.common.phone.PhoneData;

import java.util.ArrayList;
import java.util.List;

public class HomePhoneApp extends AbstractPhoneApp {

    private static final int GRID_TOP_OFFSET = 28;
    private static final int GRID_LEFT_OFFSET = 14;

    private static final int GRID_COLUMNS = 4;
    private static final int GRID_SPACING_X = 8;
    private static final int GRID_SPACING_Y = 16;

    private static final int DOCK_MARGIN_X = 12;
    private static final int DOCK_TOP_OFFSET = 48;
    private static final int DOCK_BOTTOM_OFFSET = 10;

    private static final int DOCK_ICON_OFFSET_X = 8;
    private static final int DOCK_ICON_GAP = 18;

    private static final int COLOR_DOCK = 0x66333344;

    private static final GridAppSpec[] GRID_APPS = {
            new GridAppSpec(PhoneAppId.FACETIME, "Face"),
            new GridAppSpec(PhoneAppId.CALENDAR, "Cal"),
            new GridAppSpec(PhoneAppId.CALCULATOR, "Calc"),
            new GridAppSpec(PhoneAppId.NOTES, "Notas"),

            new GridAppSpec(PhoneAppId.CONTACTS, "Cont"),
            new GridAppSpec(PhoneAppId.PHOTOS, "Fotos"),
            new GridAppSpec(PhoneAppId.MAPS, "Maps"),
            new GridAppSpec(PhoneAppId.SAFARI, "Safari"),

            new GridAppSpec(PhoneAppId.WEATHER, "Clima"),
            new GridAppSpec(PhoneAppId.CLOCK, "Reloj"),
            new GridAppSpec(PhoneAppId.APP_STORE, "Store"),
            new GridAppSpec(PhoneAppId.SETTINGS, "Config"),

            new GridAppSpec(PhoneAppId.WHATSAPP, "Wsp"),
            new GridAppSpec(PhoneAppId.INSTAGRAM, "Insta"),
            new GridAppSpec(PhoneAppId.TWITTER, "Tw"),
            new GridAppSpec(PhoneAppId.SPOTIFY, "Spot")
    };

    private static final GridAppSpec[] DOCK_APPS = {
            new GridAppSpec(PhoneAppId.PHONE, "Tel"),
            new GridAppSpec(PhoneAppId.MESSAGES, "Msg"),
            new GridAppSpec(PhoneAppId.CAMERA, "Cam")
    };

    private PhoneIconGrid mainGrid;
    private PhoneIconGrid dockGrid;

    public HomePhoneApp() {
        super(PhoneAppId.HOME);
    }

    @Override
    public void onOpen(PhoneScreen screen, ItemStack phoneStack) {
        PhoneData.initializeIfMissing(phoneStack);
        rebuildLayout(screen, phoneStack);
    }

    @Override
    public void onResize(PhoneScreen screen, int width, int height) {
        rebuildLayout(screen, screen.getPhoneStack());
    }

    @Override
    public void render(PhoneScreen screen, GuiGraphics guiGraphics, int mouseX, int mouseY, float partialTick) {
        ItemStack stack = screen.getPhoneStack();
        PhoneData.initializeIfMissing(stack);
        rebuildLayout(screen, stack);

        renderDockBackground(screen, guiGraphics);

        if (mainGrid != null) {
            mainGrid.render(screen, guiGraphics, mouseX, mouseY);
        }

        if (dockGrid != null) {
            dockGrid.render(screen, guiGraphics, mouseX, mouseY);
        }
    }

    private void rebuildLayout(PhoneScreen screen, ItemStack stack) {
        int iconSize = resolveIconSize(stack);

        mainGrid = new PhoneIconGrid(
                getGridStartX(screen, iconSize),
                screen.getPhoneY() + GRID_TOP_OFFSET,
                GRID_COLUMNS,
                iconSize,
                GRID_SPACING_X,
                GRID_SPACING_Y
        );

        List<PhoneIconGrid.IconEntry> mainEntries = new ArrayList<>();
        for (GridAppSpec app : GRID_APPS) {
            if (PhoneData.isAppInstalled(stack, app.appId())) {
                mainEntries.add(new PhoneIconGrid.IconEntry(app.appId(), app.label()));
            }
        }
        mainGrid.setEntries(mainEntries);

        List<PhoneIconGrid.IconEntry> dockEntries = new ArrayList<>();
        for (GridAppSpec app : DOCK_APPS) {
            if (PhoneData.isAppInstalled(stack, app.appId())) {
                dockEntries.add(new PhoneIconGrid.IconEntry(app.appId(), app.label()));
            }
        }

        dockGrid = new PhoneIconGrid(
                getDockStartX(screen, iconSize, dockEntries.size()),
                screen.getPhoneY() + screen.getPhoneHeight() - 42,
                Math.max(1, dockEntries.size()),
                iconSize,
                DOCK_ICON_GAP,
                0
        );

        dockGrid.setEntries(dockEntries);
    }

    private int getGridStartX(PhoneScreen screen, int iconSize) {
        int totalWidth = GRID_COLUMNS * iconSize + (GRID_COLUMNS - 1) * GRID_SPACING_X;
        return screen.getPhoneX() + (screen.getPhoneWidth() - totalWidth) / 2;
    }

    private int getDockStartX(PhoneScreen screen, int iconSize, int dockCount) {
        int totalWidth = dockCount * iconSize + Math.max(0, dockCount - 1) * DOCK_ICON_GAP;
        return screen.getPhoneX() + (screen.getPhoneWidth() - totalWidth) / 2;
    }

    private void renderDockBackground(PhoneScreen screen, GuiGraphics guiGraphics) {
        int x1 = screen.getPhoneX() + DOCK_MARGIN_X;
        int y1 = screen.getPhoneY() + screen.getPhoneHeight() - DOCK_TOP_OFFSET;
        int x2 = screen.getPhoneX() + screen.getPhoneWidth() - DOCK_MARGIN_X;
        int y2 = screen.getPhoneY() + screen.getPhoneHeight() - DOCK_BOTTOM_OFFSET;

        guiGraphics.fill(x1, y1, x2, y2, COLOR_DOCK);
    }

    private int resolveIconSize(ItemStack stack) {
        String iconSize = PhoneData.getIconSize(stack);

        if (PhoneData.SIZE_SMALL.equals(iconSize)) {
            return 20;
        }
        if (PhoneData.SIZE_LARGE.equals(iconSize)) {
            return 28;
        }
        return 24;
    }

    @Override
    public boolean mouseClicked(PhoneScreen screen, double mouseX, double mouseY, int button) {
        if (button != 0 || mainGrid == null || dockGrid == null) {
            return false;
        }

        PhoneIconGrid.ClickedIcon clicked = mainGrid.getClickedIcon(screen, mouseX, mouseY);
        if (clicked == null) {
            clicked = dockGrid.getClickedIcon(screen, mouseX, mouseY);
        }

        if (clicked != null) {
            ItemStack stack = screen.getPhoneStack();

            if (!PhoneData.isAppInstalled(stack, clicked.appId())) {
                return true;
            }

            screen.openAppFromIcon(
                    clicked.appId(),
                    new PhoneTransitionContext(clicked.x(), clicked.y(), clicked.width(), clicked.height())
            );
            return true;
        }

        return false;
    }

    private record GridAppSpec(PhoneAppId appId, String label) {
    }
}