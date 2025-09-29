package io.github.fishstiz.cursors_extended;

import io.github.fishstiz.cursors_extended.cursor.inspector.CursorProviderInspector;
import io.github.fishstiz.cursors_extended.gui.screen.ConfigurationScreen;
import io.github.fishstiz.cursors_extended.resource.BuiltinCursorResourcePack;
import io.github.fishstiz.cursors_extended.resource.CursorResourceReloader;
import net.minecraft.server.packs.PackType;
import net.minecraft.server.packs.repository.Pack;
import net.minecraft.server.packs.repository.PackSource;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.EventPriority;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.ModContainer;
import net.neoforged.fml.common.Mod;
import net.neoforged.neoforge.client.event.*;
import net.neoforged.neoforge.client.event.lifecycle.ClientStartedEvent;
import net.neoforged.neoforge.client.gui.IConfigScreenFactory;
import net.neoforged.neoforge.common.NeoForge;
import net.neoforged.neoforge.event.AddPackFindersEvent;

@Mod(value = CursorsExtended.MOD_ID, dist = Dist.CLIENT)
public class CursorsExtendedNeoForge {
    public CursorsExtendedNeoForge(ModContainer modContainer, IEventBus modEventBus) {
        modContainer.registerExtensionPoint(IConfigScreenFactory.class, (container, screen) -> new ConfigurationScreen(screen));
        modEventBus.addListener((AddPackFindersEvent event) -> {
            registerCursorPack(event, BuiltinCursorResourcePack.DEFAULT_AUTO);
            registerCursorPack(event, BuiltinCursorResourcePack.LEGACY);
        });
        modEventBus.addListener((AddClientReloadListenersEvent event) -> event.addListener(
                CursorResourceReloader.getDirectory(),
                new CursorResourceReloader()
        ));
        NeoForge.EVENT_BUS.addListener(EventPriority.HIGHEST, (ClientStartedEvent event) -> CursorsExtended.init());
        NeoForge.EVENT_BUS.addListener((ScreenEvent.Init.Post event) -> CursorProviderInspector.INSTANCE.setVisibleScreen(event.getScreen()));
        NeoForge.EVENT_BUS.addListener((ScreenEvent.Closing event) -> CursorProviderInspector.INSTANCE.setVisibleScreen(null));
        NeoForge.EVENT_BUS.addListener((ScreenEvent.Render.Post event) -> CursorProviderInspector.INSTANCE.renderDebugger(
                event.getScreen().getMinecraft(),
                event.getScreen(),
                event.getGuiGraphics(),
                event.getMouseX(),
                event.getMouseY()
        ));
    }

    private static void registerCursorPack(AddPackFindersEvent event, BuiltinCursorResourcePack pack) {
        event.addPackFinders(
                CursorsExtended.loc("resourcepacks/" + pack.getLocation().getPath()),
                PackType.CLIENT_RESOURCES,
                pack.getDisplayName(),
                PackSource.BUILT_IN,
                false,
                Pack.Position.TOP
        );
    }
}
