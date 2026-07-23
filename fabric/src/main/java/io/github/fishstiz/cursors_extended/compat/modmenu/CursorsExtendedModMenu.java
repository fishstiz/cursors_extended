package io.github.fishstiz.cursors_extended.compat.modmenu;

import com.terraformersmc.modmenu.api.ConfigScreenFactory;
import com.terraformersmc.modmenu.api.ModMenuApi;
import io.github.fishstiz.cursors_extended.gui.ConfigScreen;
import net.minecraft.client.gui.screens.Screen;

public class CursorsExtendedModMenu implements ModMenuApi {
    @Override
    public ConfigScreenFactory<Screen> getModConfigScreenFactory() {
        return ConfigScreen::new;
    }
}
