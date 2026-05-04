package santi_moder.roleplaymod.client.phone.animation;

import santi_moder.roleplaymod.common.phone.PhoneAppId;

public record PhoneTransition(PhoneAppId fromApp, PhoneAppId toApp, PhoneAnimation outgoingAnimation,
                              PhoneAnimation incomingAnimation, PhoneTransitionContext context) {

    public PhoneTransition(
            PhoneAppId fromApp,
            PhoneAppId toApp,
            PhoneAnimation outgoingAnimation,
            PhoneAnimation incomingAnimation,
            PhoneTransitionContext context
    ) {
        this.fromApp = fromApp;
        this.toApp = toApp;
        this.outgoingAnimation = outgoingAnimation;
        this.incomingAnimation = incomingAnimation;
        this.context = context == null ? PhoneTransitionContext.empty() : context;
    }

    public void tick() {
        if (outgoingAnimation != null && !outgoingAnimation.isFinished()) {
            outgoingAnimation.tick();
        }

        if (incomingAnimation != null && !incomingAnimation.isFinished()) {
            incomingAnimation.tick();
        }
    }

    public boolean isFinished() {
        boolean outgoingDone = outgoingAnimation == null || outgoingAnimation.isFinished();
        boolean incomingDone = incomingAnimation == null || incomingAnimation.isFinished();
        return outgoingDone && incomingDone;
    }
}