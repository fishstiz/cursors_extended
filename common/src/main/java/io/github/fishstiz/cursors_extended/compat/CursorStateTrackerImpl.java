package io.github.fishstiz.cursors_extended.compat;

import com.mojang.blaze3d.platform.cursor.CursorType;
import io.github.fishstiz.cursors_extended.CursorsExtended;
import io.github.fishstiz.cursors_extended.util.CursorTypeUtil;
import it.unimi.dsi.fastutil.longs.Long2ObjectOpenHashMap;
import it.unimi.dsi.fastutil.objects.Object2ObjectOpenHashMap;
import org.jetbrains.annotations.Nullable;

import java.util.Map;

class CursorStateTrackerImpl implements CursorStateTracker {
    private static volatile boolean tracking = false;
    private final Map<Long, ModCursor> cursors = new Long2ObjectOpenHashMap<>();
    private final Map<Long, Map<String, ModCursorState>> states = new Long2ObjectOpenHashMap<>();

    static CursorStateTracker get() {
        return Holder.INSTANCE;
    }

    static CursorStateTracker getOrDefault() {
        return !tracking ? CursorStateTracker.DEFAULT : Holder.INSTANCE;
    }

    static StackWalker getStackWalker() {
        return Holder.STACK_WALKER;
    }

    private static class Holder {
        static final StackWalker STACK_WALKER = StackWalker.getInstance(StackWalker.Option.RETAIN_CLASS_REFERENCE);
        static final CursorStateTracker INSTANCE = new CursorStateTrackerImpl();

        static {
            CursorStateTrackerImpl.tracking = true;
            CursorsExtended.LOGGER.info("[cursors_extended] Found mod creating its own cursors outside the vanilla API. Initialized cursor state tracker.");
        }
    }

    @Override
    public void trackCursor(ModCursor modCursor) {
        synchronized (cursors) {
            cursors.put(modCursor.handle(), modCursor);
        }
    }

    @Override
    public void untrackCursor(ModCursor modCursor) {
        synchronized (cursors) {
            cursors.remove(modCursor.handle());
        }
    }

    @Override
    public boolean isTracking() {
        return true;
    }

    @Override
    public @Nullable ModCursor getCursor(long handle) {
        synchronized (cursors) {
            return cursors.get(handle);
        }
    }

    private void setCursor(String source, long window, CursorType cursorType, boolean custom) {
        Map<String, ModCursorState> windowStates = states.computeIfAbsent(window, k -> new Object2ObjectOpenHashMap<>());
        ModCursorState state = windowStates.get(source);

        if (state == null) {
            windowStates.put(source, new ModCursorState(cursorType, custom));
            CursorsExtended.LOGGER.info("[cursors_extended] Tracking cursor state from '{}'", source);
        } else {
            state.update(cursorType, custom);
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
        Map<String, ModCursorState> windowStates = states.get(window);
        if (windowStates == null || windowStates.isEmpty()) {
            return CursorType.DEFAULT;
        }

        ModCursorState latestState = null;
        for (ModCursorState state : windowStates.values()) {
            if (shouldReplaceState(latestState, state)) {
                latestState = state;
            }
        }

        return latestState == null ? CursorType.DEFAULT : latestState.getCursorType();
    }
}
