package io.github.fishstiz.cursors_extended.resource;

import io.github.fishstiz.cursors_extended.CursorsExtended;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;

public enum BuiltinCursorResourcePack {
    DEFAULT(CursorsExtended.loc("default"), Component.translatable("cursors_extended.resource-pack.default")),
    DEFAULT_AUTO(CursorsExtended.loc("default_auto"), Component.translatable("cursors_extended.resource-pack.default.auto")),
    LEGACY(CursorsExtended.loc("legacy"), Component.translatable("cursors_extended.resource-pack.legacy"));

    private final ResourceLocation location;
    private final Component displayName;

    BuiltinCursorResourcePack(ResourceLocation location, Component displayName) {
        this.location = location;
        this.displayName = displayName;
    }

    public ResourceLocation getLocation() {
        return location;
    }

    public Component getDisplayName() {
        return displayName;
    }
}
