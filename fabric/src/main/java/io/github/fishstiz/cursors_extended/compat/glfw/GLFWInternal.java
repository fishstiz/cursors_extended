package io.github.fishstiz.cursors_extended.compat.glfw;

import org.lwjgl.glfw.GLFW;
import org.lwjgl.glfw.GLFWImage;

public class GLFWInternal {
    private static boolean enabled = true;
    private static boolean reentrantCall = false;

    private GLFWInternal() {
    }

    public static void setEnabled(boolean enabled) {
        synchronized (GLFWInternal.class) {
            GLFWInternal.enabled = enabled;
        }
    }

    public static boolean isEnabled() {
        synchronized (GLFWInternal.class) {
            return enabled;
        }
    }

    public static boolean isReentrantCall() {
        return reentrantCall;
    }

    public static long createStandardCursor(int shape) {
        synchronized (GLFWInternal.class) {
            try {
                reentrantCall = true;
                return GLFW.glfwCreateStandardCursor(shape);
            } finally {
                reentrantCall = false;
            }
        }
    }

    public static long createCursor(GLFWImage glfwImage, int xhot, int yhot) {
        synchronized (GLFWInternal.class) {
            try {
                reentrantCall = true;
                return GLFW.glfwCreateCursor(glfwImage, xhot, yhot);
            } finally {
                reentrantCall = false;
            }
        }
    }

    public static void setCursor(long window, long cursor) {
        synchronized (GLFWInternal.class) {
            try {
                reentrantCall = true;
                GLFW.glfwSetCursor(window, cursor);
            } finally {
                reentrantCall = false;
            }
        }
    }
}
