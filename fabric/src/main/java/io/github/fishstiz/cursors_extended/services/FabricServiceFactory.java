package io.github.fishstiz.cursors_extended.services;

import io.github.fishstiz.cursors_extended.CursorsExtended;

public class FabricServiceFactory implements ServiceFactory {
    @Override
    public PlatformHelper createPlatformHelper() {
        return new FabricPlatformHelper();
    }

    @Override
    public SDLMouseOpsHandler createSDLMouseOpsHandler() {
        return CursorsExtended.CONFIG.isWorkaroundsEnabled() && CursorsExtended.CONFIG.isWorkaroundsApplied()
                ? new SDLMouseOpsCompat()
                : new SDLMouseOpsHandler() {};
    }
}
