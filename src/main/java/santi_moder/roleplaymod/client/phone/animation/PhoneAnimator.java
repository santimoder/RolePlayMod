package santi_moder.roleplaymod.client.phone.animation;

import santi_moder.roleplaymod.common.phone.PhoneAppId;

public final class PhoneAnimator {

    private static final int FAST = 5;
    private static final int NORMAL = 7;
    private static final int UNLOCK = 6;

    private PhoneTransition activeTransition;

    public boolean isTransitionRunning() {
        return activeTransition != null;
    }

    public PhoneTransition getActiveTransition() {
        return activeTransition;
    }

    public void startTransition(
            PhoneAppId fromApp,
            PhoneAppId toApp,
            PhoneNavigationAction action,
            PhoneTransitionContext context
    ) {
        activeTransition = buildTransition(fromApp, toApp, action, context);
    }

    public void tick() {
        if (activeTransition == null) {
            return;
        }

        activeTransition.tick();

        if (activeTransition.isFinished()) {
            activeTransition = null;
        }
    }

    private PhoneTransition buildTransition(
            PhoneAppId fromApp,
            PhoneAppId toApp,
            PhoneNavigationAction action,
            PhoneTransitionContext context
    ) {
        return switch (action) {
            case OPEN_FROM_ICON -> new PhoneTransition(
                    fromApp,
                    toApp,
                    null,
                    new PhoneAnimation(PhoneAnimationType.ICON_EXPAND_IN, NORMAL),
                    context
            );

            case CLOSE_TO_HOME_ICON -> new PhoneTransition(
                    fromApp,
                    toApp,
                    new PhoneAnimation(PhoneAnimationType.ICON_COLLAPSE_OUT, NORMAL),
                    new PhoneAnimation(PhoneAnimationType.HOME_FADE_IN, NORMAL),
                    context
            );

            case BACK, SWITCH_TO_NEXT -> new PhoneTransition(
                    fromApp,
                    toApp,
                    new PhoneAnimation(PhoneAnimationType.APP_BACK_RIGHT_OUT, FAST),
                    new PhoneAnimation(PhoneAnimationType.APP_BACK_LEFT_IN, FAST),
                    context
            );

            case FORWARD, SWITCH_TO_PREVIOUS -> new PhoneTransition(
                    fromApp,
                    toApp,
                    new PhoneAnimation(PhoneAnimationType.APP_BACK_LEFT_IN, FAST),
                    new PhoneAnimation(PhoneAnimationType.APP_BACK_RIGHT_OUT, FAST),
                    context
            );

            case OPEN_PASSCODE -> new PhoneTransition(
                    fromApp,
                    toApp,
                    new PhoneAnimation(PhoneAnimationType.LOCKSCREEN_FADE_OUT, NORMAL),
                    new PhoneAnimation(PhoneAnimationType.PASSCODE_UP_IN, NORMAL),
                    context
            );

            case UNLOCK -> new PhoneTransition(
                    fromApp,
                    toApp,
                    new PhoneAnimation(PhoneAnimationType.PASSCODE_FADE_OUT, UNLOCK),
                    new PhoneAnimation(PhoneAnimationType.HOME_FADE_IN, UNLOCK),
                    context
            );

            case LOCK -> new PhoneTransition(
                    fromApp,
                    toApp,
                    null,
                    new PhoneAnimation(PhoneAnimationType.HOME_FADE_IN, FAST),
                    context
            );
        };
    }
}