package io.github.fishstiz.cursors_extended.services;

import net.fabricmc.loader.api.FabricLoader;

import java.nio.file.Path;

final class FabricPlatformHelper implements PlatformHelper {
    @Override
    public boolean isDevelopmentEnvironment() {
        return FabricLoader.getInstance().isDevelopmentEnvironment();
    }

    @Override
    public Path getConfigDir() {
        return FabricLoader.getInstance().getConfigDir();
    }
}
