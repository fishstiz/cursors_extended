package io.github.fishstiz.cursors_extended.resource;

import io.github.fishstiz.cursors_extended.CursorsExtended;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;

public enum BuiltinCursorResourcePack {
    DEFAULT(CursorsExtended.id("default"), Component.translatable("cursors_extended.resource-pack.default")),
    DEFAULT_AUTO(CursorsExtended.id("default_auto"), Component.translatable("cursors_extended.resource-pack.default.auto")),
    LEGACY(CursorsExtended.id("legacy"), Component.translatable("cursors_extended.resource-pack.legacy"));

    private final Identifier location;
    private final Component displayName;

    BuiltinCursorResourcePack(Identifier location, Component displayName) {
        this.location = location;
        this.displayName = displayName;
    }

    public Identifier getLocation() {
        return location;
    }

    public Component getDisplayName() {
        return displayName;
    }
}
