package io.github.fishstiz.cursors_extended.cursor;

import com.mojang.blaze3d.platform.NativeImage;
import com.mojang.blaze3d.platform.cursor.CursorType;
import io.github.fishstiz.cursors_extended.CursorsExtended;
import io.github.fishstiz.cursors_extended.config.Config;
import io.github.fishstiz.cursors_extended.config.CursorMetadata;
import io.github.fishstiz.cursors_extended.util.CursorTypeUtil;
import it.unimi.dsi.fastutil.objects.Object2ObjectLinkedOpenHashMap;
import it.unimi.dsi.fastutil.objects.Object2ObjectOpenHashMap;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.io.IOException;
import java.util.*;

public class CursorManager {
    public static final CursorManager INSTANCE = new CursorManager();
    private static final int TYPE_COUNT = 13;
    private static final float LOAD_FACTOR = 0.75F;
    private static final int CAPACITY = (int) Math.ceil(TYPE_COUNT / LOAD_FACTOR);
    private final Map<AliasMap.Key, Cursor> cursors = new Object2ObjectLinkedOpenHashMap<>(CAPACITY, LOAD_FACTOR);
    private final AliasMap aliases = new AliasMap(CAPACITY, LOAD_FACTOR);
    private final AnimationState animationState = new AnimationState();
    private Map<String, Cursor> dummies;
    private CursorRenderer renderer;
    private Cursor currentCursor;

    private CursorManager() {
        this.currentCursor = Cursor.createDummy();
        this.renderer = CursorsExtended.CONFIG.isVirtualMode() ? new CursorRenderer.Virtual() : new CursorRenderer.Native();
    }

    public void registerType(CursorType type) {
        this.cursors.put(this.aliases.addKey(type.toString()), new Cursor(type, this::onLoad));
    }

    public void registerAlias(String original, String alias) {
        this.aliases.addAlias(original, alias);
    }

    public void registerAlias(CursorType type, CursorType alias) {
        this.registerAlias(type.toString(), alias.toString());
    }

    public boolean isRegistered(CursorType cursorType) {
        return this.cursors.containsKey(this.aliases.lookup(cursorType.toString()));
    }

    public void loadCursor(Cursor cursor, NativeImage image, Config.CursorSettings settings, CursorMetadata metadata) throws IOException {
        if (!isRegistered(cursor.getType())) {
            throw new IllegalStateException("Attempting to load an unregistered cursor: " + cursor.getName());
        }

        CursorMetadata.Animation animationData = metadata.getAnimation();
        boolean animated = animationData != null;
        if (animated != (cursor instanceof AnimatedCursor)) {
            cursor.destroy();
            cursor = animated
                    ? new AnimatedCursor(cursor.getType(), this::onLoad)
                    : new Cursor(cursor.getType(), this::onLoad);
            cursors.put(aliases.lookup(cursor.getName()), cursor);
        }

        if (cursor instanceof AnimatedCursor animatedCursor) {
            animatedCursor.loadImage(image, settings, metadata, animationData);
        } else {
            cursor.loadImage(image, settings, metadata);
        }
    }

    private void onLoad(Cursor cursor) {
        Cursor appliedCursor = getAppliedCursor();
        if (appliedCursor.isLoaded() &&
            appliedCursor.getId() == cursor.getId() &&
            CursorTypeUtil.nameEquals(appliedCursor.getType(), cursor.getType())) {
            reapplyCursor();
        }
    }

    public void setCurrentCursor(@NotNull CursorType type) {
        Cursor cursor = getCursor(type);

        if (cursor == null) {
            handleCursorExternal(type);
            return;
        }

        if (cursor instanceof AnimatedCursor animatedCursor && cursor.isEnabled()) {
            handleCursorAnimation(animatedCursor);
            return;
        }

        if (type != CursorType.DEFAULT && !cursor.isEnabled()) {
            cursor = getCursor(CursorType.DEFAULT);
        }

        updateCursor(cursor);
    }

