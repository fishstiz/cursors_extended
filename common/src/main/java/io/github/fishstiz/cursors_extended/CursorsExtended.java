package io.github.fishstiz.cursors_extended;

import com.mojang.blaze3d.platform.cursor.CursorType;
import com.mojang.blaze3d.platform.cursor.CursorTypes;
import io.github.fishstiz.cursors_extended.config.Config;
import io.github.fishstiz.cursors_extended.cursor.CursorDisplay;
import io.github.fishstiz.cursors_extended.cursor.CursorRegistry;
import io.github.fishstiz.cursors_extended.cursor.CursorTypesExt;
import io.github.fishstiz.cursors_extended.lifecycle.ClientStartedListener;
import io.github.fishstiz.cursors_extended.resource.CursorTextureLoader;
import net.minecraft.client.Minecraft;
import net.minecraft.resources.ResourceLocation;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public final class CursorsExtended implements ClientStartedListener {
    private static final CursorsExtended INSTANCE = new CursorsExtended();
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

        registry.register(CursorType.DEFAULT);
        registry.registerAlias(CursorType.DEFAULT, CursorTypes.ARROW);
        registry.register(CursorTypes.POINTING_HAND);
        registry.register(CursorTypesExt.GRABBING);
        registry.register(CursorTypes.IBEAM);
        registry.register(CursorTypesExt.SHIFT);
        registry.register(CursorTypesExt.BUSY);
        registry.register(CursorTypes.NOT_ALLOWED);
        registry.register(CursorTypes.CROSSHAIR);
        registry.register(CursorTypes.RESIZE_ALL);
        registry.register(CursorTypes.RESIZE_EW);
        registry.register(CursorTypes.RESIZE_NS);
        registry.register(CursorTypesExt.RESIZE_NWSE);
        registry.register(CursorTypesExt.RESIZE_NESW);
    }

    public static CursorsExtended getInstance() {
        return INSTANCE;
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

    public static ResourceLocation loc(String path) {
        return ResourceLocation.fromNamespaceAndPath(MOD_ID, path);
    }
}
