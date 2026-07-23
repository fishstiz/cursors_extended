package io.github.fishstiz.cursors_extended.mixin.options;

import io.github.fishstiz.cursors_extended.gui.ConfigScreen;
import io.github.fishstiz.fidgetz.v0.gui.components.FZButton;
import io.github.fishstiz.fidgetz.v0.gui.components.FZButtonBase;
import net.minecraft.client.Options;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.gui.screens.options.MouseSettingsScreen;
import net.minecraft.client.gui.screens.options.OptionsSubScreen;
import net.minecraft.network.chat.CommonComponents;
import net.minecraft.network.chat.Component;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(MouseSettingsScreen.class)
public abstract class MouseOptionsScreenMixin extends OptionsSubScreen {
    protected MouseOptionsScreenMixin(Screen parent, Options gameOptions, Component title) {
        super(parent, gameOptions, title);
    }

    @Inject(method = "addOptions", at = @At("TAIL"))
    protected void addOptions(CallbackInfo ci) {
        if (this.list == null) {
            return;
        }

        FZButton settingsBtn = FZButton.builder()
                .message(Component.translatable("cursors_extended.options").append(CommonComponents.ELLIPSIS))
                .onPress(() -> minecraft.gui.setScreen(new ConfigScreen(this)))
                .build();

        FZButtonBase fillerBtn = FZButton.builder().visible(false).inactive().build();

        this.list.addSmall(settingsBtn, fillerBtn);
    }
}
