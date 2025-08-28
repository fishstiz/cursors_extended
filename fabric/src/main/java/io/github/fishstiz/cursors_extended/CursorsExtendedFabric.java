package io.github.fishstiz.cursors_extended;

import io.github.fishstiz.cursors_extended.cursor.CursorResolver;
import io.github.fishstiz.cursors_extended.resource.CursorResourceReloadListener;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientLifecycleEvents;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.client.screen.v1.ScreenEvents;
import net.fabricmc.fabric.api.resource.ResourceManagerHelper;
import net.fabricmc.fabric.api.resource.ResourcePackActivationType;
import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.client.Minecraft;
import net.minecraft.network.chat.Component;
import net.minecraft.server.packs.PackType;

public class CursorsExtendedFabric implements ClientModInitializer {
    @Override
    public void onInitializeClient() {
        ClientLifecycleEvents.CLIENT_STARTED.register(client -> CursorsExtended.init());
        ClientTickEvents.END_CLIENT_TICK.register(CursorResolver.INSTANCE::afterTick);
        ScreenEvents.AFTER_INIT.register((client, currentScreen, width, height) -> ScreenEvents.afterRender(currentScreen)
                .register((screen, guiGraphics, mouseX, mouseY, tickDelta) -> CursorResolver.INSTANCE.afterRenderTooltip(
                        Minecraft.getInstance(), screen, guiGraphics, mouseX, mouseY
                )));
        FabricLoader.getInstance().getModContainer(CursorsExtended.MOD_ID).ifPresent(modContainer ->
                ResourceManagerHelper.registerBuiltinResourcePack(
                        CursorsExtended.loc("legacy"),
                        modContainer,
                        Component.translatable("cursors_extended.resource-pack.legacy"),
                        ResourcePackActivationType.NORMAL
                )
        );
        ResourceManagerHelper.get(PackType.CLIENT_RESOURCES).registerReloadListener(new CursorResourceReloadListener());
    }
}
