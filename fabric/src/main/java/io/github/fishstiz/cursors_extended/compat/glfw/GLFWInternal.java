package io.github.fishstiz.cursors_extended.compat.glfw;

import it.unimi.dsi.fastutil.longs.Long2LongOpenHashMap;
import org.lwjgl.glfw.GLFW;
import org.lwjgl.glfw.GLFWImage;

import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;

public class GLFWInternal {
    private static final AtomicInteger IS_CREATE_STANDARD_CURSOR = new AtomicInteger(0);
    private static final AtomicInteger IS_CREATE_CURSOR = new AtomicInteger(0);
    private static final AtomicInteger IS_SET_CURSOR = new AtomicInteger(0);
    private static final Map<Long, Long> REENTRY_CURSORS = new Long2LongOpenHashMap();

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

    public static boolean consumeReentryCursor(long window, long cursor) {
        synchronized (REENTRY_CURSORS) {
            return REENTRY_CURSORS.remove(window, cursor);
        }
    }

    public static void markReentryCursor(long window, long cursor) {
        synchronized (REENTRY_CURSORS) {
            REENTRY_CURSORS.put(window, cursor);
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
