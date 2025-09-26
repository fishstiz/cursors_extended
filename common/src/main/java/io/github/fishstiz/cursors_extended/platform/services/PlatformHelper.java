package io.github.fishstiz.cursors_extended.platform.services;

import java.nio.file.Path;

public interface PlatformHelper {
    boolean isDevelopmentEnvironment();

    Path getConfigDir();

    GuiGraphicsHelper guiGraphicsHelper();

    default String mapClassName(String namespace, String className) {
        return className;
    }

    default String unmapClassName(String namespace, String className) {
        return className;
    }
}
