package io.github.fishstiz.cursors_extended.cursor;

import com.mojang.blaze3d.platform.cursor.CursorType;
import com.mojang.blaze3d.platform.cursor.CursorTypes;
import io.github.fishstiz.cursors_extended.CursorsExtended;
import io.github.fishstiz.cursors_extended.lifecycle.ClientStartedListener;
import it.unimi.dsi.fastutil.objects.Object2ObjectLinkedOpenHashMap;
import it.unimi.dsi.fastutil.objects.Object2ObjectOpenHashMap;
import it.unimi.dsi.fastutil.objects.ObjectLinkedOpenHashSet;
import it.unimi.dsi.fastutil.objects.ObjectOpenHashSet;
import net.minecraft.client.Minecraft;
import org.jetbrains.annotations.Nullable;

import java.util.Map;
import java.util.Set;

public class CursorRegistry implements ClientStartedListener {
    private final Map<String, Cursor> registry = new Object2ObjectLinkedOpenHashMap<>();
    private final Map<String, Cursor> external = new Object2ObjectOpenHashMap<>();

    @Override
    public void onClientStarted(Minecraft minecraft) {
        register(CursorType.DEFAULT);
        registerAlias(CursorType.DEFAULT, CursorTypes.ARROW);
        register(CursorTypes.POINTING_HAND);
        register(CursorTypesExt.GRABBING);
        register(CursorTypes.IBEAM);
        register(CursorTypesExt.SHIFT);
        register(CursorTypesExt.BUSY);
        register(CursorTypes.NOT_ALLOWED);
        register(CursorTypes.CROSSHAIR);
        register(CursorTypes.RESIZE_ALL);
        register(CursorTypes.RESIZE_EW);
        register(CursorTypes.RESIZE_NS);
        register(CursorTypesExt.RESIZE_NWSE);
        register(CursorTypesExt.RESIZE_NESW);
    }

    public void register(CursorType cursorType) {
        registry.put(cursorType.toString(), new Cursor(cursorType));
    }

    public void registerAlias(CursorType cursorType, CursorType alias) {
        registry.put(alias.toString(), get(cursorType));
    }

    public Cursor get(CursorType cursorType) {
        Cursor cursor = registry.get(cursorType.toString());

        if (cursor == null) {
            return external.computeIfAbsent(cursorType.toString(), name -> {
                CursorsExtended.LOGGER.info("[cursors_extended] Found external cursor type: {}", cursorType);
                return new Cursor(cursorType);
            });
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
