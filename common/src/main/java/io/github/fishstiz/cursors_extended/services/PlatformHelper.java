package io.github.fishstiz.cursors_extended.services;

import java.nio.file.Path;

public interface PlatformHelper {
    PlatformHelper INSTANCE = ServiceFactory.INSTANCE.createPlatformHelper();

    boolean isDevelopmentEnvironment();

    Path getConfigDir();

    default String mapClassName(String namespace, String className) {
        return className;
    }

    default String unmapClassName(String namespace, String className) {
        return className;
    }
}
