package santi_moder.roleplaymod.client.phone.ui.layout;

import santi_moder.roleplaymod.client.screen.PhoneScreen;
import santi_moder.roleplaymod.client.phone.animation.PhoneTransitionContext;
import santi_moder.roleplaymod.common.phone.PhoneAppId;
import santi_moder.roleplaymod.common.phone.PhoneData;

import java.util.HashMap;
import java.util.Map;

public final class PhoneHomeIconLayoutResolver {

    private static final int GRID_TOP_OFFSET = 54;
    private static final int GRID_LEFT_OFFSET = 14;

    private static final int GRID_COLUMNS = 4;
    private static final int GRID_SPACING_X = 8;
    private static final int GRID_SPACING_Y = 16;

    private static final int DOCK_ICON_OFFSET_X = 8;
    private static final int DOCK_ICON_GAP = 18;

    private static final PhoneAppId[] GRID_APPS = {
            PhoneAppId.FACETIME,
            PhoneAppId.CALENDAR,
            PhoneAppId.CALCULATOR,
            PhoneAppId.NOTES,

            PhoneAppId.CONTACTS,
            PhoneAppId.PHOTOS,
            PhoneAppId.MAPS,
            PhoneAppId.INSTAGRAM,

            PhoneAppId.TWITTER,
            PhoneAppId.SPOTIFY,
            PhoneAppId.WEATHER,
            PhoneAppId.CLOCK,

            PhoneAppId.APP_STORE,
            PhoneAppId.SETTINGS,
            PhoneAppId.WHATSAPP
    };

    private static final PhoneAppId[] DOCK_APPS = {
            PhoneAppId.PHONE,
            PhoneAppId.MESSAGES,
            PhoneAppId.CAMERA
    };

    private PhoneHomeIconLayoutResolver() {
    }

    public static PhoneTransitionContext resolveIconContext(PhoneScreen screen, PhoneAppId appId) {
        if (appId == null) {
            return PhoneTransitionContext.empty();
        }

        int iconSize = resolveIconSize(screen);

        Map<PhoneAppId, PhoneTransitionContext> positions = buildLayout(screen, iconSize);
        return positions.getOrDefault(appId, PhoneTransitionContext.empty());
    }

    private static Map<PhoneAppId, PhoneTransitionContext> buildLayout(PhoneScreen screen, int iconSize) {
        Map<PhoneAppId, PhoneTransitionContext> map = new HashMap<>();

        int gridStartX = screen.getPhoneX() + GRID_LEFT_OFFSET;
        int gridStartY = screen.getPhoneY() + GRID_TOP_OFFSET;

        for (int i = 0; i < GRID_APPS.length; i++) {
            int col = i % GRID_COLUMNS;
            int row = i / GRID_COLUMNS;

            int x = gridStartX + col * (iconSize + GRID_SPACING_X);
            int y = gridStartY + row * (iconSize + GRID_SPACING_Y);

            map.put(GRID_APPS[i], new PhoneTransitionContext(x, y, iconSize, iconSize));
        }

        int dockStartX = screen.getPhoneX() + GRID_LEFT_OFFSET + DOCK_ICON_OFFSET_X;
        int dockY = screen.getPhoneY() + screen.getPhoneHeight() - 42;

        for (int i = 0; i < DOCK_APPS.length; i++) {
            int x = dockStartX + i * (iconSize + DOCK_ICON_GAP);
            map.put(DOCK_APPS[i], new PhoneTransitionContext(x, dockY, iconSize, iconSize));
        }

        return map;
    }

    private static int resolveIconSize(PhoneScreen screen) {
        String iconSize = PhoneData.getIconSize(screen.getPhoneStack());

        if (PhoneData.SIZE_SMALL.equals(iconSize)) {
            return 20;
        }
        if (PhoneData.SIZE_LARGE.equals(iconSize)) {
            return 28;
        }
        return 24;
    }
}