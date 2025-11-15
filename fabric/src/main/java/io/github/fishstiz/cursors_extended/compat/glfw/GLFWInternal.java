package io.github.fishstiz.cursors_extended.compat.glfw;

import it.unimi.dsi.fastutil.longs.LongOpenHashSet;
import org.lwjgl.glfw.GLFW;
import org.lwjgl.glfw.GLFWImage;

import java.util.Set;
import java.util.concurrent.atomic.AtomicInteger;

public class GLFWInternal {
    private static final AtomicInteger IS_CREATE_STANDARD_CURSOR = new AtomicInteger(0);
    private static final AtomicInteger IS_CREATE_CURSOR = new AtomicInteger(0);
    private static final AtomicInteger IS_SET_CURSOR = new AtomicInteger(0);
    private static final Set<Long> INTERNAL_CURSORS = new LongOpenHashSet();

    private GLFWInternal() {
    }

    public static boolean isCreatingStandardCursor() {
        return IS_CREATE_STANDARD_CURSOR.get() > 0;
    }

    public static boolean isCreatingCursor() {
        return IS_CREATE_CURSOR.get() > 0;
    }

    public static boolean isSettingCursor() {
        return IS_SET_CURSOR.get() > 0;
    }

    public static void trackInternalCursor(long cursor) {
        synchronized (INTERNAL_CURSORS) {
            INTERNAL_CURSORS.add(cursor);
        }
    }

    public static void untrackInternalCursor(long cursor) {
        synchronized (INTERNAL_CURSORS) {
            INTERNAL_CURSORS.remove(cursor);
        }
    }

    public static boolean isInternalCursor(long cursor) {
        synchronized (INTERNAL_CURSORS) {
            return INTERNAL_CURSORS.contains(cursor);
        }
    }

    public static long createStandardCursor(int shape) {
        IS_CREATE_STANDARD_CURSOR.incrementAndGet();
        try {
            return GLFW.glfwCreateStandardCursor(shape);
        } finally {
            IS_CREATE_STANDARD_CURSOR.decrementAndGet();
        }
    }

    public static long createCursor(GLFWImage glfwImage, int xhot, int yhot) {
        IS_CREATE_CURSOR.incrementAndGet();
        try {
            return GLFW.nglfwCreateCursor(glfwImage.address(), xhot, yhot);
        } finally {
            IS_CREATE_CURSOR.decrementAndGet();
        }
    }

    public static void setCursor(long window, long cursor) {
        IS_SET_CURSOR.incrementAndGet();
        try {
            GLFW.glfwSetCursor(window, cursor);
        } finally {
            IS_SET_CURSOR.decrementAndGet();
        }
    }
}
