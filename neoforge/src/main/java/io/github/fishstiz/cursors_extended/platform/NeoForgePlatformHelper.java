package io.github.fishstiz.cursors_extended.platform;

import io.github.fishstiz.cursors_extended.platform.services.GuiGraphicsHelper;
import io.github.fishstiz.cursors_extended.platform.services.PlatformHelper;
import net.neoforged.fml.loading.FMLLoader;
import net.neoforged.fml.loading.FMLPaths;

import java.nio.file.Path;

public class NeoForgePlatformHelper implements PlatformHelper {
    @Override
    public boolean isDevelopmentEnvironment() {
        return !FMLLoader.getCurrent().isProduction();
    }

    @Override
    public Path getConfigDir() {
        return FMLPaths.CONFIGDIR.get();
    }

    @Override
    public GuiGraphicsHelper guiGraphicsHelper() {
        return NeoForgeGuiGraphicsHelper.INSTANCE;
    }
}
