package io.github.fishstiz.cursors_extended.compat.glfw;

import it.unimi.dsi.fastutil.longs.Long2LongOpenHashMap;
import org.lwjgl.glfw.GLFW;
import org.lwjgl.glfw.GLFWImage;

public class GLFWInternal {
    private static final ThreadLocal<Boolean> IS_CREATE_STANDARD_CURSOR = new ThreadLocal<>();
    private static final ThreadLocal<Boolean> IS_CREATE_CURSOR = new ThreadLocal<>();
    private static final ThreadLocal<Boolean> IS_SET_CURSOR = new ThreadLocal<>();
    private static final Long2LongOpenHashMap REENTRY_CURSORS = new Long2LongOpenHashMap();

    private GLFWInternal() {
    }

    private static boolean get(ThreadLocal<Boolean> threadLocal) {
        Boolean value = threadLocal.get();
        if (value == null) {
            threadLocal.remove();
            return false;
        }
        return value;
    }

    public static boolean isCreatingStandardCursor() {
        return get(IS_CREATE_STANDARD_CURSOR);
    }

    public static boolean isCreatingCursor() {
        return get(IS_CREATE_CURSOR);
    }

    public static boolean isSettingCursor() {
        return get(IS_SET_CURSOR);
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
        IS_CREATE_STANDARD_CURSOR.set(true);
        try {
            return GLFW.glfwCreateStandardCursor(shape);
        } finally {
            IS_CREATE_STANDARD_CURSOR.remove();
        }
    }

    public static long createCursor(GLFWImage glfwImage, int xhot, int yhot) {
        IS_CREATE_CURSOR.set(true);
        try {
            return GLFW.nglfwCreateCursor(glfwImage.address(), xhot, yhot);
        } finally {
            IS_CREATE_CURSOR.remove();
        }
    }

    public static void setCursor(long window, long cursor) {
        IS_SET_CURSOR.set(true);
        try {
            GLFW.glfwSetCursor(window, cursor);
        } finally {
            IS_SET_CURSOR.remove();
        }
    }
}
