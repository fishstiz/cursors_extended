package io.github.fishstiz.cursors_extended.platform;

import io.github.fishstiz.cursors_extended.platform.services.PlatformHelper;
import net.fabricmc.loader.api.FabricLoader;

import java.nio.file.Path;

public class FabricPlatformHelper implements PlatformHelper {
    @Override
    public boolean isDevelopmentEnvironment() {
        return FabricLoader.getInstance().isDevelopmentEnvironment();
    }

    @Override
    public Path getConfigDir() {
        return FabricLoader.getInstance().getConfigDir();
    }

    @Override
    public String mapClassName(String namespace, String className) {
        return FabricLoader.getInstance().getMappingResolver().mapClassName(namespace, className);
    }

    @Override
    public String unmapClassName(String namespace, String className) {
        return FabricLoader.getInstance().getMappingResolver().unmapClassName(namespace, className);
    }
}
