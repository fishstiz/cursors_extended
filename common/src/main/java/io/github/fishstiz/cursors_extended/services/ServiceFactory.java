package io.github.fishstiz.cursors_extended.services;

import java.util.ServiceLoader;

interface ServiceFactory {
    ServiceFactory INSTANCE = ServiceLoader.load(ServiceFactory.class).findFirst().orElseThrow();

    PlatformHelper createPlatformHelper();

    SDLMouseOpsHandler createSDLMouseOpsHandler();
}
