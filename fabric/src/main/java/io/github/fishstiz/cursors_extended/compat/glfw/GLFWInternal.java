package io.github.fishstiz.cursors_extended.compat.glfw;

import it.unimi.dsi.fastutil.longs.LongOpenHashSet;
import org.lwjgl.glfw.GLFW;
import org.lwjgl.glfw.GLFWImage;

import java.util.Set;

public class GLFWInternal {
    private static final ThreadLocal<Boolean> INTERNAL_CALL = new ThreadLocal<>();
    private static final Set<Long> IMAGES = new LongOpenHashSet();

    private GLFWInternal() {
    }

    public static boolean isInternalCall() {
        return Boolean.TRUE.equals(INTERNAL_CALL.get());
    }

    public static boolean consumeInternalImage(long image) {
        synchronized (IMAGES) {
            return IMAGES.remove(image);
        }
    }

    public static void trackInternalImage(long image) {
        synchronized (IMAGES) {
            IMAGES.add(image);
        }
    }

    public static long createStandardCursor(int shape) {
        try {
            INTERNAL_CALL.set(true);
            return GLFW.glfwCreateStandardCursor(shape);
        } finally {
            INTERNAL_CALL.remove();
        }
    }

    public static long createCursor(GLFWImage glfwImage, int xhot, int yhot) {
        try {
            INTERNAL_CALL.set(true);
            return GLFW.nglfwCreateCursor(glfwImage.address(), xhot, yhot);
        } finally {
            INTERNAL_CALL.remove();
        }
    }

    public static void setCursor(long window, long cursor) {
        try {
            INTERNAL_CALL.set(true);
            GLFW.glfwSetCursor(window, cursor);
        } finally {
            INTERNAL_CALL.remove();
        }
    }
}
