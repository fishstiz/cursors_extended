package io.github.fishstiz.cursors_extended;

import io.github.fishstiz.cursors_extended.cursor.inspector.CursorProviderInspector;
import io.github.fishstiz.cursors_extended.resource.BuiltinCursorResourcePack;
import io.github.fishstiz.cursors_extended.resource.CursorResourceReloader;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientLifecycleEvents;
import net.fabricmc.fabric.api.client.screen.v1.ScreenEvents;
import net.fabricmc.fabric.api.resource.ResourceManagerHelper;
import net.fabricmc.fabric.api.resource.ResourcePackActivationType;
import net.fabricmc.fabric.api.resource.v1.ResourceLoader;
import net.fabricmc.loader.api.FabricLoader;
import net.fabricmc.loader.api.ModContainer;
import net.minecraft.client.Minecraft;
import net.minecraft.server.packs.PackType;

public class CursorsExtendedFabric implements ClientModInitializer {
    @Override
    public void onInitializeClient() {
        ClientLifecycleEvents.CLIENT_STARTED.register(client -> CursorsExtended.init());
        ResourceLoader.get(PackType.CLIENT_RESOURCES).registerReloader(CursorResourceReloader.getDirectory(), new CursorResourceReloader());
        FabricLoader.getInstance().getModContainer(CursorsExtended.MOD_ID).ifPresent(modContainer -> {
            registerCursorPack(modContainer, BuiltinCursorResourcePack.DEFAULT, ResourcePackActivationType.DEFAULT_ENABLED);
            registerCursorPack(modContainer, BuiltinCursorResourcePack.DEFAULT_AUTO, ResourcePackActivationType.NORMAL);
            registerCursorPack(modContainer, BuiltinCursorResourcePack.LEGACY, ResourcePackActivationType.NORMAL);
        });
        ScreenEvents.AFTER_INIT.register((client, currentScreen, width, height) -> {
            CursorProviderInspector.INSTANCE.setVisibleScreen(currentScreen);
            ScreenEvents.remove(currentScreen).register(screen -> CursorProviderInspector.INSTANCE.setVisibleScreen(null));
            ScreenEvents.afterRender(currentScreen).register((screen, guiGraphics, mouseX, mouseY, tickDelta) ->
                    CursorProviderInspector.INSTANCE.renderDebugger(Minecraft.getInstance(), screen, guiGraphics, mouseX, mouseY));
        });
    }

    private static void registerCursorPack(ModContainer mod, BuiltinCursorResourcePack pack, ResourcePackActivationType type) {
        ResourceManagerHelper.registerBuiltinResourcePack(pack.getLocation(), mod, pack.getDisplayName(), type);
    }
}