    private void handleCursorAnimation(AnimatedCursor cursor) {
        if (!CursorTypeUtil.nameEquals(getAppliedCursor().getType(), cursor.getType())) {
            animationState.reset();
        }

        Cursor currentFrameCursor = cursor.nextFrame(animationState).cursor();
        updateCursor(currentFrameCursor.getId() != 0 ? currentFrameCursor : cursor);
    }

    private void handleCursorExternal(CursorType cursorType) {
        if (this.dummies == null) {
            this.dummies = new Object2ObjectOpenHashMap<>();
        }

        Cursor cursor = this.dummies.get(cursorType.toString());
        if (cursor == null) {
            CursorsExtended.LOGGER.info("[cursors_extended] Registered an external cursor: {}", cursorType);
            cursor = this.dummies.computeIfAbsent(cursorType.toString(), name -> Cursor.loadOrCreateDummy(cursorType, this::onLoad));
        }

        if (cursor.isLoaded()) {
            updateCursor(cursor);
        } else {
            this.currentCursor = cursor;
            this.renderer.resetCursor();
            cursorType.select(Minecraft.getInstance().getWindow());
        }
    }

    private void updateCursor(Cursor cursor) {
        if (cursor == null || !CursorsExtended.CONFIG.isAggressiveCursor() && cursor.getId() == currentCursor.getId()) {
            return;
        }

        this.currentCursor = cursor;
        this.renderer.setCursor(this.currentCursor);
    }

    public void reapplyCursor() {
        Cursor cursor = this.getAppliedCursor();
        if (cursor.isLoaded() && this.isRegistered(cursor.getType())) {
            this.renderer.setCursor(cursor);
        }
    }

    public @NotNull Cursor getAppliedCursor() {
        if (this.currentCursor instanceof AnimatedCursor animatedCursor) {
            return animatedCursor.getFrame(animationState.getCurrentFrame()).cursor();
        }

        return this.currentCursor;
    }

    public boolean isEnabled(@NotNull CursorType type) {
        return isEnabled(getCursor(type.toString()));
    }

    public boolean isEnabled(@Nullable Cursor cursor) {
        return cursor != null && cursor.isEnabled();
    }

    public @Nullable Cursor getCursor(String type) {
        return this.cursors.get(this.aliases.lookup(type));
    }

    public @Nullable Cursor getCursor(CursorType type) {
        return getCursor(type.toString());
    }

    public Collection<Cursor> getCursors() {
        return cursors.values();
    }

    public boolean isActive() {
        for (Cursor cursor : this.cursors.values()) {
            if (cursor.isEnabled()) {
                return true;
            }
        }
        return false;
    }

    public boolean isAdaptive() {
        for (Cursor cursor : this.cursors.values()) {
            if (cursor.isEnabled() && cursor.getType() != CursorType.DEFAULT) {
                return true;
            }
        }
        return false;
    }

    public boolean isVirtual() {
        return this.renderer instanceof CursorRenderer.Virtual;
    }

    public void toggleVirtual() {
        this.renderer.resetCursor();
        this.renderer = this.isVirtual() ? new CursorRenderer.Native() : new CursorRenderer.Virtual();
        this.reapplyCursor();
    }

    public void renderCursor(Minecraft minecraft, GuiGraphics guiGraphics, int mouseX, int mouseY) {
        this.renderer.render(minecraft, guiGraphics, mouseX, mouseY);
    }

    private record AliasMap(Map<String, Key> aliases) {
        private AliasMap(int initialCapacity, float loadFactor) {
            this(new Object2ObjectOpenHashMap<>(initialCapacity, loadFactor));
        }

        private Key addKey(String alias) {
            return this.aliases.computeIfAbsent(alias, Key::new);
        }

        /**
         * Retroactively add keys in case a mod creates a standard cursor before it's registered.
         */
        private void addAlias(String original, String alias) {
            this.aliases.putIfAbsent(alias, this.aliases.computeIfAbsent(original, Key::new));
        }

        private Key lookup(String alias) {
            return this.aliases.get(alias);
        }

        private record Key(String id) {
        }
    }
}
