package io.github.fishstiz.cursors_extended.cursor;

import com.mojang.blaze3d.platform.NativeImage;
import com.mojang.blaze3d.platform.cursor.CursorType;
import com.mojang.blaze3d.platform.cursor.CursorTypes;
import io.github.fishstiz.cursors_extended.CursorsExtended;
import io.github.fishstiz.cursors_extended.config.AnimationData;
import io.github.fishstiz.cursors_extended.config.Config;
import io.github.fishstiz.cursors_extended.config.CursorMetadata;
import io.github.fishstiz.cursors_extended.util.CursorTypeUtil;
import it.unimi.dsi.fastutil.objects.Object2ObjectLinkedOpenHashMap;
import it.unimi.dsi.fastutil.objects.Object2ObjectOpenHashMap;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.lwjgl.system.MemoryUtil;

import java.io.IOException;
import java.util.*;

public final class CursorManager {
    public static final CursorManager INSTANCE = new CursorManager();
    private final Map<String, Cursor> cursors = new Object2ObjectLinkedOpenHashMap<>();
    private final TreeMap<Integer, String> overrides = new TreeMap<>();
    private final AnimationState animationState = new AnimationState();
    private @NotNull CursorRenderer renderer;
    private Map<String, Cursor> dummies;
    private Cursor currentCursor;

    private CursorManager() {
        this.currentCursor = Cursor.createDummy();
        this.renderer = CursorsExtended.CONFIG.isVirtualMode() ? new CursorRenderer.Virtual() : new CursorRenderer.Native();
    }

    public void registerType(CursorType cursorType) {
        this.cursors.put(cursorType.toString(), new Cursor(cursorType, this::onLoad));
    }

    public boolean isRegistered(CursorType cursorType) {
        return this.cursors.containsKey(cursorType.toString());
    }

    public void loadCursor(Cursor cursor, NativeImage image, Config.CursorSettings settings, CursorMetadata metadata) throws IOException {
        if (!cursors.containsKey(cursor.getName())) {
            throw new IllegalStateException("Attempting to load an unregistered cursor: " + cursor.getName());
        }

        AnimationData animationData = metadata.getAnimation();
        boolean animated = animationData != null;
        if (animated != (cursor instanceof AnimatedCursor)) {
            cursor.destroy();
            cursor = animated
                    ? new AnimatedCursor(cursor.getType(), this::onLoad)
                    : new Cursor(cursor.getType(), this::onLoad);
            cursors.put(cursor.getName(), cursor);
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
        Cursor override = getOverride();
        Cursor cursor = override != null ? override : getCursor(type);

        if (cursor == null) {
            handleCursorExternal(type);
            return;
        }

        if (cursor instanceof AnimatedCursor animatedCursor && cursor.getId() != MemoryUtil.NULL) {
            handleCursorAnimation(animatedCursor);
            return;
        }

        if (type != CursorType.DEFAULT && cursor.getId() == MemoryUtil.NULL || !cursor.isEnabled()) {
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
        if (this.isRegistered(cursor.getType())) {
            this.renderer.setCursor(cursor);
        }
    }

    public void overrideCursor(CursorType type, int index) {
        Cursor cursor = getCursor(type);
        if (cursor != null && cursor.isEnabled()) {
            overrides.put(index, type.toString());
        } else {
            overrides.remove(index);
        }
    }

    public void removeOverride(int index) {
        overrides.remove(index);
    }

    public @Nullable Cursor getOverride() {
        while (!overrides.isEmpty()) {
            Map.Entry<Integer, String> lastEntry = overrides.lastEntry();
            Cursor cursor = getCursor(lastEntry.getValue());

            if (cursor == null || cursor.getId() == 0) {
                overrides.remove(lastEntry.getKey());
            } else {
                return cursor;
            }
        }

        return null;
    }

    public @NotNull Cursor getAppliedCursor() {
        Cursor override = getOverride();
        Cursor cursor = override != null ? override : currentCursor;

        if (cursor instanceof AnimatedCursor animatedCursor) {
            return animatedCursor.getFrame(animationState.getCurrentFrame()).cursor();
        }

        return cursor;
    }

    public boolean isEnabled(@NotNull CursorType type) {
        return isEnabled(getCursor(type.toString()));
    }

    public boolean isEnabled(@Nullable Cursor cursor) {
        return cursor != null && cursor.isEnabled();
    }

    public @Nullable Cursor getCursor(String type) {
        if (CursorTypes.ARROW.toString().equals(type)) {
            return cursors.get(CursorType.DEFAULT.toString());
        }
        return cursors.get(type);
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
}
