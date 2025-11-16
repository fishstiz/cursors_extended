package io.github.fishstiz.cursors_extended.config;

import io.github.fishstiz.cursors_extended.CursorsExtended;
import io.github.fishstiz.cursors_extended.cursor.Cursor;
import io.github.fishstiz.cursors_extended.platform.Services;
import it.unimi.dsi.fastutil.objects.Object2ObjectOpenHashMap;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.io.Serializable;
import java.util.Map;

import static io.github.fishstiz.cursors_extended.util.SettingsUtil.*;

public class Config implements Serializable {
    private String _hash;
    private boolean itemSlotEnabled = true;
    private boolean itemGrabbingEnabled = true;
    private boolean creativeTabsEnabled = true;
    private boolean enchantmentsEnabled = true;
    private boolean stonecutterRecipesEnabled = true;
    private boolean loomPatternsEnabled = true;
    private boolean advancementTabsEnabled = true;
    private boolean worldIconEnabled = true;
    private boolean serverIconEnabled = true;
    private boolean pointerScrollbarEnabled = true;
    private boolean resizeScrollbarEnabled = true;
    private boolean heldCursorsEnabled = true;
    private boolean aggressiveCursor = false;
    private boolean virtualMode = false;
    private boolean legacyMode = true;
    private boolean showHotspotGuide = true;
    private boolean remapStandardCursors = true;
    private boolean workarounds = true;
    private final GlobalSettings global = new GlobalSettings();
    private final Object2ObjectOpenHashMap<String, CursorSettings> cursors = new Object2ObjectOpenHashMap<>();
    private transient Map<String, CursorSettings> unknownCursors;
    private transient boolean stale = false;

    Config() {
    }

    public static Config defaults() {
        return new Config();
    }

    public CursorSettings getOrCreateSettings(Cursor cursor) {
        if (cursor.isCustom()) {
            if (unknownCursors == null) unknownCursors = new Object2ObjectOpenHashMap<>();
            return unknownCursors.computeIfAbsent(cursor.name(), k -> {
                CursorsExtended.LOGGER.error("[cursors_extended] Settings created for custom external cursor '{}'. This should not happen!", k);
                return new CursorSettings();
            });
        }
        return cursors.computeIfAbsent(cursor.name(), k -> new CursorSettings());
    }

    public boolean isStale(Cursor cursor) {
        return !cursor.isCustom() && (stale || !cursors.containsKey(cursor.name()));
    }

    public void markSettingsStale() {
        this.stale = true;
    }

    public @Nullable String getHash() {
        return _hash;
    }

    public void setHash(String hash) {
        _hash = hash;
    }

    public boolean hasResourcePack() {
        String hash = _hash;
        return hash != null && !hash.isEmpty();
    }

    public static Config load() {
        return JsonLoader.loadOrDefault(Config.class, Services.PLATFORM.getConfigDir().resolve(CursorsExtended.MOD_ID), Config::new);
    }

    public void save() {
        JsonLoader.save(Services.PLATFORM.getConfigDir().resolve(CursorsExtended.MOD_ID + ".json"), this);
    }

    public GlobalSettings getGlobal() {
        return global;
    }

    public boolean isCreativeTabsEnabled() {
        return creativeTabsEnabled;
    }

    public void setCreativeTabsEnabled(boolean creativeTabsEnabled) {
        this.creativeTabsEnabled = creativeTabsEnabled;
    }

    public boolean isEnchantmentsEnabled() {
        return enchantmentsEnabled;
    }

    public void setEnchantmentsEnabled(boolean enchantmentsEnabled) {
        this.enchantmentsEnabled = enchantmentsEnabled;
    }

    public boolean isStonecutterRecipesEnabled() {
        return stonecutterRecipesEnabled;
    }

    public void setStonecutterRecipesEnabled(boolean stonecutterRecipesEnabled) {
        this.stonecutterRecipesEnabled = stonecutterRecipesEnabled;
    }

    public boolean isLoomPatternsEnabled() {
        return loomPatternsEnabled;
    }

    public void setLoomPatternsEnabled(boolean loomPatternsEnabled) {
        this.loomPatternsEnabled = loomPatternsEnabled;
    }

    public boolean isWorldIconEnabled() {
        return worldIconEnabled;
    }

    public void setWorldIconEnabled(boolean worldIconEnabled) {
        this.worldIconEnabled = worldIconEnabled;
    }

    public boolean isItemSlotEnabled() {
        return itemSlotEnabled;
    }

    public void setItemSlotEnabled(boolean itemSlotEnabled) {
        this.itemSlotEnabled = itemSlotEnabled;
    }

    public boolean isItemGrabbingEnabled() {
        return itemGrabbingEnabled;
    }

    public void setItemGrabbingEnabled(boolean itemGrabbingEnabled) {
        this.itemGrabbingEnabled = itemGrabbingEnabled;
    }

    public boolean isAdvancementTabsEnabled() {
        return advancementTabsEnabled;
    }

    public void setAdvancementTabsEnabled(boolean advancementTabsEnabled) {
        this.advancementTabsEnabled = advancementTabsEnabled;
    }

    public boolean isServerIconEnabled() {
        return serverIconEnabled;
    }

    public void setServerIconEnabled(boolean serverIconEnabled) {
        this.serverIconEnabled = serverIconEnabled;
    }

    public boolean isAggressiveCursor() {
        return aggressiveCursor;
    }

    public void setAggressiveCursor(boolean aggressiveCursor) {
        this.aggressiveCursor = aggressiveCursor;
    }

    public boolean isVirtualMode() {
        return virtualMode;
    }

    public void setVirtualMode(boolean virtualMode) {
        this.virtualMode = virtualMode;
    }

    public boolean isLegacyMode() {
        return legacyMode;
    }

