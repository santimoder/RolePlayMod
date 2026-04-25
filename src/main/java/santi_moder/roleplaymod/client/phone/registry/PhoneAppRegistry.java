package santi_moder.roleplaymod.client.phone.registry;

import santi_moder.roleplaymod.client.phone.app.*;
import santi_moder.roleplaymod.common.phone.PhoneAppId;

import java.util.EnumMap;
import java.util.Map;
import java.util.Objects;

public class PhoneAppRegistry {

    private final Map<PhoneAppId, AbstractPhoneApp> apps = new EnumMap<>(PhoneAppId.class);

    public PhoneAppRegistry() {
        registerCoreApps();
        registerPlaceholderApps();
    }

    private void registerCoreApps() {
        register(new LockScreenPhoneApp());
        register(new PasscodePhoneApp());
        register(new HomePhoneApp());
        register(new SettingsPhoneApp());
        register(new WhatsappPhoneApp());
    }

    private void registerPlaceholderApps() {
        registerPlaceholder(PhoneAppId.PHONE);
        registerPlaceholder(PhoneAppId.MESSAGES);
        registerPlaceholder(PhoneAppId.FACETIME);
        registerPlaceholder(PhoneAppId.CALENDAR);
        registerPlaceholder(PhoneAppId.CALCULATOR);
        registerPlaceholder(PhoneAppId.NOTES);
        registerPlaceholder(PhoneAppId.CONTACTS);
        registerPlaceholder(PhoneAppId.CAMERA);
        registerPlaceholder(PhoneAppId.WHATSAPP);
        registerPlaceholder(PhoneAppId.PHOTOS);
        registerPlaceholder(PhoneAppId.MAPS);
        registerPlaceholder(PhoneAppId.INSTAGRAM);
        registerPlaceholder(PhoneAppId.TWITTER);
        registerPlaceholder(PhoneAppId.SPOTIFY);
        registerPlaceholder(PhoneAppId.WEATHER);
        registerPlaceholder(PhoneAppId.CLOCK);
        registerPlaceholder(PhoneAppId.APP_STORE);
    }

    private void register(AbstractPhoneApp app) {
        Objects.requireNonNull(app, "app");
        apps.put(app.getAppId(), app);
    }

    private void registerPlaceholder(PhoneAppId appId) {
        if (!apps.containsKey(appId)) {
            register(new PlaceholderPhoneApp(appId));
        }
    }

    public AbstractPhoneApp get(PhoneAppId appId) {
        return apps.get(appId);
    }
}