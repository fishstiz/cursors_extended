package io.github.fishstiz.cursors_extended.mixin.compat.sdl;

import com.llamalad7.mixinextras.injector.ModifyReturnValue;
import com.mojang.blaze3d.platform.cursor.CursorType;
import io.github.fishstiz.cursors_extended.CursorsExtended;
import io.github.fishstiz.cursors_extended.compat.CursorStateTracker;
import io.github.fishstiz.cursors_extended.compat.ModCursor;
import io.github.fishstiz.cursors_extended.cursor.Cursor;
import io.github.fishstiz.cursors_extended.cursor.CursorRegistry;
import io.github.fishstiz.cursors_extended.services.PlatformHelper;
import io.github.fishstiz.cursors_extended.services.SDLMouseOpsCompat;
import io.github.fishstiz.cursors_extended.util.CursorTypeUtil;
import org.lwjgl.sdl.SDLMouse;
import org.lwjgl.system.MemoryUtil;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.util.stream.Stream;

@Mixin(value = SDLMouse.class, priority = 9000)
public class SDLMouseMixin {
    @Unique
    private static final int MAX_SOURCE_PACKAGE_DEPTH = 4;

    @Unique
    private static String cursors_extended$getSourcePackage(Stream<StackWalker.StackFrame> frames) {
        return frames.dropWhile(frame -> frame.getDeclaringClass() == SDLMouse.class)
                .findFirst()
                .map(frame -> {
                    String packageName = frame.getDeclaringClass().getPackageName();
                    int count = 0, index = -1;
                    while ((index = packageName.indexOf('.', index + 1)) != -1) {
                        if (++count == MAX_SOURCE_PACKAGE_DEPTH) return packageName.substring(0, index);
                    }
                    return packageName;
                })
                .orElse("placeholder");
    }

    @ModifyReturnValue(method = "SDL_CreateSystemCursor", at = @At("RETURN"))
    private static long trackStandardCursor(long handle, int id) {
        if (SDLMouseOpsCompat.isCreatingStandardCursor() || CursorStateTracker.get().getCursor(handle) != null) {
            return handle;
        }

        String name = CursorTypeUtil.mapStandardCursorName(id);
        if (name != null) {
            String sourcePackage = CursorStateTracker.getStackWalker().walk(SDLMouseMixin::cursors_extended$getSourcePackage);
            CursorStateTracker.get().trackCursor(new ModCursor(handle, sourcePackage, name));

            if (PlatformHelper.INSTANCE.isDevelopmentEnvironment()) {
                CursorsExtended.LOGGER.info("[cursors_extended] tracking standard cursor handle: {}={}, from {}", name, handle, sourcePackage);
            }
        }

        return handle;
    }

    @ModifyReturnValue(method = "SDL_CreateColorCursor", at = @At("RETURN"))
    private static long trackCustomCursor(long handle) {
        if (SDLMouseOpsCompat.isCreatingCursor() || CursorStateTracker.get().getCursor(handle) != null) {
            return handle;
        }

        String sourcePackage = CursorStateTracker.getStackWalker().walk(SDLMouseMixin::cursors_extended$getSourcePackage);
        CursorStateTracker.get().trackCursor(ModCursor.createCustom(handle, sourcePackage));

        if (PlatformHelper.INSTANCE.isDevelopmentEnvironment()) {
            CursorsExtended.LOGGER.info("[cursors_extended] tracking custom cursor handle: {}, from {}", handle, sourcePackage);
        }

        return handle;
    }

    @ModifyReturnValue(method = "SDL_CreateAnimatedCursor", at = @At("RETURN"))
    private static long trackCustomAnimatedCursor(long handle) {
        if (SDLMouseOpsCompat.isCreatingAnimatedCursor() || CursorStateTracker.get().getCursor(handle) != null) {
            return handle;
        }

        String sourcePackage = CursorStateTracker.getStackWalker().walk(SDLMouseMixin::cursors_extended$getSourcePackage);
        CursorStateTracker.get().trackCursor(ModCursor.createCustom(handle, sourcePackage));

        if (PlatformHelper.INSTANCE.isDevelopmentEnvironment()) {
            CursorsExtended.LOGGER.info("[cursors_extended] tracking custom animated cursor handle: {}, from {}", handle, sourcePackage);
        }

        return handle;
    }

    @Inject(method = "SDL_DestroyCursor", at = @At("RETURN"))
    private static void untrackCursor(long cursor, CallbackInfo ci) {
        ModCursor modCursor = CursorStateTracker.get().getCursor(cursor);

        if (modCursor != null) {
            CursorStateTracker.get().untrackCursor(modCursor);
            CursorsExtended.getInstance().getRegistry().unregisterCustom(modCursor.cursorType());
        }
    }

    @Inject(method = "SDL_SetCursor", at = @At("HEAD"), cancellable = true)
    private static void remapCursor(long cursor, CallbackInfoReturnable<Boolean> cir) {
        CursorStateTracker tracker = CursorStateTracker.get();

        if (!tracker.isTracking() || SDLMouseOpsCompat.isSettingCursor()) {
            return;
        }

        CursorRegistry registry = CursorsExtended.getInstance().getRegistry();
        Cursor mappedCursor;
        long window = SDLMouse.SDL_GetMouseFocus();

        if (cursor == MemoryUtil.NULL || cursor == SDLMouse.SDL_GetDefaultCursor()) {
            tracker.resetCursor(window, CursorStateTracker.getStackWalker().walk(SDLMouseMixin::cursors_extended$getSourcePackage));
            mappedCursor = registry.get(CursorType.DEFAULT);
        } else {
            ModCursor modCursor = tracker.getCursor(cursor);
            if (modCursor == null) {
                return;
            }

            mappedCursor = registry.get(modCursor.cursorType());
            if (!modCursor.custom() && !CursorsExtended.CONFIG.getOrCreateSettings(mappedCursor).enabled()) {
                mappedCursor = registry.get(CursorType.DEFAULT);
                tracker.resetCursor(window, modCursor.source());
            } else {
                tracker.setCursor(window, modCursor);
            }
        }

        if (!CursorsExtended.CONFIG.isRemapStandardCursors()) {
            CursorStateTracker.syncWithMinecraft(window, mappedCursor.cursorType());
            return;
        }

        cir.cancel();

        // main window handled by GuiRenderer#render and will skip the mapping on set cursor call
        if (window != CursorsExtended.getInstance().getDisplay().getWindow().handle()) {
            CursorsExtended.getInstance().getLoader().lazyLoadTexture(mappedCursor);
            CursorStateTracker.syncWithMinecraft(window, mappedCursor.cursorType());

            // skips mapping
            SDLMouseOpsCompat.INSTANCE.beforeSetCursor(mappedCursor.handle());
            // this should've been a WrapMethod, but due to ixeris using inject, WrapMethod would wrap over ixeris,
            // which is not ideal
            SDLMouse.SDL_SetCursor(mappedCursor.handle());
            SDLMouseOpsCompat.INSTANCE.afterSetCursor(mappedCursor.handle());
        }
    }
}
