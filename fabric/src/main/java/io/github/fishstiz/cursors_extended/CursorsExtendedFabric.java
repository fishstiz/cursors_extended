package io.github.fishstiz.cursors_extended;

import io.github.fishstiz.cursors_extended.resource.BuiltinCursorResourcePack;
import io.github.fishstiz.cursors_extended.resource.CursorTextureLoader;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientLifecycleEvents;
import net.fabricmc.fabric.api.client.screen.v1.ScreenEvents;
import net.fabricmc.fabric.api.resource.v1.ResourceLoader;
import net.fabricmc.fabric.api.resource.v1.pack.PackActivationType;
import net.fabricmc.loader.api.FabricLoader;
import net.fabricmc.loader.api.ModContainer;
import net.minecraft.server.packs.PackType;

public class CursorsExtendedFabric implements ClientModInitializer {
    @Override
    public void onInitializeClient() {
        ClientLifecycleEvents.CLIENT_STARTED.register(CursorsExtended.getInstance()::onClientStarted);
        ResourceLoader.get(PackType.CLIENT_RESOURCES).registerReloader(CursorTextureLoader.getDir(), CursorsExtended.getInstance().getLoader());
        FabricLoader.getInstance().getModContainer(CursorsExtended.MOD_ID).ifPresent(modContainer -> {
            registerCursorPack(modContainer, BuiltinCursorResourcePack.DEFAULT, PackActivationType.DEFAULT_ENABLED);
            registerCursorPack(modContainer, BuiltinCursorResourcePack.DEFAULT_AUTO, PackActivationType.NORMAL);
            registerCursorPack(modContainer, BuiltinCursorResourcePack.LEGACY, PackActivationType.NORMAL);
        });
        ScreenEvents.AFTER_INIT.register((client, currentScreen, width, height) -> {
            CursorsExtended.getInstance().getDisplay().setVisibleScreen(currentScreen);
            ScreenEvents.remove(currentScreen).register(screen -> CursorsExtended.getInstance().getDisplay().setVisibleScreen(null));
        });
    }

    private static void registerCursorPack(ModContainer mod, BuiltinCursorResourcePack pack, PackActivationType type) {
        ResourceLoader.registerBuiltinPack(pack.getLocation(), mod, pack.getDisplayName(), type);
    }
}
