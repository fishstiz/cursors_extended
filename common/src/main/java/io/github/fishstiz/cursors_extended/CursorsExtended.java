package io.github.fishstiz.cursors_extended;

import com.mojang.blaze3d.platform.cursor.CursorType;
import com.mojang.blaze3d.platform.cursor.CursorTypes;
import io.github.fishstiz.cursors_extended.config.Config;
import io.github.fishstiz.cursors_extended.config.ConfigLoader;
import io.github.fishstiz.cursors_extended.cursor.CursorManager;
import io.github.fishstiz.cursors_extended.cursor.CursorTypesExt;
import io.github.fishstiz.cursors_extended.platform.Services;
import net.minecraft.resources.ResourceLocation;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public final class CursorsExtended {
    public static final String MOD_ID = "cursors_extended";
    public static final Logger LOGGER = LoggerFactory.getLogger(MOD_ID);
    public static final Config CONFIG = ConfigLoader.load(Services.PLATFORM.getConfigDir().resolve(MOD_ID + ".json").toFile());

    private CursorsExtended() {
    }

    static void init() {
        CursorManager.INSTANCE.registerType(CursorType.DEFAULT);
        CursorManager.INSTANCE.registerType(CursorTypes.POINTING_HAND);
        CursorManager.INSTANCE.registerType(CursorTypesExt.GRABBING);
        CursorManager.INSTANCE.registerType(CursorTypes.IBEAM);
        CursorManager.INSTANCE.registerType(CursorTypesExt.SHIFT);
        CursorManager.INSTANCE.registerType(CursorTypesExt.BUSY);
        CursorManager.INSTANCE.registerType(CursorTypes.NOT_ALLOWED);
        CursorManager.INSTANCE.registerType(CursorTypes.CROSSHAIR);
        CursorManager.INSTANCE.registerType(CursorTypes.RESIZE_ALL);
        CursorManager.INSTANCE.registerType(CursorTypes.RESIZE_EW);
        CursorManager.INSTANCE.registerType(CursorTypes.RESIZE_NS);
        CursorManager.INSTANCE.registerType(CursorTypesExt.RESIZE_NWSE);
        CursorManager.INSTANCE.registerType(CursorTypesExt.RESIZE_NESW);
    }

    public static ResourceLocation loc(String path) {
        return ResourceLocation.fromNamespaceAndPath(MOD_ID, path);
    }
}
