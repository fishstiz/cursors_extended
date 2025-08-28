package io.github.fishstiz.cursors_extended.config;

import io.github.fishstiz.cursors_extended.CursorsExtended;
import io.github.fishstiz.cursors_extended.cursor.Cursor;
import io.github.fishstiz.cursors_extended.platform.Services;
import io.github.fishstiz.cursors_extended.util.SettingsUtil;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.io.Serializable;
import java.util.HashMap;
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
    private final GlobalSettings global = new GlobalSettings();
    private final Map<String, CursorSettings> cursors = new HashMap<>();

    Config() {
    }

    public static Config defaults() {
        return new Config();
    }

    public CursorSettings getOrCreateSettings(Cursor cursor) {
        return cursors.computeIfAbsent(cursor.getName(), k -> new CursorSettings());
    }

    public boolean isStale(Cursor cursor) {
        CursorSettings settings = cursors.get(cursor.getName());
        return settings == null || !settings.stale;
    }

    public void markSettingsStale() {
        cursors.values().forEach(settings -> settings.stale = true);
    }

    public @Nullable String getHash() {
        return _hash;
    }

    public void setHash(String hash) {
        _hash = hash;
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

    private CursorSettings filterInactive(@NotNull Cursor cursor, @NotNull Config.CursorSettings settingsToApply) {
        CursorSettings currentSettings = this.cursors.computeIfAbsent(cursor.getName(), k -> new CursorSettings());
        CursorSettings validated = settingsToApply.copy();

        if (this.global.isScaleActive()) {
            validated.setScale(currentSettings.getScale());
        }
        if (this.global.isXHotActive()) {
            validated.setXHot(cursor, currentSettings.getXHot());
        }
        if (this.global.isYHotActive()) {
            validated.setYHot(cursor, currentSettings.getYHot());
        }
        return validated;
    }

    public void replaceActiveSettings(CursorSettings settings, Cursor cursor) {
        this.cursors.put(cursor.getName(), this.filterInactive(cursor, settings));
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

    public static class CursorSettings extends AbstractCursorSettings<CursorSettings> implements Serializable {
        protected boolean enabled = SettingsUtil.ENABLED;
        protected Boolean animated;
        private transient boolean stale = false;

        CursorSettings() {
        }

        public void setScale(double scale) {
            this.scale = sanitizeScale(scale);
        }

        public void setXHot(@NotNull Cursor cursor, int xhot) {
            this.xhot = sanitizeHotspot(xhot, cursor);
        }

        public void setYHot(@NotNull Cursor cursor, int yhot) {
            this.yhot = sanitizeHotspot(yhot, cursor);
        }

        public void setEnabled(boolean enabled) {
            this.enabled = enabled;
        }

        public boolean isEnabled() {
            return enabled;
        }

        public Boolean isAnimated() {
            return this.animated;
        }

        public void setAnimated(boolean animated) {
            this.animated = animated;
        }

        public void merge(CursorSettings settings) {
            // other settings should not enable the cursor back on.
            if (this.enabled) this.enabled = settings.enabled;
            this.scale = sanitizeScale(settings.scale);
            this.xhot = sanitizeHotspot(settings.xhot, IMAGE_SIZE_MAX);
            this.yhot = sanitizeHotspot(settings.yhot, IMAGE_SIZE_MAX);
            this.animated = settings.animated;
        }

        @Override
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

    public static class GlobalSettings extends AbstractCursorSettings<GlobalSettings> implements Serializable {
        private boolean scaleActive = false;
        private boolean xhotActive = false;
        private boolean yhotActive = false;

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

        public void setScale(double scale) {
            this.scale = sanitizeScale(scale);
        }

        public void setXHot(double xhot) {
            setXHot((int) xhot);
        }

        public void setXHot(int xhot) {
            this.xhot = SettingsUtil.sanitizeGlobalHotspot(xhot);
        }

        @Override
        public int getXHot() {
            return SettingsUtil.sanitizeGlobalHotspot(this.xhot);
        }

        public void setYHot(double yhot) {
            setYHot((int) yhot);
        }

        public void setYHot(int yhot) {
            this.yhot = SettingsUtil.sanitizeGlobalHotspot(yhot);
        }

        @Override
        public int getYHot() {
            return SettingsUtil.sanitizeGlobalHotspot(this.yhot);
        }

        @Override
        GlobalSettings copy() {
            GlobalSettings globalSettings = new GlobalSettings();
            globalSettings.scale = this.scale;
            globalSettings.xhot = this.xhot;
            globalSettings.yhot = this.yhot;
            globalSettings.scaleActive = this.scaleActive;
            globalSettings.xhotActive = this.xhotActive;
            globalSettings.yhotActive = this.yhotActive;
            return globalSettings;
        }

        public <T extends AbstractCursorSettings<T>> T apply(T settings) {
            T copied = settings.copy();
            copied.scale = this.isScaleActive() ? this.getScale() : copied.getScale();
            copied.xhot = this.isXHotActive() ? this.getXHot() : copied.getXHot();
            copied.yhot = this.isYHotActive() ? this.getYHot() : copied.getYHot();
            return copied;
        }
    }
}