    public void setLegacyMode(boolean legacyMode) {
        this.legacyMode = legacyMode;
    }

    public boolean isHeldCursorsEnabled() {
        return heldCursorsEnabled;
    }

    public void setHeldCursorsEnabled(boolean heldCursorsEnabled) {
        this.heldCursorsEnabled = heldCursorsEnabled;
    }

    public boolean isPointerScrollbarEnabled() {
        return pointerScrollbarEnabled;
    }

    public void setPointerScrollbarEnabled(boolean pointerScrollbarEnabled) {
        this.pointerScrollbarEnabled = pointerScrollbarEnabled;
    }

    public boolean isResizeScrollbarEnabled() {
        return resizeScrollbarEnabled;
    }

    public void setResizeScrollbarEnabled(boolean resizeScrollbarEnabled) {
        this.resizeScrollbarEnabled = resizeScrollbarEnabled;
    }

    public boolean isShowHotspotGuide() {
        return showHotspotGuide;
    }

    public void setShowHotspotGuide(boolean showHotspotGuide) {
        this.showHotspotGuide = showHotspotGuide;
    }

    public boolean isRemapStandardCursors() {
        return workarounds && remapStandardCursors;
    }

    public void setRemapStandardCursors(boolean remapStandardCursors) {
        this.remapStandardCursors = remapStandardCursors;
    }

    public boolean isWorkaroundsEnabled() {
        return workarounds;
    }

    public void setWorkarounds(boolean workarounds) {
        this.workarounds = workarounds;
    }

    public static class CursorSettings extends AbstractCursorSettings implements Serializable {
        protected boolean enabled = ENABLED;
        protected Boolean animated = ANIMATED;

        public void setScale(float scale) {
            this.scale = sanitizeScale(scale);
        }

        public void setXHot(@NotNull Cursor cursor, int xhot) {
            this.xhot = sanitizeXHot(xhot, cursor);
        }

        public void setYHot(@NotNull Cursor cursor, int yhot) {
            this.yhot = sanitizeYHot(yhot, cursor);
        }

        public void setEnabled(boolean enabled) {
            this.enabled = enabled;
        }

        public boolean enabled() {
            return enabled;
        }

        public Boolean animated() {
            return this.animated;
        }

        public void setAnimated(boolean animated) {
            this.animated = animated;
        }

        private void mergeCommon(CursorProperties metadata) {
            this.scale = sanitizeScale(metadata.scale());
            this.xhot = sanitizeHotspot(metadata.xhot(), Integer.MAX_VALUE);
            this.yhot = sanitizeHotspot(metadata.yhot(), Integer.MAX_VALUE);
            this.animated = metadata.animated();
        }

        public void mergeSelective(CursorProperties metadata) {
            // pack settings should not enable the cursor back on. https://github.com/fishstiz/minecraft-cursor/issues/29
            if (this.enabled) this.enabled = metadata.enabled();
            mergeCommon(metadata);
        }

        public void mergeAll(CursorProperties metadata) {
            this.enabled = metadata.enabled();
            mergeCommon(metadata);
        }

        public CursorSettings copy() {
            CursorSettings settings = new CursorSettings();
            settings.scale = this.scale;
            settings.xhot = this.xhot;
            settings.yhot = this.yhot;
            settings.enabled = this.enabled;
            settings.animated = this.animated;
            return settings;
        }
    }

    public static class GlobalSettings extends AbstractCursorSettings implements Serializable {
        private boolean scaleActive = false;
        private boolean xhotActive = false;
        private boolean yhotActive = false;

        private GlobalSettings() {
        }

        public void setActiveAll(boolean active) {
            setScaleActive(active);
            setXHotActive(active);
            setYHotActive(active);
        }

        public boolean isScaleActive() {
            return scaleActive;
        }

        public void setScaleActive(boolean scaleEnabled) {
            this.scaleActive = scaleEnabled;
        }

        public boolean isXHotActive() {
            return xhotActive;
        }

        public void setXHotActive(boolean xhotActive) {
            this.xhotActive = xhotActive;
        }

        public boolean isYHotActive() {
            return yhotActive;
        }

        public void setYHotActive(boolean yhotActive) {
            this.yhotActive = yhotActive;
        }

        public void setScale(float scale) {
            this.scale = sanitizeScale(scale);
        }

        public void setXHot(double xhot) {
            setXHot((int) xhot);
        }

        public void setXHot(int xhot) {
            this.xhot = Math.max(0, xhot);
        }

        @Override
        public int xhot() {
            return Math.max(0, this.xhot);
        }

        public void setYHot(double yhot) {
            setYHot((int) yhot);
        }

        public void setYHot(int yhot) {
            this.yhot = Math.max(0, yhot);
        }

        @Override
        public int yhot() {
            return Math.max(0, this.yhot);
        }

        @Override
        public boolean enabled() {
            throw new UnsupportedOperationException("GlobalSettings does not have an enabled setting");
        }

        @Override
        public Boolean animated() {
            throw new UnsupportedOperationException("GlobalSettings does not have an animated setting");
        }

        public CursorSettings apply(CursorSettings settings) {
            CursorSettings copied = settings.copy();
            copied.scale = this.isScaleActive() ? this.scale() : copied.scale();
            copied.xhot = this.isXHotActive() ? this.xhot() : copied.xhot();
            copied.yhot = this.isYHotActive() ? this.yhot() : copied.yhot();
            return copied;
        }
    }

    public abstract static class AbstractCursorSettings implements CursorProperties {
        protected float scale = SCALE;
        protected int xhot = X_HOT;
        protected int yhot = Y_HOT;

        public float scale() {
            return scale;
        }

        public int xhot() {
            return xhot;
        }

        public int yhot() {
            return yhot;
        }
    }
}
