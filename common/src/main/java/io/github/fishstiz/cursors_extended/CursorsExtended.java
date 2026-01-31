package io.github.fishstiz.cursors_extended;

import io.github.fishstiz.cursors_extended.config.Config;
import io.github.fishstiz.cursors_extended.cursor.CursorDisplay;
import io.github.fishstiz.cursors_extended.cursor.CursorRegistry;
import io.github.fishstiz.cursors_extended.resource.CursorTextureLoader;
import net.minecraft.client.Minecraft;
import net.minecraft.resources.Identifier;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public final class CursorsExtended {
    public static final String MOD_ID = "cursors_extended";
    public static final Logger LOGGER = LoggerFactory.getLogger(MOD_ID);
    public static final Config CONFIG = Config.load();
    private final CursorRegistry registry = new CursorRegistry();
    private final CursorDisplay display = new CursorDisplay(registry);
    private final CursorTextureLoader textureLoader = new CursorTextureLoader(registry);

    private CursorsExtended() {
    }

    public void onClientStarted(Minecraft minecraft) {
        textureLoader.onClientStarted(minecraft);
        display.onClientStarted(minecraft);
    }

    private static final class Holder {
        private static final CursorsExtended INSTANCE = new CursorsExtended();
    }

    public static CursorsExtended getInstance() {
        return Holder.INSTANCE;
    }

    public CursorRegistry getRegistry() {
        return registry;
    }

    public CursorTextureLoader getLoader() {
        return textureLoader;
    }

    public CursorDisplay getDisplay() {
        return display;
    }

    public static Identifier id(String path) {
        return Identifier.fromNamespaceAndPath(MOD_ID, path);
    }
}
