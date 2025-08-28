package io.github.fishstiz.cursors_extended.resource;

import com.mojang.blaze3d.platform.cursor.CursorType;
import io.github.fishstiz.cursors_extended.cursor.CursorManager;
import net.fabricmc.fabric.api.resource.IdentifiableResourceReloadListener;
import net.minecraft.resources.ResourceLocation;
import org.jetbrains.annotations.NotNull;

import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Executor;

public class CursorResourceReloadListener implements IdentifiableResourceReloadListener {
    @Override
    public ResourceLocation getFabricId() {
        return CursorResourceLoader.getDirectory();
    }

    @Override
    public @NotNull CompletableFuture<Void> reload(
            SharedState sharedState,
            Executor backgroundExecutor,
            PreparationBarrier preparationBarrier,
            Executor gameExecutor
    ) {
        gameExecutor.execute(this::resetCursor);
        return CompletableFuture.runAsync(() -> CursorResourceLoader.reload(sharedState.resourceManager()), backgroundExecutor)
                .thenCompose(preparationBarrier::wait)
                .thenRunAsync(this::resetCursor, gameExecutor);
    }

    private void resetCursor() {
        CursorManager.INSTANCE.setCurrentCursor(CursorType.DEFAULT);
    }
}
