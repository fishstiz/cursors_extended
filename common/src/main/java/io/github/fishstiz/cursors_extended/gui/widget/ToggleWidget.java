package io.github.fishstiz.cursors_extended.gui.widget;

import net.minecraft.client.gui.components.Button;
import net.minecraft.client.input.InputWithModifiers;
import net.minecraft.client.input.MouseButtonEvent;
import net.minecraft.network.chat.CommonComponents;
import net.minecraft.network.chat.Component;
import org.jetbrains.annotations.NotNull;

import java.util.function.BiConsumer;
import java.util.function.Consumer;

public class ToggleWidget extends Button {
    private final BiConsumer<ToggleWidget, Boolean> listener;
    private final Component prefix;
    private boolean value;

    public ToggleWidget(
            int x,
            int y,
            int width,
            int height,
            boolean value,
            @NotNull Component prefix,
            @NotNull BiConsumer<ToggleWidget, Boolean> listener
    ) {
        super(x, y, width, height, prefix, btn -> {}, DEFAULT_NARRATION);

        this.listener = listener;
        this.prefix = prefix;

        this.setValue(value);
    }

    public ToggleWidget(boolean value, @NotNull Component prefix, @NotNull BiConsumer<ToggleWidget, Boolean> listener) {
        this(0, 0, DEFAULT_WIDTH, DEFAULT_HEIGHT, value, prefix, listener);
    }

    public ToggleWidget(boolean value, @NotNull Component prefix, @NotNull Consumer<Boolean> listener) {
        this(0, 0, DEFAULT_WIDTH, DEFAULT_HEIGHT, value, prefix, (target, v) -> listener.accept(v));
    }

    @Override
    public void onPress(InputWithModifiers inputWithModifiers) {
        this.value = !this.value;
        this.listener.accept(this, this.value);
        this.updateMessage();
    }

    public void setValue(boolean value) {
        this.value = value;
        this.updateMessage();
    }

    private void updateMessage() {
        this.setMessage(this.prefix.copy().append(": ").append(value ? CommonComponents.OPTION_ON : CommonComponents.OPTION_OFF));
    }

    @Override
    public void onRelease(MouseButtonEvent mouseButtonEvent) {
        this.setFocused(false);
    }
}
