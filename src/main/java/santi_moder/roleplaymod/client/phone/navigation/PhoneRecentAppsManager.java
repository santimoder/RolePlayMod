package santi_moder.roleplaymod.client.phone.navigation;

import santi_moder.roleplaymod.common.phone.PhoneAppId;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;

public final class PhoneRecentAppsManager {

    private static final int MAX_RECENTS = 8;

    private final LinkedList<PhoneAppId> recentApps = new LinkedList<>();

    public void onAppBecameForeground(PhoneAppId appId) {
        if (!isTrackedApp(appId)) {
            return;
        }

        recentApps.remove(appId);
        recentApps.addFirst(appId);
        trimToMaxSize();
    }

    public List<PhoneAppId> getRecentApps() {
        return new ArrayList<>(recentApps);
    }

    public PhoneAppId getPreviousRecent(PhoneAppId currentApp) {
        int index = recentApps.indexOf(currentApp);
        if (index == -1) {
            return recentApps.isEmpty() ? null : recentApps.getFirst();
        }

        int target = index + 1;
        if (target >= recentApps.size()) {
            return null;
        }

        return recentApps.get(target);
    }

    public PhoneAppId getNextRecent(PhoneAppId currentApp) {
        int index = recentApps.indexOf(currentApp);
        if (index <= 0) {
            return null;
        }

        return recentApps.get(index - 1);
    }

    public boolean hasRecents() {
        return !recentApps.isEmpty();
    }

    public boolean contains(PhoneAppId appId) {
        return recentApps.contains(appId);
    }

    public void remove(PhoneAppId appId) {
        recentApps.remove(appId);
    }

    public void clear() {
        recentApps.clear();
    }

    private boolean isTrackedApp(PhoneAppId appId) {
        if (appId == null) {
            return false;
        }

        return appId.isVisibleOnHome();
    }

    private void trimToMaxSize() {
        while (recentApps.size() > MAX_RECENTS) {
            recentApps.removeLast();
        }
    }
}