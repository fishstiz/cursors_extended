package io.github.fishstiz.cursors_extended.compat;

import com.mojang.blaze3d.platform.cursor.CursorType;
import com.mojang.blaze3d.platform.cursor.CursorTypes;
import io.github.fishstiz.cursors_extended.CursorsExtended;
import io.github.fishstiz.cursors_extended.util.CursorTypeUtil;
import it.unimi.dsi.fastutil.longs.Long2ObjectOpenHashMap;
import it.unimi.dsi.fastutil.objects.Object2ObjectOpenHashMap;
import org.jspecify.annotations.Nullable;
import org.lwjgl.system.MemoryUtil;

import java.util.Map;

final class CursorStateTrackerImpl implements CursorStateTracker {
    private static CursorStateTracker instance = new DefaultTracker();
    private final Long2ObjectOpenHashMap<ModCursor> cursors = new Long2ObjectOpenHashMap<>();
    private final Long2ObjectOpenHashMap<Map<String, ModCursorState>> windowStates = new Long2ObjectOpenHashMap<>();
    private final Long2ObjectOpenHashMap<ModCursorState> latestStates = new Long2ObjectOpenHashMap<>();

    private CursorStateTrackerImpl() {
        CursorsExtended.LOGGER.info("[cursors_extended] Found mod creating its own cursors outside the vanilla API. Initialized cursor state tracker.");
    }

    static CursorStateTracker getInstance() {
        return instance;
    }

    static StackWalker getStackWalker() {
        return Holder.STACK_WALKER;
    }

    private static final class Holder {
        static final StackWalker STACK_WALKER = StackWalker.getInstance(StackWalker.Option.RETAIN_CLASS_REFERENCE);
        static final CursorStateTracker INSTANCE = new CursorStateTrackerImpl();

        static {
            CursorStateTrackerImpl.instance = INSTANCE;
        }
    }

    static final class DefaultTracker implements CursorStateTracker {
        @Override
        public void trackCursor(ModCursor cursor) {
            Holder.INSTANCE.trackCursor(cursor);
        }

        @Override
        public void resetCursor(long window, String source) {
            Holder.INSTANCE.resetCursor(window, source);
        }

        @Override
        public void setCursor(long window, ModCursor cursor) {
            Holder.INSTANCE.setCursor(window, cursor);
        }
    }

    @Override
    public void trackCursor(ModCursor modCursor) {
        if (modCursor.handle() != MemoryUtil.NULL) {
            cursors.put(modCursor.handle(), modCursor);
        }
    }

    @Override
    public void untrackCursor(ModCursor modCursor) {
        if (!modCursor.custom()) {
            CursorType modCursorType = modCursor.cursorType();
            long internalHandle = CursorTypeUtil.nonDefault(modCursorType)
                    ? CursorsExtended.getInstance().getRegistry().get(modCursorType).cursorType().handle
                    : CursorTypes.ARROW.handle;

            if (internalHandle == modCursor.handle()) {
                return;
            }
        }

        cursors.remove(modCursor.handle());
    }

    @Override
    public boolean isTracking() {
        return true;
    }

    @Override
    public @Nullable ModCursor getCursor(long handle) {
        return cursors.get(handle);
    }

    private void setCursor(String source, long window, CursorType cursorType, boolean custom) {
        Map<String, ModCursorState> windowState = windowStates.computeIfAbsent(window, _ -> new Object2ObjectOpenHashMap<>());
        ModCursorState state = windowState.get(source);

        if (state == null) {
            state = new ModCursorState(cursorType, custom);
            windowState.put(source, state);
            CursorsExtended.LOGGER.info("[cursors_extended] Tracking cursor state from '{}'", source);
        } else {
            state.update(cursorType, custom);
        }

        ModCursorState currentLatest = latestStates.get(window);
        if (shouldReplaceState(currentLatest, state)) {
            latestStates.put(window, state);
        }
    }

    @Override
    public void resetCursor(long window, String source) {
        setCursor(source, window, CursorType.DEFAULT, false);
    }

    @Override
    public void setCursor(long window, ModCursor cursor) {
        setCursor(cursor.source(), window, cursor.cursorType(), cursor.custom());
    }

    private boolean shouldReplaceState(ModCursorState current, ModCursorState other) {
        if (current == null) { // replace if first cursor
            return true;
        }

        boolean isCurrentCustom = current.isCustom();
        boolean isOtherCustom = other.isCustom();
        if (isOtherCustom != isCurrentCustom) { // replace if custom
            return isOtherCustom;
        }

        if (isCurrentCustom) { // if both custom, replace if timestamp is more recent
            return other.getTimestamp() > current.getTimestamp();
        }

        boolean currentDefault = !CursorTypeUtil.nonDefault(current.getCursorType());
        boolean otherDefault = !CursorTypeUtil.nonDefault(other.getCursorType());
        if (currentDefault != otherDefault) { // replace if non-default
            return !otherDefault;
        }

        return other.getTimestamp() > current.getTimestamp(); // replace if timestamp is more recent
    }

    @Override
    public CursorType getCurrentCursor(long window) {
        ModCursorState state = latestStates.get(window);
        return state == null ? CursorType.DEFAULT : state.getCursorType();
    }
}
