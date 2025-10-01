package io.github.fishstiz.cursors_extended.cursor;

import com.mojang.blaze3d.platform.cursor.CursorType;
import io.github.fishstiz.cursors_extended.CursorsExtended;
import it.unimi.dsi.fastutil.objects.Object2ObjectLinkedOpenHashMap;
import it.unimi.dsi.fastutil.objects.Object2ObjectOpenHashMap;
import it.unimi.dsi.fastutil.objects.ObjectLinkedOpenHashSet;
import it.unimi.dsi.fastutil.objects.ObjectOpenHashSet;
import org.jetbrains.annotations.Nullable;

import java.util.Map;
import java.util.Set;

public class CursorRegistry {
    private final Map<String, Cursor> registry = new Object2ObjectLinkedOpenHashMap<>();
    private final Map<String, Cursor> external = new Object2ObjectOpenHashMap<>();

    public void register(CursorType cursorType) {
        registry.put(cursorType.toString(), new Cursor(cursorType));
    }

    public void registerAlias(CursorType cursorType, CursorType alias) {
        registry.put(alias.toString(), get(cursorType));
    }

    public Cursor get(CursorType cursorType) {
        Cursor cursor = registry.get(cursorType.toString());

        if (cursor == null) {
            CursorsExtended.LOGGER.info("[cursors_extended] Found external cursor type: {}", cursorType);
            return external.computeIfAbsent(cursorType.toString(), name -> new Cursor(cursorType));
        }

        return cursor;
    }

    public @Nullable Cursor tryGet(String cursorType) {
        Cursor cursor = registry.get(cursorType);
        return cursor != null ? cursor : external.get(cursorType);
    }

    public Set<Cursor> getCursors() {
        Set<Cursor> cursors = new ObjectOpenHashSet<>();
        cursors.addAll(registry.values());
        cursors.addAll(external.values());
        return cursors;
    }

    public Set<Cursor> getInternalCursors() {
        return new ObjectLinkedOpenHashSet<>(registry.values());
    }
}
